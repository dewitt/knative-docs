(defproject helloworld-clojure "0.1.0-SNAPSHOT"
  :description "A simple Knative app written in Clojure"
  :url "https://github.com/knative/docs"
  :license {:name "Apache License 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.6.1"]
                 [ring "1.7.0"]]
  :main ^:skip-aot helloworld-clojure.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :uberjar-name "app.jar"}})
