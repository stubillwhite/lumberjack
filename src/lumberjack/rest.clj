(ns lumberjack.rest
  (:require [compojure.core :refer [context defroutes DELETE GET POST PUT]]
            [compojure.route :as route]
            [lumberjack.core :as core]
            [lumberjack.utils :refer [def-]]
            [mount.core :refer [defstate]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [ring.middleware.json
             :refer
             [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.util.response
             :refer
             [content-type created not-found redirect response]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn- with-response-defaults
  ([response]
    (-> response
      (content-type "application/json"))))

(defroutes app-routes
  (context "/projects" []
           (GET  "/" {params :params} (core/project-names)))

  (context "/dependencies" []
           (GET  "/" {params :params} (core/dependencies)))

  (context "/clashes" []
           (GET  "/" {params :params} (core/clashes)))
  
  (GET "/" [] (redirect "/index.html"))
  (route/resources "/")
  (route/not-found (not-found "Not found")))

(def default-config
  {:params {:urlencoded true
            :keywordize true
            :nested     true
            :multipart  true}

   :responses {:not-modified-responses true
               :absolute-redirects     true
               :content-types          true}})

(defn- wrap-log-request [handler]
  (fn [req]
    (debug "Handling request" req)
    (handler req)))

(def app
  (-> app-routes
      (wrap-log-request)
      (wrap-defaults default-config)
      (wrap-json-body)
      (wrap-json-params)
      (wrap-json-response)
      (wrap-content-type)))

(def- state (atom {:server nil}))

(defstate server
  :start (let [server (run-jetty #'lumberjack.rest/app {:port 8080 :join? false})]
           (swap! state assoc :server server))
  :stop  (if (:server @state)
           (do
             (.stop (:server @state))
             (swap! state assoc :server nil))))

