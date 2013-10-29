(ns zoo-data.model.projects
  (:require [zoo-data.model.database :as db]
            [paneer.core :as p]
            [korma.core :refer :all]
            [paneer.db :as pd]
            [zoo-data.model.project-subjects :as ps]))

(defn- drop-project-tables
  [name]
  (p/if-exists
    (p/drop-table (str name "_subjects_collections")))   
  (p/if-exists
    (p/drop-table (str name "_classifications")))
  (p/if-exists
    (p/drop-table (str name "_denormalized_classifications")))
  (p/if-exists
    (p/drop-table (str name "_subjects"))))

(defn delete-project
  [{:keys [id name]}]
  (drop-project-tables name)
  (db/delete-by-id db/project id))

(defn create
  [{:keys [name subject_schema] :as record}]
  (try (-> (p/create* :if-exists true)
           (p/table (str name "_subjects"))
           (p/varchar :id 24 :primary-key)
           (ps/create-schema subject_schema)
           pd/execute)

       (p/if-not-exists
         (p/create-table (str name "_classifications")
                         (p/serial :id :primary-key)
                         (p/refer-to (str name "_subjects") "varchar(24)")))

       (p/if-not-exists
         (p/create-table (str name "_denormalized_classifications")
                         (p/varchar :id 24 :primary-key)
                         (p/refer-to (str name "_subjects") "varchar(24)")))

       (p/if-not-exists
         (p/create-table (str name "_subjects_collections")
                         (p/serial :id "PRIMARY KEY")
                         (p/refer-to :collections "integer")
                         (p/refer-to (str name "_subjects") "varchar(24)"))) 

       (db/insert-record db/project record)
       (catch Exception e
         (drop-project-tables name)
         (throw e))))

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
