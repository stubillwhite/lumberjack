(ns client.api
  (:require [client.state :refer [state update-state!]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [client.utils :as utils])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Overridden in dev.cljs.edn to localhost for development builds
(goog-define base-url "http://localhost:8080")

(def api-url base-url)
(def resources-url base-url)

(defn- with-credentials
  ([]
   (with-credentials {}))
  ([params]
   (merge params
          {:with-credentials? false})))

(defn load-project-data [f]
  (go (f (<! (http/post (str api-url "/load-project-data")
                        (-> (with-credentials)))))))

(defn get-project-names [f]
  (go (f (<! (http/get (str api-url "/project-names")
                       (with-credentials))))))

(defn get-all-dependencies [f]
  (go (f (<! (http/get (str api-url "/all-dependencies")
                       (with-credentials))))))

(defn get-clashes [f]
  (go (f (<! (http/get (str api-url "/clashes")
                       (with-credentials))))))

(defn get-dependencies-for-project [project-name f]
  (go (f (<! (http/post (str api-url "/dependencies-for-project")
                        (-> {:json-params {:name project-name}}
                            (with-credentials)))))))

(defn get-graph-for-project [project-name f]
  (go (f (<! (http/post (str api-url "/dependency-graph-for-project")
                        (-> {:json-params {:name project-name}}
                            (with-credentials)))))))
