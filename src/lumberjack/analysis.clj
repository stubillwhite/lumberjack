(ns lumberjack.analysis
  (:require
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders]
   [lumberjack.config :refer [config]]
   [lumberjack.parsers.sbt :as sbt]
   [clojure.string :as string]
   [clojure.java.io :as io]))

(timbre/refer-timbre)

(defn- map-vals [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn artifact-to-version-map [projects]
  (->> (vals projects)
       (mapcat keys)
       (flatten)
       (group-by (fn [{:keys [org pkg ver]}] {:org org :pkg pkg}))
       (map-vals (fn [x] (into #{} (map #(select-keys % [:ver]) x))))))

(defn dependency-clashes [projects]
  (into {} (filter
            (fn [[k vs]] (> (count vs) 1))
            (artifact-to-version-map projects))))
