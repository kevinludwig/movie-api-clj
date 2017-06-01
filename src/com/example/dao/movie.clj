(ns com.example.dao.movie
    (:import (java.util.Date))
    (:require
        [taoensso.timbre :as log]
        [clojure.set :refer [map-invert]]
        [clojure.walk :refer [postwalk-replace]]
        [datomic.api :only [db q pull] :as d]
        [com.example.dao.db :as db]))

(def mappings 
    {:id :db/id
     :title :movie/title
     :synopsis :movie/synopsis
     :runtime :movie/runtime
     :release_year :movie/release-year
     :genres :movie/genres
     :cast :movie/cast
     :person_name :cast/name
     :person_role :cast/role
     :person_id :cast/id
     :rating :movie/rating
     :rating_value :rating/value
     :rating_advisories :rating/advisories
     :rating_source :rating/source})

(defn- make-movie [id body]
    (postwalk-replace mappings (assoc body :id id)))

(defn- transform [m]
    (postwalk-replace (map-invert mappings) m))

(defn- audit-log [audit]
    (let [tx-id (d/tempid :db.part/tx)]
        {:db/id tx-id
         :audit/user (:user audit)
         :audit/message (:message audit)}))

(defn create [body audit]
    (let [cxn (db/get-conn)
          id (d/tempid :db.part/user)
          datom (make-movie id body)
          tx-datom (audit-log audit)
          tx @(d/transact cxn [datom tx-datom])]
        (d/resolve-tempid (db/get-db) (:tempids tx) (:db/id datom))))

(defn find-by-id [id t]
    (let [as-of-db (if t (d/as-of (db/get-db) (Long/parseLong t)) (db/get-db))
          entity (d/pull as-of-db '[*] (Long/parseLong id))]
        (when entity (transform entity))))

(defn updat [id body audit] 
    (let [cxn (db/get-conn)
          datom (make-movie (Long/parseLong id) body)
          tx-datom (audit-log audit)
          tx @(d/transact cxn [datom tx-datom])]
        (find-by-id id nil)))

(defn delete [id audit] 
    (let [cxn (db/get-conn)
          retract [:db.fn/retractEntity (Long/parseLong id)]
          tx-datom (audit-log audit)
          datom @(d/transact cxn [retract tx-datom])]
        (log/debug "deleted" (:tx-data datom))))

(defn- to-obj [data keys]
    (map #(zipmap keys %) data))

(defn history [id]
    (let [hdb (d/history (db/get-db))
          tx-keys [:id :tid :ts :user :message]
          query '[:find ?id ?t ?ts ?user ?message
                  :in $ ?id
                  :where [?id _ _ ?t]
                         [?t :db/txInstant ?ts]
                         [?t :audit/user ?user]
                         [?t :audit/message ?message]]]
        (sort-by :tid (to-obj (d/q query hdb (Long/parseLong id)) tx-keys))))

(defn- resolve-attribute-history-query [root-attr leaf-attr]
    (let [root-query '[:find ?id ?attr ?value ?op ?t ?ts ?user ?message
                       :in $ ?id ?attr
                       :where [?id ?attr ?value ?t ?op]
                       [?t :db/txInstant ?ts]
                       [?t :audit/user ?user]
                       [?t :audit/message ?message]]
          leaf-query '[:find ?id ?attr ?value ?op ?t ?ts ?user ?message
                       :in $ ?id ?root ?attr
                       :where [?id ?root ?ref]
                       [?ref ?attr ?value ?t ?op]
                       [?t :db/txInstant ?ts]
                       [?t :audit/user ?user]
                       [?t :audit/message ?message]]]
        (if leaf-attr leaf-query root-query)))

(defn attribute-history [id root-attr-name leaf-attr-name]
    (let [hdb (d/history (db/get-db))
          tx-keys [:id :attr :value :added :tid :ts :user :message]
          root-attr (get mappings (keyword root-attr-name))
          leaf-attr (when leaf-attr-name (get mappings (keyword leaf-attr-name)))
          query (resolve-attribute-history-query root-attr leaf-attr)] 
        (sort-by :tid (to-obj (d/q query hdb (Long/parseLong id) root-attr leaf-attr) tx-keys))))
