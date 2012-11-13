(ns eisago.es
  "Namespace providing ES interaction."
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.pprint :refer :all])
  (:import (org.apache.commons.codec.digest DigestUtils)))

;; TODO: configify these three settings
(def es-url "http://localhost:9200")
(def es-index "clojuredocs")
(def es-opts {:basic-auth "user:Passw0rd"
              :debug false
              :debug-body false
              :save-request? false
              :as :json
              :throw-exceptions false})

(def mapping* (delay (-> "mapping.clj"
                         io/resource
                         slurp
                         read-string)))

(defn mapping [] @mapping*)

(defn md5
  "MD5 a string."
  [s]
  (DigestUtils/md5Hex s))

(defn delete-index
  "Delete an index with the given name."
  [idx-name]
  (http/delete (str es-url "/" idx-name) es-opts))

(defn create-index
  "Create an index with the given name."
  [idx-name]
  (let [settings {:number_of_shards 10 :number_of_replicas 0}
        body (json/encode {:mappings (mapping) :settings settings})]
    (http/post (str es-url "/" idx-name) (assoc es-opts :body body))))

(defn put-doc
  "Insert a document into ES using a HTTP PUT request"
  [{:keys [index type id doc routing]}]
  (let [body (json/encode (assoc doc :index-date (java.util.Date.)))]
    (http/put (str es-url "/" index "/" type "/" id)
              (merge es-opts
                     {:body body}
                     (when routing {:query-params {:routing routing}})))))

(defn project-doc
  "Given a metadata clojure map, preare an ES doc about the project
  for indexing"
  [m]
  (let [data (update-in m [:namespaces] (fn [nses] (mapv name (keys nses))))
        data (update-in data [:license] (constantly (-> m :license :name)))]
    (assoc data :id (md5 (str (:group data) "." (:name data))))))

(defn index-project
  "Given the metadata clojure map, generate and index the project
  document from it"
  [m]
  (let [doc (project-doc m)]
    (:body (put-doc {:index es-index
                     :type "project"
                     :id (:id doc)
                     :doc doc}))))

(defn id-of
  "Return the ID for a the var given the project, namespace and var name."
  [project ns varname]
  (md5 (str project ":" (name ns) "." varname)))

(defn var-doc
  "Create a single var document for ES."
  [ns var project]
  ;; Note that a var's ID will always be the same, this is some it can
  ;; be replaced when the project is re-indexed (and all the fields
  ;; will be updated at that time too). It also needs to remain
  ;; constant so that the child docs (examples and comments) still
  ;; point to the same place and can be found with the same query.
  {:id (id-of (:name project) ns (:name var))
   :project (str (:group project) "/" (:name project))
   :name (:name var)
   :ns (name ns)
   :arglists (:arglists var)
   :library (:name project)
   :lib-version (:version project)
   :line (:line var)
   :file (:file var)
   :doc (:doc var)
   :source (:source var)})
(defn get-all-var-docs
  "Given the metadata map, create all of the var docs for the project."
  [m]
  (let [proj-meta (dissoc m :namespaces)
        ns-meta (for [[ns vars] (:namespaces m)]
                  (map (fn [var] (var-doc ns var proj-meta)) vars))
        docs (apply concat ns-meta)]
    docs))

(defn index-var
  "Index a single doc into ES"
  [doc]
  (let [es-doc {:doc doc
                :index es-index
                :type "var"
                :id (:id doc)}]
    (:body (put-doc es-doc))))

(defn index-all-vars
  "Given the metadata map, index all vars from the map into ES."
  [m]
  (doall (map index-var (get-all-var-docs m))))

(defn munge-child
  "Adds in :type data to the child document map"
  [doc]
  (assoc (:fields doc) :type (:_type doc)))

;; The following 'child-for' and 'add-child' methods are designed to
;; work for both examples and comments, since they operate generically
;; on ES child docs.

(defn child-for
  "Generic method to retrieve the children for a particular type,
  project, namespace and var name."
  [type project ns varname]
  (let [fields "id,body,parent-id,index-date,type"
        id (id-of project ns varname)
        q-str (json/encode {:query
                            {:query_string
                             {:query (str "parent-id:" id)}}})
        data (->> (http/get (str es-url "/" es-index "/" (name type) "/_search")
                            (merge es-opts {:query-params {:fields fields
                                                           :size 1000}
                                            :body q-str}))
                  :body
                  :hits
                  :hits
                  (map munge-child))]
    data))

(defn add-child
  "Generic method to add a child doc for the given type, es-id and text"
  [type es-id text]
  (let [child-id (md5 (str (java.util.UUID/randomUUID)))
        es-doc {:id child-id
                :index es-index
                :type (name type)
                :routing es-id
                :doc {:body text
                      :parent-id es-id
                      :id child-id}}]
    (:body (put-doc es-doc))))

;; These functions use the generic 'child-for' and 'add-child' methods
;; above, and are defined only to be helpers for REPL usage
(defn add-comment
  "Given the ES id for a doc and the comment text, index the comment
  for that document."
  [es-id comment-text]
  (add-child :comment es-id comment-text))

(defn comments-for
  "Given a project, namespace and var name, return a seq of comments for
  that var."
  [project ns varname]
  (child-for :comment project ns varname))

(defn add-example
  "Given the ES id for a doc and the example text, index the example
  for that document."
  [es-id example-text]
  (add-child :example es-id example-text))

(defn examples-for
  "Given a project, namespace and var name, return a seq of examples for
  that var."
  [project ns varname]
  (child-for :example project ns varname))

(defn meta-for
  "Return the var metadata and children for a given project, namespace and
  var name."
  [project ns varname]
  (let [fields (str "id,project,name,ns,arglists,library,lib-version,"
                    "line,file,doc,index-date,source")
        parent (-> (http/get (str es-url "/" es-index "/var/"
                                (id-of project ns varname))
                             (merge es-opts {:query-params {:fields fields}}))
                   :body
                   :fields)
        children (child-for "example,comment" project ns varname)]
    (-> parent
        (assoc :children children)
        ;; TODO: figure out why the children get dates that look
        ;; correct, but the parent ends up getting just a Long instead
        ;; of a date string.
        #_(update-in [:index-date] #(java.util.Date. (long %))))))

;; fns used for testing/etc
(defn drop-indices []
  (delete-index es-index))

(defn create-indices []
  (create-index es-index))
