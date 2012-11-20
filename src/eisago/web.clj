(ns eisago.web
  (:require [clabango.parser :refer [render-file]]
            [clojure.stacktrace :refer [print-stack-trace]]
            [eisago.es :as es]))

(defn missing
  "Response for non-matching web requests."
  [req]
  {:status 404
   :headers {"content-type" "text/plain"}
   :body "404"})

(defn error
  "Response for web 500 errors"
  [req ex]
  {:status 500
   :headers {"content-type" "text/plain"}
   :body (with-out-str
           (print-stack-trace ex))})

(defn index [request]
  (render-file "eisago/templates/index.html" {}))
