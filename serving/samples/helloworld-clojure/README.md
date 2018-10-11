# Hello World - Clojure sample

A simple web app written in Clojure (a dialect of Lisp that can run on
the JVM) that you can use for testing. It reads in an env variable
`TARGET` and prints "Hello, ${TARGET}!". If TARGET is not specified,
it will use "World" as the TARGET.

## Prerequisites

* A Kubernetes cluster with Knative installed. Follow the
  [installation
  instructions](https://github.com/knative/docs/blob/master/install/README.md)
  if you need to create one.
* [Docker](https://www.docker.com) installed and running on your local machine,
  and a Docker Hub account configured (we'll use it for a container registry).
* A [Java SDK](https://openjdk.java.net) installed on your local
  machine. This demo has been tested against OpenJDK 8, though more
  recent and/or alternative JDKs should work as well.
* [Leiningen](https://leiningen.org/) is a tool that automates the
  creation, building, and running of Clojure projects. Please install
  the 'lein' script as directed for your local machine.

## Recreating the sample code

While you can clone all of the code from this directory, hello world
apps are generally more useful if you build them step-by-step. The
following instructions recreate the source files from this folder.

1. Initialize a new Clojure project with the following commands. We
   will be editing the files it creates and populating them with our
   example code.
   
   ```shell
   lein new app helloworld-clojure
   ```

1. Edit the top-level file `project.clj` to contain the following
   metadata. Leiningen will use this metadata to fetch dependencies
   for the app and build it.

    ```clojure
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
    ```

1. Edit the file `src/helloworld-clojure/core.clj` to contain the
   following application logic. This application uses the popular
   `compojure` and `ring` libraries to expose a simple HTTP request
   handler to callers on the specified port.

    ```clojure
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

    ```

1. In your project directory, create a file named `Dockerfile` and
   copy the code block below into it. This configuration file
   specifies a two-stage build process that uses an initial image to
   produce a standalone "uberjar" (a single jar bundled with all
   application dependencies), then creates a second, much smaller
   container image containing only the Java runtime and the single
   uberjar.

    ```docker
    FROM clojure AS build
    WORKDIR /usr/src/app
    COPY project.clj /usr/src/app
    RUN lein deps
    COPY . /usr/src/app
    RUN lein uberjar

    FROM openjdk:8-jre-alpine
    WORKDIR /app
    COPY --from=build /usr/src/app/target/uberjar/app.jar /app/app.jar
    ENTRYPOINT ["java", "-jar", "app.jar"]
    ```

1. Create a new file, `service.yaml` and copy the following service
   definition into the file. Make sure to replace `{username}` with
   your Docker Hub username.

    ```yaml
    apiVersion: serving.knative.dev/v1alpha1
    kind: Service
    metadata:
      name: helloworld-clojure
      namespace: default
    spec:
      runLatest:
        configuration:
          revisionTemplate:
            spec:
              container:
                image: docker.io/{username}/helloworld-clojure
                env:
                - name: TARGET
                  value: "Clojure"
    ```

## Building and deploying the sample

Once you have recreated the sample code files (or used the files in the sample
folder) you're ready to build and deploy the sample app.

1. Use Docker to build the sample code into a container. To build and push with
   Docker Hub, run these commands replacing `{username}` with your
   Docker Hub username:

    ```shell
    # Build the container on your local machine
    docker build -t {username}/helloworld-clojure .

    # Push the container to docker registry
    docker push {username}/helloworld-clojure
    ```

1. After the build has completed and the container is pushed to docker hub, you
   can deploy the app into your cluster. Ensure that the container image value
   in `service.yaml` matches the container you built in
   the previous step. Apply the configuration using `kubectl`:

    ```shell
    kubectl apply --filename service.yaml
    ```

1. Now that your service is created, Knative will perform the following steps:
   * Create a new immutable revision for this version of the app.
   * Network programming to create a route, ingress, service, and load balance for your app.
   * Automatically scale your pods up and down (including to zero active pods).

1. To find the IP address for your service, use
   `kubectl get svc knative-ingressgateway -n istio-system` to get the ingress IP for your
   cluster. If your cluster is new, it may take sometime for the service to get asssigned
   an external IP address.

    ```shell
    kubectl get svc knative-ingressgateway --namespace istio-system

    NAME                     TYPE           CLUSTER-IP     EXTERNAL-IP      PORT(S)                                      AGE
    knative-ingressgateway   LoadBalancer   10.23.247.74   35.203.155.229   80:32380/TCP,443:32390/TCP,32400:32400/TCP   2d

    ```

1. To find the URL for your service, use
    ```
    kubectl get services.serving.knative.dev helloworld-clojure  --output=custom-columns=NAME:.metadata.name,DOMAIN:.status.domain
    NAME                DOMAIN
    helloworld-clojure   helloworld-clojure.default.example.com
    ```

1. Now you can make a request to your app to see the result. Replace
   `{IP_ADDRESS}` with the address you see returned in the previous step.

    ```shell
    curl -H "Host: helloworld-clojure.default.example.com" http://{IP_ADDRESS}
    Hello World: NOT SPECIFIED
    ```

## Removing the sample app deployment

To remove the sample app from your cluster, delete the service record:

```shell
kubectl delete --filename service.yaml
```
