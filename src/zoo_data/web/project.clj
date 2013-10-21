(ns zoo-data.web.project
  (:use compojure.core
        zoo-data.web.resp-util)
  (:require [zoo-data.model.projects :as p]
            [zoo-data.model.project-subjects :as ps]))

(defn wrap-project
  [handler]
  (fn [req]
    (let [req (update-in req [:params :project] p/by-name)]
      (handler req))))

(defn find-by-name
  [name]
  (p/by-name name))

(defn all-projects
  []
  (p/all))

(defn- create-project-from-json
  [{:strs [id name secondary-index display_name]}] 
  (resp-created (p/create {:id id
                           :name name
                           :secondary_index secondary-index
                           :display_name display_name})))

(defn- update-secondary-index
  [project body]
  (resp-ok (p/update-secondary-index project (:secondary_index body))))

(defroutes project-routes
  (routes
    (context "/projects" []
             (GET "/" [] (resp-ok (all-projects)))
             (POST "/" {body :body} (create-project-from-json body))
             (context "/:project" []
                      (wrap-project 
                        (routes 
                          (GET "/" [project] (resp-ok project))
                          (PATCH "/" [project :as {body :body}] 
                                 (update-secondary-index project body))
                          (POST "/subjects/schema" [project :as {body :body}] 
                                (ps/create-schema project body))
                          (POST "/subjects" [project :as {body :body}]
                                (ps/create-subjects project body))
                          (PUT "/subjects" [project :as {body :body}]
                               (ps/update-subjects project body :replace))
                          (PATCH "/subjects" [project :as {body :body}]
                                 (ps/update-subjects project body))
                          (PUT "/subjects/:id" [project id :as {body :body}]
                               (ps/update-subject project id body :replace))
                          (PATCH "/subjects/:id" [project id :as {body :body}]
                                 (ps/update-subject project id body))
                          (GET "/subjects" [project]
                               (ps/get-subjects project))))))))
