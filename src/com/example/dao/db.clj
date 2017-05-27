(ns com.example.dao.db
    (:require [datomic.api :as d]
        [com.example.config :as config]))

(declare conn)

(defn create-db []
    (let [uri (config/get-key :db :uri)]
        (d/create-database uri)))

(defn get-conn []
    (let [uri (config/get-key :db :uri)]
        (defonce conn (d/connect uri))
        conn))

(defn get-db []
    (let [conn (get-conn)]
        (d/db conn)))
