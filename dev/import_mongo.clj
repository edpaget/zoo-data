(ns import-mongo
  (:use korma.core
        pghstore-clj.core)
  (:require [monger.core :as mc]
            [monger.collection :as m]
            [zoo-data.model.database :as db]
            [clojure.string :as str]))

(defentity gzs-subject
  (pk :id)
  (table :galaxy-zoo-starburst-members)
  (prepare #(update-in % [:attributes] to-hstore))
  (entity-fields :zooniverse-id :sdss-photo-id :ra :dec :attributes))
    
(defn galaxy-zoo-starburst-format
  [subject]
  (let  [new-subject {:zooniverse-id (:zooniverse_id subject)
                      :sdss-photo-id (get-in subject [:metadata :sdss_id])
                      :ra (get-in subject [:coords 0])
                      :dec (get-in subject [:coords 1])
                      :attributes {:u (get-in subject [:metadata :umag])
                                   :g (get-in subject [:metadata :gmag])
                                   :r (get-in subject [:metadata :rmag])
                                   :i (get-in subject [:metadata :imag])
                                   :z (get-in subject [:metadata :zmag])}}
         extra-attributes (dissoc (:metadata subject) :sdss_id :counters :umag :rmag :imag :gmag :zmag)]
    (update-in new-subject [:attributes] merge extra-attributes)))

(defn import-subjects
  [subject fun & {:keys [database collection project]}]
  (mc/connect!)
  (mc/set-db! (mc/get-db database))
  (let [project (str/replace project "_" "-")
        subjects (map fun (m/find-maps collection))]
    (insert subject 
            (values subjects))))
