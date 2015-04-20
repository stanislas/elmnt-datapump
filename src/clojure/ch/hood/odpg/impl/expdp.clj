(ns ch.hood.odpg.impl.expdp
	(:require [ch.hood.odpg.impl.common :as c]
						[clojure.string :as str]
						[schema.core :as s])
	(:import (java.util Calendar)))

(defn double-quote [string]
	(str "''" string "''"))

(defn single-quote [string]
	(str "'" string "'"))

(def ExpData
	{:schemas                      {s/Str {(s/optional-key :include-tables)    #{s/Str}
																				 (s/optional-key :exclude-tables)    #{s/Str}
																				 (s/optional-key :subquery-filters)  {s/Str s/Str}
																				 (s/optional-key :partition-filters) {s/Str s/Str}}}
	 :file-prefix                  s/Str
	 :directory                    s/Str
	 (s/optional-key :remote-link) s/Str})

(defn render-schema-metadatafilter [ctx schemas]
	(let [schema-names (keys schemas)
				schema-names (map double-quote schema-names)
				schema-names (str/join ", " schema-names)]
		(str "dbms_datapump.metadata_filter("
				 "handle => handle, "
				 "name => 'SCHEMA_EXPR', "
				 "value => 'IN (" schema-names ")'"
				 ");")))

(defn filter-type-operator [filter-type]
	(case filter-type
		:include-tables "IN"
		:exclude-tables "NOT IN"))

(defn render-table-metadatafile
	([ctx filter-type table-names]
	 (let [table-names (map double-quote table-names)
				 table-names (str/join ", " table-names)]
		 (str "dbms_datapump.metadata_filter("
					"handle => handle, "
					"name => 'NAME_EXPR', "
					"value => '" (filter-type-operator filter-type) " (" table-names ")', "
					"object_type => 'TABLE'"
					");")))
	([ctx schemas]
		;; if we come here, we have the guarantee of a single schema mode. see validate.
	 (let [filter-types [:include-tables :exclude-tables]
				 tables ((apply juxt filter-types) (get schemas (-> schemas keys first)))
				 tables (map #(when (not (nil? %2)) (render-table-metadatafile ctx %1 %2)) filter-types tables)
				 tables (filter (comp not nil?) tables)]
		 (str/join "\n" tables))))

(defn render-subquery-datafilter [ctx schema-name table-name subquery]
	(str "dbms_datapump.data_filter("
			 "handle => handle, "
			 "name => 'SUBQUERY', "
			 "value => '" subquery "', "
			 "table_name => '" table-name "', "
			 "schema_name => '" schema-name "'"
			 ");"))

(defn render-subqueries-datafilter [ctx schema-name tables]
	(->> tables
			 (map #(render-subquery-datafilter ctx schema-name (first %) (second %)))
			 (str/join "\n")))

(defn render-schemas [ctx {:keys [schemas]}]
	(let [metadata-schema-filter (render-schema-metadatafilter ctx schemas)
				table-metadata-filter (render-table-metadatafile ctx schemas)
				subquery-filters (into {} (map #(vector (first %) (:subquery-filters (second %))) schemas))
				data-filters (map #(render-subqueries-datafilter ctx (first %) (second %)) subquery-filters)]
		(str metadata-schema-filter "\n" table-metadata-filter "\n"
				 (str/join "\n" data-filters))))

(defn file-type-suffix [file-type]
	(case file-type
		:log-file ".log"
		:dump-file ".dump"
		))

(defn file-type-filetype [file-type]
	(case file-type
		:log-file "dbms_datapump.KU$_FILE_TYPE_LOG_FILE"
		:dump-file "dbms_datapump.KU$_FILE_TYPE_DUMP_FILE"))

(defn render-add-file [ctx directory file-type file-basename]
	(str "dbms_datapump.add_file("
			 "handle => handle, "
			 "filename => '" (str file-basename (file-type-suffix file-type)) "', "
			 "directory => '" directory "', "
			 "filetype => " (file-type-filetype file-type) ");"
			 ))

(defn render-today []
	(let [cal (Calendar/getInstance)]
		(str (.get cal Calendar/YEAR) "_"
				 (.get cal Calendar/MONTH) "_"
				 (.get cal Calendar/DAY_OF_MONTH))))

(defn render-header [ctx {:keys [remote-link file-prefix directory]}]
	(let [file-basename (str file-prefix "_" (render-today))]
		(str/join "\n"
							["declare"
							 "handle number;"
							 "job_state varchar2(50);"
							 "begin"
							 (str "handle := dbms_datapump.open('EXPORT', 'SCHEMA'"
										(if (nil? remote-link) "" (str ", remote_link => " (single-quote remote-link))) ");")
							 (render-add-file ctx directory :dump-file file-basename)
							 (render-add-file ctx directory :log-file (str file-basename "_exp"))
							 ])))

(defn validate [exp-data]
	(let [schemas (:schemas exp-data)
				schema-count (-> schemas keys count)
				table-filter-exists (some (comp #(or (contains? % :include-tables)
																						 (contains? % :exclude-tables)) second) schemas)]
		(if (and table-filter-exists (> schema-count 1))
			(throw (IllegalArgumentException. ":tables filter is allowed only on single schema exp-data.")))
		exp-data))

(s/defn render-exp-script
	([exp-data :- ExpData]
		(validate exp-data)
		(let [ctx nil]
			(str/join "\n"
								[(render-header ctx exp-data)
								 (render-schemas ctx exp-data)
								 (c/render-footer)])))
	([file exp-data :- ExpData]
		(let [script (render-exp-script exp-data)]
			(spit file script))))

(comment
	(def input {:schemas     {"SIMON1"
														{:include-tables #{"CUSTOMER" "LC_CONFIGURATION"}
														 :exclude-tables #{"OP_BIN_ARCHIVE"}
														 :subquery-filters
																						 {"CUSTOMER"         "WHERE ID = 3600233"
																							"LC_CONFIGURATION" "WHERE CUSTOMER_ID = 3600233"}}}
							:file-prefix "simon1"
							:directory   "DATA_PUMP_DIR"})


	(println (render-exp-script input))

	(render-subqueries-datafilter nil "PERGO" {
																						 "BOOKING_SETTER" "WHERE POINT_OF_SALE in ('2001', '200')"
																						 "BOOKING_DELTA"  "WHERE Pth = 231"
																						 })

	(render-schema-metadatafilter nil (:schemas input)))
