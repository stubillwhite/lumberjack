(ns client.routes
  (:require [client.pages.home :as home]
            [client.pages.visualise :as visualise]))

(def unauthenticated-pages
  [{:id          "/home"
    :label       "Projects"
    :icon        "home"
    :view-fn     home/view
    :on-entry-fn home/on-entry
    :on-exit-fn  home/on-exit}
   
   {:id          "/visualise"
    :label       "Search"
    :icon        "search"
    :view-fn     visualise/view
    :on-entry-fn visualise/on-entry
    :on-exit-fn  visualise/on-exit}])

(defn pages []
  unauthenticated-pages)
