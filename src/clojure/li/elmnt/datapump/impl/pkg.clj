(ns li.elmnt.datapump.impl.pkg)

(defn render-pkg-type [pkg-type]
	(case pkg-type
		:package-header "package"
		:package-body "package body"))

(defn header [type]
	(str "create or replace " (render-pkg-type type) " is"))

(defn footer [sqlplus?]
	(str "end;\n" (when sqlplus? "/\n")))

