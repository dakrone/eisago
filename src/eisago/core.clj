(ns eisago.core
  (:require [eisago.http :as http])
  (:gen-class))

(defn -main [& _]
  (println "Starting Eisago API Server...")
  (http/start-api-server)
  (println "Starting Eisago Web Server...")
  (http/start-web-server))
