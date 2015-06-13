(defproject
  li.elmnt/datapump "0.7.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [prismatic/schema "0.4.0"]]
  :profiles {:dev {:dependencies [[junit/junit "4.11"]
                                  [midje "1.6.3"]]}}
  :plugins [[lein-junit "1.1.8"]
            [lein-localrepo "0.5.3"]
            [lein-midje "3.1.3"]
            [lein-marginalia "0.8.0"]]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :test-paths ["test/clojure"]
  :junit ["test/java"])
