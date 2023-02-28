package com.bidirectionaltyping;

import com.bidirectionaltyping.Type.*;
import com.bidirectionaltyping.bdlc.BDLCExpression.Application;
import com.bidirectionaltyping.bdlc.*;
import com.bidirectionaltyping.stlc.*;

public class Main {
    public static boolean STLCExample() {
        TypingContext context = new TypingContext();
        context.put("x", new Unit());

        Type t = new Function(
                new Unit(),
                new Unit());
        STLCExpression e = new STLCExpression.Lambda("x", new STLCExpression.Empty());

        return new SimplyTypedLambdaCalculus(context).typeCheck(e, t);
    }

    public static boolean BDLCExample1() {
        TypingContext context = new TypingContext();
        context.put("x", new Type.Unit());
        context.put("z", new Type.Unit());
        context.put("g", new Type.Function(new Type.Unit(), new Type.Unit()));

        BDLCExpression yVar = new BDLCExpression.Variable("y");
        BDLCExpression zVar = new BDLCExpression.Variable("z");
        BDLCExpression gVar = new BDLCExpression.Variable("g");

        BDLCExpression f = new BDLCExpression.Lambda("x", yVar);
        Type t = new Unit();
        BDLCExpression y = new BDLCExpression.Intersect(zVar, gVar);
        BDLCExpression application = new BDLCExpression.Application(f, yVar);
        BDLCExpression let = new BDLCExpression.Let("y", y, application);

        System.out.println(context.toString());
        System.out.println(let.toString());

        return new BidirectionalLambdaCalculus(context).typeCheck(let, t);
    }

    public static boolean BDLCExample2() {
        TypingContext context = new TypingContext();
        context.put("f", new Type.Function(new Type.Unit(), new Type.Unit()));
        context.put("g", new Type.Function(new Type.Unit(),
                new Type.Function(new Type.Function(new Type.Unit(), new Type.Unit()), new Type.Unit())));

        BDLCExpression e = new BDLCExpression.Application(
                new BDLCExpression.Application(new BDLCExpression.Variable("g"), new BDLCExpression.Empty()),
                new BDLCExpression.Variable("f"));

        System.out.println(context.toString());
        System.out.println(e.toString());
        return new BidirectionalLambdaCalculus(context).typeCheck(e, new Type.Unit());
    }

    public static boolean BDCLPolymorphismExample() {
        TypingContext context = new TypingContext();
        Type unitToUnit = new Type.Function(new Type.Unit(), new Type.Unit());

        context.put("f", unitToUnit);

        BDLCExpression let = new BDLCExpression.Let("x",
                new BDLCExpression.Intersect(new BDLCExpression.Empty(), new BDLCExpression.Variable("f")),
                new Application(new BDLCExpression.Variable("f"), new BDLCExpression.Variable("x")));

        System.out.println(context.toString());
        System.out.println(let.toString());

        return new BidirectionalLambdaCalculus(context).typeCheck(let, new Type.Unit());
    }

    public static void printResult(boolean result) {
        System.out.println(result ? "PASSED" : "FAILED");
    }

    public static void main(String[] args) {
        printResult(STLCExample());
        printResult(BDLCExample1());
        printResult(BDLCExample2());
        printResult(BDCLPolymorphismExample());
    }
}
