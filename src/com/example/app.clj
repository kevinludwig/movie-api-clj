(ns com.example.app
    (:require 
        [ring.adapter.jetty :refer [run-jetty]]
        [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
        [com.example.config :as config]
        [com.example.routes :as routes]
        [taoensso.timbre :as log]
        [ring.middleware.json :as middleware])
    (:gen-class))

(def app 
    (-> routes/main-routes
        (middleware/wrap-json-body {:keywords? true :bigdecimals? true})
        (middleware/wrap-json-response)
        (wrap-defaults api-defaults)))

(defn start-server []
    (let [opts {:port (config/get-key :port)}]
        (run-jetty app opts)))

(defn -main [& args] (start-server))
