package ch.hood.odpg.api.expfluent;

public interface SchemasOrRemoteLink extends SchemaChoice {
	SchemaChoice withOptionalRemoteLink(String remoteLink);
}
