(ns zoo-data.system
  (:require [zoo-data.web.server :as s]
            [zoo-data.web.routes :as r]
            [zoo-data.model.database :as db]
            [clojure.string :as str]))

(defn postgres-url-to-korma
  [url]
  (let [url (java.net.URI. url)
        [username password] (str/split (.getUserInfo url) #":")]
    {:db (apply str (drop 1 (.getPath url)))
     :user username
     :password password
     :host (.getHost url)
     :port (.getPort url)}))

(defn system
  "Returns configuration for a new instance of the application"
  [& [port]]
  (let [redis-url (or (get (System/getenv) "REDISTOGO")
                      (get (System/getenv) "REDIS"))]
    {:postgres (postgres-url-to-korma (get (System/getenv) "DATABASE_URL")) 
     :redis {:pool {} :spec {:url redis-url}}
     :handler r/routes
     :zooniverse-api "http://localhost:3000"
     :port (or port 3002)}))

(defn start
  "Runs the application based on configuration."
  [system]
  (let [database (db/create! (:postgres system)
                             (:redis system))
        server (s/create ((:handler system) system)
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
