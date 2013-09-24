(ns zoo-data.model.project-table
  (:use korma.core)
  (:require [clojure.java.jdbc :as sql]
            [clojure.walk :as w]
            [zoo-data.model.database :as db]))

(defentity projects
  (pk :id)
  (table :projects)
  (entity-fields :name :primary-index :secondary-index :data-table :classification-table))

(defn create-project
  [{:strs [name primary_index secondary_index data_table classification_table]}]
  (insert projects
          (values {:name name
                   :primary-index primary_index
                   :secondary-index secondary_index
                   :data-table data_table
                   :classification-table classification_table})))

(defmacro with-conn* [& body]
  `(do (sql/with-connection (db/connection) (sql/transaction ~@body))))

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

(defn gen-schema
  ([primary-index columns]
   (conj (mapv to-column columns)
         [(keyword primary-index) "VARCHAR(255)" "NOT NULL" "PRIMARY KEY"]))
  ([primary-index secondary-index columns]
   (let [columns (gen-schema primary-index columns)]
     (conj columns [(keyword secondary-index) "VARCHAR(255)" "UNIQUE" "NOT NULL"]))))

(defn create-table
  [{:strs [primary_index secondary_index]} table {:strs [columns data]}]
  (let [columns (if secondary_index 
                  (gen-schema primary_index secondary_index columns) 
                  (gen-schema primary_index columns))]
    (with-conn* 
      (apply sql/create-table table columns)))
  (when data
    (doseq [datum data] 
      (insert table
              (values (w/keywordize-keys datum))))))

(defn- create-reference
  [table field]
  (str "REFERENCES " table " (" field ") " "ON DELETE SET NULL"))

(defn create-collection-join
  [{:strs [name data_table primary_index]}]
  (with-conn*
    (sql/create-table 
      (str name "_subjects_collections")
      [:id "INTEGER" "NOT NULL" "PRIMARY KEY" "AUTO_INCREMENT"]
      [:collection_id "INTEGER" (create-reference "collections" "id")]
      [:subject_id "VARCHAR(255)" (create-reference data_table primary_index)])))

(defn by-name
  [name]
  (first (select projects
                 (where {:name name}))))

(defn data-by-name
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
