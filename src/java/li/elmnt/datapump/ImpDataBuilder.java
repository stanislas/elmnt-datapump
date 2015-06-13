package li.elmnt.datapump;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;
import li.elmnt.datapump.common.FileSpec;
import li.elmnt.datapump.impfluent.MetadataFilter;
import li.elmnt.datapump.impfluent.Render;
import li.elmnt.datapump.impfluent.Schemas;
import li.elmnt.datapump.impfluent.TablespacesRemap;

import java.io.File;
import java.util.List;
import java.util.Map;

import static li.elmnt.datapump.Common.CUSTOM;

public class ImpDataBuilder implements MetadataFilter, TablespacesRemap, Schemas, Render {

	public static final IFn RENDER_IMP_SCRIPT;

	public static final String ELMNT_DATAPUMP_IMPDP = "elmnt.datapump.impdp";

	public static final Keyword TABLESPACES_REMAP = ClojureUtils.keyword(":tablespaces-remap");
	public static final Keyword REMAP_TO = ClojureUtils.keyword(":remap-to");

	static {
		ClojureUtils.requireNamespace(ELMNT_DATAPUMP_IMPDP);
		RENDER_IMP_SCRIPT = Clojure.var(ELMNT_DATAPUMP_IMPDP, "render-imp-script");
	}

	public static MetadataFilter imp(FileSpec fileSpec, String directory) {
		return new ImpDataBuilder(fileSpec, directory);
	}

	private IPersistentMap impData;

	private ImpDataBuilder(FileSpec fileSpec, String directory) {
		impData = Common.CONFIG_WITH_EMPTY_SCHEMA;
		impData = Common.assocFileSpec(impData, fileSpec);
		impData = Common.assocDirectory(impData, directory);
	}

	@Override
	public TablespacesRemap includeObjectTypes(List<String> objectTypes) {
		impData = impData.assoc(Common.INCLUDE_OBJECT_TYPES, PersistentVector.create(objectTypes));
		return this;
	}

	@Override
	public TablespacesRemap excludeObjectTypes(List<String> objectTypes) {
		impData = impData.assoc(Common.EXCLUDE_OBJECT_TYPES, PersistentVector.create(objectTypes));
		return this;
	}

	@Override
	public Schemas withTablespacesRemap(Map<String, String> tablespacesRemap) {
		impData = impData.assoc(TABLESPACES_REMAP, PersistentHashMap.create(tablespacesRemap));
		return this;
	}

	@Override
	public Render withSchemas(Map<String, String> schemasWithOptionalRemap) {
		for (String schemaName : schemasWithOptionalRemap.keySet()) {
			IPersistentMap schema = PersistentHashMap.EMPTY;
			String optionalRemap = schemasWithOptionalRemap.get(schemaName);
			if (optionalRemap != null && optionalRemap.length() > 0) {
				schema = schema.assoc(REMAP_TO, optionalRemap);
			}
			impData = Common.assocNewSchema(impData, schemaName, schema);
		}
		return this;
	}

	@Override
	public Render withCustomLines(List<String> customLines) {
		impData = impData.assoc(CUSTOM, PersistentVector.create(customLines));
		return this;
	}

	@Override
	public String render(boolean sqlplus) {
		impData = impData.assoc(Common.SQLPLUS, sqlplus);
		return (String) RENDER_IMP_SCRIPT.invoke(impData);
	}

	@Override
	public void render(File file, boolean sqlplus) {
		impData = impData.assoc(Common.SQLPLUS, sqlplus);
		RENDER_IMP_SCRIPT.invoke(file, impData);
	}

	@Override
	public String render() {
		return render(true);
	}

	@Override
	public void render(File file) {
		render(file, true);
	}
}
