package ch.hood.odpg.api;

import ch.hood.odpg.api.fluid.Directory;
import ch.hood.odpg.api.fluid.FilePrefix;
import ch.hood.odpg.api.fluid.Render;
import ch.hood.odpg.api.fluid.SchemaChoice;
import ch.hood.odpg.api.fluid.Schemas;
import ch.hood.odpg.api.fluid.SchemasOrRemoteLink;
import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.hood.odpg.api.ClojureUtils.keyword;

public class ExpDataBuilder implements Directory, FilePrefix, Render, SchemaChoice, Schemas, SchemasOrRemoteLink {

	public static final IFn RENDER_EXP_SCRIPT;

	public static final String CH_HOOD_ODPG_IMPL_EXPDP = "ch.hood.odpg.impl.expdp";

	public static final Keyword SCHEMAS = keyword(":schemas");
	public static final Keyword TABLES = keyword(":tables");
	public static final Keyword SUBQUERY_FILTERS = keyword(":subquery-filters");
	public static final Keyword PARTITION_FILTERS = keyword(":partition-filters");
	public static final Keyword FILE_PREFIX = keyword(":file-prefix");
	public static final Keyword DIRECTORY = keyword(":directory");
	public static final Keyword REMOTE_LINK = keyword(":remote-link");

	private IPersistentMap expData;

	static {
		IFn require = Clojure.var("clojure.core", "require");
		require.invoke(Clojure.read(CH_HOOD_ODPG_IMPL_EXPDP));
		RENDER_EXP_SCRIPT = Clojure.var(CH_HOOD_ODPG_IMPL_EXPDP, "render-exp-script");
	}

	public static FilePrefix builder() {
		return new ExpDataBuilder();
	}

	private ExpDataBuilder() {
		expData = PersistentHashMap.EMPTY;
		expData = expData.assoc(SCHEMAS, PersistentHashMap.EMPTY);
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
		expData = expData.assoc(FILE_PREFIX, filePrefix);
		return this;
	}

	@Override
	public SchemasOrRemoteLink withDirectory(String directory) {
		expData = expData.assoc(DIRECTORY, directory);
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
		expData = assocNewSchema(schemaName, schema);
		return this;

	}

	@Override
	public Render withSchema(String schemaName, List<String> tables, Map<String, String> subqueryFilters) {
		PersistentHashSet pTables = PersistentHashSet.create(tables);
		IPersistentMap pSubqueryFilters = PersistentHashMap.create(subqueryFilters);
		PersistentHashMap schema = PersistentHashMap.create(TABLES, pTables, SUBQUERY_FILTERS, pSubqueryFilters);
		expData = assocNewSchema(schemaName, schema);
		return this;
	}

	private IPersistentMap assocNewSchema(String schemaName, IPersistentMap schema) {
		IPersistentMap schemas = (IPersistentMap) expData.valAt(SCHEMAS);
		schemas = schemas.assoc(schemaName, schema);
		return expData.assoc(SCHEMAS, schemas);
	}

}
