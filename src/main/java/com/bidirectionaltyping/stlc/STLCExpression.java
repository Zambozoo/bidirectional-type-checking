package com.bidirectionaltyping.stlc;

import com.bidirectionaltyping.Type;
import com.bidirectionaltyping.TypingContext;

/**
 * 𝑒 ::= 𝑥 | 𝜆𝑥. 𝑒 | 𝑒 𝑒 | ()
 */
public abstract class STLCExpression {
    /**
     * Γ ⊢ 𝑒 : 𝐴 --- 𝐴 = 𝐵
     * <p>
     * ------------------- TypeEq
     * <p>
     * Γ ⊢ 𝑒 : 𝐵
     * <p>
     * ---
     * <p>
     * Γ ⊢ 𝑒 : 𝐴
     * <p>
     * --------------- An
     * <p>
     * Γ ⊢ (𝑒 : 𝐴) : 𝐴
     */
    public abstract boolean typeCheck(Type t, TypingContext context);

    public static class Variable extends STLCExpression {
        String symbol;

        public Variable(String symbol) {
            this.symbol = symbol;
        }

        /**
         * (𝑥 : 𝐴) ∈ Γ
         * <p>
         * ----------- Var
         * <p>
         * Γ ⊢ 𝑥 : 𝐴
         */
        @Override
        public boolean typeCheck(Type t, TypingContext context) {
            return t.simpleEquals(context.get(this.symbol));
        }
    }

    public static class Lambda extends STLCExpression {
        String variable;
        STLCExpression expression;

        public Lambda(String variable, STLCExpression expression) {
            this.variable = variable;
            this.expression = expression;
        }

        /**
         * Γ, 𝑥 : 𝐴1 ⊢ 𝑒 : 𝐴2
         * <p>
         * --------------------- →I
         * <p>
         * Γ ⊢ (𝜆𝑥. 𝑒) : 𝐴1 → 𝐴2
         */
        @Override
        public boolean typeCheck(Type t, TypingContext context) {
            return (t instanceof Type.Function)
                    && new Variable(variable).typeCheck(((Type.Function) t).getLeft(), context)
                    && expression.typeCheck(((Type.Function) t).getRight(), context);
        }
    }

    public static class Application extends STLCExpression {
        STLCExpression left;
        STLCExpression right;

        public Application(STLCExpression left, STLCExpression right) {
            this.left = left;
            this.right = right;
        }

        /**
         * Γ ⊢ 𝑒1 : 𝐴 → 𝐵 --- Γ ⊢ 𝑒2 : 𝐴
         * <p>
         * ----------------------------- →E
         * <p>
         * Γ ⊢ 𝑒1 𝑒2 : 𝐵
         */
        @Override
        public boolean typeCheck(Type t, TypingContext context) {
            return (t instanceof Type.Function)
                    && left.typeCheck(((Type.Function) t).getLeft(), context)
                    && right.typeCheck(((Type.Function) t).getRight(), context);
        }
    }

    public static class Empty extends STLCExpression {
        /**
         * ---
         * <p>
         * ------------- unitI
         * <p>
         * Γ ⊢ () : unit
         */
        @Override
        public boolean typeCheck(Type t, TypingContext context) {
            return (t instanceof Type.Unit);
        }
    }
}
