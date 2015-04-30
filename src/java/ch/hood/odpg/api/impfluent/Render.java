package ch.hood.odpg.api.impfluent;

import java.io.File;

public interface Render {

	String render(boolean sqlplus);

	void render(File file, boolean sqlplus);

	String render();

	void render(File file);
	
}
