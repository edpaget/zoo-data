(ns lobos.migrations
  (:refer-clojure :exclude [alter drop bigint boolean char double float time])
  (:use (lobos [migration :only [defmigration]] core schema
               config helpers)))
 
(defmigration add-projects-table
  (up []
      (create
        (tbl :projects
             (varchar :name 255)
             (varchar :primary-index 255)
             (varchar :secondary-index 255)
             (varchar :data-table 255)
             (varchar :classification-table 255))))
  (down [] (drop (table :projects))))
 
(defmigration add-collections-table
  (up [] 
      (create
        (tbl :collections
             (varchar :user 100)
             (varchar :project 255)
             (column :params (data-type :hstore))
             (refer-to :projects)))
      (index :collections [:user :project]))
  (down [] (drop (table :collections))))

(defmigration add-auth-table
  (up []
      (create
        (tbl :auth
             (varchar :auth-id 255)
             (varchar :api-key 255))))
  (down [] (drop (table :auth))))
