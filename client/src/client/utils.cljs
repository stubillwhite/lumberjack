(ns client.utils
  (:require [cljs-time.coerce :as coerce]
            [cljs-time.format :as format]
            [clojure.string :as string]
            [clojure.walk :as walk]))

;; General utilities
;; --------------------------------------------------

(defn index-by
  "Returns xs as a map indexed by (f x)."
  [f xs]
  (into {} (for [x xs] [(f x) x])))

(defn transform-keys
  "Recursively transforms all map keys in coll with the transform-key fn."
  [transform-key coll]
  (walk/postwalk (fn [x]
                   (cond (map? x)  (into {} (map (fn [[k v]] [(transform-key k) v]) x))
                         ;; (coll? x) (map (partial transform-keys transform-key) x)
                         :else     x))
                 coll))

(defn underscores-to-dashes
  "Return keyword with underscores replaced by dashes."
  [kw]
  (-> kw
      (name)
      (string/replace #"_" "-")
      (keyword)))

(defn to-clj
  "Return JavaScript data converted to ClojureScript."
  [js]
  (->> (js->clj js :keywordize-keys true)
       (transform-keys underscores-to-dashes))
  )

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn to-date-string
  "Returns the string form of a long date."
  [l]
  (format/unparse (format/formatter "yyyy-MM-dd")
                  (coerce/from-long l)))
