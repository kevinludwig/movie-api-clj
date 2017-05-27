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

(defn- audit-log [user msg]
    (let [tx-id (d/tempid :db.part/tx)]
        {:db/id tx-id
         :audit/user user
         :audit/message msg}))

(defn create [body]
    (let [cxn (db/get-conn)
          id (d/tempid :db.part/user)
          datom (make-movie id body)
          tx-datom (audit-log "kevin" "create")
          tx @(d/transact cxn [datom tx-datom])]
        (d/resolve-tempid (db/get-db) (:tempids tx) (:db/id datom))))

(defn find-by-id [id t]
    (let [as-of-db (if t (d/as-of (db/get-db) t) (db/get-db))
          entity (d/pull as-of-db '[*] (Long/parseLong id))]
        (when entity (transform entity))))

(defn updat [id body] 
    (let [cxn (db/get-conn)
          datom (make-movie (Long/parseLong id) body)
          tx-datom (audit-log "kevin" "update")
          tx @(d/transact cxn [datom tx-datom])]
        (find-by-id id nil)))

(defn delete [id] 
    (let [cxn (db/get-conn)
          retract [:db.fn/retractEntity (Long/parseLong id)]
          tx-datom (audit-log "kevin" "delete")
          datom @(d/transact cxn [retract tx-datom])]
        (log/debug "deleted" (:tx-data datom))))

(defn history [id]
    (let [hdb (d/history (db/get-db))
          query '[:find ?id ?t ?user ?message
                  :in $ ?id
                  :where [?id _ _ ?t]
                         [?t :audit/user ?user]
                         [?t :audit/message ?message]]]
        (d/q query hdb (Long/parseLong id))))

(defn attribute-history [id attr-name]
    (let [hdb (d/history (db/get-db))
          attr (keyword "movie" attr-name)
          query '[:find ?value ?op ?t ?user ?message 
                  :in $ ?id ?attr
                  :where [?id ?attr ?value ?t ?op]
                         [?t :audit/user ?user]
                         [?t :audit/message ?message]]]
        (d/q query hdb (Long/parseLong id) attr)))
