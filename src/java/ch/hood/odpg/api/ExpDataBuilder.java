package ch.hood.odpg.api;

import ch.hood.odpg.api.expfluent.Directory;
import ch.hood.odpg.api.expfluent.FilePrefix;
import ch.hood.odpg.api.expfluent.Render;
import ch.hood.odpg.api.expfluent.SchemaChoice;
import ch.hood.odpg.api.expfluent.Schemas;
import ch.hood.odpg.api.expfluent.SchemasOrRemoteLink;
import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;

import java.io.File;
import java.util.List;
import java.util.Map;

import static ch.hood.odpg.api.ClojureUtils.keyword;

public class ExpDataBuilder implements Directory, FilePrefix, Render, SchemaChoice, Schemas, SchemasOrRemoteLink {

	public static final IFn RENDER_EXP_SCRIPT;

	public static final String CH_HOOD_ODPG_IMPL_EXPDP = "ch.hood.odpg.impl.expdp";

	public static final Keyword INCLUDE_TABLES = keyword(":include-tables");
	public static final Keyword EXCLUDE_TABLES = keyword(":exclude-tables");
	public static final Keyword SUBQUERY_FILTERS = keyword(":subquery-filters");
	public static final Keyword PARTITION_FILTERS = keyword(":partition-filters");
	public static final Keyword REMOTE_LINK = keyword(":remote-link");

	private IPersistentMap expData;

	static {
		ClojureUtils.requireNamespace(CH_HOOD_ODPG_IMPL_EXPDP);
		RENDER_EXP_SCRIPT = Clojure.var(CH_HOOD_ODPG_IMPL_EXPDP, "render-exp-script");
	}

	public static FilePrefix builder() {
		return new ExpDataBuilder();
	}

	private ExpDataBuilder() {
		expData = PersistentHashMap.EMPTY;
		expData = expData.assoc(Common.SCHEMAS, PersistentHashMap.EMPTY);
	}

	@Override
	public String render() {
		return (String) RENDER_EXP_SCRIPT.invoke(expData);
	}

	@Override
	public void render(File file) {
		RENDER_EXP_SCRIPT.invoke(file, expData);
	}

	@Override
	public Directory withFilePrefix(String filePrefix) {
		expData = expData.assoc(Common.FILE_PREFIX, filePrefix);
		return this;
	}

	@Override
	public SchemasOrRemoteLink withDirectory(String directory) {
		expData = expData.assoc(Common.DIRECTORY, directory);
		return this;
	}

	@Override
	public SchemaChoice withOptionalRemoteLink(String remoteLink) {
		expData = expData.assoc(REMOTE_LINK, remoteLink);
		return this;
	}

	@Override
	public Schemas addSchema(String schemaName, Map<String, String> subqueryFilters) {
		IPersistentMap pSubqueryFilters = PersistentHashMap.create(subqueryFilters);
		PersistentHashMap schema = PersistentHashMap.create(SUBQUERY_FILTERS, pSubqueryFilters);
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

	private IPersistentMap assocTableList(IPersistentMap schema, Keyword keyword, List<String> tables) {
		if (tables != null && !tables.isEmpty()) {
			PersistentHashSet tableSet = PersistentHashSet.create(tables);
			return schema.assoc(keyword, tableSet);
		}
		return schema;
	}

}
