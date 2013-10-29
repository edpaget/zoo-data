(ns migrations.20131029133853-add-subject-schema-to-projects
  (:refer-clojure :exclude [bigint boolean char double float]) 
  (:require [paneer.core :refer :all]))

(defn up
  "Migrates the database up to version 20131029133853."
  []
  (alter-table :projects
    (add-columns (column :subject_schema :hstore))))

(defn down
  "Migrates the database down from version 20131029133853."
  []
  (alter-table :projects (drop-column :subject_schema)))