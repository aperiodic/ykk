(defproject ykk "1.1.0-SNAPSHOT"
  :description "Some utilities for clojure zippers."
  :url "https://github.com/aperiodic/ykk"
  :license {:name "GNU Lesser General Public License"
            :url "http://www.gnu.org/licenses/lgpl.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :profiles {:dev {:dependencies [[expectations "1.4.4"]]}}
  :plugins [[org.clojars.aperiodic/lein-expectations "0.0.5"]])
