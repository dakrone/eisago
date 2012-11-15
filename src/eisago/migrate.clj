(ns eisago.migrate
  (:require [clj-http.client :as http]
            [clojure.java.jdbc :as sql]
            [eisago.es :as es]))

;; migrate existing clojuredocs examples to eisago, this is only
;; intended to be run by me (Lee) and from a REPL, since hopefully you
;; don't have to do it more than once.

(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/clojuredocs_production"
               :user "root"})

(defn add-example
  [m]
  (when (= (:library m) "Clojure Core")
    (let [id (es/id-of "clojure"
                       (:ns m)
                       (:function m))]
      (es/add-example id (:body m)))))

(defn add-comment
  [m]
  (when (= (:library m) "Clojure Core")
    (let [id (es/id-of "clojure"
                       (:ns m)
                       (:function m))]
      (es/add-comment id (:body m)))))

(defn add-all-comments
  []
  (http/with-connection-pool {}
    (sql/with-connection mysql-db
      (sql/with-query-results rows
        ["SELECT * FROM flat_comments_view"]
        (println "Added" (count (doall (map add-comment rows)))
                 "comments.")))))

(defn add-all-examples
  []
  (http/with-connection-pool {}
    (sql/with-connection mysql-db
      (sql/with-query-results rows
        ["SELECT * FROM flat_examples_view"]
        (println "Added" (count (doall (map add-example rows)))
                 "examples.")))))
