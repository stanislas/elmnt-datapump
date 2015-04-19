package ch.hood.odpg.api;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class ExpDataBuilderTest {

	@Test
	public void renderTest() {
		List<String> tables = new ArrayList<>();
		tables.add("CUSTOMER");
		tables.add("LC_CONFIGURATION");
		Map<String, String> subqueryFilters = new HashMap<>();
		subqueryFilters.put("CUSTOMER", "WHERE ID = 1000");
		subqueryFilters.put("LC_CONFIGURATION", "WHERE CUSTOMER_ID = 1000");
		String rendered = ExpDataBuilder.builder()
			.withFilePrefix("simon1")
			.withDirectory("DATA_PUMP_DIR")
			.withSchema("SIMON1", tables, subqueryFilters)
			.render();
		assertNotNull(rendered);
	}

}
