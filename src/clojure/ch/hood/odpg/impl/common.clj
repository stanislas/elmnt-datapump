(ns ch.hood.odpg.impl.common
	(:require [clojure.string :as str]))

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

(defn render-footer []
	(str/join "\n"
						["dbms_datapump.start_job(handle => handle);"
						 "dbms_datapump.wait_for_job(handle => handle, job_state => job_state);"
						 "dbms_output.put_line('Job finished with state ' || job_state);"
						 "end;"
						 "/"]))
