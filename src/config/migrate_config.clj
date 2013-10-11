(ns config.migrate-config
  (:require [taoensso.carmine :as car]))

(defn redis-conn
  []
  {:spec {:url (get (System/getenv) "REDIS")} 
   :poll {}})

(defn current-db
  []
  (or (car/wcar (redis-conn)
                (car/get "drift:db-version"))
      0))

(defn update-db
  [version]
  (car/wcar (redis-conn)
            (car/set "drift:db-version" version)))

(defn migrate-config
  []
  {:directory "/src/migrations"
   :ns-content "\n (:use migrate-helpers.helper)
                \n (:require [clojure.java.jdbc :as sql])"
   :current-version current-db
   :update-version update-db})