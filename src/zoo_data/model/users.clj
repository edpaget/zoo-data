(ns zoo-data.model.users
  (:use korma.core)
  (:require [zoo-data.model.database :as db]
            [zoo-data.model.projects :as p]
            [crypto.random :as r]))

(defn- gen-api-key
  []
  (r/base64 50))

(defn- admin-of?
  [[id roles]]
  (some #{"science" "admin"} roles))

(defn- project-exists?
  [id]
  (not (empty? (select db/project 
                       (where {:id (name id)})))))

(defn add-projects
  [{:keys [id]} roles]
  (let [roles (map first (filter admin-of? roles))
        roles (filter project-exists? roles)]
    (if-not (empty? roles)
      (insert :users_projects
              (values (map #(hash-map :project_id (name %) :user_id (name id)) roles))))))

(defn select-by-id
  [id]
  (first (select db/user
                 (with db/project)
                 (where {:id id})))) 

(defn create
  [record]
  (let [roles (:roles record)
        record (dissoc record :roles)
        db-entry  (db/insert-record db/user (assoc record :api_key (gen-api-key)))]
    (when roles 
      (add-projects db-entry roles))
    (select-by-id (:id db-entry))))
