(ns zoo-data.web.project
  (:use compojure.core
        zoo-data.web.resp-util)
  (:require [zoo-data.model.projects :as p]))

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
  [{:strs [id name primary-index secondary-index display_name]}] 
  (p/create {:id id
             :name name
             :primary_index primary-index
             :secondary_index secondary-index
             :display_name display_name}))

(defroutes project-routes
  (routes
    (context "/projects" []
             (GET "/" [] (resp-ok (all-projects)))
             (POST "/" {body :body} (resp-created (create-project-from-json body)))
             (GET "/:name" [name] (resp-ok (find-by-name name))))))
