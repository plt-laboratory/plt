package hr.fer.ppj.lab4.model;

import java.util.LinkedList;
import java.util.List;

public class NonterminalSymbol extends Symbol {

    private String type;
    private String ntype; //nasljedno svojstvo tip
    private String nameProperty;
    private List<String> types;
    private List<String> names;
    private int l_expression;
    private int numOfElements;
    private String Value="";
    private CodeBlock codeBlock; //nasljedno svojstvo
    private boolean unarPush = false;
    private boolean logPush = false;

    public boolean isUnarPush() {
        return unarPush;
    }

    public void setUnarPush(boolean unarPush) {
        this.unarPush = unarPush;
    }

    public NonterminalSymbol(String name) {
        this.name = name;
        types = new LinkedList<>();
        names = new LinkedList<>();

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNtype() {
        return ntype;
    }

    public void setNtype(String ntype) {
        this.ntype = ntype;
    }

    public String getNameProperty() {
        return nameProperty;
    }

    public void setNameProperty(String nameProperty) {
        this.nameProperty = nameProperty;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public int getL_expression() {
        return l_expression;
    }

    public void setL_expression(int l_expression) {
        this.l_expression = l_expression;
    }

    public int getNumOfElements() {
        return numOfElements;
    }

    public void setNumOfElements(int numOfElements) {
        this.numOfElements = numOfElements;
    }

    public CodeBlock getCodeBlock() {
        return codeBlock;
    }

    public void setCodeBlock(CodeBlock codeBlock) {
        this.codeBlock = codeBlock;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }


    public boolean isLogPush() {
        return logPush;
    }

    public void setLogPush(boolean logPush) {
        this.logPush = logPush;
    }

    @Override
    public String toString() {
        return name;
    }

}
