(ns zoo-data.model.projects
  (:require [zoo-data.model.database :as db]
            [paneer.core :as p]
            [korma.core :refer :all]
            [paneer.db :as pd]))

(defn- to-column
  [[name type]]
  (let [type (cond
               (= "float" type) "float"
               (= "string" type) "varchar(255)"
               (= "integer" type) "integer"
               (= "hash" type) "hstore"
               (= "boolean" type) "boolean"
               (= "str_array" type) "text[]"
               (= "int_array" type) "integer[]")]
    [(keyword name) type]))

(defn create-schema
  [command schema]
  (reduce (fn [command [col-name col-type]]
            (p/column command col-name col-type))
          command
          (map to-column schema)))

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
  (delete :users_projects (where {:project_id id}))
  (db/delete-by-id db/project id))

(defn create
  [{:keys [name subject_schema classification_schema] :as record}]
  (try 
    (-> (p/create* :if-exists true)
        (p/table (str name "_subjects"))
        (p/varchar :id 24 :primary-key)
        (create-schema subject_schema)
        pd/execute)

    (-> (p/create* :if-exists true) 
        (p/table (str name "_classifications"))
        (p/serial :id :primary-key)
        (p/refer-to (str name "_subjects") "varchar(24)")
        (create-schema classification_schema)
        pd/execute)

    (-> (p/create* :if-exists true) 
        (p/table (str name "_denormalized_classifications"))
        (p/serial :id :primary-key)
        (p/refer-to (str name "_subjects") "varchar(24)")
        (create-schema classification_schema)
        pd/execute)

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
