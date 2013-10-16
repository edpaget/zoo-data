(ns migrations.20131015160553-add-users-table
  (:refer-clojure :exclude [bigint boolean char double float time drop]) 
  (:use paneer.core))

(defn up
  "Migrates the database up to version 20131015160553."
  []
  (create
    (table :users
           (varchar :id 24 :not-null :primary-key)
           (varchar :name 255 :not-null))))

(defn down
  "Migrates the database down from version 20131015160553."
  []
  (drop
    (table :users)))