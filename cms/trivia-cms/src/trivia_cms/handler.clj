(ns trivia-cms.handler
  (:require [clojure.java.io]

            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])

  (:use stencil.core))

(defn read-template [filepath]
  (slurp (clojure.java.io/resource filepath)))

; filepath is relative to `resources` directory
(defn render-html [filepath view-args]
  (let [html (read-template filepath)]
    (render-string html view-args)))

(defmulti load-page :page-type)

(defn index [view-args] {:page-type :index :filepath "views/index.html" :view-args view-args})
(defn quiz [view-args] {:page-type :quiz  :filepath "views/quiz.html" :view-args view-args})

(defmethod load-page :index [view]
  (render-html (:filepath view) (:view-args view)))

(defmethod load-page :quiz [view]
  (render-html (:filepath view) (:view-args view)))

(defroutes app-routes
  (GET "/" [] (load-page (index {})))
  (GET "/quizzes" (load-page (quiz {})))
  (route/resources "/resources")
  (route/not-found "404"))

(def app
  (wrap-defaults app-routes site-defaults))
