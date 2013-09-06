(ns lobos.migrations
  (:refer-clojure :exclude [alter drop
                            bigint boolean char double float time])
  (:use (lobos [migration :only [defmigration]] core schema
               config helpers)))

(defmigration add-collections-table
  (up [] 
      (create
        (tbl :collections
             (varchar :user 100)
             (varchar :project 100)
             (column :params (data-type :hstore))))
      (index :collections [:user :project]))
  (down [] (drop (table :collections))))
