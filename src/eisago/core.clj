(ns eisago.core
  (:require [eisago.api :as api])
  (:gen-class))

(defn -main [& _]
  (println "Starting Eisago API...")
  (api/start-server))
