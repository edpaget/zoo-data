(ns lobos.config
  (:use lobos.connectivity))

(def db 
  {:classname "org.postgresql.Driver"
   :subprotocol "postgresql"
   :user "edward"
   :password ""
   :subname "//localhost:5432/zoo-data"})

(open-global db)
