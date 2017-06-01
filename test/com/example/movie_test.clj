(ns com.example.movie-test
    (:use clojure.test)
    (:require 
        [cheshire.core :as json]
        [ring.mock.request :as mock]
        [com.example.app :refer [app]]))

(def gravity-id (atom nil))
(def rating-id (atom nil))

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
                     :rating {:rating_value "R" :rating_source "MPAA"} :synopsis "first synopsis" :runtime 90}
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

    (testing "GET /movie/:id should get the movie record"
        (let [res (app (mock/request :get (str "/movie/" @gravity-id)))
              body (-> (:body res) (json/parse-string true))]
            (swap! rating-id #(get-in %2 [:rating :id]) body)
            (is (= (:status res) 200))
            (is (not (contains? body :error)))
            (is (= (:title body) "Gravity"))
            (is (number? (get-in body [:rating :id])))))

    (testing "PUT /movie/:id should update the movie record"
        (let [movie {:movie {:rating {:id @rating-id :rating_value "PG-13"} :synopsis "updated synopsis" 
                     :cast [{:person_name "Sandra Bullock" :person_role "Actor"}]}
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
              (is (= (get-in body [:rating :rating_source]) "MPAA"))
              (is (= (get-in body [:rating :rating_value]) "PG-13"))))
    
    (testing "GET /movie/:id/history should return entity level history records"
        (let [res (app (mock/request :get (str "/movie/" @gravity-id "/history")))
              body (-> (:body res) (json/parse-string true))]
            (is (= (:status res) 200))
            (is (not (contains? body :error)))
            (is (= (map :message (:history body)) ["test create", "add cast, fix genres"]))))

    (testing "GET /movie/:id/history/:attr should return attribute level history records"
        (let [res (app (mock/request :get (str "/movie/" @gravity-id "/history/synopsis")))
              body (-> (:body res) (json/parse-string true))]
            (is (= (:status res) 200))
            (is (not (contains? body :error)))
            (is (= (count (:history body)) 3))))

    (testing "GET /movie/:id/history/:root-attr/:leaf-attr should return nested attribute level history records"
        (let [res (app (mock/request :get (str "/movie/" @gravity-id "/history/rating/rating_value")))
              body (-> (:body res) (json/parse-string true))]
            (is (= (:status res) 200))
            (is (not (contains? body :error)))
            (is (= (count (:history body)) 3))))

    (testing "DELETE /movie/:id should remove the entity"
        (let [audit {:audit {:user "kevin" :message "done testing"}}
              res (-> (mock/request :delete (str "/movie/" @gravity-id))
                      (mock/content-type "application/json")
                      (mock/body (json/generate-string audit))
                      app)
              body (-> (:body res) (json/parse-string true))]
            (is (= (:status res) 200))
            (is (not (contains? body :error)))))
    
    (testing "GET /movie/:id/asof/:t should return movie at point in time"
        (let [h-res (app (mock/request :get (str "/movie/" @gravity-id "/history")))
              h-body (-> (:body h-res) (json/parse-string true))
              tid (first (map :tid (:history h-body)))
              res (app (mock/request :get (str "/movie/" @gravity-id "/asof/" tid)))
              body (-> (:body res) (json/parse-string true))]
            (is (= (:status res) 200))
            (is (not (contains? body :error)))
            (is (= (:title body) "Gravity"))))

    (testing "404 for not found route"
        (let [res (app (mock/request :get "/some/random/route"))]
            (is (= (:status res) 404)))))
