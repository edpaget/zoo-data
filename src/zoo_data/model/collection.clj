(ns zoo-data.model.collection
  (:use korma.core
        pghstore-clj.core)
  (:require [zoo-data.model.database :as db]
            [clj-http.client :as client]
            [clojure.string :as str]))

(defn- ouroboros-url
  [project]
  (str (get (System/getenv) "ZOONIVERSE_URL") "/projects/" project))

(defmulti build-collection (fn [params & _] (keyword (params "type"))))
(defmethod build-collection :talk-collection [{:strs [talk-collection]} id project]
  (println (ouroboros-url project))
  (let [subjects (-> (str (ouroboros-url project) "/talk/collections/" talk-collection)
                     (client/get {:as :json})
                     :body
                     :subjects)
        zoo-ids (map :zooniverse_id subjects)]
    (insert (str project "_subjects_collections")
            (values (map #(array-map :collection_id id :subject_id %) zoo-ids))))) 

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
  [id params project]
  (delete (str project "_subjects_collections")
          (where {:collection_id (Integer. id)}))
  (build-collection params (Integer. id) project)
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
  (let [table (str project "_subjects")
        join-table (str table "_collections")]
    (select join-table
            (fields (keyword (str table ".*")))
            (where {:collection_id (Integer. id)})
            (join table (= (keyword (str table ".zooniverse_id")) 
                           (keyword (str join-table ".subject_id")))))))
