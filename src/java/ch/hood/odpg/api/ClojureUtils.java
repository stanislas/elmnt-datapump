package ch.hood.odpg.api;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Keyword;

public final class ClojureUtils {

	public static final IFn REQUIRE = Clojure.var("clojure.core", "require");

	public static void requireNamespace(String namespace) {
		REQUIRE.invoke(Clojure.read(namespace));
	}

	public static Keyword keyword(String keyword) {
		return (Keyword) Clojure.read(keyword);
	}

}
