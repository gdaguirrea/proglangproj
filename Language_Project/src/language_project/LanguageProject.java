package language_project;

import java.util.*;
import UI.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lex.Token;
import lex.Tree;
import lex.Tree.*;

public class LanguageProject {

    public static LanguageUI gui;
    static String fileName = "";

    public static boolean startSim;
    public static String initProgram;
    static Tree<Token> sTree;
    static ArrayList<Token> myTokens;
    static Iterator<Token> iToken;
    static Token currentToken;

    public static void initializeVariables() {
        startSim = false;
        sTree = null;
        myTokens = null;
        iToken = null;
        currentToken = null;
    }

    public static void main(String[] args) {
        gui = new LanguageUI();

        while (true) {
            runProgram();
        }
    }

    public static void runProgram() {
        initializeVariables();
        while (!startSim) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(LanguageProject.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String text = gui.getProgram();
        ArrayList<String> lines = preprocessor(text);
        ArrayList<LexLine> lexLines = lexicalAnalyser(lines);

        initProgram = gui.getProgram();
        String Preprocessor = preprocessorShow(initProgram);
        gui.setPreprocessorText(Preprocessor);
        runLex(Preprocessor);
    }

    public static void runLex(String initProgram) {
        String file = initProgram;
        myTokens = lex(file);

        System.out.println(file);
        for (Token tok : myTokens) {
            System.out.println(tok);
        }

        makeTree();
        printTree(sTree.root, 0, 1);
        makeUITree(sTree.root);
    }

    public static void saveFile() {
        fileName = pickFileName("./");
        if (fileName == "") {
            fileName = pickFileName("./");
        }

        String text = gui.getProgram();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(text);
            writer.close();
        } catch (Exception e) {

        }

    }

    public static void openFile(String base) {
        File file = new File(pickFileName(base));

        String text = "";

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
            scanner.useDelimiter("\\Z");
            text = scanner.next();
        } catch (Exception e) {

        }

