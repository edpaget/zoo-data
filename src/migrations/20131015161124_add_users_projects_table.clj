(ns migrations.20131015161124-add-users-projects-table
  (:refer-clojure :exclude [bigint boolean char double float])
  (:require [paneer.core :refer :all]))

(defn up
  "Migrates the database up to version 20131015161124."
  []
  (create-table :users_projects
                (serial :id :not-null :primary-key)
                (refer-to :users "varchar(24)")
                (refer-to :projects "varchar(24)")))

(defn down
  "Migrates the database down from version 20131015161124."
  []
  (drop-table :users_projects))