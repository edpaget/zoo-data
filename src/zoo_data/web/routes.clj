(ns zoo-data.web.routes
  (:use ring.middleware.json
        ring.middleware.stacktrace
        [clojure.string :only [upper-case]])
  (:require [compojure.core :as cmpj :refer [OPTIONS 
                                             GET 
                                             POST 
                                             PUT  
                                             DELETE 
                                             defroutes
                                             context]]
            [compojure.route :as route]
            [zoo-data.model.collection :as c]
            [zoo-data.model.project-table :as pt]
            [zoo-data.web.project :as p]))

(defn- resp
  [status body]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body body})

(defn- resp-ok
  [body]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body body})

(defn- resp-created
  [body]
  {:status 201
   :headers {"Content-Type" "application/json"}
   :body body})

(defn- resp-no-content
  [& args]
  {:status 204
   :body ""})

(defn wrap-cors
  [handler]
  (fn [req] 
    (let [response (handler req)]
      (update-in response 
                 [:headers] 
                 merge 
                 {"Access-Control-Allow-Origin" "*"
                  "Access-Control-Allow-Headers" "content-type"
                  "Access-Control-Allow-Methods" "GET, OPTIONS, PUT, POST, DELETE"}))))

(defn wrap-project
  [handler]
  (fn [req]
    (let [req (update-in req [:params :project] pt/by-name)]
      (handler req))))

(defroutes collections-routes
  (cmpj/routes
    (context "/collection" [user-id project]
             (GET "/" [] 
                  (resp-ok (c/find-by-user-and-project user-id project)))
             (GET "/:id" [id] 
                  (resp-ok (c/find-by-id id)))
             (POST "/" {body :body}
                   (resp-created (c/create user-id project body)))
             (PUT "/:id" {{:keys [id]} :params body :body} 
                  (resp-ok (c/update-col id body project)))
             (DELETE "/:id" [id]
                     (resp-no-content (c/delete-col id)))
             (GET "/:id/data" [id] (resp-ok (c/get-data id project)))
             (POST "/:id/bless" [id] (resp-ok (c/bless id))))))
 
(defroutes app-routes
  (cmpj/routes
    (OPTIONS "/*" [] (resp-ok ""))
    (context "/project" []
             (GET "/:project" [project] (resp-ok (p/find-by-name project)))
             (POST "/" {body :body} (resp 202 (p/create-project body))))
    (context "/user/:user-id" [] 
             (context "/project/:project" [] (wrap-project collections-routes))))
  (route/not-found "Not Found"))

(defn routes
  []
  (-> (wrap-json-response app-routes)
      wrap-json-body
      wrap-cors
      wrap-stacktrace))
