(defproject com.example/clj-template "1.0.0-SNAPSHOT"
    :description "basic clj project"
    :plugins [[lein-ring "0.7.3"]]
    :ring {:handler com.example.app/app}
    :main com.example.app
    :dependencies [
        [org.clojure/clojure "1.8.0"]
        [com.datomic/datomic-pro "0.9.5561"]
        [compojure "1.5.0"]
        [ring/ring-jetty-adapter "1.4.0"]
        [ring/ring-json "0.4.0"]
        [ring/ring-defaults "0.2.0"]
        [cheshire "5.6.1"]
        [clj-http "2.2.0"]
        [com.taoensso/timbre "4.3.1"]
        [clj-config "0.2.0"]
    ]
    :profiles {
        :uberjar {:aot :all}
        :dev {:plugins [
            [lein-ancient "0.6.3"]
            [lein-kibit "0.1.2"]
            [lein-midje "3.2"]
            [lein-cloverage "1.0.6"]
            [lein-cljfmt "0.5.3"]
            [jonase/eastwood "0.2.3"]]
            :dependencies [
                [midje "1.8.3"]
                [ring/ring-mock "0.3.0"]
            ]}})

