(ns config.migrate-config
  (:refer-clojure :exclude [bigint boolean char double float])
  (:require [paneer.core :refer :all]
            [korma.db :refer :all :exclude [transaction]]
            [korma.core :refer [select order limit insert values]]
            [zoo-data.system :refer [postgres-url-to-korma]]))

(defn- connect-paneer
  [& args]
  (-> (System/getenv) 
      (get "DATABASE_URL")  
      postgres-url-to-korma 
      postgres 
      create-db 
      default-connection))

(defn- maybe-create-schema-table
  []
  (if-not-exists
    (create-table :schema_version
                  (bigint :version :not-null)
                  (timestamp :created_at :not-null :default "now()"))))

(defn current-db
  []
  (maybe-create-schema-table)
  (or (:version (first (select :schema_version
                               (order :created_at :desc)
                               (limit 1))))
      0))

(defn update-db
  [version]
  (insert :schema_version
          (values {:version version})))

(defn migrate-config
  []
  {:directory "/src/migrations"
   :ns-content "\n  (:refer-clojure :exclude [bigint boolean char double float]) \n  (:require [paneer.core :refer :all])"
   :current-version current-db
   :init connect-paneer
   :update-version update-db})