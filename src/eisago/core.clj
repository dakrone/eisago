(ns eisago.core
  (:require [eisago.http :as http])
  (:gen-class))

(defn -main [& _]
  (println "Starting Eisago HTTP Server...")
  (http/start-server))
