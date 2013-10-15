(ns migrations.20131015144913-add-projects-table
  (:refer-clojure :exclude [bigint boolean char double float time drop]) 
  (:use paneer.core))

(defn up
  "Migrates the database up to version 20131015144913."
  []
  (create
    (table :projects
           (varchar :id 24 :not-null :primary-key)
           (varchar :name 255 "NOT NULL")
           (varchar :display_name 255)
           (varchar :primary_index 255)
           (varchar :secondary_index 255))))

(defn down
  "Migrates the database down from version 20131015144913."
  []
  (drop
    (table :projects)))