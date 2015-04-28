package ch.hood.odpg.api;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;

import static ch.hood.odpg.api.ClojureUtils.keyword;

public final class Common {

	public static final Keyword SCHEMAS = keyword(":schemas");
	public static final Keyword DIRECTORY = keyword(":directory");
	public static final Keyword FILE_PREFIX = keyword(":file-prefix");
	public static final Keyword EXCLUDE_OBJECT_TYPES = keyword(":exclude-object-types");
	public static final Keyword INCLUDE_OBJECT_TYPES = keyword(":include-object-types");

	public static IPersistentMap assocNewSchema(IPersistentMap pmap, String schemaName, IPersistentMap schema) {
		IPersistentMap schemas = (IPersistentMap) pmap.valAt(SCHEMAS);
		schemas = schemas.assoc(schemaName, schema);
		return pmap.assoc(SCHEMAS, schemas);
	}

	private Common() {
	}
}
