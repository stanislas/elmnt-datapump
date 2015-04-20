(ns ch.hood.odpg.impl.common
	(:require [clojure.string :as str]))

(defn render-footer []
	(str/join "\n"
						["dbms_datapump.start_job(handle => handle);"
						 "dbms_datapump.wait_for_job(handle => handle, job_state => job_state);"
						 "dbms_output.put_line('Job finished with state ' || job_state);"
						 "end;"
						 "/"]))
