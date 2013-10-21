(ns zoo-data.model.projects
  (:use korma.core)
  (:require [zoo-data.model.database :as db]
            [paneer.core :as p]
            [paneer.korma :refer [exec-korma]]))

(defentity projects
  (pk :id)
  (table :projects)
  (entity-fields :name :display_name :secondary_index))

(defn create
  [record]
  (exec-korma
    (p/create
      (table (str (:name record) "_subjects")
             (p/varchar :id 24 :primary-key))))
  (exec-korma 
    (p/create
      (table (str (:name record) "_classifications")
             (p/serial :id :primary-key))))
  (exec-korma 
    (p/create
      (table (str (:name record) "_denormalized_classifications")
             (p/varchar :id 24 :primary-key))))
  (exec-korma 
    (p/create
      (table (str name "_subjects_collections")
             (p/serial :id "PRIMARY KEY")
             (p/refer-to :collections "integer")
             (p/refer-to (str name "_subjects") "varchar(24)")))) 
  (db/insert-record projects record))

(defn update-secondary-index
  [project new-index]
  (db/update-by-id projects (:id project) {:secondary_index new-index}))

(defn by-name
  [name]
  (first (select projects
                 (where {:name name}))))

(defn all
  []
  (select projects))
