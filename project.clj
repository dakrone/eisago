(defproject eisago "0.1.0-SNAPSHOT"
  :description "Next-gen clojuredocs importer, API, and website."
  :url "http://clojuredocs.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths ["etc"]
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [c3p0 "0.9.1.2"]
                 [cheshire "4.0.4"]
                 [clabango "0.3"]
                 [clj-http "0.5.7"]
                 [commons-codec "1.6"]
                 [laeggen "0.4"]
                 [org.apache.lucene/lucene-queryparser "4.0.0"]
                 [sonian/carica "1.0.0"]
                 ;; for both postgres and mysql
                 [org.clojure/java.jdbc "0.2.3"]
                 ;; postgres is runtime because it is actually needed
                 [postgresql/postgresql "8.4-702.jdbc4"]]
  ;; mysql is only needed for the migration tool, so only dev time
  :profiles {:dev {:dependencies [[mysql/mysql-connector-java "5.1.6"]]}}
  :main eisago.core)
