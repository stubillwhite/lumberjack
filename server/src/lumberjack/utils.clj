(ns lumberjack.utils
  (:require [clojure.string :as string]))

(defmacro def-
  ([name & decls]
    (list* `def (with-meta name (assoc (meta name) :private true)) decls)))

(defmacro defmulti-
  [name & decls]
  (list* `defmulti (with-meta name (assoc (meta name) :private true)) decls))

(defmacro defmethod-
  [name & decls]
  (list* `defmethod (with-meta name (assoc (meta name) :private true)) decls))

(defn to-canonical-name
  "Returns the canonical name for a dependency."
  [{:keys [org pkg ver]}]
  (str org ":" pkg ":" ver))

(defn from-canonical-name
  "Returns the dependency information parsed from a canonical name."
  [s]
  (let [[org pkg ver] (string/split s #":")]
    {:org org :pkg pkg :ver ver}))
