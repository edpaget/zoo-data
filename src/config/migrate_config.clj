(ns config.migrate-config
  (:require [paneer.core :as p]
            [paneer.db :as db]
            [clojure.java.jdbc :as j]))

(defn- connect-paneer
  [db]
  (let [db (or db (get (System/getenv) "DATABASE_URL"))]
    (db/set-default-db! db)))

(defn- maybe-create-schema-table
  []
  (p/create-if-not-exists
    (p/table :schema_version
           (p/integer :version :not-null)
           (p/timestamp :not-null :default "now()"))))

(defn current-db
  []
  (or (:version (:first (j/query @db/__default 
                                 ["SELECT * FROM \"schema_version\" ORDER BY \"created_at\" DESC LIMIT 1"])))))

(defn update-db
  [version]
  (j/insert! @db/__default "schema_version" {:version version}))

(defn migrate-config
  []
  {:directory "/src/migrations"
   :ns-content "\n (:require [paneer.core :as p])"
   :current-version current-db
   :init connect-paneer
   :update-version update-db})