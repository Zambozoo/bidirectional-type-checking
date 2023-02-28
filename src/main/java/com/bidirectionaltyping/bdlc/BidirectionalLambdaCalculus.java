package com.bidirectionaltyping.bdlc;

import com.bidirectionaltyping.Type;
import com.bidirectionaltyping.TypingContext;

public class BidirectionalLambdaCalculus {
    TypingContext context;

    public BidirectionalLambdaCalculus(TypingContext context) {
        this.context = context;
    }

    public boolean typeCheck(BDLCExpression e, Type t) {
        return e.typecheck(t, context);
    }

    public Type synthesize(BDLCExpression e) {
        return e.synthesize(context);
    }
}
