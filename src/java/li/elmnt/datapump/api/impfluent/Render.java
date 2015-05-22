package li.elmnt.datapump.api.impfluent;

import java.io.File;
import java.util.List;

public interface Render {

	Render withCustomLines(List<String> customLines);

	String render(boolean sqlplus);

	void render(File file, boolean sqlplus);

	String render();

	void render(File file);
	
}
