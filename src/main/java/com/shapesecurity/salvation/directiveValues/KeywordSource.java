package com.shapesecurity.salvation.directiveValues;


import com.shapesecurity.salvation.data.GUID;
import com.shapesecurity.salvation.data.Origin;
import com.shapesecurity.salvation.data.URI;
import com.shapesecurity.salvation.interfaces.MatchesSource;

import javax.annotation.Nonnull;

public class KeywordSource implements SourceExpression, AncestorSource, MatchesSource {
    @Nonnull public static final KeywordSource Self = new KeywordSource("self");
    @Nonnull public static final KeywordSource UnsafeInline = new KeywordSource("unsafe-inline");
    @Nonnull public static final KeywordSource UnsafeEval = new KeywordSource("unsafe-eval");
    @Nonnull public static final KeywordSource UnsafeRedirect =
        new KeywordSource("unsafe-redirect");
    @Nonnull private final String value;

    private KeywordSource(@Nonnull String value) {
        this.value = value;
    }

    @Override public boolean matchesSource(@Nonnull Origin origin, @Nonnull URI source) {
        return this == Self && origin.equals(source);
    }

    @Override public boolean matchesSource(@Nonnull Origin origin, @Nonnull GUID source) {
        return this == Self && origin.equals(source);
    }

    @Nonnull @Override public String show() {
        return "'" + this.value + "'";
    }

}


