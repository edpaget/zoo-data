(ns zoo-data.system
  (:require [zoo-data.web.server :as s]
            [zoo-data.web.routes :as r]
            [zoo-data.model.database :as db]))

(defn system
  "Returns configuration for a new instance of the application"
  [& [port]]
  {:postgres {:db "zoo-data"
              :user "edward"
              :password ""
              :host "localhost"
              :port "5432"}
   :redis {:spec {:host "127.0.0.01"
                  :port 6379
                  :db 2}
           :pool {}}
   :handler (r/routes)
   :port (or port 3002)})

(defn start
  "Runs the application based on configuration."
  [system]
  (let [database (db/create! (:postgres system)
                             (:redis system))
        server (s/create (:handler system)
                         :port (:port system))]
    (into system {:server server :database database})))

(defn stop
  "Stops a running instance fo the application"
  [system]
  (when-let [server (:server system)]
    (s/stop server))
  (dissoc system :server :database))

(defn -main
  [& [port]]
  (start (merge (system) {:port (Integer. port)})))
