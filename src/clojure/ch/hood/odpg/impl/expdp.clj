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

(defn render-schema-metadatafilter [schemas]
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
	([filter-type table-names]
	 (let [table-names (map double-quote table-names)
				 table-names (str/join ", " table-names)]
		 (str "dbms_datapump.metadata_filter("
					"handle => handle, "
					"name => 'NAME_EXPR', "
					"value => '" (filter-type-operator filter-type) " (" table-names ")', "
					"object_type => 'TABLE'"
					");")))
	([schemas]
		;; if we come here, we have the guarantee of a single schema mode. see validate.
	 (let [filter-types [:include-tables :exclude-tables]
				 tables ((apply juxt filter-types) (get schemas (-> schemas keys first)))
				 tables (map #(when (not (nil? %2)) (render-table-metadatafile %1 %2)) filter-types tables)
				 tables (filter (comp not nil?) tables)]
		 (str/join "\n" tables))))

(defn render-subquery-datafilter [schema-name table-name subquery]
	(str "dbms_datapump.data_filter("
			 "handle => handle, "
			 "name => 'SUBQUERY', "
			 "value => '" subquery "', "
			 "table_name => '" table-name "', "
			 "schema_name => '" schema-name "'"
			 ");"))

(defn render-subqueries-datafilter [schema-name tables]
	(->> tables
			 (map #(render-subquery-datafilter schema-name (first %) (second %)))
			 (str/join "\n")))

(defn render-schemas [{:keys [schemas]}]
	(let [metadata-schema-filter (render-schema-metadatafilter schemas)
				table-metadata-filter (render-table-metadatafile schemas)
				subquery-filters (into {} (map #(vector (first %) (:subquery-filters (second %))) schemas))
				data-filters (map #(render-subqueries-datafilter (first %) (second %)) subquery-filters)]
		(str metadata-schema-filter "\n" table-metadata-filter "\n"
				 (str/join "\n" data-filters))))

(defn render-today []
	(let [cal (Calendar/getInstance)]
		(str (.get cal Calendar/YEAR) "_"
				 (.get cal Calendar/MONTH) "_"
				 (.get cal Calendar/DAY_OF_MONTH))))

(defn render-header [{:keys [remote-link file-prefix directory]}]
	(let [file-basename (str file-prefix "_" (render-today))]
		(str/join "\n"
							["declare"
							 "handle number;"
							 "job_state varchar2(50);"
							 "begin"
							 (str "handle := dbms_datapump.open('EXPORT', 'SCHEMA'"
										(if (nil? remote-link) "" (str ", remote_link => " (single-quote remote-link))) ");")
							 (c/render-add-file directory :dump-file file-basename)
							 (c/render-add-file directory :exp-log-file file-basename)
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
		(str/join "\n"
							[(render-header exp-data)
							 (render-schemas exp-data)
							 (c/render-footer)]))
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

	(render-subqueries-datafilter "PERGO" {
																				 "BOOKING_SETTER" "WHERE POINT_OF_SALE in ('2001', '200')"
																				 "BOOKING_DELTA"  "WHERE Pth = 231"
																				 })

	(render-schema-metadatafilter (:schemas input)))
