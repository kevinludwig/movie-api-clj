(ns com.example.controllers.movie-test
    (:use midje.sweet)
    (:require 
        [ring.mock.request :as mock]
        [com.example.controllers.movie :as movie]))

(facts "just testing midje setup"
    (map inc (range 5)) => [1 2 3 4 5]
    (map dec (range 5)) => [-1 0 1 2 3])
