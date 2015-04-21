package ch.hood.odpg.api.expfluent;

import java.util.List;
import java.util.Map;

public interface SchemaChoice {
	Schemas addSchema(String schema, Map<String, String> tableSubqueries);
	Render withSchema(String schema, List<String> includeTables, List<String> excludeTables, Map<String, String> tableSubqueries);
}
