package hr.fer.ppj.lab3;

import hr.fer.ppj.lab3.model.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static hr.fer.ppj.lab3.model.Const.*;

/**
 *
 */
public class SemantickiAnalizator {

    private static final String TEST_FILE_INPUT_PATH = "./src/hr/fer/ppj/lab2/res/out/SA_out.txt";
    private static final String TEST_FILE_OUTPUT_PATH = "./src/hr/fer/ppj/lab3/res/out/out.txt";

    private static List<Production> productions;
    private static List<String> input;
    private static List<TerminalSymbol> terminalSymbols;
    private static Element startingElement;
    private static CodeBlock startingCodeBlock;
    private static List<String> declarationIdentifiers = Arrays.asList(VOID,INT,CHAR,CONST);

    /**
     *
     */
    public static void main(String[] args) throws IOException {
        setupStdIO();
        readFromInput();
        fillProductions();

        /*for (Production production : productions) {
            System.out.println(production);
        }*/

        buildGeneratingTree();
        buildCodeBlocks();
        check(startingElement);
        //System.out.println("OK");
    }

    private static void buildCodeBlocks() {
        int lastLine = terminalSymbols.get(terminalSymbols.size()-1).getLine();
        startingCodeBlock = new CodeBlock(1,lastLine);

        int line = 1;
        List<String> currentLine = terminalSymbols.stream().filter(symbol->symbol.getLine()==line).map(terminalSymbol -> terminalSymbol.getValue()).collect(Collectors.toList());
        if(declarationIdentifiers.contains(currentLine.get(0))){
            if(currentLine.get(0).equals(VOID)){
                //funkcija
                Function function = analyzeFunction(currentLine,line);
                //pogledati je li funkcija vec deklararirana

                if(currentLine.get(currentLine.size()-1).equals(L_VIT_ZAGRADA)){
                    function.setDefinedAt(line);
                }
                
            }else{
                
            }
        }

    }

    private static Function analyzeFunction(List<String> currentLine, int line) {
        String returnType = currentLine.get(0);
        String name = currentLine.get(1);
        List<String> inputParameters = new LinkedList<>();
        List<String> inputParametersContent = currentLine.subList(3,currentLine.indexOf(D_ZAGRADA));

        int i=0;
        while(true){
            String parameter;
            if(inputParametersContent.contains(ZAREZ)) {
                parameter = parseType(inputParametersContent.subList(i, inputParametersContent.indexOf(ZAREZ)));
                inputParameters.add(parameter);
                i = inputParametersContent.indexOf(ZAREZ)+1;
                inputParametersContent = inputParametersContent.subList(i,inputParametersContent.size());
            }else{
                parameter = parseType(inputParametersContent);
                inputParameters.add(parameter);
                break;
            }
        }

        return new Function(name,inputParameters,returnType);
    }

    private static String parseType(List<String> content) {
        return "";
    }

    /**
     *
     */
    private static void setupStdIO() throws IOException {
        System.setIn(new FileInputStream(new File(TEST_FILE_INPUT_PATH)));
        System.setOut(new PrintStream(new File(TEST_FILE_OUTPUT_PATH)));
    }

