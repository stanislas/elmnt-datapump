package li.elmnt.datapump.common;

public final class FileSpec {

	public static FileSpec filePrefix(String filePrefix) {
		return new FileSpec(filePrefix, null, null);
	}

	public static FileSpec dumpFileAndFilePrefix(String dumpFile, String filePrefix) {
		return new FileSpec(filePrefix, dumpFile, null);
	}

	public static FileSpec logFileAndFilePrefix(String logFile, String filePrefix) {
		return new FileSpec(filePrefix, null, logFile);
	}

	public static FileSpec dumpFileAndLogFile(String dumpFile, String logFile) {
		return new FileSpec(null, dumpFile, logFile);
	}

	public static FileSpec fileSpec(String filePrefix, String dumpFile, String logFile) {
		return new FileSpec(filePrefix, dumpFile, logFile);
	}

	private final String filePrefix;
	private final String dumpFile;
	private final String logFile;

	private FileSpec(String filePrefix, String dumpFile, String logFile) {
		this.filePrefix = filePrefix;
		this.dumpFile = dumpFile;
		this.logFile = logFile;
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public String getDumpFile() {
		return dumpFile;
	}

	public String getLogFile() {
		return logFile;
	}

}
