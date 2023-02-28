package com.bidirectionaltyping.bdlc;

import java.util.ArrayList;
import java.util.List;

import com.bidirectionaltyping.Type;
import com.bidirectionaltyping.TypingContext;
import com.bidirectionaltyping.Type.Function;

/**
 * 𝑒 ::= 𝑥 | 𝜆𝑥. 𝑒 | 𝑒 𝑒 | ()
 */
public abstract class BDLCExpression {
    /**
     * Γ ⊢ 𝑒 ⇒ 𝐴
     * <p>
     * Under Γ, expression 𝑒 synthesizes type 𝐴
     * <p>
     * ---
     * <p>
     * Γ ⊢ 𝑒 ⇐ 𝐴
     * <p>
     * --------------- Anno⇒
     * <p>
     * Γ ⊢ (𝑒 : 𝐴) ⇒ 𝐴
     */
    public abstract Type synthesize(TypingContext context);

    /**
     * Γ ⊢ 𝑒 ⇐ 𝐴
     * <p>
     * Under Γ, expression 𝑒 checks against type 𝐴
     * <p>
     * ---
     * <p>
     * Γ ⊢ 𝑒 ⇒ 𝐴 --- 𝐴 = 𝐵
     * <p>
     * ------------------- Sub⇐
     * <p>
     * Γ ⊢ 𝑒 ⇐ 𝐵
     */
    public boolean typecheck(Type t, TypingContext context) {
        Type result = synthesize(context);
        return result != null && result.isSubtype(t);
    }

    public static class Variable extends BDLCExpression {
        String symbol;

        public Variable(String symbol) {
            this.symbol = symbol;
        }

        /**
         * (𝑥 : 𝐴) ∈ Γ
         * <p>
         * ----------- Var⇒
         * <p>
         * Γ ⊢ 𝑥 ⇒ 𝐴
         */
        @Override
        public Type synthesize(TypingContext context) {
            return context.get(this.symbol);
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    public static class Lambda extends BDLCExpression {
        String variable;
        BDLCExpression expression;

        public Lambda(String variable, BDLCExpression expression) {
            this.variable = variable;
            this.expression = expression;
        }

        /**
         * Γ, 𝑥 : 𝐴1 ⊢ 𝑒 ⇐ 𝐴2
         * <p>
         * --------------------- →I⇐
         * <p>
         * Γ ⊢ (𝜆𝑥. 𝑒) ⇐ 𝐴1 → 𝐴2
         */
        @Override
        public boolean typecheck(Type t, TypingContext context) {
            return (t instanceof Type.Function)
                    && new Variable(variable).typecheck(((Type.Function) t).getLeft(), context)
                    && expression.typecheck(((Type.Function) t).getRight(), context);
        }

        @Override
        public Type synthesize(TypingContext context) {
            Type l = new Variable(variable).synthesize(context);
            Type r = expression.synthesize(context);
            return l != null && r != null ? new Type.Function(l, r) : null;
        }

        @Override
        public String toString() {
            return "LAMBDA(" + variable + ") (" + expression.toString() + ")";
        }
    }

    public static class Application extends BDLCExpression {
        BDLCExpression left;
        BDLCExpression right;

        public Application(BDLCExpression left, BDLCExpression right) {
            this.left = left;
            this.right = right;
        }

        /**
         * Γ ⊢ 𝑒1 ⇒ 𝐴 → 𝐵 --- Γ ⊢ 𝑒2 ⇐ 𝐴
         * <p>
         * ----------------------------- →E⇒
         * <p>
         * Γ ⊢ 𝑒1 𝑒2 ⇒ 𝐵
         */
        @Override
        public Type synthesize(TypingContext context) {
            Type appType = left.synthesize(context);
            return (appType instanceof Type.Function)
                    && right.typecheck(((Type.Function) appType).getLeft(), context)
                            ? ((Type.Function) appType).getRight()
                            : null;
        }

        /**
         * Γ ⊢ 𝑒2 ⇒ 𝐴 --- Γ ⊢ 𝑒1 ⇐ 𝐴 → 𝐵
         * <p>
         * ----------------------------- -Elim
         * <p>
         * Γ ⊢ 𝑒1 𝑒2 ⇐ 𝐵
         */
        @Override
        public boolean typecheck(Type t, TypingContext context) {
            Type r = right.synthesize(context);
            if (r == null)
                return false;
            Function l = new Function(r, t);
            return left.typecheck(l, context);
        }

        @Override
        public String toString() {
            return "(" + left.toString() + ") (" + right.toString() + ")";
        }
    }

    public static class Empty extends BDLCExpression {
        /**
         * ---
         * <p>
         * ------------- unitI⇐
         * <p>
         * Γ ⊢ () ⇐ unit
         */
        @Override
        public boolean typecheck(Type t, TypingContext context) {
            return t instanceof Type.Unit;
        }

        @Override
        public Type synthesize(TypingContext context) {
            return new Type.Unit();
        }

        @Override
        public String toString() {
            return "()";
        }
    }

    public static class Let extends BDLCExpression {
        String variable;
        BDLCExpression assignment;
        BDLCExpression expression;

        public Let(String variable, BDLCExpression assignment, BDLCExpression expression) {
            this.variable = variable;
            this.assignment = assignment;
            this.expression = expression;
        }

        /**
         * Γ ⊢ 𝑒 ⇒ 𝐴 --- Γ, 𝑥 : 𝐴 ⊢ 𝑒' : 𝐵
         * <p>
         * -------------------------------- Let⇒
         * <p>
         * * Γ ⊢ let 𝑥 = 𝑒 in 𝑒' : 𝐵
         */
        @Override
        public Type synthesize(TypingContext context) {
            Type a = assignment.synthesize(context);
            context.put(variable, a);
            return a != null ? expression.synthesize(context) : null;
        }

        @Override
        public String toString() {
            return "let " + variable + "=" + assignment.toString() + "; " + expression.toString() + "";
        }
    }

    public static class Intersect extends BDLCExpression {
        List<BDLCExpression> expressions;

        public Intersect(BDLCExpression... expressions) {
            this.expressions = new ArrayList<>();
            for (BDLCExpression e : expressions)
                this.expressions.add(e);
        }

        @Override
        public Type synthesize(TypingContext context) {
            List<Type> types = new ArrayList<>();
            for (BDLCExpression e : expressions) {
                Type t = e.synthesize(context);
                if (t == null)
                    return null;
                types.add(t);
            }
            return new Type.Intersection(types);
        }

        @Override
        public String toString() {
            String result = "(";
            String spacer = "";
            for (BDLCExpression e : expressions) {
                result += spacer + e.toString();
                spacer = " /\\ ";
            }
            return result + ")";
        }
    }
}
