(ns zoo-data.model.database
  (:use korma.db
        korma.core)
  (:require [taoensso.carmine :as car]))

;; Setup Database
(def redis-connection nil)
(def pg-connection nil)

(defmacro wcar* [& body] `(car/wcar redis-connection ~@body))

(defn connection
  []
  @(:pool pg-connection))

(defmacro cache-query 
  [id expiry & body]
  `(if-let [cache-check (wcar* (car/expire ~id ~expiry)
                               (car/get ~id))]
     cache-check
     (let [result ~@body]
       (wcar* (car/set ~id result)
              (car/expire ~id ~expiry))
       result)))

(defn create!
  [post-conn red-conn]
  (alter-var-root #'redis-connection (constantly red-conn)) 
  (alter-var-root #'pg-connection (constantly (defdb pg (postgres post-conn)))))

;; A couple nice functions to have around 
(defn select-by-id
  [ent id]
  (select ent
          (where {:id id})))

(defn update-by-id
  [ent id record]
  (update ent
          (set-fields record)
          (where {:id id})))

(defn delete-by-id
  [ent id]
  (delete ent
          (where {:id id})))

(defn insert-record
  [ent record]
  (insert ent
          (values record)))
