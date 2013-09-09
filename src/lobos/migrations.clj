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

(defmigration add-galaxy-zoo-starburst-members-table
  (up []
      (create
        (-> (table :galaxy-zoo-starburst-members)
            surrogate-key
            (varchar :zooniverse-id 20)
            (varchar :sdss-photo-id 40)
            (float :ra)
            (float :dec)
            (column :attributes (data-type :hstore))))
      (index :galaxy-zoo-starburst-members [:zooniverse-id])
      (index :galaxy-zoo-starburst-members [:sdss-photo-id]))
  (down [] (drop (table :galaxy-zoo-starburst-members))))

(defmigration add-galaxy-zoo-starburst-members-collections
  (up []
      (create 
        (tbl :galaxy-zoo-starburst-members-collections
             (refer-to :galaxy-zoo-starburst-members)
             (refer-to :collections))))
  (down [] (drop (table :galaxy-zoo-starburst-members-collections))))
