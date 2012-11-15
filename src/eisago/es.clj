(ns eisago.es
  "Namespace providing ES interaction."
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.pprint :refer :all]
            [eisago.config :refer [config]])
  (:import (org.apache.commons.codec.digest DigestUtils)
           (org.apache.lucene.queryparser.classic QueryParserBase)))

(def mapping* (delay (-> "mapping.clj"
                         io/resource
                         slurp
                         read-string)))

(defn mapping
  "Get the ES mapping currently used."
  []
  @mapping*)

(defn md5
  "Return the hex for a MD5 sum of a string."
  [s]
  (DigestUtils/md5Hex s))

(defn delete-index
  "Delete an index with the given name."
  [idx-name]
  (http/delete (str (config :es-url) idx-name)
               (config :es-http-opts)))

(defn create-index
  "Create an index with the given name."
  [idx-name]
  (let [settings {:number_of_shards 10 :number_of_replicas 0}
        body (json/encode {:mappings (mapping) :settings settings})]
    (http/post (str (config :es-url) idx-name)
               (assoc (config :es-http-opts) :body body))))

(defn index-exists?
  "Check whether the clojuredocs index exists."
  []
  (= 200 (:status (http/head (str (config :es-url) (config :es-index))
                             (config :es-http-opts)))))

(defn put-doc
  "Insert a document into ES using a HTTP PUT request"
  [{:keys [index type id doc routing]}]
  (let [body (json/encode (assoc doc :index-date (java.util.Date.)))]
    (http/put (str (config :es-url) index "/" type "/" id)
              (merge (config :es-http-opts)
                     {:body body}
                     (when routing {:query-params {:routing routing}})))))

(defn scroll
  "Given a query and scroll options, return a lazy seq of all
  documents matching that using scrolling documents.

  Use like: (scroll \"*:*\" {:_type \"project\" :fields [:id :name :group]})"
  [q opts]
  (let [timeout (or (:_timeout opts) "10m")
        scroll-fn (fn scroll-fn
                    [sid]
                    (lazy-seq
                     (let [params {:query-params {"search_type" "scan"
                                                  "scroll" timeout
                                                  "scroll_id" sid}}
                           results (-> (http/get (str (config :es-url)
                                                      "_search/scroll")
                                                 (merge (config :es-http-opts)
                                                        params))
                                       :body)
                           sid (:_scroll_id results)]
                       (when-let [hits (seq (-> results :hits :hits))]
                         (concat hits (scroll-fn sid))))))
        opts (merge {:size 30} opts)
        query-body (json/encode
                    (merge
                     {:query {:query_string {:default_field :name :query q}}}
                     (dissoc opts :_timeout :_type)))
        initial-resp (-> (http/post
                          (if (:_type opts)
                            (str (config :es-url) (config :es-index) "/"
                                 (name (:_type opts)) "/_search")
                            (str (config :es-url) (config :es-index)
                                 "/_search"))
                          (merge (config :es-http-opts)
                                 {:query-params {"search_type" "scan"
                                                 "scroll" timeout}
                                  :body query-body}))
                         :body)
        total-hits (-> initial-resp :hits :total)
        initial-scroll-id (:_scroll_id initial-resp)
        all-hits (map :fields
                      (concat (-> initial-resp :hits :hits)
                              (scroll-fn initial-scroll-id)))]
    (when all-hits
      (with-meta all-hits {:total total-hits}))))

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
    (:body (put-doc {:index (config :es-index)
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
   :arglists (str (seq (:arglists var)))
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
                :index (config :es-index)
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
  "Generic method to retrieve the children for either an id, or a particular
  type,project, namespace and var name."
  ([type project ns varname]
     (child-for type (id-of project ns varname)))
  ([type id]
     (let [fields "id,body,parent-id,index-date,type"
           q-str (json/encode {:query
                               {:query_string
                                {:query (str "parent-id:" id)}}})
           data (->> (http/get (str (config :es-url) (config :es-index)
                                    "/" (name type) "/_search")
                               (merge (config :es-http-opts)
                                      {:query-params {:fields fields
                                                      :size 1000}
                                       :body q-str}))
                     :body
                     :hits
                     :hits
                     (map munge-child))]
       data)))

(defn add-child
  "Generic method to add a child doc for the given type, es-id and text"
  [type es-id text]
  (let [child-id (md5 (str (java.util.UUID/randomUUID)))
        es-doc {:id child-id
                :index (config :es-index)
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
  "Return the var metadata and children for either an id, or a given
  project, namespace and var name."
  ([project ns varname]
     (meta-for (id-of project ns varname)))
  ([id]
     (let [fields (str "id,project,name,ns,arglists,library,lib-version,"
                       "line,file,doc,index-date,source")
           parent (-> (http/get (str (config :es-url) "/" (config :es-index)
                                     "/var/" id)
                                (merge (config :es-http-opts)
                                       {:query-params {:fields fields}}))
                      :body
                      :fields)
           children (child-for "example,comment" id)]
       (when parent
         (assoc parent :children children)))))

(defn- munge-result
  "take an ES result, returning all the fields with the :score added."
  [result]
  (assoc (:fields result) :score (:_score result)))

(defn search
  "Return a seq of all docs for the given query, lib and ns are optional."
  [{:keys [q lib ns name]}]
  (let [fields "id,project,name,ns,arglists,library"
        must (concat nil (when lib
                           [{:term {:library lib}}]))
        must (concat must (when ns [{:term {:ns ns}}]))
        must (concat must (when name
                            [{:term {:name name}}]))
        q-map {:query {:bool (merge (when q
                                      {:should
                                       [{:query_string
                                         {:query (QueryParserBase/escape q)}}]})
                                    (when (seq must) {:must must}))}}
        q-str (json/encode q-map)
        results (-> (http/post (str (config :es-url) "/"
                                    (config :es-index) "/var/_search")
                               (merge (config :es-http-opts)
                                      {:query-params {:fields fields}
                                       :body q-str}))
                    :body)
        hits {:hits (map munge-result (-> results :hits :hits))
              :total (or (-> results :hits :total) 0)
              :time (or (-> results :took) 0)}]
    hits))

(defn all-projects
  "Return a seq of all projects eisago knows about."
  []
  (scroll "*:*" {:_type "project"
                 :fields [:id :name :group :version
                          :description :license :index-date]}))

;; fns used for testing/etc
(defn drop-indices []
  (delete-index (config :es-index)))

(defn create-indices []
  (create-index (config :es-index)))
