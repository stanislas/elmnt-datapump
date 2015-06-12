package li.elmnt.datapump;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;

public final class Common {

	public static final Keyword SCHEMAS = ClojureUtils.keyword(":schemas");
	public static final Keyword DIRECTORY = ClojureUtils.keyword(":directory");
	public static final Keyword REUSE_DUMP_FILE = ClojureUtils.keyword(":reuse-dump-file");
	public static final Keyword FILE_PREFIX = ClojureUtils.keyword(":file-prefix");
	public static final Keyword EXCLUDE_OBJECT_TYPES = ClojureUtils.keyword(":exclude-object-types");
	public static final Keyword INCLUDE_OBJECT_TYPES = ClojureUtils.keyword(":include-object-types");
	public static final Keyword SQLPLUS = ClojureUtils.keyword(":sqlplus?");
	public static final Keyword CUSTOM = ClojureUtils.keyword(":custom");

	public static IPersistentMap assocNewSchema(IPersistentMap pmap, String schemaName, IPersistentMap schema) {
		IPersistentMap schemas = (IPersistentMap) pmap.valAt(SCHEMAS);
		schemas = schemas.assoc(schemaName, schema);
		return pmap.assoc(SCHEMAS, schemas);
	}

	private Common() {
	}
}
