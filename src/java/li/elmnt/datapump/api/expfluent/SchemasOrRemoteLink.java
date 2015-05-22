package li.elmnt.datapump.api.expfluent;

public interface SchemasOrRemoteLink extends SchemaChoice {
	SchemaChoice withOptionalRemoteLink(String remoteLink);
}
