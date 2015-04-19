package ch.hood.odpg.api.fluid;

import java.util.Map;

public interface Schemas extends Render {
	Schemas addSchema(String schema, Map<String, String> tableSubqueries);
}
