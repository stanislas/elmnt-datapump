package li.elmnt.datapump.api.expfluent;

import java.util.List;
import java.util.Map;

public interface Schemas extends Render {

	Schemas addSchema(String schema, Map<String, String> tableSubqueries);

	Schemas addSchema(String schema, Map<String, List<String>> partitionLists, Map<String, String> tableSubqueries);

}
