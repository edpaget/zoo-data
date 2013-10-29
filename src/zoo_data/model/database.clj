(ns zoo-data.model.database
  (:require [taoensso.carmine :as car]
            [korma.db :refer :all]
            [pghstore-clj.core :refer :all]
            [clj-time.core :refer [now]]
            [clj-time.coerce :refer [to-timestamp]]
            [korma.core :refer :all]))

;; Setup Database
(def redis-connection nil)
(def pg-connection nil)

(defmacro wcar* [& body] `(car/wcar redis-connection ~@body))

(defn connection
  []
  @(:pool pg-connection))

(defmacro cache-query 
  [id expiry & body]
  `(if-let [cache-check (wcar* (car/expire ~id ~expiry)
                               (car/get ~id))]
     cache-check
     (let [result ~@body]
       (wcar* (car/set ~id result)
              (car/expire ~id ~expiry))
       result)))

(defn create!
  [post-conn red-conn]
  (alter-var-root #'redis-connection (constantly red-conn)) 
  (alter-var-root #'pg-connection (constantly (defdb pg (postgres post-conn)))))

;; A couple nice functions to have around 
(defn select-by-id
  [ent id]
  (select ent
          (where {:id id})))

(defn update-by-id
  [ent id record]
  (update ent
          (set-fields record)
          (where {:id id})))

(defn delete-by-id
  [ent id]
  (delete ent
          (where {:id id})))

(defn insert-record
  [ent record]
  (insert ent
          (values record)))

;; Entities

(defn- updated-at
  [record]
  (let [record (dissoc record :updated_at)]
    (assoc record :updated_at (to-timestamp (now)))))

(defn- column-to-hstore
  [& columns]
  (fn [record]
    (reduce #(update-in %1 (vector %2) to-hstore) record columns)))

(declare project collection)

(defentity user
  (pk :id)
  (table :users)
  (entity-fields :name :api_key :ouroboros_api_key)
  (has-many collection)
  (prepare updated-at)
  (many-to-many project :users_projects {:lfk :user_id :rfk :project_id}))
 
(defentity project
  (pk :id)
  (table :projects)
  (has-many collection)
  (prepare (comp updated-at (column-to-hstore :subject_schema :classification_schema)))
  (many-to-many user :users_projects {:lfk :user_id :rfk :project_id})
  (entity-fields :name :display_name :secondary_index :subject_schema :classification_schema))
 
(defentity collection
  (pk :id)
  (belongs-to user)
  (belongs-to project)
  (table :collections)
  (prepare (comp updated-at (column-to-hstore :params)))
  (entity-fields :params :blessed))
