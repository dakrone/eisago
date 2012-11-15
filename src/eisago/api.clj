(ns eisago.api
  (:require [cheshire.core :as json]
            [eisago.config :refer [config]]
            [eisago.es :as es]
            [laeggen.core :as laeggen]
            [laeggen.dispatch :as dispatch]))

(defn missing
  "Response for non-matching API requests."
  [req]
  {:status 404 :body (json/encode {:status 404 :message "Invalid request"})})

(defn search
  "API implementation of searching, lib and namespace can be optionally
  specified as paths, with name and query optionally specified in
  the query-string."
  ([{:keys [query-string] :as req}]
     (if query-string
       (let [results (es/search query-string)]
         {:status 200 :body (json/encode results)})
       {:status 404 :body "Must specify a query!"}))
  ([req lib]
     (search (update-in req [:query-string] assoc :lib lib)))
  ([req lib namespace]
     (search (-> req
                 (update-in [:query-string] assoc :lib lib)
                 (update-in [:query-string] assoc :ns namespace)))))

(defn children-for
  "API implementation of returning examples and comments for a given method/id."
  ([request id]
     {:status 200 :body (-> (es/meta-for id)
                            :children
                            json/encode)})
  ([request lib namespace varname]
     {:status 200 :body (-> (es/meta-for lib namespace varname)
                            :children
                            json/encode)}))

(defn doc-for
  "API implementation of returning all information for a given method/id."
  ([request id]
     {:status 200 :body (json/encode (es/meta-for id))})
  ([request lib namespace varname]
     {:status 200 :body (json/encode (es/meta-for lib namespace varname))}))

(defn stats
  "API implementation for statistics"
  [request]
  {:status 200
   :body (json/encode {:total (or (es/es-count) 0)
                       :projects (or (es/es-count :project) 0)
                       :vars (or (es/es-count :var) 0)
                       :examples (or (es/es-count :example) 0)
                       :comments (or (es/es-count :comment) 0)})})

(defn all-projects
  "API implementation for returning all projects"
  [request]
  {:status 200 :body (json/encode (es/all-projects))})

(def urls
  (dispatch/urls
   #"^/doc/([^/]+)/?$" #'doc-for
   #"^/doc/([^/]+)/([^/]+)/([^/]+)/?$" #'doc-for

   #"^/meta/([^/]+)/?$" #'children-for
   #"^/meta/([^/]+)/([^/]+)/([^/]+)/?$" #'children-for

   #"^/([^/]+)/([^/]+)/_search/?$" #'search
   #"^/([^/]+)/_search/?$" #'search
   #"^/_search/?$" #'search

   #"^/_projects/?" #'all-projects

   #"^/_stats/?$" #'stats

   :404 #'missing))

(defn start-server []
  (laeggen/start (assoc (config :laeggen) :urls urls)))
