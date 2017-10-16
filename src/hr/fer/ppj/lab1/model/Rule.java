package hr.fer.ppj.lab1.model;

import java.util.ArrayList;
import java.util.List;

public class Rule {

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

    private void parseRule() {
        String[] parts = rule.split("\n");

        state = new State(parts[0].substring(1).split(">")[0]);
        regex = new Regex(parts[0].substring(1).split(">")[1]);

        for (String part : parts) {
            if (part.equals("{") || part.equals("}")) {
                continue;
            }
            actionList.add(new Action(part));
        }
    }

}
