package de.buw.fm4se;

import java.beans.Expression;
import java.security.Signature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4viz.VizGUI;
import edu.mit.csail.sdg.ast.*;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.mit.csail.sdg.translator.A4Options;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod;

import static edu.mit.csail.sdg.ast.Sig.UNIV;

public class AlloyChecker {

    public static List<String> findDeadSignatures(String fileName, A4Options options, A4Reporter rep) {

        System.out.println("findDeadSignatures");

        VizGUI viz = null;

        Module world = CompUtil.parseEverything_fromFile(rep, null, fileName);

        //Use the first command in the Alloy file. To see how to parse Alloy models and how to access commands see, e.g.,
        // lines 57 and 65 in class ExampleUsingTheCompiler.


        SafeList<Sig> allsigs = world.getAllSigs();


        // You may update the predicate a command cmd checks to expression e by using the returned Command of cmd.change(e).


        // To see how you can create formulas from signatures and other formulas see, e.g., line 90 in class ExampleUsingTheAPI.
        //Expr expr1 = A.some().and(atMost3.call(B, B));


        List<String> deadSigs = new ArrayList<>();


        for (int i = 0; i < allsigs.size(); ++i) {

            for (Command command : world.getAllCommands()) {


                command.change(allsigs.get(i).no().always()); // Change command to check if the current feature can always be removed

                A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);

                if (ans.eval(allsigs.get(i)).size() == 0) {

                    deadSigs.add(allsigs.get(i).toString());
                    // You can query "ans" to find out the values of each set or
                    // type.
                    // This can be useful for debugging.
                    //
                    // You can also write the outcome to an XML file
                    ans.writeXML("alloy_example_output.xml");
                    //
                    // You can then visualize the XML file by calling this:
                    if (viz == null) {
                        viz = new VizGUI(false, "alloy_example_output.xml", null);
                    } else {
                        viz.loadXML("alloy_example_output.xml", true);
                    }
                }
            }
        }
        System.out.println("Dead features: " + deadSigs);
        return deadSigs;
    }

    public static List<String> findCoreSignatures(String fileName, A4Options options, A4Reporter rep) {



        Module world = CompUtil.parseEverything_fromFile(rep, null, fileName);

        //Use the first command in the Alloy file. To see how to parse Alloy models and how to access commands see, e.g.,
        // lines 57 and 65 in class ExampleUsingTheCompiler.


        SafeList<Sig> allsigs = world.getAllSigs();

        System.out.println("File: " + fileName);
        System.out.println("findCoreSignatures");
        System.out.println("allsigs: " + allsigs);


        List<String> coresigs = new ArrayList<>();


        for (int i = 0; i < allsigs.size(); ++i) {
            System.out.println("=====================================================================");
            for (Command command : world.getAllCommands()) {
                try {
                    //command = command.change(allsigs.get(i).not()); // Change command to check if the current feature can always be removed
                    command.change(allsigs.get(i).no().always());

                    System.out.println("command: : " + command.getScope(allsigs.get(i)));
                    A4Solution ans = TranslateAlloyToKodkod.execute_command(rep,world.getAllReachableSigs() , command, options);

                    System.out.println("Current checked signature: : " + allsigs.get(i));
                    System.out.println("ans.satisfiable(): " + ans.satisfiable());

                    System.out.println("ans.eval(allsigs.get(i)).size(): " + ans.eval(allsigs.get(i)).size());

                    if (ans.eval(allsigs.get(i)).size() > 0) { // ans.satisfiable() &&
                        coresigs.add(allsigs.get(i).toString());
                    }
                } catch (Exception e) {

                }
            }
        }
        System.out.println("=====================================================================");
        System.out.println("=====================================================================");
        System.out.println("=====================================================================");
        System.out.println("Core features: " + coresigs);
        return coresigs;







/*/








        // You may update the predicate a command cmd checks to expression e by using the returned Command of cmd.change(e).


        // To see how you can create formulas from signatures and other formulas see, e.g., line 90 in class ExampleUsingTheAPI.
        //Expr expr1 = A.some().and(atMost3.call(B, B));



        List<String> coreSigs = new ArrayList<>();



        for (int i = 0; i < allsigs.size(); ++i) {

            for (Command command : world.getAllCommands()) {


                command.change(allsigs.get(i).some()); // Change command to check if the current feature can always be removed

                A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);

                if (ans.eval(allsigs.get(i)).size() != 0){

                    coreSigs.add(allsigs.get(i).toString());
                    // You can query "ans" to find out the values of each set or
                    // type.
                    // This can be useful for debugging.
                    //
                    // You can also write the outcome to an XML file
                    ans.writeXML("alloy_example_output.xml");
                    //
                    // You can then visualize the XML file by calling this:
                    if (viz == null) {
                        viz = new VizGUI(false, "alloy_example_output.xml", null);
                    } else {
                        viz.loadXML("alloy_example_output.xml", true);
                    }
                }
            }
        }
        System.out.println("Core features: " + coreSigs);
        return coreSigs;/**/
    }

    /**
     * Computes for each user-defines signature a minimal scope for which the model
     * is still satisfiable. Note that the scopes will be independent, i.e., minimum
     * 0 for sig A and 0 for sig B does not mean that both can be 0 together.
     *
     * @param fileName
     * @param options
     * @param rep
     * @return map from signature names to minimum scopes
     */
    public static Map<String, Integer> findMinScope(String fileName, A4Options options, A4Reporter rep) {


        System.out.println("File: " + fileName);
        System.out.println("findMinScope");


        Module world = CompUtil.parseEverything_fromFile(rep, null, fileName);

        SafeList<Sig> allsigs = world.getAllSigs();

        System.out.println(allsigs);

        Map<String, Integer> minScope = new HashMap<>();

        for (int i = 0; i < allsigs.size(); ++i) {

            for (Command command : world.getAllCommands()) {

                int maxScope = getMaxScope(allsigs.get(i), command);

                for (int scope = maxScope; 0 <= scope; --scope) {

                    command = command.change(allsigs.get(i), false, scope);

                    try {
                        A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);

                        if (ans.hasConfigs()) {
                            minScope.put(allsigs.get(i).toString(), scope);
                        }
                    } catch (Exception e) {
                    }
                }


            }
        }
        System.out.println("Min Scope: " + minScope);


        return minScope;

    }

    /**
     * Computes the maximum scope for a signature in a command. This is either the
     * default of 4, the overall scope, or the specific scope for the signature in
     * the command.
     *
     * @param sig
     * @param cmd
     * @return
     */
    public static int getMaxScope(Sig sig, Command cmd) {
        int scope = 4; // Alloy's default
        if (cmd.overall != -1) {
            scope = cmd.overall;
        }
        CommandScope cmdScope = cmd.getScope(sig);
        if (cmdScope != null) {
            scope = cmdScope.endingScope;
        }
        return scope;
    }

}
