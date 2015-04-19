package ch.hood.odpg.api.fluid;

import java.util.List;
import java.util.Map;

public interface SchemaChoice {
	Schemas addSchema(String schema, Map<String, String> tableSubqueries);
	Render withSchema(String schema, List<String> tables, Map<String, String> tableSubqueries);
}
