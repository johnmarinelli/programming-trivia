(ns trivia-cms.handler-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [ring.mock.request :as mock]
            [trivia-cms.handler :refer :all]
            [trivia-cms.db :refer :all]
            [monger.core :as mg]
            [monger.collection :as mc]))

(def test-question-1 
{ :questions [{:question "test question 1"
              :category "test category 1"
              :answer "test answer 1"
              :value 1}]
 :name "test_quiz_1"})

(def test-question-2 
{ :questions [{:question "test question 2"
              :category "test category 2"
              :answer "test answer 2"
              :value 2}]
 :name "test_quiz_2"})

(defn init-db []
  (println "Seeding test database...")
  (mc/insert db-handle quizzes-collection-name test-question-1)
  (mc/insert db-handle quizzes-collection-name test-question-2))

(defn teardown-db []
  (println "Removing all records from test database...")
  (mc/remove db-handle quizzes-collection-name))

(defn trivia-fixture [f]
  (init-db)
  (f)
  (teardown-db))

(use-fixtures :once trivia-fixture)

(deftest test-app
  (testing "homepage"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))))

  (testing "show quizzes"
    (let [response (app (mock/request :get "/quizzes"))]
      (is (= (:status response) 200))))

  (testing "create quiz api"
    (let [response (app (mock/request :post "/quizzes/create"))]
      (is (= (:status response) 200))))

  (testing "get quiz api - invalid quiz"
    (let [response (app (mock/request :get "/quizzes/invalid"))]
      (is (= (:status response) 404))
      (is (= (:body response) "Quiz 'invalid' not found."))))

  (testing "get quiz api - valid quiz"
    (let [response (app (mock/request :get (str "/quizzes/" (:name test-question-2))))]
      (is (= (:status response) 200))
      (is (= (dissoc (json/read-str (:body response) :key-fn keyword) :_id) 
             test-question-2))))

  (testing "delete quiz api - delete a quiz"
    (let [response (app (mock/request :delete (str "/quizzes/" (:name test-question-1))))]
      (is (= (:status response) 200))
      (is (= (json/read-str (:body response):key-fn keyword) {:num-deleted 1}))))

  (testing "delete quiz api - delete an invalid quiz"
    (let [response (app (mock/request :delete "/quizzes/invalid"))]
      (is (= (:status response) 200))
      (is (= (json/read-str (:body response) :key-fn keyword) {:num-deleted 0}))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
