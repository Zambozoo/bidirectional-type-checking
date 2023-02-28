package com.bidirectionaltyping;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Types 𝐴, 𝐵,𝐶 ::= unit | 𝐴 → 𝐴
 */
public abstract class Type {
    public abstract boolean simpleEquals(Type t);

    public boolean isSubtype(Type t) {
        return simpleEquals(t);
    }

    public static class Unit extends Type {
        @Override
        public boolean simpleEquals(Type t) {
            return (t instanceof Unit);
        }

        @Override
        public String toString() {
            return "()";
        }
    }

    public static class Function extends Type {
        Type left;
        Type right;

        public Function(Type left, Type right) {
            this.left = left;
            this.right = right;
        }

        public Type getLeft() {
            return left;
        }

        public Type getRight() {
            return right;
        }

        @Override
        public boolean simpleEquals(Type t) {
            return (t instanceof Function)
                    && Objects.equals(((Function) t).left, left)
                    && Objects.equals(((Function) t).right, right);
        }

        @Override
        public boolean isSubtype(Type t) {
            if (!(t instanceof Function))
                return false;
            Function f = (Function) t;
            return this.left.isSubtype(f.left) && f.right.isSubtype(this.right);
        }

        @Override
        public String toString() {
            return "(" + this.left.toString() + ")->(" + this.right.toString() + ")";
        }
    }

    public static class Intersection extends Type {
        List<Type> types;

        public Intersection(List<Type> types) {
            this.types = types;
        }

        public Intersection(Type... types) {
            this.types = new ArrayList<>();
            for (Type t : types) {
                this.types.add(t);
            }
        }

        @Override
        public String toString() {
            String result = "";
            String spacer = "";
            for (Type t : types) {
                result += spacer + t.toString();
                spacer = "/\\";
            }
            return result;
        }

        @Override
        public boolean simpleEquals(Type t) {
            throw new UnsupportedOperationException("Unimplemented method 'simpleEquals'");
        }

        @Override
        public boolean isSubtype(Type t) {
            for (Type s : types)
                if (s.isSubtype(t))
                    return true;
            return false;
        }
    }
}
