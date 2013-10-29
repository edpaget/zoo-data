(ns zoo-data.web.project
  (:use compojure.core
        zoo-data.web.resp-util)
  (:require [zoo-data.model.projects :as p]
            [zoo-data.web.user :as u]
            [zoo-data.model.project-subjects :as ps]))

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
  [{:strs [id name secondary-index display_name subject_schema]}] 
  (resp-created (p/create {:id id
                           :name name
                           :secondary_index secondary-index
                           :subject_schema subject_schema
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
                          (GET "/subjects" [project]
                               (ps/get-subjects project))
                          (u/wrap-project-auth
                            (routes 
                              (PATCH "/" [project :as {body :body}] 
                                     (update-secondary-index project body))
                              (DELETE "/" [project] (p/delete-project project))
                              (POST "/subjects" [project :as {body :body}]
                                    (ps/create-subjects project body))
                              (PUT "/subjects" [project :as {body :body}]
                                   (ps/update-subjects project body :replace))
                              (PATCH "/subjects" [project :as {body :body}]
                                     (ps/update-subjects project body))
                              (PUT "/subjects/:id" [project id :as {body :body}]
                                   (ps/update-subject project id body :replace))
                              (PATCH "/subjects/:id" [project id :as {body :body}]
                                     (ps/update-subject project id body))))))))))
