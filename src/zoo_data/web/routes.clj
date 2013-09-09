(ns zoo-data.web.routes
  (:use ring.middleware.json
        ring.middleware.stacktrace)
  (:require [compojure.core :as cmpj :refer [OPTIONS 
                                             GET 
                                             POST 
                                             PUT  
                                             DELETE 
                                             context]]
            [compojure.route :as route]
            [zoo-data.model.collection :as c]))

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

(defn routes
  []
  (let [handler (cmpj/routes
                  (context "/user/:user-id" [user-id] 
                           (context "/project/:project-name" [project-name]
                                    (context "/collection" []
                                             (GET "/" [] 
                                                  (resp-ok (c/find-by-user-and-project user-id project-name)))
                                             (GET "/:id" [id] 
                                                  (resp-ok (c/find-by-id id)))
                                             (POST "/" [params]
                                                   (resp-created (c/create user-id project-name params)))
                                             (PUT "/:id" [id params] 
                                                  (resp-ok (c/update-col id params)))
                                             (DELETE "/:id" [id]
                                                     (resp-no-content (c/delete-col id)))
                                             (GET "/:id/data" [id] (resp-ok (c/get-data id project-name)))))))]
    (-> (wrap-json-response handler)
        wrap-json-params
        wrap-stacktrace)))
