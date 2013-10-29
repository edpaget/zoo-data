(ns migrations.20131029101528-add-timestamps-to-all-tables
  (:refer-clojure :exclude [bigint boolean char double float]) 
  (:require [paneer.core :refer :all])
  (:require [paneer.db :refer [execute]])
  )

(defn up
  "Migrates the database up to version 20131029101528."
  []
  (alter-table :users
               (add-columns (timestamps)))
  (alter-table :projects
               (add-columns (timestamps)))
  (alter-table :collections
               (add-columns (timestamps))))

(defn- drop-timestamps 
  [tbl]
  (-> (alter*)
      (table tbl)
      (column :created_at)
      (drop-column* :updated_at)
      execute))

(defn down
  "Migrates the database down from version 20131029101528."
  []
  (drop-timestamps :users)
  (drop-timestamps :projects)
  (drop-timestamps :collections))