(ns elmnt.datapump.expdp
  (:require [elmnt.datapump.common :as c]
            [clojure.string :as str]
            [schema.core :as s]))

(def ExpData
  {:schemas                               {s/Str {(s/optional-key :include-tables)         #{s/Str}
                                                  (s/optional-key :exclude-tables)         #{s/Str}
                                                  (s/optional-key :subquery-filters)       {s/Str s/Str}
                                                  (s/optional-key :partition-list-filters) {s/Str [s/Str]}}}
   :directory                             s/Str
   :dump-file                             s/Str
   :log-file                              s/Str
   (s/optional-key :file-prefix)        s/Str
   (s/optional-key :reuse-dump-file)      s/Bool
   (s/optional-key :sqlplus?)             s/Bool
   (s/optional-key :exclude-object-types) [s/Str]
   (s/optional-key :include-object-types) [s/Str]
   (s/optional-key :remote-link)          s/Str
   (s/optional-key :custom)               [s/Str]})


(defn datafilter-name [datafilter-type]
  (case datafilter-type
    :subquery-filters "SUBQUERY"
    :partition-list-filters "PARTITION_LIST"))

(defn render-datafilter [schema-name table-name datafilter-type value]
  (str "dbms_datapump.data_filter("
       "handle => handle, "
       "name => '" (datafilter-name datafilter-type) "', "
       "value => '" value "', "
       "table_name => '" table-name "', "
       "schema_name => '" schema-name "'"
       ");"))

(defn render-datafilters [schema-name datafilter-type tables]
  (->> tables
       (map #(render-datafilter schema-name (first %) datafilter-type (second %)))
       (str/join "\n")))

(defn render-schema-datafilters [datafilter-type schemas f]
  (let [schemas (into [] (map #(vector (first %) (f (datafilter-type (second %)))) schemas))
        filters (map #(render-datafilters (first %) datafilter-type (second %)) schemas)]
    (str/join "\n" filters)))

(defn render-schemas [{:keys [schemas]}]
  (let [metadata-schema-filter (c/render-schema-metadatafilter schemas)
        table-metadata-filter (c/render-table-metadatafilter schemas)
        partition-filters (render-schema-datafilters :partition-list-filters schemas
                                                     #(into {} (for [k (keys %)]
                                                                 [k (str/join "," (map c/double-quote (get % k)))])))
        subquery-filters (render-schema-datafilters :subquery-filters schemas identity)]
    (str/join "\n" [metadata-schema-filter table-metadata-filter partition-filters subquery-filters])))

(defn validate [exp-data]
  (let [schemas (:schemas exp-data)
        schema-count (-> schemas keys count)
        table-filter-exists (some (comp #(or (contains? % :include-tables)
                                             (contains? % :exclude-tables)) second) schemas)]
    (if (and table-filter-exists (> schema-count 1))
      (throw (IllegalArgumentException. ":tables filter is allowed only on single schema exp-data.")))
    exp-data))

(s/defn render-exp-script-strict
  ([exp-data :- ExpData]
    (validate exp-data)
    (str/join "\n"
              [(c/render-header :export exp-data)
               (c/render-object-type-metadatafilter exp-data)
               (render-schemas exp-data)
               (str/join "\n" (:custom exp-data))
               (c/render-footer (get exp-data :sqlplus? true))]))
  ([file exp-data :- ExpData]
    (let [script (render-exp-script-strict exp-data)]
      (spit file script))))

(defn render-exp-script
  ([exp-data]
    (c/normalize exp-data :export render-exp-script-strict))
  ([file exp-data]
    (c/normalize exp-data :export (partial render-exp-script-strict file))))

(comment
  (def input {:schemas              {"SIMON1"
                                     {:include-tables         #{"CUSTOMER" "LC_CONFIGURATION"}
                                      :exclude-tables         #{"OP_BIN_ARCHIVE"}
                                      :subquery-filters
                                                              {"CUSTOMER"         "WHERE ID = 3600233"
                                                               "LC_CONFIGURATION" "WHERE CUSTOMER_ID = 3600233"}
                                      :partition-list-filters {"T1" ["SYS1" "SYS2"]
                                                               "T2" ["SYS3" "SYS4"]}}}
              :exclude-object-types ["VIEW"]
              :file-prefix        "simon1"
              :directory            "DATA_PUMP_DIR"
              :reuse-dump-file      true})


  (println (render-exp-script input))

  (render-schema-metadatafilter (:schemas input)))
