(ns trivia-cms.handler
  (:require [clojure.java.io]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]

            [monger.core :as mg]
            [monger.collection :as mc]

            [trivia-cms.db :refer :all])

  (:use [stencil.core] ; html template rendering
        [ring.util.response :only [response not-found]])) ; wrap json response

(defn read-template [filepath]
  (slurp (clojure.java.io/resource filepath)))

; filepath is relative to `resources` directory
(defn render-html [filepath view-args]
  (let [html (read-template filepath)]
    (render-string html view-args)))

(defn index 
  [view-args] 
  {:page-type :index 
   :filepath "views/index.html" 
   :view-args view-args})

(defn quiz 
  [view-args] 
  {:page-type :quiz  
   :filepath "views/quizzes.html" 
   :view-args view-args})

(defn load-page 
  [view]
  (render-html 
   (:filepath view) 
   (:view-args view)))

(defn save! [quiz]
  (mc/insert-and-return db-handle quizzes-collection-name quiz))

(defn get-quiz [name]
  (let [qs (mc/find-maps db-handle quizzes-collection-name {:name name})]
    (if (> (count qs) 0) 
      (update-in 
       (first qs)
       [:_id]
       (fn [qid]
         (.toString qid)))
      {:error-message (str "Quiz '" name "' not found.")})))

(defn delete-quiz [name]
  (let [res (mc/remove db-handle quizzes-collection-name {:name name})] 
    {:num-deleted (.getN res)}))

(defroutes app-routes
  (GET "/" [request] (load-page (index {})))
  (GET "/quizzes" [request] (load-page (quiz {})))
  (POST "/quizzes/create" req
        (let [params (:params req)
              question (get params :question)
              category (get params :category)
              answer (get params :answer)
              point-num (get params :points)
              quiz {:question question
                    :category category
                    :answer answer
                    :value (str point-num)}] 
          (save! quiz)))

  (DELETE "/quizzes/:name" [name]
          (response (delete-quiz name)))

  (GET "/quizzes/:name" [name]
       (let [quiz (get-quiz name)]
         (if (:error-message quiz)
           (not-found (:error-message quiz))
           (response quiz))))
  (route/resources "/resources")
  (route/not-found "404"))

(def app
  (->
   app-routes
   (wrap-defaults api-defaults)
   (wrap-json-response)))
