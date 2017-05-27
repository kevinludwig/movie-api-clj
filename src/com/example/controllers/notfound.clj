(ns com.example.controllers.notfound
    (:require [ring.util.response :refer [response]]))

(defn notfound []
    (response {:errors [{:message "page not found" :code 404}]}))
