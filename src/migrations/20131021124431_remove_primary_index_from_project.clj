(ns migrations.20131021124431-remove-primary-index-from-project
  (:refer-clojure :exclude [alter bigint boolean char double float time drop]) 
  (:use paneer.core))

(defn up
  "Migrates the database up to version 20131021124431."
  []
  (alter
    (table :projects
           (drop-column :primary_index))))

(defn down
  "Migrates the database down from version 20131021124431."
  []
  (alter
    (table :projects
           (add-column (varchar :primary_index 255)))))