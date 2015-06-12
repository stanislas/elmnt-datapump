(ns elmnt.datapump.common-test
  (:require
    [midje.sweet :refer :all]
    [elmnt.datapump.common :as c]))

(defn statement [test-fn statement]
  (str (-> test-fn meta :name) " " statement))

(facts "derive-file-name trustful"
       (c/derive-file-name "filename.log" nil nil) => "filename.log"
       (c/derive-file-name "filename.log" "filename" :dump-file) => "filename.log"
       (c/derive-file-name nil "filename" :dump-file) => "filename.dump")

(def error (throws AssertionError))

(facts "derive-file-name defensive"
       (c/derive-file-name nil nil :dump-file) => error
       (c/derive-file-name nil "filename" nil) => error
       (c/derive-file-name nil nil nil) => error)

(defn derive-file-names-trustful [test-fn]
  (facts (statement test-fn "trustful")
         (test-fn {:file-prefix "file"} :export)
         => (contains {:log-file "file_exp.log" :dump-file "file.dump"})
         (test-fn {:file-prefix "file" :log-file "logfile"} :export)
         => (contains {:log-file "logfile" :dump-file "file.dump"})
         (test-fn {:file-prefix "file" :dump-file "dumpfile"} :export)
         => (contains {:log-file "file_exp.log" :dump-file "dumpfile"})
         (test-fn {:file-prefix "file" :dump-file "dumpfile" :log-file "logfile"} :export)
         => (contains {:log-file "logfile" :dump-file "dumpfile"})))

(derive-file-names-trustful #'c/derive-file-names)
(derive-file-names-trustful #'c/normalize)

(defn derive-file-names-defensive [test-fn]
  (facts (statement test-fn "defensive")
         (test-fn {} :export) => error
         (test-fn {:log-file "logfile.log"} :export) => error
         (test-fn {:dump-file "dumpfile.dump"} :export) => error))

(derive-file-names-defensive #'c/derive-file-names)
(derive-file-names-defensive #'c/normalize)
