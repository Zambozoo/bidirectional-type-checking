package com.bidirectionaltyping.stlc;

import com.bidirectionaltyping.Type;
import com.bidirectionaltyping.TypingContext;

public class SimplyTypedLambdaCalculus {
    TypingContext context;

    public SimplyTypedLambdaCalculus(TypingContext context) {
        this.context = context;
    }

    /*
     * (𝑥 : 𝐴) ∈ Γ
     * Γ ⊢ 𝑥 : 𝐴
     * Var
     * 
     * Γ ⊢ 𝑒 : 𝐴 𝐴 = 𝐵
     * Γ ⊢ 𝑒 : 𝐵
     * TypeEq
     * 
     * Γ ⊢ 𝑒 : 𝐴
     * Γ ⊢ (𝑒 : 𝐴) : 𝐴
     * Anno
     * 
     * Γ ⊢ () : unit
     * unitI
     * 
     * Γ, 𝑥 : 𝐴1 ⊢ 𝑒 : 𝐴2
     * Γ ⊢ (𝜆𝑥. 𝑒) : 𝐴1 → 𝐴2
     * →I
     * 
     * Γ ⊢ 𝑒1 : 𝐴 → 𝐵 Γ ⊢ 𝑒2 : 𝐴
     * Γ ⊢ 𝑒1 𝑒2 : 𝐵
     * →E
     */
    public boolean typeCheck(STLCExpression e, Type t) {
        return e.typeCheck(t, context);
    }
}
