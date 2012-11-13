(ns eisago.api
  (:require [laeggen.core :as laeggen]
            [laeggen.dispatch :as dispatch]))

(defn hello
  ([req]
     {:status 200 :body "Stuff will go here."})
  ([req thing]
     {:status 200 :body (str thing " will go here.")}))

(defn missing
  [req]
  {:status 404 :body "Stuff's missing yo."})

(def urls
  (dispatch/urls
   #"^/$" #'hello
   #"^/([^/]+)$" #'hello
   :404 #'missing))

(defn server-start []
  (laeggen/start {:port 5000
                  :append-slash? false
                  :urls urls
                  :websocket true}))
