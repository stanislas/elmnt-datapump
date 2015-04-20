(ns ch.hood.odpg.impl.common
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

(defn render-add-file [directory file-type file-basename]
	(str "dbms_datapump.add_file("
			 "handle => handle, "
			 "filename => '" (str file-basename (file-type-suffix file-type)) "', "
			 "directory => '" directory "', "
			 "filetype => " (file-type-filetype file-type) ");"
			 ))

(defn render-header [datapump-operation {:keys [remote-link file-prefix directory]}]
	(str/join "\n"
						["declare"
						 "handle number;"
						 "job_state varchar2(50);"
						 "begin"
						 (str "handle := dbms_datapump.open('" (render-datapump-operation datapump-operation) "', 'SCHEMA'"
									(if (nil? remote-link) "" (str ", remote_link => " (single-quote remote-link))) ");")
						 (render-add-file directory :dump-file file-prefix)
						 (render-add-file directory (log-file-type datapump-operation) file-prefix)
						 ]))

(defn render-footer []
	(str/join "\n"
						["dbms_datapump.start_job(handle => handle);"
						 "dbms_datapump.wait_for_job(handle => handle, job_state => job_state);"
						 "dbms_output.put_line('Job finished with state ' || job_state);"
						 "end;"
						 "/"]))

(defn render-schema-metadatafilter [schemas]
	(let [schema-names (keys schemas)
				schema-names (map double-quote schema-names)
				schema-names (str/join ", " schema-names)]
		(str "dbms_datapump.metadata_filter("
				 "handle => handle, "
				 "name => 'SCHEMA_EXPR', "
				 "value => 'IN (" schema-names ")'"
				 ");")))
