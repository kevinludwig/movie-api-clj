(ns com.example.dao.movie
    (:import (java.util.Date))
    (:require
        [taoensso.timbre :as log]
        [clojure.set :refer [rename-keys map-invert]]
        [datomic.api :only [db q pull] :as d]
        [com.example.dao.db :as db]))

(def mappings 
    {:id :db/id
     :title :movie/title
     :release_year :movie/release-year
     :genre :movie/genre})

(defn- make-movie [id body]
    (rename-keys (assoc body :id id) mappings))

(defn- transform [m]
    (rename-keys m (map-invert mappings)))

(defn create [body]
    (let [cxn (db/get-conn)
          id (d/tempid :db.part/user)
          datom (make-movie id body)
          tx @(d/transact cxn [datom])]
        (d/resolve-tempid (db/get-db) (:tempids tx) (:db/id datom))))

(defn find-by-id [id t]
    (let [as-of-db (if t (d/as-of (db/get-db) t) (db/get-db))
          entity (d/pull as-of-db '[*] (Long/parseLong id))]
        (when entity (transform entity))))

(defn update [id body] 
    (let [cxn (db/get-conn)
          tx-data (make-movie (Long/parseLong id) body)
          tx @(d/transact cxn [tx-data])]
        (find-by-id id nil)))

(defn delete [id] 
    (let [cxn (db/get-conn)
          retract [[:db.fn/retractEntity (Long/parseLong id)]]
          datom @(d/transact cxn retract)]
        (log/debug "deleted" (:tx-data datom))))
