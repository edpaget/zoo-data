(ns zoo-data.model.projects
  (:use korma.core)
  (:require [zoo-data.model.database :as db]
            [paneer.core :as p]
            [paneer.korma :refer [exec-korma]]))

(defn create
  [{:keys [name] :as record}]
  (exec-korma
    (p/create-if-not-exists
      (table (str name "_subjects")
             (p/varchar :id 24 :primary-key))))
  (exec-korma 
    (p/create-if-not-exists
      (table (str name "_classifications")
             (p/serial :id :primary-key))))
  (exec-korma 
    (p/create-if-not-exists
      (table (str name "_denormalized_classifications")
             (p/varchar :id 24 :primary-key))))
  (exec-korma 
    (p/create-if-not-exists
      (table (str name "_subjects_collections")
             (p/serial :id "PRIMARY KEY")
             (p/refer-to :collections "integer")
             (p/refer-to (str name "_subjects") "varchar(24)")))) 
  (db/insert-record db/project record))

(defn update-secondary-index
  [project new-index]
  (db/update-by-id db/project (:id project) {:secondary_index new-index}))

(defn by-name
  [name]
  (first (select db/project
                 (where {:name name}))))

(defn all
  []
  (select db/project))
