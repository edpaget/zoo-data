(ns lobos.config
  (:use lobos.connectivity)
  (:require [clojure.string :as str]))

(def db 
  {:classname "org.postgresql.Driver"
   :subprotocol "postgresql"
   :user "edward"
   :password "blah"
   :subname "//localhost:5432/zoo-data"})

(defn url-to-connection-map
  [url]
  (when url
    (let [subname (str "//" (get (str/split url #"@") 1))
          url (java.net.URI. url)
          [username password] (str/split (.getUserInfo url) #":")]
      {:classname "org.postgresql.Driver"
       :subprotocol "postgresql"
       :user username
       :password password
       :subname subname})))

(defn connect-lobos
  []
  (open-global (or (url-to-connection-map (get (System/getenv) "DATABASE_URL")) db)))
