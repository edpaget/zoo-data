(ns migrations.20131016133901-add-api-key-to-users
  (:refer-clojure :exclude [bigint boolean char double float]) 
  (:require [paneer.core :refer :all]))

(defn up
  "Migrates the database up to version 20131016133901."
  []
  (alter-table :users
               (add-columns (varchar :api_key 255)
                            (varchar :ouroboros_api_key 255))))

(defn down
  "Migrates the database down from version 20131016133901."
  []
  (alter-table :users
               (drop-column :api_key))
  (alter-table :users
               (drop-column :ouroboros_api_key)))