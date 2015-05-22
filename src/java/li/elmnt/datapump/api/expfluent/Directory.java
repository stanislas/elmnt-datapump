package li.elmnt.datapump.api.expfluent;

public interface Directory {
	SchemasOrRemoteLink withDirectory(String directory, boolean reuseDumpFile);
}
