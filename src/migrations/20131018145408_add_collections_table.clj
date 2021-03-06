(ns migrations.20131018145408-add-collections-table
  (:refer-clojure :exclude [bigint boolean char double float])
  (:require [paneer.core :refer :all]))

(defn up
  []
  (create-table :collections
           (serial :id :primary-key)
           (refer-to :users "varchar(24)")
           (boolean :blessed)
           (refer-to :projects "varchar(24)")
           (column :params :hstore)))

(defn down
  []
  (drop-table :collections))