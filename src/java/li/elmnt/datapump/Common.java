package li.elmnt.datapump;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import li.elmnt.datapump.common.FileSpec;

public final class Common {

	public static final Keyword SCHEMAS = ClojureUtils.keyword(":schemas");
	public static final Keyword DIRECTORY = ClojureUtils.keyword(":directory");
	public static final Keyword REUSE_DUMP_FILE = ClojureUtils.keyword(":reuse-dump-file");
	public static final Keyword FILE_PREFIX = ClojureUtils.keyword(":file-prefix");
	public static final Keyword DUMP_FILE = ClojureUtils.keyword(":dump-file");
	public static final Keyword LOG_FILE = ClojureUtils.keyword(":log-file");
	public static final Keyword EXCLUDE_OBJECT_TYPES = ClojureUtils.keyword(":exclude-object-types");
	public static final Keyword INCLUDE_OBJECT_TYPES = ClojureUtils.keyword(":include-object-types");
	public static final Keyword SQLPLUS = ClojureUtils.keyword(":sqlplus?");
	public static final Keyword CUSTOM = ClojureUtils.keyword(":custom");

	public static final IPersistentMap CONFIG_WITH_EMPTY_SCHEMA = PersistentHashMap.EMPTY.assoc(SCHEMAS, PersistentHashMap.EMPTY);

	public static IPersistentMap assocNewSchema(IPersistentMap pmap, String schemaName, IPersistentMap schema) {
		IPersistentMap schemas = (IPersistentMap) pmap.valAt(SCHEMAS);
		schemas = schemas.assoc(schemaName, schema);
		return pmap.assoc(SCHEMAS, schemas);
	}

	public static IPersistentMap assocDirectory(IPersistentMap pmap, String directory) {
		return pmap.assoc(DIRECTORY, directory);
	}

	public static IPersistentMap assocReuseDumpFile(IPersistentMap pmap, boolean reuseDumpFile) {
		return pmap.assoc(REUSE_DUMP_FILE, reuseDumpFile);
	}

	public static IPersistentMap assocFileSpec(IPersistentMap pmap, FileSpec fileSpec) {
		IPersistentMap result = assocIfNotNull(pmap, FILE_PREFIX, fileSpec.getFilePrefix());
		result = assocIfNotNull(result, DUMP_FILE, fileSpec.getDumpFile());
		result = assocIfNotNull(result, LOG_FILE, fileSpec.getLogFile());
		return result;
	}

	public static IPersistentMap assocIfNotNull(IPersistentMap pmap, Keyword keyword, Object value) {
		return value == null ? pmap : pmap.assoc(keyword, value);
	}

	private Common() {
	}
}
