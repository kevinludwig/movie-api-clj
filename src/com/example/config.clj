(ns com.example.config 
    (:require [clj-config.core :as config]))

(defn get-key
    ([k] (config/get-key "./config/default.clj" k))
    ([k & more] (get-in (config/get-key "./config/default.clj" k) more)))
