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
    (fn [req] 
        (wrap-try 
            (log/debug "movie find-by-id" id t)
            (response (dao/find-by-id id t)))))

(defn create [] 
    (fn [{{movie :movie audit :audit} :body}] 
        (wrap-try 
            (log/debug "movie create" movie)
            (response {:id (dao/create movie audit)}))))

(defn updat [id] 
    (fn [{{movie :movie audit :audit} :body}] 
        (wrap-try 
            (log/debug "movie update id=" id "body=" movie)
            (response (dao/updat id movie audit)))))

(defn delete [id] 
    (fn [{{audit :audit} :body}] 
        (wrap-try
            (log/debug "movie delete" id)
            (response (dao/delete id audit)))))

(defn attribute-history [id root-attr leaf-attr] 
    (fn [req] 
        (wrap-try
            (log/debug "movie attribute history" id root-attr leaf-attr)
            (response {:history (dao/attribute-history id root-attr leaf-attr)}))))

(defn history [id] 
    (fn [req] 
        (wrap-try
            (log/debug "movie history" id)
            (response {:history (dao/history id)}))))
