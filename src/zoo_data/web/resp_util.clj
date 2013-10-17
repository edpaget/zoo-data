(ns zoo-data.web.resp-util)

(defn resp
  [status body]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body body})

(defn resp-ok
  [body]
  (if body
    (resp 200 body)
    (resp 404 {"status" "Not Found"})))

(defn resp-created
  [body]
  (resp 201 body))

(defn resp-no-content*
  []
  (resp 204 ""))

(defmacro resp-no-content
  [& action]
  `(do 
     ~action
     (resp-no-content*)))
