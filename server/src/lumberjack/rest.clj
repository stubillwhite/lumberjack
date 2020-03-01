(ns lumberjack.rest
  (:require [compojure.core :refer [context defroutes DELETE GET POST PUT]]
            [compojure.route :as route]
            [lumberjack.core :as core]
            [lumberjack.utils :refer [def-]]
            [mount.core :refer [defstate]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.resource :refer [wrap-resource]]
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
  (context "/project-names" []
           (GET  "/" {params :json-params}
                 (response (core/project-names))))

  (context "/all-dependencies" []
           (GET  "/" {params :json-params}
                 (response (core/all-dependencies))))

  (context "/dependencies-for-project" []
           (POST  "/" {params :json-params}
                  (response (core/dependencies-for-project (get-in params ["name"])))))

  (context "/dependency-graph-for-project" []
           (POST  "/" {params :json-params}
                  (response (core/dependency-graph-for-project (get-in params ["name"])))))

  (context "/load-project-data" []
           (POST  "/" {params :json-params}
                  (response (core/load-project-data!))))
  
  (context "/clashes" []
           (GET  "/" {params :json-params}
                 (response (core/clashes))))
  
  (GET "/" [] (redirect "/web/index.html"))
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
      (wrap-cors :access-control-allow-origin [#".*"] :access-control-allow-methods [:get :put :post :delete])
      (wrap-resource "public")
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
