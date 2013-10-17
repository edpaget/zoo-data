(ns zoo-data.model.projects
  (:use korma.core)
  (:require [zoo-data.model.database :as db]
            [paneer.core :as p]
            ))

(defentity projects
  (pk :id)
  (table :projects)
  (entity-fields :name :display_name :primary_index :secondary_index))

(defn create
  [record]
  (db/insert-record projects record))

(defn by-name
  [name]
  (first (select projects
                 (where {:name name}))))

(defn all
  []
  (select projects))
