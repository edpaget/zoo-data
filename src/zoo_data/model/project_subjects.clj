(ns zoo-data.model.project-subjects
  (:use korma.core)
  (:require [clojure.walk :as w]
            [zoo-data.model.database :as db]
            [zoo-data.model.projects :as p]))

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
