 (defproject know-your-xwing-api "0.1.0-SNAPSHOT"
   :description "FIXME: write description"
   :dependencies [[cheshire "5.11.0"]
                  [org.clojure/clojure "1.10.0"]
                  [metosin/compojure-api "2.0.0-alpha30"]
                  [mount "0.1.16"]
                  [ring/ring-jetty-adapter "1.9.6"]
                  [ring/ring-devel "1.9.6"]]
   :ring {:handler know-your-xwing-api.handler/app}
   :uberjar-name "server.jar"
   :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                                  [ring/ring-mock "0.3.2"]]
                   :plugins [[lein-ring "0.12.5"]]}})
