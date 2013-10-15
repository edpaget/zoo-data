(ns config.migrate-config
  (:require [paneer.core :as p]
            [paneer.db :as db]
            [clojure.java.jdbc :as j]))

(defn- connect-paneer
  [& [db]]
  (let [db (or db (get (System/getenv) "DATABASE_URL"))]
    (db/set-default-db! db)))

(defn- maybe-create-schema-table
  []
  (p/create-if-not-exists
    (table :schema_version
           (p/bigint :version :not-null)
           (p/timestamp :created_at :not-null :default "now()"))))

(defn current-db
  []
  (connect-paneer)
  (maybe-create-schema-table)
  (println (:version (first (j/query @db/__default 
                                      ["SELECT * FROM \"schema_version\" ORDER BY \"created_at\" DESC LIMIT 1"]))))

  (or (:version (first (j/query @db/__default 
                                 ["SELECT * FROM \"schema_version\" ORDER BY \"created_at\" DESC LIMIT 1"]))) 0))

(defn update-db
  [version]
  (j/insert! @db/__default "schema_version" {:version version}))

(defn migrate-config
  []
  {:directory "/src/migrations"
   :ns-content "\n  (:refer-clojure :exclude [bigint boolean char double float time drop]) \n  (:use paneer.core)"
   :current-version current-db
   :init connect-paneer
   :update-version update-db})