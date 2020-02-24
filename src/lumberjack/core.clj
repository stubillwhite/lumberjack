(ns lumberjack.core
  (:require
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders]
   [lumberjack.config :refer [config]]
   [lumberjack.parsers.sbt :as sbt]
   [clojure.string :as string]
   [clojure.java.io :as io]
   [lumberjack.analysis :as analysis]))

(timbre/refer-timbre)

(timbre/merge-config! {:appenders {:println {:enabled? false}}})   

(timbre/merge-config!
  {:appenders {:spit (appenders/spit-appender {:fname "lumberjack.log"})}})

(defn- load-project-dependencies [path]
  (->> (io/file path)
       (slurp)
       (string/trim)
       (sbt/parse-dependency-tree)))

(defn load-projects [config]
  (into {} (for [p (:projects config)] [p (load-project-dependencies p)])))

(def projects (load-projects config))

;; Public

(defn project-names
  "Return a list of the project names."
  []
  (keys projects))

(defn dependencies
  "Return a list of the dependencies."
  []
  (->> projects
       (vals)
       (map keys)
       (flatten)))

;; TODO: Return types need some thought
(defn clashes
  "Return a list of the dependency clashes."
  []
  (list (analysis/dependency-clashes projects)))

