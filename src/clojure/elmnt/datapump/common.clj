(ns elmnt.datapump.common
  (:require [clojure.string :as str]))

(defn double-quote [string]
  (str "''" string "''"))

(defn single-quote [string]
  (str "'" string "'"))

(defn render-datapump-operation [datapump-operation]
  (case datapump-operation
    :export "EXPORT"
    :import "IMPORT"))

(defn log-file-type [datapump-operation]
  (case datapump-operation
    :export :exp-log-file
    :import :imp-log-file))

(defn file-type-suffix [file-type]
  (case file-type
    :exp-log-file "_exp.log"
    :imp-log-file "_imp.log"
    :dump-file ".dump"
    ))

(defn file-type-filetype [file-type]
  (case file-type
    :exp-log-file "dbms_datapump.KU$_FILE_TYPE_LOG_FILE"
    :imp-log-file "dbms_datapump.KU$_FILE_TYPE_LOG_FILE"
    :dump-file "dbms_datapump.KU$_FILE_TYPE_DUMP_FILE"))

(defn derive-file-name
  "The function `derive-file-name` computes the definitive file name for given file type. The file name is
  either already defined or it is computed from a prefix and a suffix based on its type. If neither file
  name nor the pair file prefix and file type are defined, an error is thrown."
  [file-name file-prefix file-type]
  {:pre [(not (and (nil? file-name) (or (nil? file-prefix) (nil? file-type))))]}
  (if (nil? file-name)
    (str file-prefix (file-type-suffix file-type))
    file-name))

(defn derive-file-names
  "The function `derive-file-names` derive the dump file name and the log file name for the given operation."
  [{:keys [dump-file log-file file-prefix] :as config} operation]
  (assoc config :dump-file (derive-file-name dump-file file-prefix :dump-file)
                :log-file (derive-file-name log-file file-prefix (log-file-type operation))))

(defn normalize
  "The function `normalize` takes a configuration map and a schema and will apply normalizing functions
  to derive obligatory values from optional values when applicable.

  The triadic version will call the supplied function to the normalize configuration map."
  ([data operation]
    (derive-file-names data operation))
  ([data operation f]
    (let [data (normalize data operation)]
      (f data))))

(defn render-add-file [directory file-type file-name reuse-dump-file]
  (str "dbms_datapump.add_file("
       "handle => handle, "
       "filename => '" file-name "', "
       "directory => '" directory "', "
       "filetype => " (file-type-filetype file-type)
       (when (= file-type :dump-file) (str ", reusefile => " (if reuse-dump-file 1 0)))
       ");"
       ))

(defn render-header [datapump-operation {:keys [remote-link log-file dump-file directory reuse-dump-file]}]
  (str/join "\n"
            ["declare"
             "handle number;"
             "job_state varchar2(50);"
             "begin"
             (str "handle := dbms_datapump.open('" (render-datapump-operation datapump-operation) "', 'SCHEMA'"
                  (if (nil? remote-link) "" (str ", remote_link => " (single-quote remote-link))) ");")
             (render-add-file directory :dump-file dump-file reuse-dump-file)
             (render-add-file directory (log-file-type datapump-operation) log-file reuse-dump-file)
             ]))

(defn render-footer [sqlplus?]
  (str/join "\n"
            ["dbms_datapump.start_job(handle => handle);"
             "dbms_datapump.wait_for_job(handle => handle, job_state => job_state);"
             "dbms_output.put_line('Job finished with state ' || job_state);"
             (str "end;\n" (when sqlplus? "/\n"))
             ]))

(defn render-schema-metadatafilter [schemas]
  (let [schema-names (keys schemas)
        schema-names (map double-quote schema-names)
        schema-names (str/join ", " schema-names)]
    (str "dbms_datapump.metadata_filter("
         "handle => handle, "
         "name => 'SCHEMA_EXPR', "
         "value => 'IN (" schema-names ")'"
         ");")))

(defn filter-type-name [filter-type]
  (case filter-type
    :include-tables "NAME_EXPR"
    :exclude-tables "NAME_EXPR"
    :include-object-types "INCLUDE_PATH_EXPR"
    :exclude-object-types "EXCLUDE_PATH_EXPR"))

(defn filter-type-operator [filter-type]
  (case filter-type
    :include-tables "IN"
    :exclude-tables "NOT IN"
    :include-object-types "IN"
    :exclude-object-types "IN"))

(defn render-metadata-filter
  ([filter-type filters {:keys [object-type]}]
   (let [filters (map double-quote filters)
         filters (str/join ", " filters)
         filters (str (filter-type-operator filter-type) "(" filters ")")]
     (str "dbms_datapump.metadata_filter("
          "handle => handle, "
          "name => '" (filter-type-name filter-type) "', "
          "value => '" filters "'"
          (when (not (nil? object-type)) (str ", object_type => '" object-type "'"))
          ");")))
  ([data {:keys [filter-types] :as opt}]
   (let [filters ((apply juxt filter-types) data)
         filters (map #(when (not (nil? %2)) (render-metadata-filter %1 %2 opt)) filter-types filters)
         filters (filter (comp not nil?) filters)]
     (str/join "\n" filters))))

(defn render-table-metadatafilter
  [schemas]
  ;; if we come here, we have the guarantee of a single schema mode. see validate.
  (render-metadata-filter (get schemas (-> schemas keys first)) {:filter-types [:include-tables :exclude-tables]
                                                                 :object-type  "TABLES"}))
(defn render-object-type-metadatafilter
  [data]
  (render-metadata-filter data {:filter-types [:include-object-types :exclude-object-types]}))

