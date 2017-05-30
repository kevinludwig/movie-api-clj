(ns com.example.movie-test
    (:use clojure.test)
    (:require 
        [cheshire.core :as json]
        [ring.mock.request :as mock]
        [com.example.app :refer [app]]))

(def gravity-id (atom nil))

(deftest test-app
    (testing "POST /schema should create movie schema"
        (let [res (-> (mock/request :post "/schema") 
                      (mock/content-type "application/json")
                      app)]
            (is (= (:status res) 200))
            (is (= (get-in res [:headers "Content-Type"]) "application/json; charset=utf-8"))
            (is (> (-> (:body res) (json/parse-string true) :schema count) 0))))
    
    (testing "POST /movie should create a movie record"
        (let [movie {:movie {:title "Gravity" :release_year 2013 :genres ["Drama" "Thriller"] 
                     :rating {:rating_value "R" :rating_source "MPAA"} :runtime 90}
                     :audit {:user "kevin" :message "test create"}}
              res (-> (mock/request :post "/movie")
                      (mock/content-type "application/json")
                      (mock/body (json/generate-string movie))
                      app)
              body (-> (:body res) (json/parse-string true))]
            (swap! gravity-id #(:id %2) body)
            (is (= (:status res) 200))
            (is (not (contains? body :error)))
            (is (= (get-in res [:headers "Content-Type"]) "application/json; charset=utf-8"))
            (is (number? @gravity-id))))

    (testing "PUT /movie/:id should update the movie record"
        (let [movie {:movie {:rating {:rating_value "PG-13"} :cast [{:person_name "Sandra Bullock" :person_role "Actor"}]}
                     :audit {:user "kevin" :message "add cast, fix genres"}}
              res (-> (mock/request :put (str "/movie/" @gravity-id))
                      (mock/content-type "application/json")
                      (mock/body (json/generate-string movie))
                      app)
              body (-> (:body res) (json/parse-string true))]
              (is (= (:status res) 200))
              (is (not (contains? body :error)))
              (is (= (get-in res [:headers "Content-Type"]) "application/json; charset=utf-8"))
              (is (= (:title body) "Gravity"))
              (is (= (get-in body [:cast 0 :person_name]) "Sandra Bullock"))
              (is (= (get-in body [:rating :rating_value]) "PG-13"))))

    (testing "GET /movie/:id should get the movie record"
        (let [res (app (mock/request :get (str "/movie/" @gravity-id)))
              body (-> (:body res) (json/parse-string true))]
            (is (= (:status res) 200))
            (is (not (contains? body :error)))
            (is (= (:title body) "Gravity"))))

    (testing "404 for not found route"
        (let [res (app (mock/request :get "/some/random/route"))]
            (is (= (:status res) 404)))))
