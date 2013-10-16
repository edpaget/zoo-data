(ns migrations.20131016133901-add-api-key-to-users
  (:refer-clojure :exclude [alter bigint boolean char double float time drop]) 
  (:use paneer.core))

(defn up
  "Migrates the database up to version 20131016133901."
  []
  (alter
    (table :users
           (add-column (varchar :api_key 255))))
  (alter
    (table :users
           (add-column (varchar :ouroboros_api_key 255)))))

(defn down
  "Migrates the database down from version 20131016133901."
  []
  (alter 
    (table :users
           (drop-column :api_key)))
  (alter
    (table :users
           (drop-column :ouroboros_api_key))))