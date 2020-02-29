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

(defn get-projects [f]
  (go (f (<! (http/get (str api-url "/projects")
                       (with-credentials))))))

(defn get-dependencies [f]
  (go (f (<! (http/get (str api-url "/dependencies")
                       (with-credentials))))))

(defn get-clashes [f]
  (go (f (<! (http/get (str api-url "/clashes")
                       (with-credentials))))))

(defn get-dependencies-for-project [project-name f]
  (go (f (<! (http/post (str api-url "/project-dependencies")
                        (-> {:json-params {:name project-name}}
                            (with-credentials)))))))
