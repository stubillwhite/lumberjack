(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require [clojure.java.javadoc :refer [javadoc]]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint print-table]]
            [clojure.reflect :refer [reflect]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.stacktrace :refer [print-stack-trace]]
            [clojure.test :as test]
            [clojure.edn :as edn]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [clojure.tools.trace :refer [trace-forms trace-ns untrace-ns]]
            [mount.core :as mount]
            [lumberjack.config :refer [config]]
            [lumberjack.core :as core]
            [lumberjack.analysis :as analysis]
            [taoensso.nippy :as nippy]
            [taoensso.timbre :as timbre]
            [clojure.string :as string])
  (:import [java.io DataInputStream DataOutputStream]))

(defn print-methods [x]
  (->> x
       reflect
       :members 
       (filter #(contains? (:flags %) :public))
       (sort-by :name)
       print-table))

(defn write-object [fnam obj]
  (with-open [w (io/output-stream fnam)]
    (nippy/freeze-to-out! (DataOutputStream. w) obj)))

(defn read-object [fnam]
  (with-open [r (io/input-stream fnam)]
    (nippy/thaw-from-in! (DataInputStream. r))))

(defn start []
  (timbre/merge-config! {:appenders {:println {:enabled? true}}})   
  (mount/start))

(defn stop []
  (mount/stop))

(defn reset []
  (stop)
  (refresh :after 'user/start))
