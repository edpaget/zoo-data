(ns zoo-data.model.users
  (:use korma.core)
  (:require [zoo-data.model.database :as db]
            [zoo-data.model.projects :as p]
            [crypto.random :as r]))

(defentity users
  (pk :id)
  (table :users)
  (entity-fields :name :api_key :ouroboros_api_key)
  (many-to-many p/projects :users_projects))

(defn- gen-api-key
  []
  (r/base64 100))

(defn create
  [record]
  (db/insert-record users (assoc record :api_key (gen-api-key))))

(defn add-projects
  [{:keys [id]} projects]
  (let [records (map #(assoc % :user_id id) projects)] 
    (insert :users_projects
            (values records))))
