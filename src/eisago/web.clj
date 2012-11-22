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

(defn project-view [request project version]
  (str "project-view for " project "-" version))
(defn namespace-view [request project version namespace]
  (str "namespace-view for " project "-" version " ns: " namespace))
(defn var-view [request project version namespace var]
  (str "namespace-view for " project "-" version " var: " namespace "/" var))

(defn redirect-project [request project]
  {:status 302
   :headers {"location" (str "/" project "-" "1.2" "/")}})
(defn redirect-namespace [request project namespace]
  {:status 302
   :headers {"location" (str "/" project "-" "1.2" "/" namespace "/")}})
(defn redirect-var [request project namespace var]
  {:status 302
   :headers {"location" (str "/" project "-" "1.2" "/" namespace "/" var "/")}})
