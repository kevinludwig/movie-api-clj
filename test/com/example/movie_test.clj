(ns com.example.movie-test
    (:use clojure.test)
    (:require 
        [cheshire.core :as json]
        [ring.mock.request :as mock]
        [com.example.app :refer [app]]))

(deftest test-app
    (testing "POST /schema should create movie schema"
        (let [res (-> (mock/request :post "/schema") 
                      (mock/content-type "application/json")
                      app)]
            (is (= (:status res) 200))
            (is (= (get-in res [:headers "Content-Type"]) "application/json; charset=utf-8"))
            (is (> (-> (:body res) (json/parse-string true) :schema count) 0))))
    
    (testing "404 for not found route"
        (let [res (app (mock/request :get "/some/random/route"))]
            (is (= (:status res) 404)))))
