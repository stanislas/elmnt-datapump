package li.elmnt.datapump;

import li.elmnt.datapump.expfluent.FilePrefix;
import li.elmnt.datapump.expfluent.SchemaChoice;
import li.elmnt.datapump.expfluent.SchemasOrRemoteLink;
import li.elmnt.datapump.expfluent.Directory;
import li.elmnt.datapump.expfluent.Render;
import li.elmnt.datapump.expfluent.Schemas;
import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentVector;

import java.io.File;
import java.util.List;
import java.util.Map;

import static li.elmnt.datapump.Common.CUSTOM;

public class ExpDataBuilder implements Directory, FilePrefix, Render, SchemaChoice, Schemas, SchemasOrRemoteLink {

	public static final IFn RENDER_EXP_SCRIPT;

	public static final String ELMNT_DATAPUMP_EXPDP = "elmnt.datapump.expdp";

	public static final Keyword INCLUDE_TABLES = ClojureUtils.keyword(":include-tables");
	public static final Keyword EXCLUDE_TABLES = ClojureUtils.keyword(":exclude-tables");
	public static final Keyword SUBQUERY_FILTERS = ClojureUtils.keyword(":subquery-filters");
	public static final Keyword PARTITION_LIST_FILTERS = ClojureUtils.keyword(":partition-list-filters");
	public static final Keyword REMOTE_LINK = ClojureUtils.keyword(":remote-link");

	private IPersistentMap expData;

	static {
		ClojureUtils.requireNamespace(ELMNT_DATAPUMP_EXPDP);
		RENDER_EXP_SCRIPT = Clojure.var(ELMNT_DATAPUMP_EXPDP, "render-exp-script");
	}

	public static FilePrefix builder() {
		return new ExpDataBuilder();
	}

	private ExpDataBuilder() {
		expData = PersistentHashMap.EMPTY;
		expData = expData.assoc(Common.SCHEMAS, PersistentHashMap.EMPTY);
	}

	@Override
	public String render(boolean sqlplus) {
		expData = expData.assoc(Common.SQLPLUS, sqlplus);
		return (String) RENDER_EXP_SCRIPT.invoke(expData);
	}

	@Override
	public void render(File file, boolean sqlplus) {
		expData = expData.assoc(Common.SQLPLUS, sqlplus);
		RENDER_EXP_SCRIPT.invoke(file, expData);
	}

	@Override
	public String render() {
		return render(true);
	}

	@Override
	public void render(File file) {
		render(file, true);
	}

	@Override
	public Directory withFilePrefix(String filePrefix) {
		expData = expData.assoc(Common.FILE_PREFIX, filePrefix);
		return this;
	}

	@Override
	public SchemasOrRemoteLink withDirectory(String directory, boolean reuseDumpFile) {
		expData = expData.assoc(Common.DIRECTORY, directory);
		expData = expData.assoc(Common.REUSE_DUMP_FILE, reuseDumpFile);
		return this;
	}

	@Override
	public SchemaChoice withOptionalRemoteLink(String remoteLink) {
		expData = expData.assoc(REMOTE_LINK, remoteLink);
		return this;
	}

	@Override
	public Schemas addSchema(String schemaName, Map<String, String> subqueryFilters) {
		PersistentHashMap schema = PersistentHashMap.create(SUBQUERY_FILTERS, PersistentHashMap.create(subqueryFilters));
		expData = Common.assocNewSchema(expData, schemaName, schema);
		return this;
	}

	@Override
	public Schemas addSchema(String schemaName, Map<String, List<String>> partitionLists, Map<String, String> tableSubqueries) {
		IPersistentMap schema = PersistentHashMap.create(SUBQUERY_FILTERS, PersistentHashMap.create(tableSubqueries));
		schema = schema.assoc(PARTITION_LIST_FILTERS, PersistentHashMap.create(partitionLists));
		expData = Common.assocNewSchema(expData, schemaName, schema);
		return this;
	}

	@Override
	public Render withSchema(String schemaName, List<String> includeTables, List<String> excludeTables, Map<String, String> subqueryFilters) {
		IPersistentMap pSubqueryFilters = PersistentHashMap.create(subqueryFilters);
		IPersistentMap schema = PersistentHashMap.create(SUBQUERY_FILTERS, pSubqueryFilters);
		schema = assocTableList(schema, INCLUDE_TABLES, includeTables);
		schema = assocTableList(schema, EXCLUDE_TABLES, excludeTables);
		expData = Common.assocNewSchema(expData, schemaName, schema);
		return this;
	}

	@Override
	public Render withCustomLines(List<String> customLines) {
		expData = expData.assoc(CUSTOM, PersistentVector.create(customLines));
		return this;
	}

	private IPersistentMap assocTableList(IPersistentMap schema, Keyword keyword, List<String> tables) {
		if (tables != null && !tables.isEmpty()) {
			PersistentHashSet tableSet = PersistentHashSet.create(tables);
			return schema.assoc(keyword, tableSet);
		}
		return schema;
	}

}
