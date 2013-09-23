(ns zoo-data.web.project
  (:require [zoo-data.model.project-table :as pt]))

(defn find-by-name
  [name]
  (pt/by-name name))

(defn- create-project-from-json
  [{:strs [project data classifications]}] 
  (pt/create-project project)
  (pt/create-table project (get project "data_table") data)
  (pt/create-collection-join project)
  (when classifications
    (pt/create-table project (get project "classifications_table") classifications)))

(defn create-project
  [body]
  (create-project-from-json body)
  " ")

(comment (defn update-data
  [{:keys [content-type]} {:strs [data]} & [body]]
  (cond
    (= content-type "application/json") (if (vector? data) 
                                          (doseq [datum data] (pt/update-data datum))
                                          (pt/update-data datum))
    (= content-type "text/csv") (doseq [datum data] (pt/udpate-data dataum)))))
