(ns com.example.routes
    (:require 
        [compojure.core :refer [defroutes GET POST PUT DELETE]]
        [compojure.route :as route]
        [com.example.controllers.movie :as movie]
        [com.example.controllers.schema :as schema]
        [com.example.controllers.notfound :as notfound]))

(defroutes main-routes
    (POST "/schema" [] (schema/create))
    (GET "/movie/:id/asof/:t" [id t] (movie/find-by-id id t))
    (GET "/movie/:id" [id] (movie/find-by-id id nil))
    (POST "/movie" req (movie/create req))
    (PUT "/movie/:id" req (movie/updat req))
    (DELETE "/movie/:id" req (movie/delete req))
    (GET "/movie/:id/history" [id] (movie/history id))
    (GET "/movie/:id/history/:attr" [id attr] (movie/attribute-history id attr))
    (route/not-found (notfound/notfound)))
