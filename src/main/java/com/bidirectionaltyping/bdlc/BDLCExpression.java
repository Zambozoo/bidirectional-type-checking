package com.bidirectionaltyping.bdlc;

import java.util.ArrayList;
import java.util.List;

import com.bidirectionaltyping.Type;
import com.bidirectionaltyping.TypingContext;
import com.bidirectionaltyping.Type.Function;

/**
 * π ::= π₯ | ππ₯. π | π π | ()
 */
public abstract class BDLCExpression {
    /**
     * Ξ β’ π β π΄
     * <p>
     * Under Ξ, expression π synthesizes type π΄
     * <p>
     * ---
     * <p>
     * Ξ β’ π β π΄
     * <p>
     * --------------- Annoβ
     * <p>
     * Ξ β’ (π : π΄) β π΄
     */
    public abstract Type synthesize(TypingContext context);

    /**
     * Ξ β’ π β π΄
     * <p>
     * Under Ξ, expression π checks against type π΄
     * <p>
     * ---
     * <p>
     * Ξ β’ π β π΄ --- π΄ = π΅
     * <p>
     * ------------------- Subβ
     * <p>
     * Ξ β’ π β π΅
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
         * (π₯ : π΄) β Ξ
         * <p>
         * ----------- Varβ
         * <p>
         * Ξ β’ π₯ β π΄
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
         * Ξ, π₯ : π΄1 β’ π β π΄2
         * <p>
         * --------------------- βIβ
         * <p>
         * Ξ β’ (ππ₯. π) β π΄1 β π΄2
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
         * Ξ β’ π1 β π΄ β π΅ --- Ξ β’ π2 β π΄
         * <p>
         * ----------------------------- βEβ
         * <p>
         * Ξ β’ π1 π2 β π΅
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
         * Ξ β’ π2 β π΄ --- Ξ β’ π1 β π΄ β π΅
         * <p>
         * ----------------------------- -Elim
         * <p>
         * Ξ β’ π1 π2 β π΅
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
         * ------------- unitIβ
         * <p>
         * Ξ β’ () β unit
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
         * Ξ β’ π β π΄ --- Ξ, π₯ : π΄ β’ π' : π΅
         * <p>
         * -------------------------------- Letβ
         * <p>
         * * Ξ β’ let π₯ = π in π' : π΅
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

    public static class IntegerExpression extends BDLCExpression {
        int value;

        public IntegerExpression(int value) {
            this.value = value;
        }

        @Override
        public Type synthesize(TypingContext context) {
            return new Type.Int();
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static class BooleanExpression extends BDLCExpression {
        boolean value;

        public BooleanExpression(boolean value) {
            this.value = value;
        }

        @Override
        public Type synthesize(TypingContext context) {
            return new Type.Int();
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

}
