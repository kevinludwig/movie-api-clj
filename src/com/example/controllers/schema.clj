(ns com.example.controllers.schema
    (:require 
        [ring.util.response :refer [response]]
        [taoensso.timbre :as log]
        [com.example.dao.db :as db]
        [com.example.dao.schema :as schema]))

(defn create []
    (try 
        (log/debug "schema create")
        (when (db/create-db)
            (response (schema/create)))
        (catch Exception e
            (log/error e)
            (response {:error (.getMessage e)}))))
