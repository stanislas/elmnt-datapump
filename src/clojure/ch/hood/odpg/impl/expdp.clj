(ns ch.hood.odpg.impl.expdp
	(:require [ch.hood.odpg.impl.common :as c]
						[clojure.string :as str]
						[schema.core :as s]))

(def ExpData
	{:schemas                               {s/Str {(s/optional-key :include-tables)    #{s/Str}
																									(s/optional-key :exclude-tables)    #{s/Str}
																									(s/optional-key :subquery-filters)  {s/Str s/Str}
																									(s/optional-key :partition-filters) {s/Str s/Str}}}
	 :file-prefix                           s/Str
	 :directory                             s/Str
	 (s/optional-key :exclude-object-types) [s/Keyword]
	 (s/optional-key :include-object-types) [s/Keyword]
	 (s/optional-key :remote-link)          s/Str})

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
		:exclude-object-types "NOT IN"))

(defn render-metadata-filter
	([filter-type filters {:keys [object-type]}]
	 (let [filters (map c/double-quote filters)
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
	(let [metadata-schema-filter (c/render-schema-metadatafilter schemas)
				table-metadata-filter (render-table-metadatafilter schemas)
				subquery-filters (into {} (map #(vector (first %) (:subquery-filters (second %))) schemas))
				data-filters (map #(render-subqueries-datafilter (first %) (second %)) subquery-filters)]
		(str metadata-schema-filter "\n" table-metadata-filter "\n"
				 (str/join "\n" data-filters))))

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
							[(c/render-header :export exp-data)
							 (render-object-type-metadatafilter exp-data)
							 (render-schemas exp-data)
							 (c/render-footer)]))
	([file exp-data :- ExpData]
		(let [script (render-exp-script exp-data)]
			(spit file script))))

(comment
	(def input {:schemas              {"SIMON1"
																		 {:include-tables #{"CUSTOMER" "LC_CONFIGURATION"}
																			:exclude-tables #{"OP_BIN_ARCHIVE"}
																			:subquery-filters
																											{"CUSTOMER"         "WHERE ID = 3600233"
																											 "LC_CONFIGURATION" "WHERE CUSTOMER_ID = 3600233"}}}
							:exclude-object-types ["VIEW"]
							:file-prefix          "simon1"
							:directory            "DATA_PUMP_DIR"})


	(println (render-exp-script input))

	(render-subqueries-datafilter "PERGO" {
																				 "BOOKING_SETTER" "WHERE POINT_OF_SALE in ('2001', '200')"
																				 "BOOKING_DELTA"  "WHERE Pth = 231"
																				 })

	(render-schema-metadatafilter (:schemas input)))
