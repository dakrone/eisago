(ns eisago.http
  (:require [eisago.api :as api]
            [eisago.config :refer [config]]
            [eisago.web :as web]
            [laeggen.core :as laeggen]
            [laeggen.dispatch :as dispatch]))

(def api-urls
  (dispatch/urls
   [#"^/api/v1/doc/([^/]+)/?$"
    #"^/api/v1/doc/([^/]+)/([^/]+)/([^/]+)/?$"]
   #'api/doc-for

   [#"^/api/v1/meta/([^/]+)/?$"
    #"^/api/v1/meta/([^/]+)/([^/]+)/([^/]+)/?$"]
   #'api/children-for

   [#"^/api/v1/([^/]+)/([^/]+)/_search/?$"
    #"^/api/v1/([^/]+)/_search/?$"
    #"^/api/v1/_search/?$"]
   #'api/search

   #"^/api/v1/_projects/?" #'api/all-projects

   #"^/api/v1/_stats/?$" #'api/stats

   #"^/api/v1/.*" #'api/missing))

(def web-urls
  (dispatch/urls
   #"^/$" #'web/index

   #"^/([^/]+)-([^/]+)/$" #'web/project-view
   #"^/([^/]+)-([^/]+)/([^/]+)/$" #'web/namespace-view
   #"^/([^/]+)-([^/]+)/([^/]+)/([^/]+)/$" #'web/var-view

   #"^/([^/]+)/$" #'web/redirect-project
   #"^/([^/]+)/([^/]+)/$" #'web/redirect-namespace
   #"^/([^/]+)/([^/]+)/([^/]+)/$" #'web/redirect-var

   :404 #'web/missing))

(defn error [req ex]
  (if false ; (is this a web request?)
    (web/error req ex)
    (api/error req ex)))

(def all-urls (dispatch/merge-urls
               api-urls
               web-urls
               (dispatch/urls
                :500 #'error)))

(defn start-server []
  (laeggen/start (assoc (config :laeggen)
                   :urls all-urls)))

;; (swap! laeggen/routes assoc (config :laeggen :port) (dispatch/merge-urls laeggen.views/default-urls all-urls))
