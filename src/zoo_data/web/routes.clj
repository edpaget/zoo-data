(ns zoo-data.web.routes
  (:use ring.middleware.json
        ring.middleware.stacktrace
        [clojure.string :only [upper-case]]
        zoo-data.web.resp-util)
  (:require [compojure.core :as cmpj :refer [OPTIONS 
                                             GET 
                                             POST 
                                             PUT  
                                             DELETE 
                                             defroutes
                                             context]]
            [compojure.route :as route]
            [zoo-data.model.collection :as c]
            [zoo-data.web.user :as u]
            [zoo-data.web.project :as p]))

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

(defn wrap-system
  [handler system]
  (fn [req]
    (handler (update-in req [:params] assoc :system system))))

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
    (POST "/login" [system :as {body :body}] (resp-ok (u/login system body)))
    p/project-routes
    (context "/user/:user-id" [] 
             (context "/project/:project" [] (p/wrap-project collections-routes))))
  (route/not-found "Not Found"))

(defn routes
  [system]
  (-> (wrap-system app-routes system)
      wrap-json-response 
      wrap-json-body
      wrap-cors
      wrap-stacktrace))
