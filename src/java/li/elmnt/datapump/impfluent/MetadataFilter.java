package li.elmnt.datapump.impfluent;

import java.util.List;

public interface MetadataFilter extends TablespacesRemap {

	TablespacesRemap includeObjectTypes(List<String> objectTypes);

	TablespacesRemap excludeObjectTypes(List<String> objectTypes);

}
