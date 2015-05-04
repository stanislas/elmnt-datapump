package ch.hood.odpg.api;

import ch.hood.odpg.api.impfluent.Directory;
import ch.hood.odpg.api.impfluent.FilePrefix;
import ch.hood.odpg.api.impfluent.MetadataFilter;
import ch.hood.odpg.api.impfluent.Render;
import ch.hood.odpg.api.impfluent.Schemas;
import ch.hood.odpg.api.impfluent.TablespacesRemap;
import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;

import java.io.File;
import java.util.List;
import java.util.Map;

import static ch.hood.odpg.api.Common.CUSTOM;

public class ImpDataBuilder implements FilePrefix, Directory, MetadataFilter, TablespacesRemap, Schemas, Render {

	public static final IFn RENDER_IMP_SCRIPT;

	public static final String CH_HOOD_ODPG_IMPL_IMPDP = "ch.hood.odpg.impl.impdp";

	public static final Keyword TABLESPACES_REMAP = ClojureUtils.keyword(":tablespaces-remap");
	public static final Keyword REMAP_TO = ClojureUtils.keyword(":remap-to");

	static {
		ClojureUtils.requireNamespace(CH_HOOD_ODPG_IMPL_IMPDP);
		RENDER_IMP_SCRIPT = Clojure.var(CH_HOOD_ODPG_IMPL_IMPDP, "render-imp-script");
	}

	public static FilePrefix builder() {
		return new ImpDataBuilder();
	}

	private IPersistentMap impData;

	private ImpDataBuilder() {
		impData = PersistentHashMap.EMPTY;
		impData = impData.assoc(Common.SCHEMAS, PersistentHashMap.EMPTY);
	}

	@Override
	public Directory withFilePrefix(String filePrefix) {
		impData = impData.assoc(Common.FILE_PREFIX, filePrefix);
		return this;
	}

	@Override
	public MetadataFilter withDirectory(String directory) {
		impData = impData.assoc(Common.DIRECTORY, directory);
		return this;
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
