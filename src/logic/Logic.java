package logic;

import debugging.Log;
import logic.model.*;
import tools.Assign;
import tools.Parser;
import tools.Syntax;

import java.util.*;

public class Logic {

    private static List<Atom> allAtoms ;
    private static List<String> combinations;
    private static int currentPhase;

    public static String function(String input0, String input1) {

        Log.init();
        Log.write_head("Original");
        Log.write_info("(One): " + input0);
        Log.write_info("(Two): " + input1);

        String noSpaceInput0 = Parser.noSpace(input0);
        String noSpaceInput1 = Parser.noSpace(input1);
        input0 = noSpaceInput0;
        input1 = noSpaceInput1;
        Log.write_head("No Space");
        Log.write_info("(One): " + noSpaceInput0);
        Log.write_info("(Two): " + noSpaceInput1);

        String noDNot0 = Parser.noDoubleNot(input0);
        String noDNot1 = Parser.noDoubleNot(input1);
        Log.write_head("Double NOT");
        Log.write_info("(One): " + noDNot0);
        Log.write_info("(Two): " + noDNot1);

        input0 = Parser.putSignForOperator(noDNot0);
        input1 = Parser.putSignForOperator(noDNot1);
        Log.write_head("Highlight symbols(@ + symbol + $)");
        Log.write_info("(One): " + input0);
        Log.write_info("(Two): " + input1);

        if (Syntax.validate(input0) || Syntax.validate(input1)) {
            return null;
        }
        Log.write_head("Syntax Validation");
        Log.write_info("(One): " + "validated");
        Log.write_info("(Two): " + "validated");

        String postfix0 = Parser.infixToPostfix(input0);
        String postfix1 = Parser.infixToPostfix(input1);
        Log.write_head("Convert to Postfix");
        Log.write_info("(One): " + postfix0);
        Log.write_info("(Two): " + postfix1);

        allAtoms = Assign.createAllAtoms(input0, input1);
        Log.write_head("Atoms");
        StringBuilder tmp = new StringBuilder();
        for (Atom atom : allAtoms) { tmp.append(atom.getID()); tmp.append(", "); }
        tmp.replace(tmp.length()-2, tmp.length(), "");
        tmp.append(" atoms are created.");
        Log.write_info(tmp.toString());

        Formula[] formulas = Assign.createFormulas(postfix0, postfix1);

        Log.write_head("Truth-Table");
        combinations = getCombinations(allAtoms.size());
        Log.write_info("Corresponding table created.");

        currentPhase = combinations.size();
        boolean equCheck = true;
        boolean impCheck = true;
        Log.write_head("Satisfiability");
        while (currentPhase >= 0) {
            assignAtomValue(allAtoms);
            Value f0Value = findColumnValue(formulas[0].getPostfix());
            Value f1Value = findColumnValue(formulas[1].getPostfix());

            StringBuilder tmpAtomValue = new StringBuilder();
//            tmpAtomValue.append("Atom:Value - ");
            for (Atom atom : allAtoms) {
                tmpAtomValue.append(atom.getID()).append(": ").append(atom.getValue()).append(" / ");
            }
            tmpAtomValue.replace(tmpAtomValue.length() - 2, tmpAtomValue.length() - 1, "");
            Log.write_info(tmpAtomValue.toString());
            Log.write_info("(One): " + f0Value.toString());
            Log.write_info("(Two): " + f1Value.toString());

            if (!f0Value.equals(f1Value)) {
                equCheck = false;
            }

            if (Operation.Operate(f1Value, f0Value, Operator.OPERATOR_IMP) != Value.TRUE) {
                impCheck = false;
            }

            if (!impCheck) {
                break;
            }
        }

        String result;
        result = "Formula One(\"" + noDNot0 + "\") ";
        if (impCheck) {
            result += "IMPLIES";
        } else {
            result += "does NOT imply";
        }
        if (equCheck) {
            result += " / is EQUIVALENT to";
        } else {
            result += " / is NOT equivalent to";
        }
        result += " Two(\"" + noDNot1 + "\").";
        Log.write_head("Result");
        Log.write_info(result);

        Log.finish();
        return result;
    }

    private static Value findColumnValue(String postfix) {
        Stack<Value> stack = new Stack<>();
        Queue<Character> atomName = new LinkedList<>();
        for (int i = 0; i < postfix.length(); ++i) {
            char ch = postfix.charAt(i);

            if (ch == '$') {

                //make the operator:
                StringBuilder temp = new StringBuilder();
                do {
                    if (ch != '$') {
                        temp.append(ch);
                    }
                    ++i;
                    ch = postfix.charAt(i);
                } while (ch != '@');
                String oprtr = temp.toString();

                if (oprtr.equals("~")) {
                    Value value = Operation.Operate(stack.pop(), Operator.getOperatorByName(oprtr));
                    stack.add(value);
                }
                else if (stack.size() >= 2) {
                    Value value = Operation.Operate(stack.pop(), stack.pop(), Operator.getOperatorByName(oprtr));
                    stack.add(value);
                }
                else {
                    System.out.println("Logic Error: Not enough values in stack");
                }
            }
            else {
                atomName.add(ch);
                StringBuilder name = new StringBuilder();
                for (char chr : atomName) {
                    name.append(chr);
                }
                for (Atom atom : allAtoms) {
                    if (atom.getID().equals(name.toString())) {
                        stack.add(atom.getValue());
                        atomName.clear();
                    }
                }
            }
        }
        if (stack.size() > 1) {
            System.out.println("Logic Error: More values in stack than expected");
        }
        else if (stack.size() < 1) {
            System.out.println("Logic Error: No values in stack");
        }
        return stack.pop();
    }

    private static void assignAtomValue(List<Atom> allAtoms) {
        --currentPhase;
        if (currentPhase >= 0) {
            String phase = combinations.get(currentPhase);
            for (int i = 0; i < phase.length(); ++i) {
                if (phase.charAt(i) == '0') {
                    allAtoms.get(i).setValue(Value.FALSE);
                } else {
                    allAtoms.get(i).setValue(Value.TRUE);
                }
            }
        }
    }

    private static List<String> getCombinations(int num) {
        List<String> combinations = new ArrayList<>();
        int number = 0;
        for (int i = 0; i < Math.pow(2, num); ++i) {
            StringBuilder res = new StringBuilder(Integer.toString(number, 2));
            while (res.length() < num) {
                res.insert(0, '0');
            }
            combinations.add(res.toString());
            number++;
        }
        return combinations;
    }

}
