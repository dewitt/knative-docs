(ns helloworld-clojure.core
  (:gen-class)
  (:require [compojure.core :as compojure]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]))

(defn hello []
  (response/response
   (format "Hello, %s!" (or (System/getenv "TARGET") "World"))))

(compojure/defroutes app
  (compojure/GET "/" [] (hello))
  (route/not-found "Page not found"))

(defn port []
  (Integer/parseInt (or (System/getenv "PORT") "8080")))

(defn -main []
  (jetty/run-jetty app {:join? false :port (port)}))
