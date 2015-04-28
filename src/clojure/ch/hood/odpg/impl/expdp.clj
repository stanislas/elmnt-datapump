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
	 (s/optional-key :exclude-object-types) [s/Str]
	 (s/optional-key :include-object-types) [s/Str]
	 (s/optional-key :remote-link)          s/Str})

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
				table-metadata-filter (c/render-table-metadatafilter schemas)
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
							 (c/render-object-type-metadatafilter exp-data)
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
