(ns eisago.import
  "Main import namespace"
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.pprint :refer :all]
            [eisago.es :as es])
  (:import (java.util.zip GZIPInputStream)
           (org.apache.commons.codec.digest DigestUtils)))

(defn read-json-file
  "Reads a gzipped json file into the clojure datastructure known as
  the 'metadata map'"
  [filename]
  (with-open [is (-> filename io/file io/input-stream)
              gz (io/reader (GZIPInputStream. is))]
    (json/decode-stream gz true)))

(defn index-everything
  "Given the filename of a gzipped json file containing the metadata,
  index all the metadata into ES."
  [filename]
  (let [data (read-json-file filename)]
    (when-not (es/index-exists?)
      (es/create-index es/es-index))
    (es/index-project data)
    (es/index-all-vars data)))

(defn- test-it [& [filename]]
  ;; connection-pool brings it from ~12 seconds to ~4.5
  (http/with-connection-pool {}
    (index-everything filename)
    (es/add-example "e194bda67affc5915db2460bd4bc29a6"
                    "This is an example for reduce.")
    (es/add-example "e194bda67affc5915db2460bd4bc29a6"
                    "This is a different example for reduce.")
    (es/add-comment "e194bda67affc5915db2460bd4bc29a6"
                    "This is a comment for reduce.")
    (println "done.")))
