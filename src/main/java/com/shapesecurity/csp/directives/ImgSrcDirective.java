package com.shapesecurity.csp.directives;

import com.shapesecurity.csp.sources.SourceExpression;

import javax.annotation.Nonnull;
import java.util.List;

public class ImgSrcDirective extends SourceListDirective {
    @Nonnull
    public static final String name = "img-src";

    public ImgSrcDirective(@Nonnull List<SourceExpression> sourceExpressions) {
        super(ImgSrcDirective.name, sourceExpressions);
    }
}