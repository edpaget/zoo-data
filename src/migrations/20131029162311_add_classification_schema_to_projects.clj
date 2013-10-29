(ns migrations.20131029162311-add-classification-schema-to-projects
  (:refer-clojure :exclude [bigint boolean char double float]) 
  (:require [paneer.core :refer :all]))

(defn up
  "Migrates the database up to version 20131029162311."
  []
  (alter-table :projects
    (add-columns (column :classification_schema :hstore))))

(defn down
  "Migrates the database down from version 20131029162311."
  []
  (alter-table :projects (drop-column :classification_schema)))