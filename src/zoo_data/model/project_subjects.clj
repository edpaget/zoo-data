(ns zoo-data.model.project-subjects
  (:use korma.core)
  (:require [clojure.walk :as w]
            [paneer.core :as pa]
            [zoo-data.model.database :as db]))

(defn- subject-table
  [project]
  (str (:name project) "_subjects"))

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
