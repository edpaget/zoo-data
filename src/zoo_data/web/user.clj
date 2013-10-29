(ns zoo-data.web.user
  (:use zoo-data.web.resp-util)
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [clojure.data.codec.base64 :as b64]
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
        {:keys [success id name talk api_key]} (send-login login-url username password)]
    (when success
      (if-let [user-record (u/select-by-id id)]
        (do (u/add-projects user-record (:roles talk)) 
            (u/select-by-id id))
        (u/create {:id id
                   :name name
                   :ouroboros_api_key api_key
                   :roles (:roles talk)})))))

(defn find-user
  [id api-key]
  (when-let [user (u/select-by-id id)]
    (when (= api-key (:api_key user))
      user)))

(defn wrap-user
  [handler]
  (fn [req]
    (-> (if-let [header (get-in req [:headers "authorization"])] 
          (let [auth (last (re-find #"^Basic (.+)$" header))
                [id api-key] (str/split (->> (b64/decode (.getBytes auth))
                                             (map char)
                                             (apply str)) #":")
                user (find-user id api-key)]
            (if user 
              (handler (update-in req [:params] assoc :user user))
              (resp-not-authorized)))
          (resp-not-authorized))
        (update-in [:headers] merge {"WWW-Authenticate" "Basic realm=\"Application\""}))))

(defn wrap-project-auth
  [handler]
  (fn [req]
    (let [project-id (get-in req [:params :project :id])
          user-allowed-project-ids (map :id (get-in req [:params :user :projects]))]
      (if (some #{project-id} user-allowed-project-ids)
        (handler req)
        (resp-forbidden)))))
