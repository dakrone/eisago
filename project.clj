(defproject eisago "0.1.0-SNAPSHOT"
  :description "Next-gen clojuredocs importer, API, and website."
  :url "http://clojuredocs.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths ["etc"]
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [cheshire "4.0.4"]
                 [clj-http "0.5.7"]
                 [commons-codec "1.6"]
                 [laeggen "0.3"]
                 [org.apache.lucene/lucene-queryparser "4.0.0"]
                 [sonian/carica "1.0.0"]]
  :profiles {:dev {:dependencies [[org.clojure/java.jdbc "0.2.3"]
                                  [mysql/mysql-connector-java "5.1.6"]]}}
  :main eisago.core)
