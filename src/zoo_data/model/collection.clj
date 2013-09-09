(ns zoo-data.model.collection
  (:use korma.core
        pghstore-clj.core)
  (:require [zoo-data.model.database :as db]
            [clj-http.client :as client]
            [clojure.string :as str]))

(defn- project-to-table
  [project]
  (str (str/replace project "_" "-") "-members"))

(defmulti build-collection (fn [params & _] (keyword (params "type"))))
(defmethod build-collection :talk-collection [{:strs [talk-collection]} id project]
  (let [subjects (-> (str "https://api.zooniverse.org/projects/" 
                          project "/talk/collections/" 
                          talk-collection)
                     (client/get {:as :json})
                     :body
                     :subjects)
        zoo-ids (map :zooniverse_id subjects)
        table (project-to-table project)
        members (map :id (select table
                                 (fields :id)
                                 (where (in :zooniverse-id zoo-ids))))]
    (insert (str table "-collections")
            (values (map #(array-map :collection_id id :galaxy-zoo-starburst-member_id %) 
                         members))))) 
(defentity collections
  (pk :id)
  (table :collections)
  (prepare #(update-in % [:params] to-hstore))
  (entity-fields :user :project :params))

(defn create
  [user project params]
  (let [col (insert collections
                    (values {:user user 
                             :project project 
                             :params params}))]
    (build-collection params (:id col) project)
    col))

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
  (first (select collections
                 (where {:id (Integer. id)}))))

(defn get-data
  [id project]
  (let [table (project-to-table project)
        join-table (str table "-collections")
        join-table-fk (str (str/replace project "_" "-") "-member_id")
        results (select join-table
                (fields [(keyword (str table ".zooniverse-id")) :uid] 
                        (keyword (str table ".ra"))
                        (keyword (str table ".dec")) 
                        (keyword (str table ".sdss-photo-id")) 
                        (keyword (str table ".attributes")))
                (where {:collection_id (Integer. id)})
                (join table (= (keyword (str table ".id")) 
                               (keyword (str join-table "." join-table-fk)))))]
    (map #(dissoc (merge % (:attributes %)) :attributes) results)))
