package ch.hood.odpg.api.fluid;

public interface SchemasOrRemoteLink extends SchemaChoice {
	SchemaChoice withOptionalRemoteLink(String remoteLink);
}
