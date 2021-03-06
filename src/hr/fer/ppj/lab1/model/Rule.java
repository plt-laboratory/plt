package hr.fer.ppj.lab1.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The class represents a rule of the lexical analyzer generator
 */
public class Rule implements Serializable {

    private String rule;
    private State state;
    private Regex regex;
    private List<Action> actionList = new ArrayList<>();

    public Rule(String rule) {
        this.rule = rule;
        parseRule();
    }

    public State getState() {
        return state;
    }

    public Regex getRegex() {
        return regex;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    /**
     * The method parses rule's parts
     */
    private void parseRule() {

        String[] parts = rule.split("\n");

        int idx = 0;
        for (int i = 1, n = parts[0].length(); i < n; i++) {
            if (parts[0].charAt(i) == '>') {
                idx = i;
                break;
            }
        }

        state = new State(parts[0].substring(1, idx));
        regex = new Regex(parts[0].substring(idx + 1));

        for (String part : parts) {
            if (part.equals("{") || part.equals("}") || part.startsWith("<")) {
                continue;
            }
            actionList.add(new Action(part));
        }

    }

}
