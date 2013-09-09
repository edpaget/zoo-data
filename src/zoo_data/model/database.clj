(ns zoo-data.model.database
  (:use korma.db)
  (:require [taoensso.carmine :as car]))

(def redis-connection nil)

(defmacro wcar* [& body] `(car/wcar redis-connection ~@body))

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
  (defdb pg (postgres post-conn)))
