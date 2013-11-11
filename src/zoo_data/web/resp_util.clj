(ns zoo-data.web.resp-util)

(defn resp
  [status body]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body body})

(defn resp-not-found
  []
  (resp 404 {"status" "Not Found"}))
 
(defn resp-ok
  [body]
  (if body
    (resp 200 body)
    (resp-not-found)))

(defn resp-created
  [body]
  (resp 201 body))

(defn resp-no-content*
  []
  (resp 204 {}))

(defn resp-not-authorized
  []
  (resp 401 {"status" "Not Authorized"}))

(defn resp-forbidden
  []
  (resp 403 {"status" "Forbidden"}))

(defmacro resp-no-content
  [& action]
  `(do 
     ~action
     (resp-no-content*)))
