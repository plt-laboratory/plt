package hr.fer.ppj.lab1;

import hr.fer.ppj.lab1.enums.ActionType;
import hr.fer.ppj.lab1.helper.EpsilonNFA;
import hr.fer.ppj.lab1.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Class that performs lexical analysis of given program using definitions generated by GLA
 */
public class LA {

    /**
     * Constants
     */
    private final static String TEST_FILE_INPUT_PATH = "./src/res/in/minusLang.in";
    private final static String TEST_FILE_OUTPUT_PATH = "./src/res/out/LA_out.txt";

    /**
     *
     */
    private static String program;
    private static List<Regex> regexList;
    private static List<State> stateList;
    private static List<Identifier> identifierList;
    private static List<Rule> ruleList;
    private static List<EpsilonNFA> epsilonNFAList;
    private static int numberOfRows = 1;

    /**
     * Entry point
     */
    public static void main(String[] args) throws IOException {

        setupStdIO();

        try (Scanner scanner = new Scanner(System.in)) {

            readInputProgram(scanner);
            deserializeData();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        //tryOut();
        runLexer();
    }

    private static void tryOut() {
        EpsilonNFA epsilonNFA = epsilonNFAList.get(5);
                System.out.println(epsilonNFA.recognizes("\\( \\)"));
    }


    /**
     * Standard I/O redirection
     */
    private static void setupStdIO() throws IOException {
        System.setIn(new FileInputStream(new File(TEST_FILE_INPUT_PATH)));
        System.setOut(new PrintStream(new File(TEST_FILE_OUTPUT_PATH)));
    }


    /**
     * Method for reading program from standard input
     */
    private static void readInputProgram(Scanner scanner) {
        StringBuilder sb = new StringBuilder();

        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine());
        }

        program = sb.toString();
    }

    /**
     * Method for deserializing data generated by GLA
     */
    private static void deserializeData() throws Exception {

        try {

            File file = new File(GLA.SERIALIZATION_FILE_PATH);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

            regexList = (List<Regex>) ois.readObject();
            stateList = (List<State>) ois.readObject();
            identifierList = (List<Identifier>) ois.readObject();
            ruleList = (List<Rule>) ois.readObject();
            epsilonNFAList = (List<EpsilonNFA>) ois.readObject();

            fis.close();
            ois.close();

        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Method that starts the lexical analysis of given program
     */
    private static void runLexer() {

        State currentState = stateList.get(0);
        List<EpsilonNFA> positiveENFA = new ArrayList<>();

        int first = 0, last = 0;
        int end = program.length() - 1;

        while (last <= end) {

            boolean added = false;

            for (EpsilonNFA epsilonNFA : epsilonNFAList) {
                if (epsilonNFA.getRule().getState().equals(currentState) && epsilonNFA.recognizes(program.substring(first, last+1))) {
                    positiveENFA.add(epsilonNFA);
                    added = true;
                }
            }

            if (!positiveENFA.isEmpty() && !added) {
                EpsilonNFA nfa = positiveENFA.get(positiveENFA.size() - 1);
                positiveENFA.clear();
                List<Action> actions = nfa.getRule().getActionList();

                for (Action action : actions) {
                    if (action.getActionType().equals(ActionType.GO_BACK)) {
                        last = Integer.parseInt(action.getArgument());
                    }
                }

                for (Action action : actions) {
                    switch (action.getActionType()) {
                        case LEX_TOKEN:
                            System.out.println(action.getArgument() + " " + numberOfRows + " " + program.substring(first, last));
                            first = last;
                            break;
                        case MINUS:
                            first = last;
                            break;
                        case NEW_LINE:
                            numberOfRows += 1;
                            break;
                        case ENTER_STATE:
                            currentState = new State(action.getArgument());
                            break;
                    }

                }
            } else {
                last++;
            }

        }

    }

}
