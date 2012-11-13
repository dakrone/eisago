(ns eisago.api
  (:require [eisago.es :as es]
            [laeggen.core :as laeggen]
            [laeggen.dispatch :as dispatch]))

(defn hello
  ([req]
     {:status 200 :body "Stuff will go here."})
  ([req thing]
     {:status 200 :body (str thing " will go here.")}))

(defn missing
  [req]
  {:status 404 :body "Stuff's missing yo."})

(defn not-done-yet
  [req & things]
  {:status 200 :body "Implement me!"})

(defn search
  ([{:keys [query-string] :as req}]
     (if query-string
       (let [results (es/search query-string)]
         {:status 200 :body results})
       {:status 404 :body "Must specify a query!"}))
  ([req lib]
     (search (update-in req [:query-string] assoc :library lib)))
  ([req lib namespace]
     (search (-> req
                 (update-in [:query-string] assoc :library lib)
                 (update-in [:query-string] assoc :namespace namespace)))))

(def urls
  (dispatch/urls
   #"^/examples/([^/]+)/([^/]+)/([^/]+)$" not-done-yet
   #"^/comments/([^/]+)/([^/]+)/([^/]+)$" not-done-yet
   #"^/search/([^/]+)/([^/]+)/([^/]+)$" not-done-yet
   #"^/([^/]+)/([^/]+)/_search/?$" search
   #"^/([^/]+)/_search/?$" search
   #"^/_search/?$" search
   #"^/$" #'hello
   #"^/([^/]+)$" #'hello
   :404 #'missing))

(defn server-start []
  (laeggen/start {:port 5000
                  :append-slash? false
                  :urls urls
                  :websocket true}))
