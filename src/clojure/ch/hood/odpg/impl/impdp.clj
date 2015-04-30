(ns ch.hood.odpg.impl.impdp
	(:require [schema.core :as s]
						[clojure.string :as str]
						[ch.hood.odpg.impl.common :as c]))

(def ImpData
	{:schemas                               {s/Str {(s/optional-key :remap-to) s/Str}}
	 :tablespaces-remap                     {s/Str s/Str}
	 :file-prefix                           s/Str
	 (s/optional-key :sqlplus?)             s/Bool
	 (s/optional-key :exclude-object-types) [s/Str]
	 (s/optional-key :include-object-types) [s/Str]
	 :directory                             s/Str})

(defn render-remap-type [remap-type]
	(case remap-type
		:remap-tablespace "REMAP_TABLESPACE"
		:remap-schema "REMAP_SCHEMA"))

(defn render-remap [remap-type [old new]]
	(str "dbms_datapump.metadata_remap("
			 "handle => handle, "
			 "name => '" (render-remap-type remap-type) "', "
			 "old_value => '" old "', "
			 "value => '" new "');"))

(defn render-schemas [schemas]
	(str (c/render-schema-metadatafilter schemas) "\n"
			 (str/join "\n"
								 (->> schemas
											(map #(vector (first %) (:remap-to (second %))))
											(filter (comp not nil? second))
											(map #(render-remap :remap-schema %))))))

(defn render-tablespaces-remap [tablespaces-remap]
	(str/join "\n"
						(map #(render-remap :remap-tablespace %) tablespaces-remap)))

(s/defn render-imp-script
	([imp-data :- ImpData]
		(str/join "\n"
							[(c/render-header :import imp-data)
							 (render-schemas (:schemas imp-data))
							 (render-tablespaces-remap (:tablespaces-remap imp-data))
							 (c/render-object-type-metadatafilter imp-data)
							 (c/render-footer (get imp-data :sqlplus? true))]))
	([file imp-data :- ImpData]
		(spit file (render-imp-script imp-data))))

(comment
	(def imp-data {:directory         "DATA_PUMP_DIR"
								 :file-prefix       "arims_2015_3_20"
								 :tablespaces-remap {"SIMON1_DATA" "STAN1_DATA"}
								 :schemas           {"SIMON1" {:remap-to "STAN1"}}})

	(println (render-imp-script imp-data))
	)
