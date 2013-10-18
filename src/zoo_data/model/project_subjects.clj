(ns zoo-data.model.project-subjects
  (:use korma.core)
  (:require [clojure.walk :as w]
            [zoo-data.model.database :as db]
            [zoo-data.model.projects :as p]
            [paneer.core :as pa]
            paneer.korma))

(defn create-collection-join
  [{:strs [name primary_index]}]
  (pa/create
    (table (keyword (str name "_subjects_collections"))
           (pa/serial :id "PRIMARY KEY")
           (pa/refer-to :collections "integer")
           (pa/refer-to (keyword (str name "_subjects")) "varchar(24)"))))
 
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
  [schema]
  )

(defn create-schema
  [project schema]
  )

(defn create-subject
  [project subject]
  )

(defn update-subject
  [project subject])

(defn create-subjects
  [project subjects]
  (doseq [subject subjects]
    (create-subject project subject)))

(defn update-subjects
  [project subjects]
  )

(defn- create-reference
  [table field]
  (str "REFERENCES " table " (" field ") " "ON DELETE SET NULL"))

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
