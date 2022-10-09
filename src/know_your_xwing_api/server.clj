(ns know-your-xwing-api.server
  (:require
   [know-your-xwing-api.handler :as handler]
   [mount.core :as mount]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.reload :refer [wrap-reload]])
  (:import (org.eclipse.jetty.server Server)))
  
(def app (wrap-reload handler/app {:dirs ["src/clj"]}))

(mount/defstate ^{:on-reload :noop} ApplicationServer
  :start
  (jetty/run-jetty #'app {:port 3000 :join? false})
  :stop (when instance? Server ApplicationServer
              (.stop ApplicationServer)))

(defn -main
  [& args]
  (mount/start))
