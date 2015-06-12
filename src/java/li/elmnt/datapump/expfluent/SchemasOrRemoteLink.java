package li.elmnt.datapump.expfluent;

public interface SchemasOrRemoteLink extends SchemaChoice {
	SchemaChoice withOptionalRemoteLink(String remoteLink);
}
