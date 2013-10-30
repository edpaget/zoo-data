(ns zoo-data.model.projects
  (:require [zoo-data.model.database :as db]
            [paneer.core :as p]
            [korma.core :refer :all]
            [paneer.db :as pd]))

(defn- qualify-table
  [table]
  (str "\"subject_classifications\".\"" table \"))

(defn- subject-table
  [project]
  (str (:name project) "_subjects"))
 
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
    (p/drop-table (qualify-table (str name "_subjects_collections"))))   
  (p/if-exists
    (p/drop-table (qualify-table (str name "_classifications"))))
  (p/if-exists
    (p/drop-table (qualify-table (str name "_denormalized_classifications"))))
  (p/if-exists
    (p/drop-table (qualify-table (str name "_subjects")))))

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
        (p/schema :subject_classifications)
        (p/varchar :id 24 :primary-key)
        (create-schema subject_schema)
        pd/execute)

    (-> (p/create* :if-exists true) 
        (p/table (str name "_classifications"))
        (p/schema :subject_classifications)
        (p/serial :id :primary-key)
        (p/refer-to (subject-table record) "varchar(24)" :subject_classifications)
        (create-schema classification_schema)
        pd/execute)

    (-> (p/create* :if-exists true) 
        (p/table (str name "_denormalized_classifications"))
        (p/schema :subject_classifications)
        (p/serial :id :primary-key)
        (p/refer-to (subject-table record) "varchar(24)" :subject_classifications)
        (create-schema classification_schema)
        pd/execute)

    (p/if-not-exists
      (p/create-table (str name "_subjects_collections")
                      (p/serial :id "PRIMARY KEY")
                      (p/refer-to :collections "integer")
                      (p/refer-to (subject-table record) "varchar(24)" :subject_classifications)))

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

(defn create-subject
  [project subject]
  (insert (subject-table project)
          (values subject)))

(defn update-subject
  [project id subject]
  (update (subject-table project)
          (where {:id id})
          (set-fields subject)))

(defn create-subjects
  [project subjects]
  (doseq [subject subjects]
    (create-subject project subject)))

(defn update-subjects
  [project subjects]
  (doseq [{:strs [zooniverse_id] :as subject} subjects]
    (update-subject project zooniverse_id subject)))

(defn get-subjects 
  [{:keys [primary-index data-table classification-table]}]
  (let [query (select* data-table)]
    (if classification-table
      (-> query 
          (join classification-table
                (= (keyword (str data-table "." primary-index))
                   (keyword (str classification-table "." primary-index))))
          (select))
      (-> query
          (select)))))
