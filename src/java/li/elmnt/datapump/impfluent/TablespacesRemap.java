package li.elmnt.datapump.impfluent;

import java.util.Map;

public interface TablespacesRemap {
	Schemas withTablespacesRemap(Map<String, String> tablespacesRemap);
}
