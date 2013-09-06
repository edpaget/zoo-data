(defproject zoo-data "0.1.0-SNAPSHOT"
  :description "FIXME"
  :url "FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [pghstore-clj "0.1.0"]
                 [korma "0.3.0-RC5"]
                 [lobos "1.0.0-beta1"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [com.taoensso/carmine "2.2.0"] 
                 [ring/ring-devel "1.2.0-RC1"]
                 [ring/ring-jetty-adapter "1.2.0-RC1"]]
  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [[ring-mock "0.1.5"]
                        [org.clojure/tools.namespace "0.2.3"]]}}
  :min-lein-version "2.0.0") 
