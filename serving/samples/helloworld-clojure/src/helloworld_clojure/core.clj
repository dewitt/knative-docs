(ns helloworld-clojure.core
  (:gen-class)
  (:require [compojure.core :as compojure]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]))

(defn hello []
  (response/response
   (format "Hello, %s!" (or (System/getenv "TARGET") "World"))))

(compojure/defroutes app
  (compojure/GET "/" hello))

(defn -main []
  (jetty/run-jetty app {:join? false :port 8080}))
