(ns migrate-helpers.helper
  (:require [clojure.java.jdbc :as j]
            [clojure.string :as str]))

(defn- url-to-connection-map
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

(defn postgres-conn
  []
  (url-to-connection-map (get (System/getenv) "DATABASE_URL")))

(defmacro with-conn* [& body] `(j/with-connection (postgres-conn) ~@body))
