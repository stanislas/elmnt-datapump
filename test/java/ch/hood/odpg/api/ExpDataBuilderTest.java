package ch.hood.odpg.api;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class ExpDataBuilderTest {

	@Test
	public void renderTest() {
		List<String> includeTables = new ArrayList<>();
		includeTables.add("CUSTOMER");
		includeTables.add("LC_CONFIGURATION");
		List<String> excludeTables = new ArrayList<>();
		excludeTables.add("OP_BIN_ARCHIVE");
		Map<String, String> subqueryFilters = new HashMap<>();
		subqueryFilters.put("CUSTOMER", "WHERE ID = 1000");
		subqueryFilters.put("LC_CONFIGURATION", "WHERE CUSTOMER_ID = 1000");
		String rendered = ExpDataBuilder.builder()
			.withFilePrefix("simon1")
			.withDirectory("DATA_PUMP_DIR", true)
			.withSchema("SIMON1", includeTables, excludeTables, subqueryFilters)
			.render();
		assertNotNull(rendered);
	}

}
