(ns zoo-data.web.project
  (:require [zoo-data.model.projects :as p]
            [zoo-data.web.user :as u]
            [zoo-data.model.users :as us]
            [compojure.core :refer :all]
            [zoo-data.web.resp-util :refer :all]
            [clojure-csv.core :refer [parse-csv]]))

(defn wrap-project
  [handler]
  (fn [req]
    (let [req (update-in req [:params :project] p/by-name)]
      (if (get-in req [:params :project]) 
        (handler req)
        (resp-not-found)))))

(defn find-by-name
  [name]
  (p/by-name name))

(defn all-projects
  []
  (p/all))

(defn- create-project-from-json
  [user {:strs [id name secondary-index display_name subject_schema classification_schema]}] 
  (let [project (p/create {:id id
                           :name name
                           :secondary_index secondary-index
                           :subject_schema subject_schema
                           :display_name display_name
                           :classification_schema classification_schema})] 
    (us/add-project (:id user) (:id project))
    (resp-created project)))

(defn read-csv-body
  [csv-body]
  (let [[headers & body] (parse-csv (slurp csv-body))] 
    (map (fn [line] (zipmap headers line)) body)))

(defn wrap-csv-body
  [handler]
  (fn [req]
    (if (= "text/csv" (get-in req [:headers "content-type"]))
      (handler (update-in req [:body] read-csv-body))
      (handler req))))

(defn- update-secondary-index
  [project body]
  (resp-ok (p/update-secondary-index project (:secondary_index body))))

(defroutes subject-routes
  (wrap-csv-body
    (routes
      (POST "/subjects" [project :as {body :body}]
            (resp-no-content p/create-subjects project body))
      (PUT "/subjects" [project :as {body :body}]
           (p/update-subjects project body :replace))
      (PATCH "/subjects" [project :as {body :body}]
             (p/update-subjects project body))
      (PUT "/subjects/:id" [project id :as {body :body}]
           (resp-ok (p/update-datum (p/qualify-table (p/subject-table project)) 
                             project id body :replace)))
      (PATCH "/subjects/:id" [project id :as {body :body}]
             (resp-ok (p/update-datum (p/qualify-table (p/subject-table project)) 
                                        project id body))))))

(defroutes project-routes
  (routes
    (context "/projects" []
             (GET "/" [] (resp-ok (all-projects)))
             (POST "/" [user :as {body :body}] (create-project-from-json user body))
             (context "/:project" []
                      (wrap-project 
                        (routes 
                          (GET "/" [project] (resp-ok project))
                          (GET "/subjects" [project]
                               (p/get-subjects project))
                          (u/wrap-project-auth
                            (routes 
                              (PATCH "/" [project :as {body :body}] 
                                     (update-secondary-index project body))
                              (DELETE "/" [project] (resp-no-content p/delete-project project))
                              subject-routes)))))))) 
