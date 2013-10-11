(ns migrations.20131011142059-add-projects-table
  (:use migrate-helpers.helper)
  (:require [clojure.java.jdbc :as sql]))

(defn up
  "Migrates the database up to version 20131011142059."
  []
  (with-conn* 
    (sql/create-table
      "projects"
      [:id "VARCHAR(24)" "NOT NULL" "PRIMARY KEY"]
      [:name "VARCHAR(255)" "NOT NULL"]
      [:display_name "VARCHAR(255)"]
      [:primary_index "VARCHAR(255)"]
      [:secondary_index "VARCHAR(255)"])))

(defn down
  "Migrates the database down from version 20131011142059."
  []
  (with-conn*
    (sql/drop-table "projects")))