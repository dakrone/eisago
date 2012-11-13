(ns leiningen.eisago
  (:require [leiningen.core.eval :as lein]))

(defn eisago
  [project & [filename]]
  (if filename
    (lein/eval-in-project
     project
     `(do (println (str "Importing " '~filename "..."))
          (eisago.import/index-everything '~filename)
          (println "Done importing" '~filename))
     '(require 'eisago.import))
    (println "Please specify a file to import.")))

;; actual lein plugin function
#_(defn clojuredocs
    "Publish vars for clojuredocs"
    [project]
    (lein/eval-in-project
     (-> project
         (update-in [:dependencies] conj ['cadastre "0.1.1"]))
     `(binding [cadastre.analyzer/*verbose* true]
        (cadastre.analyzer/gen-project-docs-json '~project))
     '(require 'cadastre.analyzer)))
