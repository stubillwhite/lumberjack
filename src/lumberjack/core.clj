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

(defn add-one
  ([x] 
   (+ x 1)))

(defn- load-project-dependencies [path]
  (->> (io/file path)
       (slurp)
       (string/trim)
       (sbt/parse-dependency-tree)))

(defn load-projects [config]
  (into {} (for [p (:projects config)] [p (load-project-dependencies p)])))

(def projects (load-projects config))

;; (def v (first (vals projects)))


;; (def grouped (group-by (fn [{:keys [org pkg ver]}] [org pkg]) (keys v)))


;; (clojure.pprint/pprint (into {} (filter (fn [[k vs]] (> (count vs) 1)) grouped)))



;; (analysis/dependency-clashes projects)

;; (clojure.pprint/pprint (analysis/artifact-to-version-map projects))

