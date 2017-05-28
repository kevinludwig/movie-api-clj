(ns com.example.movie-test
    (:use midje.sweet)
    (:require 
        [ring.mock.request :as mock]
        [com.example.app :refer [app]]))

(facts "POST /schema should create movie schema"
    (let [req (-> (mock/request :post "/schema") (mock/content-type "application/json"))]
        (:status (app req))) => 200) 
