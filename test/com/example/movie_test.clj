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

    (testing "404 for not found route"
        (let [res (app (mock/request :get "/some/random/route"))]
            (is (= (:status res) 404)))))
