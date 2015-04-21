package ch.hood.odpg.api.impfluent;

import java.util.Map;

public interface TablespacesRemap {
	Schemas withTablespacesRemap(Map<String, String> tablespacesRemap);
}
