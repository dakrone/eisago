(ns eisago.db
  (:require [clojure.java.jdbc :as sql]
            [eisago.config :refer [config]])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(defn create-pool [spec]
  {:datasource (doto (ComboPooledDataSource.)
                 (.setJdbcUrl (:connection-uri spec))
                 (.setMaxPoolSize 1))})

(defonce pool (create-pool (config :db)))

;; (defn node-create [data]
;;   (sql/with-connection pool
;;     (sql/with-query-results results
;;       ["SELECT node_create(?) AS node_id" data]
;;       (-> results first :node_id))))
