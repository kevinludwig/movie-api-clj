(ns com.example.dao.schema 
    (:require 
        [datomic.api :as d]
        [taoensso.timbre :as log]
        [com.example.dao.db :as db]))

(defn create []
    (let [conn (db/get-conn)
          schema-tx (read-string (slurp "src/com/example/dao/schema.edn"))]
        (log/debug (str "adding db schema " schema-tx))
        @(d/transact conn schema-tx)
        schema-tx))
