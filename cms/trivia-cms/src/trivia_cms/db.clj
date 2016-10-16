(ns trivia-cms.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [environ.core :refer [env]])
  (:import [com.mongodb MongoOptions ServerAddress]))

(def conn 
  (mg/connect 
   {:host 
    (get (System/getenv) "MONGO_TRIVIA_BOT_HOST") 
    :port 
    (Integer. (get (System/getenv) "MONGO_TRIVIA_BOT_PORT"))}))

(def database (env :database-name))
(def db-handle (mg/get-db conn database))
(def quizzes-collection-name "quizzes")
