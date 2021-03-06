(ns zoo-data.model.collection
  (:use korma.core
        [clojure.walk :only [keywordize-keys]]
        [clojure.set :only [rename-keys]])
  (:require [zoo-data.model.database :as db]
            [clj-http.client :as client]
            [clojure.string :as str]))

(defn- ouroboros-url
  [project]
  (str (get (System/getenv) "ZOONIVERSE_URL") "/projects/" project))

(defn- get-primary-key
  [project]
  (fn [subject] 
    (get subject (keyword (:primary-index project)))))

(defn- create-join
  [project id ids]
  (insert (str (:data-table project) "_collections")
          (values (map #(array-map :collection_id id :subject_id %) ids))))

(defmulti build-collection (fn [params & _] (keyword (params "type"))))

(defmethod build-collection :talk-collection [{:strs [talk-collection]} id project]
  (let [subjects (-> (:name project)
                     ouroboros-url
                     (str "/talk/collections/" talk-collection)
                     (client/get {:as :json})
                     :body
                     :subjects)
        ids (map (get-primary-key project) subjects)]
    (create-join project id ids)))

(defmethod build-collection :query [params id project]
  (let [params (keywordize-keys (dissoc params "type"))
        subjects (select (:data-table project)
                         (where params))
        ids (map (get-primary-key project) subjects)]
    (create-join project id ids)))

(defn create
  [user project {:strs [name params]}]
  (let [col (insert db/collection
                    (values {:user user 
                             :project (:name project) 
                             :project_id (:id project)
                             :name name
                             :params params}))]
    (build-collection params (:id col) project)
    col))

(defn bless
  [id]
  (println id)
  (update db/collection
          (set-fields {:blessed true})
          (where {:id (Integer. id)})))

(defn update-col
  [id {:strs [name params]} project]
  (delete (str (:data-table project) "_collections")
          (where {:collection_id (Integer. id)}))
  (let [collection (first (select db/collection (where {:id (Integer. id)})))
        name (or name (:name collection))
        params (or params (into {} (:params collection)))]
    (build-collection params (Integer. id) project)
    (update db/collection
            (set-fields {:params params
                         :name name})
            (where {:id (Integer. id)}))))

(defn delete-col 
  [id]
  (delete db/collection
          (where {:id (Integer. id)})))

(defn find-by-user-and-project
  [user project]
  (select db/collection
          (where (or {:user user
                      :project (:name project)}
                     {:blessed true}))))

(defn find-by-id
  [id]
  (first (select db/collection
                 (where {:id (Integer. id)}))))

(defn get-data
  [id project]
  (let [table (str (:data-table project))
        join-table (str table "_collections")
        results (select join-table
                        (fields (str "\"" table "\".*"))
                        (where {:collection_id (Integer. id)})
                        (join table (= (keyword (str table ".zooniverse_id")) 
                                       (keyword (str join-table ".subject_id")))))]
    (map #(rename-keys % {(keyword (:primary-index project)) :uid}) results)))
