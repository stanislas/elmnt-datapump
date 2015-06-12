package li.elmnt.datapump.expfluent;

public interface Directory {
	SchemasOrRemoteLink withDirectory(String directory, boolean reuseDumpFile);
}
