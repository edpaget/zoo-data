(ns zoo-data.model.collection
  (:use korma.core
        pghstore-clj.core)
  (:require [zoo-data.model.database :as db]))

(defentity collections
  (pk :id)
  (table :collections)
  (database db/postgres-connection)
  (prepare #(update-in % [:params] to-hstore))
  (entity-fields :user :project :params))

(defn create
  [user project params]
  (insert collections
          (values {:user user 
                   :project project 
                   :params params})))

(defn update-col
  [id params]
  (update collections
             (set-fields {:params params})
             (where {:id (Integer. id)})))

(defn delete-col 
  [id]
  (delete collections
          (where {:id (Integer. id)})))

(defn find-by-user-and-project
  [user project]
  (select collections
          (where {:user user
                  :project project})))

(defn find-by-id
  [id]
  (select collections
          (where {:id (Integer. id)})))
