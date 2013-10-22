(ns zoo-data.model.project-subjects
  (:use korma.core)
  (:require [clojure.walk :as w]
            [paneer.core :as pa]
            [paneer.korma :as pk]
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

(defn- subject-table
  [project]
  (str (:name project) "_subjects"))

(defn create-schema
  [project schema]
  (pk/exec-korma* 
    (reduce (fn [command [col-name col-type]]
              (pa/column command col-name col-type))
            (pa/alter* (pa/table* (subject-table project)) :if-exists)
            (map to-column schema))))

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