        gui.setText(text);
    }

    public static String pickFileName(String base) {
        fileName = "";
        fileName = gui.filePicker(base);
        System.out.println(fileName);

        return fileName;
    }

    public static ArrayList<String> preprocessor(String text) {
        String temp = "";
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != (char) 13) {
                temp += text.charAt(i);
            }
        }
        text = temp;

        String[] l = text.split("\n");
        ArrayList<String> lines = new ArrayList<>();

        for (int i = 0; i < l.length; i++) {
            lines.add(l[i]);
        }

        for (int i = 0; i < lines.size(); i++) {
            lines.set(i, lines.get(i).replaceAll("\\s+", " "));
        }

        for (int i = 0; i < lines.size(); i++) {
            lines.set(i, lines.get(i).trim());
        }

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).equals(null) || lines.get(i).equals("")) {
                lines.remove(i);
            }
        }

        return lines;
    }

    static class LexPair {

        public String type;
        public String token;

        LexPair(String type, String token) {
            this.type = type;
            this.token = token;
        }

        @Override
        public String toString() {
            return "<" + this.type + "," + this.token + ">";
        }
    }

    static class LexLine {

        public ArrayList<LexPair> tokens;

        LexLine(ArrayList<LexPair> tokens) {
            this.tokens = tokens;
        }

        @Override
        public String toString() {
            String s = "";
            for (int i = 0; i < this.tokens.size(); i++) {
                s.concat(this.tokens.get(i).toString());
            }
            return s;
        }
    }

    public static ArrayList<LexLine> lexicalAnalyser(ArrayList<String> lines) {

        ArrayList<LexLine> lexLines = new ArrayList();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int iword = 0;
            int fword = 0;

            ArrayList<LexPair> lexic = new ArrayList();

            while (iword < line.length()) {

                while (iword < line.length()) {
                    char iw = line.charAt(iword);
                    if (isOperator(iw)) {
                        lexic.add(new LexPair("operator", String.valueOf(iw)));
                        iword++;
                    } else if (isSymbol(iw)) {
                        lexic.add(new LexPair("symbol", String.valueOf(iw)));
                        iword++;
                    } else if (iw == ' ') {
                        iword++;
                    } else {
                        break;
                    }
                }
                if (iword >= line.length()) {
                    break;
                }
                fword = iword;

                while (fword < line.length() && !isOperator(line.charAt(fword)) && !isSymbol(line.charAt(fword)) && line.charAt(fword) != ' ') {
                    System.out.println(line.charAt(fword));
                    fword++;
                }
                String word = line.substring(iword, fword);
                String result = wordAnalyzer(word);

                if (result.equals("error")) {
                    System.out.println("ERROR!!!!!!! " + word);
                } else {
                    lexic.add(new LexPair(result, word));
                    System.out.println("GOOD");
                }
                iword = fword;
            }
            System.out.println(lexic);
            lexLines.add(new LexLine(lexic));
        }

        return lexLines;
    }

    public static String wordAnalyzer(String word) {
        String[] keywords = new String[]{"int", "while", "if"};

        for (int i = 0; i < keywords.length; i++) {
            if (word.equals(keywords[i])) {
                return "keyword";
            }
        }

        if (word.matches("\\d+")) {
            return "number";
        } else if (word.matches("\\w+")) {
            return "identifier";
        } else {
            return "error";
        }
    }

    public static boolean isOperator(char c) {
        int[] operators = new int[]{33, 37, 38, 42, 43, 45, 47, 60, 61, 62, 124};

        for (int o : operators) {
            if ((char) o == c) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSymbol(char c) {
        int[] symbols = new int[]{35, 40, 41, 44, 46, 59, 91, 93, 123, 125};

        for (int o : symbols) {
            if ((char) o == c) {
                return true;
            }
        }
        return false;
    }

    public static String preprocessorShow(String initProgram) {
        String inputtxt = initProgram;
        String outputtxt = "";
        String temptxt = "";
        List<String> lines;
        int size;

        inputtxt = inputtxt.replaceAll("#(\t|\r|.|\n)*?#", "");
        inputtxt = inputtxt.replaceAll(";", ";\n");
        String[] linesArray = inputtxt.split("[\n]");
        lines = new LinkedList<String>(Arrays.asList(linesArray));;

        size = lines.size();

        for (int i = 0; i < linesArray.length; i++) {
            System.out.println(linesArray[i] + i);
        }

        for (int i = 0; i < lines.size(); i++) {
            temptxt = lines.get(i);
            temptxt = temptxt.replaceAll("\t", " ");
            temptxt = temptxt.trim();
            for (int j = 1; j < temptxt.length() - 1; j++) {
                if ((temptxt.charAt(j - 1) == '/' && temptxt.charAt(j) == '/')) {
                    temptxt = temptxt.substring(0, j - 1);
                    break;
                }
            }
            lines.set(i, temptxt);
        }

        int count = 0;
        boolean cond = false;

        count = 0;

        while (count != lines.size()) {
            if (lines.get(count).equals("")) {
                lines.remove(count);
                count = 0;
            } else {
                count++;
            }
        }

        size = lines.size();

        for (int i = 0; i < size; i++) {
            temptxt = lines.get(i);

            for (int j = 0; j < temptxt.length(); j++) {
                if ((temptxt.charAt(j) == ' ' && temptxt.charAt(j + 1) == ' ')) {
                    temptxt = temptxt.substring(0, j) + temptxt.substring(j + 1);
                    j = 0;
                }
            }

            if (!(temptxt.equals(""))) {
                outputtxt = outputtxt + temptxt + "\n";
            }
        }

        return outputtxt;
    }

    public static ArrayList<Token> lex(String input) {
        int line = 1;
        ArrayList<Token> tokens = new ArrayList<Token>();

        StringBuffer tokenPatternsBuffer = new StringBuffer();
        for (Token.TokenType tokenType : Token.TokenType.values()) {
            tokenPatternsBuffer.append(String.format("|(?<%s>%s)", tokenType.name(), tokenType.pattern));

        }
        Pattern tokenPatterns = Pattern.compile(tokenPatternsBuffer.substring(1));

        Matcher matcher = tokenPatterns.matcher(input);
        while (matcher.find()) {
            for (Token.TokenType tk : Token.TokenType.values()) {
                if (matcher.group(Token.TokenType.SKIP.toString()) != null) {
                    continue;
                } else if (matcher.group(Token.TokenType.LSKIP.toString()) != null) {
                    if (tk.name() == Token.TokenType.LSKIP.toString()) {
                        line++;
                    }

                    continue;
                } else if (matcher.group(tk.name()) != null) {
                    tokens.add(new Token(tk, matcher.group(tk.name()), line));
                    continue;
                }
            }
        }
        return tokens;
    }

    static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }

    public static void makeTree() {
        iToken = myTokens.iterator();

        sTree = new Tree(program());

        System.out.println(currentToken);

        if (currentToken.type == Token.TokenType.EOF) {
            System.out.println("Parse OK");
        } else {
            gui.setErrorText("ERROR at Parsing Tree.");
        }
    }

    public static Node<Token> program() {
        Node<Token> node = getNode(Node.NodeType.programNode);
        gui.setRoot(node.nodeType.toString());

        currentToken = iToken.next();
        if (currentToken.type == Token.TokenType.BEGIN) {
            currentToken = iToken.next();
        } else {
            System.out.printf("ERROR. Expected 'Begin Token' in line %d\n", currentToken.line);
            gui.setErrorText("ERROR. Expected 'Begin Token ' at line " + currentToken.line + "\n");
            runProgram();
        }
        node.children.add(block());

        if (currentToken.type == Token.TokenType.RETURN) {
            currentToken = iToken.next();
            return node;
        } else {
            System.out.printf("ERROR. Expected 'Return Token' in line %d\n", currentToken.line);
            gui.setErrorText("ERROR. Expected 'Return Token ' at line " + currentToken.line + "\n");
            return null;
        }
    }

    public static Node<Token> block() {
        Node<Token> node = getNode(Node.NodeType.blockNode);
        if (currentToken.type == Token.TokenType.LBRA) {
            currentToken = iToken.next();
            node.children.add(stats());

            if (currentToken.type == Token.TokenType.RBRA) {
                currentToken = iToken.next();
                return node;
            } else {
                System.out.printf("ERROR. Expected '} Token' in line %d\n", currentToken.line);
                gui.setErrorText("ERROR. Expected '} Token ' at line " + currentToken.line + "\n");

                return null;
            }
        } else {
            System.out.printf("ERROR. Expected '{ Token' in line %d\n", currentToken.line);
            gui.setErrorText("ERROR. Expected '{ Token ' at line " + currentToken.line + "\n");
            runProgram();
        }
        return null;
    }

    public static Node<Token> stats() {
        Node<Token> node = getNode(Node.NodeType.statsNode);

        node.children.add(stat());

        node.children.add(mStat());

        return node;
    }

    public static Node<Token> stat() {
        Node<Token> node = getNode(Node.NodeType.statNode);

        switch (currentToken.type) {
            case LBRA:
                node.children.add(block());
                break;
            case IF:
                node.children.add(ifF());
                break;
            case WHILE:
                node.children.add(whileF());
                break;
            case ID:
                node.children.add(assign());
                break;
            case INTEGER:
                node.children.add(declaration());
                break;
            default:
                break;
        }

        return node;
    }

    public static Node<Token> mStat() {
        Node<Token> node = getNode(Node.NodeType.mStatNode);

        node.children.add(stat());
        if (node.children.get(0).children.isEmpty() && node.children.get(0).data.isEmpty()) {
            return node;
        }
        node.children.add(mStat());
        return node;
    }

    public static Node<Token> ifF() {
        Node node = getNode(Node.NodeType.ifNode);

        if (currentToken.type == Token.TokenType.IF) {
            currentToken = iToken.next();
            if (currentToken.type == Token.TokenType.LPAREN) {
                currentToken = iToken.next();
            } else {
                System.out.printf("ERROR. Expected '( token' at line %d", currentToken.line);
                gui.setErrorText("ERROR. Expected '( Token ' at line " + currentToken.line + "\n");
                runProgram();
            }

            node.children.add(expr());
            node.children.add(lOperator());
            node.children.add(expr());

            if (currentToken.type == Token.TokenType.RPAREN) {
                currentToken = iToken.next();
            } else {
                System.out.printf("ERROR. Expected ') token' at line %d", currentToken.line);
                gui.setErrorText("ERROR. Expected ') Token ' at line " + currentToken.line + "\n");
                runProgram();

            }
            node.children.add(block());
            return node;
        }
        return null;
    }

    public static Node<Token> whileF() {
        Node node = getNode(Node.NodeType.whileNode);

        if (currentToken.type == Token.TokenType.WHILE) {
            currentToken = iToken.next();

            if (currentToken.type == Token.TokenType.LPAREN) {
                currentToken = iToken.next();
            } else {
                System.out.printf("ERROR. Expected '( token' at line %d", currentToken.line);
                gui.setErrorText("ERROR. Expected '( Token ' at line " + currentToken.line + "\n");
                runProgram();

            }

            node.children.add(expr());
            node.children.add(lOperator());
            node.children.add(expr());

            if (currentToken.type == Token.TokenType.RPAREN) {
                currentToken = iToken.next();
            } else {
                System.out.printf("ERROR. Expected ') token' at line %d", currentToken.line);
                gui.setErrorText("ERROR. Expected ') Token ' at line " + currentToken.line + "\n");
                runProgram();

            }
            node.children.add(block());
            return node;
        }
        return null;
    }

    public static Node<Token> assign() {
        Node<Token> node = getNode(Node.NodeType.assignNode);

        if (currentToken.type == Token.TokenType.ID) {
            node.data.add(currentToken);
            currentToken = iToken.next();

            if (currentToken.type == Token.TokenType.ASSIGN) {
                currentToken = iToken.next();
            } else {
                System.out.printf("ERROR. Expected '= Token' at line %d", currentToken.line);
                gui.setErrorText("ERROR. Expected '= Token ' at line " + currentToken.line + "\n");
                runProgram();
            }

            node.children.add(expr());

            if (currentToken.type == Token.TokenType.SEMICOLON) {
                currentToken = iToken.next();
            } else {
                System.out.printf("ERROR. Expected '; Token' at line %d", currentToken.line);
                gui.setErrorText("ERROR. Expected '; Token ' at line " + currentToken.line + "\n");
                runProgram();
            }
            return node;
        } else {
            System.out.printf("ERROR. Expected 'ID Token' at line %d", currentToken.line);
            gui.setErrorText("ERROR. Expected 'ID Token ' at line " + currentToken.line + "\n");
            runProgram();
        }
        return null;
    }

    public static Node<Token> declaration() {
        Node<Token> node = getNode(Node.NodeType.declarationNode);

        if (currentToken.type == Token.TokenType.INTEGER) {
            currentToken = iToken.next();

            if (currentToken.type == Token.TokenType.ID) {
                node.data.add(currentToken);
                currentToken = iToken.next();
            } else {
                System.out.printf("ERROR. Expected 'ID Token' at line %d", currentToken.line);
                gui.setErrorText("ERROR. Expected 'ID Token ' at line " + currentToken.line + "\n");
                runProgram();

            }

            if (currentToken.type == Token.TokenType.SEMICOLON) {
                currentToken = iToken.next();
            } else {
                System.out.printf("ERROR. Expected '; Token' at line %d", currentToken.line);
                gui.setErrorText("ERROR. Expected '; Token ' at line " + currentToken.line + "\n");
                runProgram();
            }
            return node;
        } else {
            System.out.printf("ERROR. Expected 'INTEGER Token' at line %d", currentToken.line);
            gui.setErrorText("ERROR. Expected 'INTEGER Token ' at line " + currentToken.line + "\n");
            runProgram();
        }
        return null;
    }

    public static Node<Token> expr() {
        Node<Token> node = getNode(Node.NodeType.exprNode);

        node.children.add(t());

        if (currentToken.type == Token.TokenType.MULT) {
            currentToken = iToken.next();
            node.children.add(expr());
        } else if (currentToken.type == Token.TokenType.DIV) {
            currentToken = iToken.next();
            node.children.add(expr());
        }

        return node;
    }

    public static Node<Token> lOperator() {

        Node<Token> node = getNode(Node.NodeType.loNode);

        switch (currentToken.type) {
            case EQ:
                node.data.add(currentToken);
                currentToken = iToken.next();
                break;
            case DIFF:
                node.data.add(currentToken);
                currentToken = iToken.next();
                break;
            case GEQT:
                node.data.add(currentToken);
                currentToken = iToken.next();
                break;
            case GT:
                node.data.add(currentToken);
                currentToken = iToken.next();
                break;
            case LEQT:
                node.data.add(currentToken);
                currentToken = iToken.next();
                break;
            case LT:
                node.data.add(currentToken);
                currentToken = iToken.next();
                break;
        }
        return node;
    }

    public static Node<Token> t() {
        Node<Token> node = getNode(Node.NodeType.tNode);

        node.children.add(f());

        if (currentToken.type == Token.TokenType.ADD) {
            currentToken = iToken.next();
            node.children.add(t());

        } else if (currentToken.type == Token.TokenType.SUB) {
            currentToken = iToken.next();
            node.children.add(t());
        }

        return node;
    }

    public static Node<Token> f() {
        Node<Token> node = getNode(Node.NodeType.fNode);

        if (currentToken.type == Token.TokenType.LPAREN) {
            currentToken = iToken.next();

            node.children.add(expr());

            if (currentToken.type == Token.TokenType.RPAREN) {
                currentToken = iToken.next();
            } else {
                System.out.printf("ERROR. Expected ') Token' at line %d", currentToken.line);
                gui.setErrorText("ERROR. Expected ') Token ' at line " + currentToken.line + "\n");
                runProgram();

            }
        } else if (currentToken.type == Token.TokenType.ID) {
            node.data.add(currentToken);
            currentToken = iToken.next();
        } else if (currentToken.type == Token.TokenType.NUMBER) {
            node.data.add(currentToken);
            currentToken = iToken.next();
        }

        return node;
    }

    public static Node<Token> getNode(Node.NodeType nodeType) {
        return new Node<Token>(nodeType);
    }

    public static void printTree(Node<Token> node, int t, int s) {

        for (int i = 0; i <= t; i++) {
            System.out.print("\t");
        }

        System.out.println(node.nodeType);

        t += s;

        if (!node.data.isEmpty()) {
            for (Token tk : node.data) {
                for (int i = 0; i <= t; i++) {
                    System.out.print("\t");
                }
                System.out.println(tk);
            }
        }

        if (!node.children.isEmpty()) {
            for (Node<Token> nd : node.children) {
                printTree(nd, t, 1);
            }
        }
        for (int i = 0; i < t; i++) {
            System.out.print("\t");
        }
        System.out.println(node.nodeType);
    }

    public static void makeUITree(Node<Token> node) {
        if (!node.data.isEmpty()) {
            for (Token tk : node.data) {
                gui.addToNode(node.nodeType.toString(), tk.data.toString());
            }
        }

        if (!node.children.isEmpty()) {
            for (Node<Token> nd : node.children) {
                gui.addToNode(node.nodeType.toString(), nd.nodeType.toString());
                makeUITree(nd);
            }
        }
    }
}
