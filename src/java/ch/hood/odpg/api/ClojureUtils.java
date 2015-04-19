package ch.hood.odpg.api;

import clojure.java.api.Clojure;
import clojure.lang.Keyword;

public final class ClojureUtils {

	public static Keyword keyword(String keyword) {
		return (Keyword) Clojure.read(keyword);
	}

}
