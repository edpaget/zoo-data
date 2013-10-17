(ns zoo-data.model.projects
  (:use korma.core)
  (:require [zoo-data.model.database :as db]
            [paneer.core :as p]))

(defentity projects
  (pk :id)
  (table :projects)
  (entity-fields :name :display_name :primary_index :secondary_index))

(defn create
  [record]
  (p/create
    (table (str (:name record) "_subjects")
           (p/varchar (:primary_index record) 255 :primary-key)))
  (p/create
    (table (str (:name record) "_classifications")
           (p/serial :id :primary-key)))
  (p/create
    (table (str (:name record) "_denormalized_classifications")
           (p/varchar (:primary_index record) 255 :primary-key)))
  (db/insert-record projects record))

(defn by-name
  [name]
  (first (select projects
                 (where {:name name}))))

(defn all
  []
  (select projects))
