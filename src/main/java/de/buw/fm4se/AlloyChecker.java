package de.buw.fm4se;

import java.beans.Expression;
import java.security.Signature;
import java.sql.Savepoint;
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
        SafeList<Sig> allsigs = world.getAllSigs();
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

        ConstList<Sig> allReachableSigs = world.getAllReachableUserDefinedSigs(); // if this function is used to get the signatures, the dreadburry tests passes as well
        // if world.getAllSigs() is used, the dreadburry test fails as the only one because it has 12 instead of 14 signatures


        System.out.println("File: " + fileName);
        System.out.println("findCoreSignatures");
        System.out.println("allReachableSigs: " + allReachableSigs.size());


        List<String> coresigs = new ArrayList<>();


        for (int i = 0; i < allReachableSigs.size(); ++i) {
            for (Command command : world.getAllCommands()) {

                command = command.change(allReachableSigs.get(i).no()); // no instance of the current feature is wanted

                A4Solution ans = TranslateAlloyToKodkod.execute_command(rep,world.getAllReachableSigs() , command, options);

                if (!ans.satisfiable()){
                    // the current feature is a core feature
                    coresigs.add(allReachableSigs.get(i).toString());
                }
            }
        }
        System.out.println("Core features: " + coresigs);
        return coresigs;
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

        Map<String, Integer> minScope = new HashMap<>(); // hashmap

        for (int i = 0; i < allsigs.size(); ++i) {

            for (Command command : world.getAllCommands()) {

                int maxScope = getMaxScope(allsigs.get(i), command);

                for (int scope = maxScope; 0 <= scope; --scope) { // we check from the max scope to the min scope

                    command = command.change(allsigs.get(i), false, scope); // update command 

                    try {
                        A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);

                        // if we have configurations, we update the min scope value
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
