(ns com.example.controllers.movie
    (:require 
        [ring.util.response :refer [response]]
        [taoensso.timbre :as log]
        [com.example.dao.movie :as dao]))

(defmacro wrap-try [& body]
    `(try 
        ~@body
        (catch Exception e# 
            (log/error e#)
            (response {:error (.getMessage e#)}))))

(defn find-by-id [id t] 
    (wrap-try 
        (log/debug "movie find-by-id" id t)
        (response (dao/find-by-id id t))))

(defn create [body] 
    (wrap-try 
        (log/debug "movie create" body)
        (response {:id (dao/create body)})))

(defn updat [id body] 
    (wrap-try 
        (log/debug "movie update id=" id "body=" body)
        (response (dao/updat id body))))

(defn delete [id] 
    (wrap-try
        (log/debug "movie delete" id)
        (response {:status "ok"})))

(defn attribute-history [id attr] 
    (wrap-try
        (log/debug "movie attribute history" id attr)
        (response {:history (dao/attribute-history id attr)})))

(defn history [id] 
    (wrap-try
        (log/debug "movie history" id)
        (response {:history (dao/history id)})))
