package li.elmnt.datapump.expfluent;

import java.util.List;
import java.util.Map;

public interface SchemaChoice extends Schemas {

	Render withSchema(String schema, List<String> includeTables, List<String> excludeTables, Map<String, String> tableSubqueries);

}
