(ns zoo-data.web.user
  (:use zoo-data.web.resp-util)
  (:require [clj-http.client :as http]
            [zoo-data.model.users :as u]))

(defn- send-login
  [login-url username password]
  (-> (http/post login-url {:form-params {:username username
                                          :password password}
                            :content-type :json
                            :as :json})
      :body))

(defn login
  [{:keys [zooniverse-api]} {:strs [username password]}]
  (let [login-url (str zooniverse-api "/login")
        {:keys [id name talk api_key]} (send-login login-url username password)]
    (if-let [user-record (first (u/select-by-id id))]
      user-record
      (u/create {:id id
                 :name name
                 :ouroboros_api_key api_key
                 :roles (:roles talk)}))))
