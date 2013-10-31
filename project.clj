(defproject zoo-data "0.1.0-SNAPSHOT"
  :description "FIXME"
  :url "FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [com.benburkert/pghstore-clj "0.1.1"]
                 [korma "0.3.0-RC5"]
                 [clj-http "0.7.6"]
                 [clj-time "0.6.0"]
                 [drift "1.5.2"]
                 [crypto-random "1.1.0"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [com.taoensso/carmine "2.2.0"] 
                 [ring/ring-devel "1.2.0-RC1"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [org.clojure/java.jdbc "0.3.0-alpha5"]
                 [ring/ring-jetty-adapter "1.2.0-RC1"]
                 [org.clojure/data.codec "0.1.0"]
                 [paneer "0.2.0-SNAPSHOT"]]
  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [[ring-mock "0.1.5"]
                        [org.clojure/tools.namespace "0.2.3"]]}}
  :plugins [[drift "1.5.2"]]
  :min-lein-version "2.0.0") 
