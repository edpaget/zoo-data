(ns zoo-data.web.user
  (:use zoo-data.web.resp-util)
  (:require [clj-http.client :as http]
            [zoo-data.model.users :as u]))

(defn- send-login
  [login-url username password]
  (-> (http/post login-url {:form-params {:username username
                                          :password password}
                            :content-type :json})
      :body))

(defn login
  [{:keys [zooniverse-api]} {:keys [username password]}]
  (let [login-url (str zooniverse-api "/login")
        {:keys [id name projects api_key]} (send-login login-url username password)
        user-record (u/create {:id id
                               :name name
                               :ouroboros_api_key api_key})]
    (u/add-projects user-record projects)))
