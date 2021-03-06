package com.shapesecurity.salvation;

import com.shapesecurity.salvation.data.Location;
import com.shapesecurity.salvation.tokens.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokeniser {
    private static final Pattern directiveSeparator = Pattern.compile(";");
    private static final Pattern policySeparator = Pattern.compile(",");
    private static final Pattern directiveNamePattern = Pattern.compile("[a-zA-Z0-9-]+");
    private static final Pattern directiveValuePattern = Pattern.compile("[!-+--:<-~]+");
    @Nonnull protected final ArrayList<Token> tokens;
    @Nonnull protected final String sourceText;
    protected final int length;
    protected int index = 0;

    protected Tokeniser(@Nonnull String sourceText) {
        this.tokens = new ArrayList<>();
        this.sourceText = sourceText;
        this.length = sourceText.length();
        this.eatWhitespace();
    }

    @Nonnull public static Token[] tokenise(@Nonnull String sourceText) throws TokeniserException {
        return new Tokeniser(sourceText).tokenise();
    }

    private static boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t';
    }

    @Nonnull protected TokeniserException createError(@Nonnull String message) {
        return new TokeniserException(message);
    }

    protected boolean eat(@Nonnull Function<String, Token> ctor, @Nonnull Pattern pattern) {
        if (this.index >= this.length)
            return false;
        Matcher matcher = pattern.matcher(this.sourceText);
        if (!matcher.find(this.index) || matcher.start() != this.index)
            return false;
        int start = this.index;
        this.index = matcher.end();
        this.tokens.add(ctor.apply(this.sourceText.substring(start, this.index)));
        this.eatWhitespace();
        return true;
    }

    private boolean eatSeparator() {
        return this.eat(DirectiveSeparatorToken::new, Tokeniser.directiveSeparator) || this
            .eat(PolicySeparatorToken::new, Tokeniser.policySeparator);
    }

    private boolean eatDirectiveName() {
        return this.eat(DirectiveNameToken::new, Tokeniser.directiveNamePattern);
    }

    private boolean eatDirectiveValue() {
        return this.eat(DirectiveValueToken::new, Tokeniser.directiveValuePattern);
    }

    private void eatWhitespace() {
        while (this.hasNext() && Tokeniser.isWhitespace(this.sourceText.charAt(this.index))) {
            ++this.index;
        }
    }

    private boolean hasNext() {
        return this.index < this.length;
    }

    // invariant: hasNext has been called and returned true; next has not been called since; eat has not returned true since
    private String next() {
        int i = this.index;
        while (i < this.length) {
            char ch = this.sourceText.charAt(i);
            if (Tokeniser.isWhitespace(ch) || ch == ';')
                break;
            ++i;
        }
        return this.sourceText.substring(this.index, i);
    }

    @Nonnull protected Token[] tokenise() throws TokeniserException {
        while (this.hasNext()) {
            if (this.eatSeparator())
                continue;
            if (!this.eatDirectiveName()) {
                throw this.createError("expecting directive-name but found " + this.next());
            }
            if (this.eatSeparator())
                continue;
            while (this.hasNext()) {
                if (!this.eatDirectiveValue()) {
                    String token = this.next();
                    int cp = token.codePointAt(0);
                    throw this.createError(String.format("expecting directive-value but found U+%04X (%s). Non-ASCII and non-printable characters must be percent-encoded", cp, new String(new int[]{cp}, 0, 1)));
                }
                if (this.eatSeparator())
                    break;
            }
        }
        Token[] tokensArray = new Token[this.tokens.size()];
        return this.tokens.toArray(tokensArray);
    }

    public static class TokeniserException extends Exception {
        @Nullable public Location location;

        public TokeniserException(@Nonnull String message) {
            super(message);
        }

        @Nonnull @Override public String getMessage() {
            if (location == null) {
                return super.getMessage();
            }
            return location.show() + ": " + super.getMessage();
        }
    }
}
