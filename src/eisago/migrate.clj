(ns eisago.migrate
  (:require [clj-http.client :as http]
            [clojure.java.jdbc :as sql]
            [eisago.es :as es]))

;; migrate existing clojuredocs examples to eisago

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

(defn add-all-examples
  []
  (http/with-connection-pool {}
    (sql/with-connection mysql-db
      (sql/with-query-results rows
        ["SELECT * FROM flat_examples_view"]
        (println "Added" (count (doall (map add-example rows)))
                 "examples.")))))
