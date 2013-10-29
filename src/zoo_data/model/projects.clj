(ns zoo-data.model.projects
  (:require [zoo-data.model.database :as db]
            [paneer.core :as p]
            [korma.core :refer :all]))

(defn create
  [{:keys [name] :as record}]
  (try (p/if-not-exists
         (p/create-table (str name "_subjects")
                         (p/varchar :id 24 :primary-key)))
       (p/if-not-exists
         (p/create-table (str name "_classifications")
                         (p/serial :id :primary-key)
                         (p/refer-to (str name "_subjects") "varchar(24)")))
       (p/if-not-exists
         (p/create-table (str name "_denormalized_classifications")
                         (p/varchar :id 24 :primary-key)
                         (p/refer-to (str name "_subjects") "varchar(24)")))
       (p/if-not-exists
         (p/create-table (str name "_subjects_collections")
                         (p/serial :id "PRIMARY KEY")
                         (p/refer-to :collections "integer")
                         (p/refer-to (str name "_subjects") "varchar(24)"))) 
       (db/insert-record db/project record)
       (catch Exception e
         (p/if-exists
           (p/drop-table (str name "_subjects_collections")))   
         (p/if-exists
           (p/drop-table (str name "_classifications")))
         (p/if-exists
           (p/drop-table (str name "_denormalized_classifications")))
         (p/if-exists
           (p/drop-table (str name "_subjects"))))))

(defn update-secondary-index
  [project new-index]
  (db/update-by-id db/project (:id project) {:secondary_index new-index}))

(defn by-name
  [name]
  (first (select db/project
                 (where {:name name}))))

(defn all
  []
  (select db/project))