    /**
     *
     */
    private static void readFromInput() {

        try (Scanner scanner = new Scanner(System.in)) {
            input = new LinkedList<>();

            while (scanner.hasNextLine()) {
                input.add(scanner.nextLine());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        terminalSymbols = new LinkedList<>();
        for(String line : input){
            line = line.trim();
            if(!line.startsWith("<")){
                terminalSymbols.add(new TerminalSymbol(line.trim().split(" ")));
            }
        }

    }

    /**
     *
     */
    private static void buildGeneratingTree() {
        int lineIdx = 0;
        startingElement = new Element(new NonterminalSymbol(input.get(lineIdx)), lineIdx);
        List<Element> newElements = new LinkedList<>();
        newElements.add(startingElement);

        while (!newElements.isEmpty()) {
            List<Element> newElementsTemp = new LinkedList<>();
            for (Element element : newElements) {
                List<Element> childrenElements = getChildrenElements(element);
                element.setChildrenElements(childrenElements);
                newElementsTemp.addAll(childrenElements);
            }
            newElements.clear();
            newElements.addAll(newElementsTemp);
        }

        //printGeneratingTree(startingElement,0);

    }

    /**
     *
     */
    private static List<Element> getChildrenElements(Element element) {
        int lineIdx = element.getLineIdx();
        int indentation = getIndentation(input.get(lineIdx));
        List<Element> childrenElements = new LinkedList<>();

        for (int i = lineIdx + 1; i < input.size(); i++) {
            String line = input.get(i);
            if (getIndentation(line) <= indentation) {
                break;
            }
            if (getIndentation(line) == indentation + 1) {
                String content = line.trim();
                if (content.startsWith("<")) {
                    childrenElements.add(new Element(new NonterminalSymbol(content), i));
                } else {
                    childrenElements.add(new Element(new TerminalSymbol(content.split(" ")), i));
                }
            }
        }
        return childrenElements;

    }

    /**
     *
     */
    private static int getIndentation(String line) {
        int i = 0;
        while (Character.isWhitespace(line.charAt(i))) {
            ++i;
        }
        return i;
    }

    /**
     *
     */
    private static void printGeneratingTree(Element element, int level) {
        System.out.println(getIndentation(level) + element.getSymbol().getName());
        List<Element> childrenElements = element.getChildrenElements();
        if (childrenElements != null) {
            if (!childrenElements.isEmpty()) {
                childrenElements.forEach(node -> printGeneratingTree(node, level + 1));
            }
        }
    }

    /**
     *
     */
    private static String getIndentation(int level) {
        String indentation = "";
        int i = 0;
        while (i < level) {
            indentation = indentation.concat(" ");
            ++i;
        }
        return indentation;
    }

    /**
     *
     */
    private static void fillProductions() {
        productions = new ArrayList<>();

        addNewProduction("<primarni_izraz>", "IDN");
        addNewProduction("<primarni_izraz>", "BROJ");
        addNewProduction("<primarni_izraz>", "ZNAK");
        addNewProduction("<primarni_izraz>", "NIZ_ZNAKOVA");
        addNewProduction("<primarni_izraz>", "L_ZAGRADA <izraz> D_ZAGRADA");

        addNewProduction("<postfiks_izraz>", "<primarni_izraz>");
        addNewProduction("<postfiks_izraz>", "<postfiks_izraz> L_UGL_ZAGRADA <izraz> D_UGL_ZAGRADA");
        addNewProduction("<postfiks_izraz>", "<postfiks_izraz> L_ZAGRADA D_ZAGRADA");
        addNewProduction("<postfiks_izraz>", "<postfiks_izraz> L_ZAGRADA <lista_argumenata> D_ZAGRADA");
        addNewProduction("<postfiks_izraz>", "<postfiks_izraz> OP_INC");
        addNewProduction("<postfiks_izraz>", "<postfiks_izraz> OP_DEC");

        addNewProduction("<lista_argumenata>", "<izraz_pridruzivanja>");
        addNewProduction("<lista_argumenata>", "<lista_argumenata> ZAREZ <izraz_pridruzivanja>");

        addNewProduction("<unarni_izraz>", "<postfiks_izraz>");
        addNewProduction("<unarni_izraz>", "OP_INC <unarni_izraz>");
        addNewProduction("<unarni_izraz>", "OP_DEC <unarni_izraz>");
        addNewProduction("<unarni_izraz>", "<unarni_operator> <cast_izraz>");

        addNewProduction("<cast_izraz>", "<unarni_izraz>");
        addNewProduction("<cast_izraz>", "L_ZAGRADA <ime_tipa> D_ZAGRADA <cast_izraz>");

        addNewProduction("<ime_tipa>", "<specifikator_tipa>");
        addNewProduction("<ime_tipa>", "KR_CONST <specifikator_tipa>");

        addNewProduction("<specifikator_tipa>", "KR_VOID");
        addNewProduction("<specifikator_tipa>", "KR_CHAR");
        addNewProduction("<specifikator_tipa>", "KR_INT");

        addNewProduction("<multiplikativni_izraz>", "<cast_izraz>");
        addNewProduction("<multiplikativni_izraz>", "<multiplikativni_izraz> OP_PUTA <cast_izraz>");
        addNewProduction("<multiplikativni_izraz>", "<multiplikativni_izraz> OP_DIJELI <cast_izraz>");
        addNewProduction("<multiplikativni_izraz>", "<multiplikativni_izraz> OP_MOD <cast_izraz>");

        addNewProduction("<aditivni_izraz>", "<multiplikativni_izraz>");
        addNewProduction("<aditivni_izraz>", "<aditivni_izraz> PLUS <multiplikativni_izraz>");
        addNewProduction("<aditivni_izraz>", "<aditivni_izraz> MINUS <multiplikativni_izraz>");

        addNewProduction("<odnosni_izraz>", "<aditivni_izraz>");
        addNewProduction("<odnosni_izraz>", "<odnosni_izraz> OP_LT <aditivni_izraz>");
        addNewProduction("<odnosni_izraz>", "<odnosni_izraz> OP_GT <aditivni_izraz>");
        addNewProduction("<odnosni_izraz>", "<odnosni_izraz> OP_LTE <aditivni_izraz>");
        addNewProduction("<odnosni_izraz>", "<odnosni_izraz> OP_GTE <aditivni_izraz>");

        addNewProduction("<jednakosni izraz>", "<odnosni_izraz>");
        addNewProduction("<jednakosni izraz>", "<jednakosni_izraz> OP_EQ <odnosni_izraz>");
        addNewProduction("<jednakosni izraz>", "<jednakosni_izraz> OP_NEQ <odnosni_izraz>");

        addNewProduction("<bin_i_izraz>", "<jednakosni_izraz>");
        addNewProduction("<bin_i_izraz>", "<bin_i_izraz> OP_BIN_I <jednakosni_izraz>");

        addNewProduction("<bin xili izraz>", "<bin_i_izraz>");
        addNewProduction("<bin xili izraz>", "<bin_xili_izraz> OP_BIN_XILI <bin_i_izraz>");

        addNewProduction("<bin ili izraz>", "<bin_xili_izraz>");
        addNewProduction("<bin ili izraz>", "<bin_ili_izraz> OP_BIN_ILI <bin_xili_izraz>");

        addNewProduction("<log i izraz>", "<bin_ili_izraz>");
        addNewProduction("<log i izraz>", "<log_i_izraz> OP_I <bin_ili_izraz>");

        addNewProduction("<log ili izraz>", "<log_i_izraz>");
        addNewProduction("<log ili izraz>", "<log_ili_izraz> OP_ILI <log_i_izraz>");

        addNewProduction("<izraz_pridruzivanja>", "<log_ili_izraz>");
        addNewProduction("<izraz_pridruzivanja>", "<postfiks_izraz> OP_PRIDRUZI <izraz_pridruzivanja>");

        addNewProduction("<izraz>", "<izraz_pridruzivanja>");
        addNewProduction("<izraz>", "<izraz> ZAREZ <izraz_pridruzivanja>");

        addNewProduction("<slozena naredba>", "L_VIT_ZAGRADA <lista_naredbi> D_VIT_ZAGRADA");
        addNewProduction("<slozena naredba>", "L_VIT_ZAGRADA <lista_deklaracija> <lista_naredbi> D_VIT_ZAGRADA");

        addNewProduction("<lista_naredbi>", "<naredba>");
        addNewProduction("<lista_naredbi>", "<lista_naredbi> <naredba>");

        addNewProduction("<izraz naredba>", "TOCKAZAREZ");
        addNewProduction("<izraz naredba>", "<izraz> TOCKAZAREZ");

        addNewProduction("<naredba_grananja>", "KR_IF L_ZAGRADA <izraz> D_ZAGRADA <naredba>");
        addNewProduction("<naredba_grananja>", "KR_IF L_ZAGRADA <izraz> D_ZAGRADA <naredba> KR_ELSE <naredba>");

        addNewProduction("<naredba petlje>", "KR_WHILE L_ZAGRADA <izraz> D_ZAGRADA <naredba>");
        addNewProduction("<naredba petlje>", "KR_FOR L_ZAGRADA <izraz_naredba> <izraz_naredba> D_ZAGRADA <naredba>");
        addNewProduction("<naredba petlje>", "KR_FOR L_ZAGRADA <izraz_naredba> <izraz_naredba> <izraz> D_ZAGRADA <naredba>");

        addNewProduction("<naredba skoka>", "KR_CONTINUE TOCKAZAREZ");
        addNewProduction("<naredba skoka>", "KR_BREAK TOCKAZAREZ");
        addNewProduction("<naredba skoka>", "KR_RETURN TOCKAZAREZ");
        addNewProduction("<naredba skoka>", "KR_RETURN <izraz> TOCKAZAREZ");

        addNewProduction("<prijevodna jedinica>", "<vanjska_deklaracija>");
        addNewProduction("<prijevodna jedinica>", "<prijevodna_jedinica> <vanjska_deklaracija>");

        addNewProduction("<definicija funkcije>", "<ime_tipa> IDN L_ZAGRADA KR_VOID D_ZAGRADA <slozena_naredba>");
        addNewProduction("<definicija funkcije>", "<ime_tipa> IDN L_ZAGRADA <lista_parametara> D_ZAGRADA <slozena_naredba>");

        addNewProduction("<lista parametara>", "<deklaracija_parametra>");
        addNewProduction("<lista parametara>", "<lista_parametara> ZAREZ <deklaracija_parametra>");

        addNewProduction("<deklaracija parametra>", "<ime_tipa> IDN");
        addNewProduction("<deklaracija parametra>", "<ime_tipa> IDN L_UGL_ZAGRADA D_UGL_ZAGRADA");

        addNewProduction("<lista deklaracija>", "<deklaracija>");
        addNewProduction("<lista deklaracija>", "<lista_deklaracija> <deklaracija>");

        addNewProduction("<deklaracija>", "<ime_tipa> <lista_init_deklaratora> TOCKAZAREZ");

        addNewProduction("<lista init deklaratora>", "<init_deklarator>");
        addNewProduction("<lista init deklaratora>", "<lista_init_deklaratora> ZAREZ <init_deklarator>");

        addNewProduction("<init deklarator>", "<izravni_deklarator>");
        addNewProduction("<init deklarator>", "<izravni_deklarator> OP_PRIDRUZI <inicijalizator>");

        addNewProduction("<izravni deklarator>", "IDN");
        addNewProduction("<izravni deklarator>", "IDN L_UGL_ZAGRADA BROJ D_UGL_ZAGRADA");
        addNewProduction("<izravni deklarator>", "IDN L_ZAGRADA KR_VOID D_ZAGRADA");
        addNewProduction("<izravni deklarator>", "IDN L_ZAGRADA <lista_parametara> D_ZAGRADA");
        addNewProduction("<izravni deklarator>", "IDN L_UGL_ZAGRADA BROJ D_UGL_ZAGRADA");

        addNewProduction("<inicijalizator>", "<izraz_pridruzivanja>");
        addNewProduction("<inicijalizator>", "L_VIT_ZAGRADA <lista_izraza_pridruzivanja> D_VIT_ZAGRADA");

        addNewProduction("<lista izraza pridruzivanja>", "<izraz_pridruzivanja>");
        addNewProduction("<lista izraza pridruzivanja>", "<lista_izraza_pridruzivanja> ZAREZ <izraz_pridruzivanja>");
    }

    /**
     *
     */
    private static void addNewProduction(String leftSide, String rightSides) {

        NonterminalSymbol leftSideOfProd = new NonterminalSymbol(leftSide);
        List<Symbol> rightSideOfProd = new ArrayList<>();

        for (String symbol : rightSides.split("\\s+")) {

            if (symbol.startsWith("<")) {
                rightSideOfProd.add(new NonterminalSymbol(symbol));
            } else {
                rightSideOfProd.add(new TerminalSymbol(symbol));
            }

        }

        productions.add(new Production(leftSideOfProd, rightSideOfProd));
    }

    /**
     *
     */
    private static void check(Element element) {

        NonterminalSymbol leftSide = new NonterminalSymbol(element.getSymbol().getName());
        List<Symbol> rightSide = new ArrayList<>();
        for (Element next : element.getChildrenElements()) {
            rightSide.add(next.getSymbol());
        }

        Production nextProduction = new Production(leftSide, rightSide);
        Integer productionIndex = productions.indexOf(nextProduction);

        if (productionIndex == -1) {
            System.out.println(nextProduction);
            System.exit(0);
        }

        performActions(productionIndex, element);

    }

    /**
     *
     */
    private static void performActions(Integer productionIndex, Element element) {
        NonterminalSymbol leftSide = (NonterminalSymbol) element.getSymbol();
        List<Element> rightSide = element.getChildrenElements();

        switch (productionIndex) {

            //<primarni_izraz>
            case 0:
                // provjerit je li ime deklarirano
                setTypeAndL(leftSide, rightSide.get(0));
                break;

            case 1:
                setTypeAndL(leftSide, INT, ZERO);
                Integer intValue = Integer.valueOf(((TerminalSymbol) rightSide.get(0).getSymbol()).getName().split("\\s+")[2]);
                if (intValue <= -2147483648 || intValue <= 2147483647) {
                    semanticAnalysisFailure(null);
                }
                break;

            case 2:
                setTypeAndL(leftSide, CHAR, ZERO);
                // provjera ZNAK dovrsit
                char strValue = ((TerminalSymbol) rightSide.get(0).getSymbol()).getName().split("\\s+")[2].charAt(0);
                if (strValue == '\"' || strValue == '"' || strValue == '\t' || strValue == '\n' || strValue == '\0' || strValue == '\\' || ((int) strValue >= 0 && (int) strValue <= 127)) {
                }
                break;

            case 3:
                setTypeAndL(leftSide, NIZ_CONST_CHAR, ZERO);
                break;

            case 4:
                check(rightSide.get(1));
                setTypeAndL(leftSide, rightSide.get(1));
                break;

            case 5:
                check(rightSide.get(0));
                setTypeAndL(leftSide, rightSide.get(0));
                break;

            case 6:
                check(rightSide.get(0));
                // <postfiks_izraz>.tip = niz(X)
                check(rightSide.get(2));
                // <izraz>.tip ∼ int
                // tip ← X
                // l-izraz ← X = const(T)
                break;

            case 7:
                check(rightSide.get(0));
                // <postfiks_izraz>.tip = funkcija(void → pov)
                leftSide.setL_expression(ZERO);
                break;

            case 8:
                check(rightSide.get(0));
                check(rightSide.get(0));
                // <postfiks_izraz>.tip = funkcija(params → pov) i redom po elementima arg-tip iz <lista_argumenata>.tipovi i param-tip iz params vrijedi arg-tip ∼ param-tip
                leftSide.setL_expression(ZERO);
                break;

            case 9:
                check(rightSide.get(0));
                ((NonterminalSymbol) rightSide.get(0).getSymbol()).setL_expression(ONE);
                if (!((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                setTypeAndL(leftSide, INT, ZERO);
                break;

            case 10:
                check(rightSide.get(0));
                leftSide.getTypes().addAll(((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes());
                break;

            case 11:
                check(rightSide.get(0));
                check(rightSide.get(2));
                leftSide.getTypes().addAll(((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes());
                leftSide.getTypes().addAll(((NonterminalSymbol) rightSide.get(2).getSymbol()).getTypes());
                break;

            case 12:
                check(rightSide.get(0));
                setTypeAndL(leftSide, rightSide.get(0));
                break;

            case 13:
            case 14:
                check(rightSide.get(1));
                ((NonterminalSymbol) rightSide.get(1).getSymbol()).setL_expression(1);
                if (!((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                setTypeAndL(leftSide, INT, ZERO);
                break;

            case 15:
                check(rightSide.get(1));
                if (!((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                setTypeAndL(leftSide, INT, ZERO);
                break;

            case 16:
                check(rightSide.get(0));
                setTypeAndL(leftSide, rightSide.get(0));
                break;

            case 17:
                check(rightSide.get(1));
                check(rightSide.get(3));
                leftSide.getTypes().addAll(((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes());
                leftSide.setL_expression(0);
                // <cast_izraz>.tip se moˇ ze pretvoriti u <ime_tipa>.tip po poglavlju 4.3.1
                break;

            case 18:
                check(rightSide.get(0));
                leftSide.getTypes().addAll(((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes());
                break;

            case 19:
                check(rightSide.get(1));
                if (((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes().equals(VOID)) {
                    semanticAnalysisFailure(null);
                }
                break;

            case 20:
                leftSide.getTypes().add(VOID);
                break;

            case 21:
                leftSide.getTypes().add(CHAR);
                break;

            case 22:
                leftSide.getTypes().add(INT);
                break;

            case 23:
            case 27:
            case 30:
            case 32:
            case 36:
            case 39:
            case 41:
            case 43:
            case 45:
            case 47:
            case 49:
            case 51:
                check(rightSide.get(0));
                setTypeAndL(leftSide, rightSide.get(0));
                break;

            case 24:
            case 25:
            case 26:
                check(rightSide.get(0));
                if (!((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                check(rightSide.get(2));
                if (!((NonterminalSymbol) rightSide.get(3).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(2).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                setTypeAndL(leftSide, INT, ZERO);
                break;

            case 28:
            case 29:
            case 31:
            case 34:
            case 33:
            case 35:
            case 37:
            case 38:
            case 40:
            case 42:
            case 44:
            case 46:
            case 48:
                check(rightSide.get(0));
                if (!((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                check(rightSide.get(2));
                if (!((NonterminalSymbol) rightSide.get(2).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(2).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                setTypeAndL(leftSide, INT, ZERO);
                break;

            case 50:
                check(rightSide.get(0));
                ((NonterminalSymbol) rightSide.get(0).getSymbol()).setL_expression(1);
                check(rightSide.get(2));
                // <izraz_pridruzivanja>.tip ∼ <postfiks_izraz>.tip
                leftSide.getTypes().addAll(((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes());
                leftSide.setL_expression(0);
                break;

            case 52:
                check(rightSide.get(0));
                check(rightSide.get(2));
                leftSide.getTypes().addAll(((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes());
                leftSide.setL_expression(0);
                break;

            case 53:
                check(rightSide.get(1));
                break;

            case 54:
                check(rightSide.get(1));
                check(rightSide.get(2));
                break;

            case 55:
                check(rightSide.get(0));
                break;

            case 56:
                check(rightSide.get(0));
                check(rightSide.get(1));
                break;

            case 57:
                leftSide.getTypes().add(INT);
                break;

            case 58:
                leftSide.getTypes().addAll(((NonterminalSymbol) rightSide.get(0).getSymbol()).getTypes());
                check(rightSide.get(0));
                break;

            case 59:
                check(rightSide.get(1));
                if (!((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                check(rightSide.get(3));
                break;

            case 60:
                check(rightSide.get(1));
                if (!((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                check(rightSide.get(3));
                check(rightSide.get(5));
                break;


            case 61:
                check(rightSide.get(2));
                if (!((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(1).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                check(rightSide.get(4));
                break;

            case 62:
                check(rightSide.get(2));
                check(rightSide.get(3));
                if (!((NonterminalSymbol) rightSide.get(3).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(3).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                check(rightSide.get(5));
                break;

            case 63:
                check(rightSide.get(2));
                check(rightSide.get(3));
                if (!((NonterminalSymbol) rightSide.get(3).getSymbol()).getTypes().get(0).equals(CHAR) && !((NonterminalSymbol) rightSide.get(3).getSymbol()).getTypes().get(0).equals(INT)) {
                    semanticAnalysisFailure(null);
                }
                check(rightSide.get(4));
                check(rightSide.get(6));
                break;

            case 64:
                //  naredba se nalazi unutar petlje ili unutar bloka koji je ugnijezden u petlji
                break;

            case 65:
                // naredba se nalazi unutar funkcije tipa funkcija(params → void)
                break;

            case 66:
                check(rightSide.get(1));
                // naredba se nalazi unutar funkcije tipa funkcija(params → pov) i vrijedi <izraz>.tip ∼ pov
                break;

            case 67:
                check(rightSide.get(0));
                break;

            case 68:
                check(rightSide.get(0));
                check(rightSide.get(1));
                break;

            case 69:
                break;

            //<izravni_deklarator>
            case 84:
                break;
            case 85:
                break;
            case 86:
                break;
            case 87:
                break;

            //<inicijalizator>
            case 88:
                check(rightSide.get(0));
                String content = generates(rightSide.get(0));
                if(!content.equals("")){
                    leftSide.setNumOfElements(content.length()+1);
                    List<String> types = leftSide.getTypes();
                    for(int i =0;i<leftSide.getNumOfElements();i++){
                        types.add(CHAR);
                    }
                }else{
                    leftSide.getTypes().addAll(((NonterminalSymbol)rightSide.get(0).getSymbol()).getTypes());
                }
                break;

            case 89:
                check(rightSide.get(1));
                leftSide.setNumOfElements(getNonTerminalSymbol(rightSide.get(1)).getNumOfElements());
                leftSide.getTypes().addAll(getNonTerminalSymbol(rightSide.get(1)).getTypes());
                break;

            //<lista_izraza_pridruzivanja>
            case 90:
                check(rightSide.get(0));
                List<String> types90 = leftSide.getTypes();
                types90.addAll(getNonTerminalSymbol(rightSide.get(0)).getTypes());
                leftSide.setNumOfElements(1);
                break;

            case 91:
                check(rightSide.get(0));
                check(rightSide.get(2));

                NonterminalSymbol first = getNonTerminalSymbol(rightSide.get(0));
                NonterminalSymbol second = getNonTerminalSymbol(rightSide.get(2));

                List<String> types91 = leftSide.getTypes();
                types91.addAll(first.getTypes());
                types91.addAll(second.getTypes());

                leftSide.setNumOfElements(first.getNumOfElements()+1);
                break;
        }

    }

    private static NonterminalSymbol getNonTerminalSymbol(Element element){
        return (NonterminalSymbol)element.getSymbol();
    }

    private static String generates(Element element) {
        String uniform_symbol = "NIZ_ZNAKOVA";
        Element temp = element;
        while(true){
            List<Element> childrenElements = temp.getChildrenElements();
            if(childrenElements.size()!=1){
                break;
            }
            if(((TerminalSymbol)childrenElements.get(0).getSymbol()).getValue().equals(uniform_symbol)){
                return childrenElements.get(0).getSymbol().getName().split(" ")[2];
            }else{
                temp = childrenElements.get(0);
            }
        }
        return "";
    }

    /**
     *
     */
    private static void setTypeAndL(NonterminalSymbol leftSide, Element rightSide) {
        leftSide.setTypes(((NonterminalSymbol) rightSide.getSymbol()).getTypes());
        leftSide.setL_expression(((NonterminalSymbol) rightSide.getSymbol()).getL_expression());
    }

    /**
     *
     */
    private static void setTypeAndL(NonterminalSymbol leftSide, String type, int l_expr) {
        leftSide.getTypes().add(type);
        leftSide.setL_expression(l_expr);
    }

    /**
     *
     */
    private static void semanticAnalysisFailure(Production nextProduction) {
        System.out.println(nextProduction);
        System.exit(0);
    }

}
