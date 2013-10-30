(ns migrations.20131030143215-add-subject-classification-schema
  (:refer-clojure :exclude [bigint boolean char double float]) 
  (:require [paneer.core :refer :all]
            [paneer.db :refer :all]))

(defn up
  "Migrates the database up to version 20131030143215."
  []
  (-> (create-schema*)
      (schema :subject_classifications)  
      execute))

(defn down
  "Migrates the database down from version 20131030143215."
  []
  (-> (drop-schema :cascade)
      (schema :subject_classifications)  
      execute))