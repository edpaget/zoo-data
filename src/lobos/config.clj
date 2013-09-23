(ns lobos.config
  (:use lobos.connectivity))

(def db 
  {:classname "org.postgresql.Driver"
   :subprotocol "postgresql"
   :user "edward"
   :password ""
   :subname "//localhost:5432/zoo-data"})

(defn connect-lobos
  []
  (open-global (or (get (System/getenv) "HEROKU_POSTGRESQL_BLACK_URL") db)))
