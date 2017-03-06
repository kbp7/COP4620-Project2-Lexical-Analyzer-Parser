/*
Kevin Poon
n00900355
Project 1: Lexical Analyzer
 */
package project2compilers;

import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.net.URL;
import java.util.regex.*;

class DataItem {                                
    private int val;               //value
    private String identifier;
    
    
//--------------------------------------------------------------
    public DataItem(String token, int hashval)   { //for symbol table
        //tk = token;
        val = hashval;
    }
//--------------------------------------------------------------
    public int getVal() { 
        return val; 
    }
//--------------------------------------------------------------
    public String getID()   {
        return identifier;
    }
}//end dataItem
class HashTable
    {
    private final DataItem[] hashArray;    // array holds hash table
    private final int arraySize;
// -------------------------------------------------------------
    public HashTable(int size)       // constructor
        {
        arraySize = size;
        hashArray = new DataItem[arraySize];
        }
// -------------------------------------------------------------
    public int hashFunc(String myString)
        {
        //unique hashVal even for different words with same letters
        int intLength = myString.length() / 4;
        long sum = 0;
        for (int j = 0; j < intLength; j++) {
            char c[] = myString.substring(j * 4, (j * 4) + 4).toCharArray();
            long mult = 1;
            for (int k = 0; k < c.length; k++) {
                sum += c[k] * mult;
                mult *= 256;
                }
            }

        char c[] = myString.substring(intLength * 4).toCharArray();
        long mult = 1;
        for (int k = 0; k < c.length; k++) {
            sum += c[k] * mult;
            mult *= 256;
            }

        return(int)(Math.abs(sum) % arraySize);
        }
        
}  // end class HashTable

////////////////////////////////////////////////////////////////
public class Project2compilers {

    public String[] keywords = {"else", "if", "int", "return", "void", "while"};
    public String[] special;
    public static int scope;
    public static int currentScope;
    public static int tokenLength;
    public static int blockDepth;   //depth of block comments
    public static int lineDepth;    //depth of line comments
    public static BufferedWriter bw;
    public static String[] tokens;
    public static int j;
    
    
    public static void main(String[] args)  throws IOException  {
        System.out.println("Input file: " + args[0]);
        URL path = Project2compilers.class.getResource(args[0]);
        File file = new File(path.getFile());  
        File out = new File("tokens.txt");
        FileOutputStream fos = new FileOutputStream(out);
        bw = new BufferedWriter(new OutputStreamWriter(fos));
        String[] input;
        //int size = 100;
        //HashTable theHashTable = new HashTable(size); //symbol table
       
        // read input and write tokens to file
        BufferedReader buff = new BufferedReader(new FileReader(file));
        input = new String[100];
        String line;
        for (int i=0; ((line = buff.readLine()) != null); i++) {
            line = line + " ";
            input[i] = line;
            if(!line.isEmpty()) {
                System.out.println();
                //line = line + " ";
                System.out.println("INPUT: " + line);
                if(line.contains("$"))  {
                    System.out.println("============= END OF FILE ==============");
                    return;
                }
                readChar(line);
                lineDepth = 0;
            }
        }
        System.out.println("Tests below ----------------------------------------");
        bw.write("$");
        bw.close(); //finish writing tokens to file
        parser(out);
        
    }//end main    
//--------------------------------------------------------------------
    public static void parser(File out) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(out));
        String line;
        tokens = new String[500];
        for (int i=0; ((line = br.readLine()) != null); i++) {
            tokens[i] = line;
        }

        j = 0;
        program(tokens[j]);
        System.out.println("-----------------------------------------------------------");
        System.out.println("ACCEPT");
        /*
        while(tokens[j] != null)    {
            declarationlist(tokens[j]);
            j++;
        }
        */
    }//---------------------------------------------------------------

    public static void rejected()   {
        System.out.println("ERROR: " + tokens[j]);
        System.out.println("REJECT");
        System.exit(0);
    }
    public static void accept() {
        System.out.println("Detected $, finished parsing.");
    }
    //----------------------------------------------------------------
    public static void program(String token)    {
        System.out.println("invoked program");
        System.out.println(token);
        if(isTypeSpec(token))   {
            j++;
            if(tokens[j].contains("ID: "))  {
                j++;
                VF(tokens[j]);
                declarationlist2(tokens[j]);
            }
            else rejected();
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void declarationlist2(String token)   { //good
        System.out.println("invoked declarationlist2");
        System.out.println(token);
        //first of declaration
        if(isTypeSpec(token))   {
            j++;
            if(tokens[j].contains("ID: "))  {
                j++;
                VF(tokens[j]);
                declarationlist2(tokens[j]);
            }
            else rejected();
        }
        else if(token.equals("$"))  {
            accept();
        }
        
    }//--------------------------------------------------------------------
    public static void VF(String token) {
        System.out.println("invoked VF");
        System.out.println(token);
        // first of A
        if(token.equals(";") || token.equals("["))  {
            A(token);
        }
        else if(token.equals("("))  {
            AX(token);
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void vardeclaration(String token)    {
        System.out.println("invoked vardeclaration");
        System.out.println(token);
        
        if(isTypeSpec(token))   {
            j++;
            if(tokens[j].contains("ID: ")) {
                j++;
                A(tokens[j]);
            }
            else rejected();
        }
        else rejected();
    }//----------------------------------------------------------------------------
    public static void A(String token)  {
        System.out.println("invoked A");
        System.out.println(token);
        //A -> ; | [NUM]
        if(token.equals(";"))  {
            j++;
        }
        else if(token.equals("["))  {
            j++;
            if(tokens[j].contains("NUM")) {
                j++;
                if(tokens[j].equals("]")) {
                    j++;
                    if(tokens[j].equals(";"))   {
                        j++;
                    }
                    else rejected();
                }
                else rejected();
            }
            else rejected();
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void AX(String token) {
        System.out.println("invoked AX");
        System.out.println(token);
        if(token.equals("("))   {
            j++;
            params(tokens[j]);
            if(tokens[j].equals(")")) {
                j++;
                compoundstmt(tokens[j]);
            }
            else rejected();
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void params(String token) {
        System.out.println("invoked params");
        System.out.println(token);
        if(token.contains("KEYWORD: int"))  {
            j++;
            if(tokens[j].contains("ID: "))  {
                j++;
                B(tokens[j]);
                paramlist2(tokens[j]);
            }
            else rejected();
        }
        else if(token.contains("KEYWORD: float"))  {
            j++;
            if(tokens[j].contains("ID: "))  {
                j++;
                B(tokens[j]);
                paramlist2(tokens[j]);
            }
            else rejected();
        }
        else if(token.contains("KEYWORD: void"))    {
            j++;
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void paramlist2(String token) {
        System.out.println("invoked paramlist2");
        System.out.println(token);
        if(token.equals(","))   {
            j++;
            if(isTypeSpec(tokens[j]))   {
                j++;
                if(tokens[j].contains("ID: "))  {
                    j++;
                    B(tokens[j]);
                    paramlist2(tokens[j]);
                }
                else rejected();
            }
            else rejected();
        }// goes to empty
    }//-------------------------------------------------------------------------
    public static void B(String token)  {
        System.out.println("invoked B");
        System.out.println(token);
        if(token.equals("[")) {
            j++;
            if(tokens[j].equals("]"))   {
                j++;
            }
            else rejected();
        } // or empty
    }//-------------------------------------------------------------------------
    public static void compoundstmt(String token)  {
        System.out.println("invoked compound");
        System.out.println(token);
        if(token.equals("{"))   {
            j++;
            localdeclarations2(tokens[j]);
            statementlist2(tokens[j]);
            if(tokens[j].equals("}")) {
                j++;
            }
            else rejected();
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void localdeclarations2(String token) {
        System.out.println("invoked localdeclarations2");
        System.out.println(token);
        //first of var-declaration
        if(isTypeSpec(token))  {
            vardeclaration(token);
            localdeclarations2(tokens[j]);
        } // or empty
    }//-------------------------------------------------------------------------
    public static void statementlist2(String token) {
        System.out.println("invoked statementlist2");
        System.out.println(token);
        //first of statement
        if(token.contains("ID: ") || token.contains("NUM") ||
            token.contains("KEYWORD: if") || token.contains("KEYWORD: while") ||
            token.contains("KEYWORD: return") || token.equals("(") ||
            token.equals("{")) {
            
            statement(token);
            statementlist2(tokens[j]);
        }
        // or empty
    }//-------------------------------------------------------------------------
    public static void statement(String token)  {
        System.out.println("invoked statement");
        System.out.println(token);
        //first of expression-stmt
        if(token.contains("ID: ") || token.contains("NUM") || token.equals("(") ||
           token.equals(";")) {
            
            D(token);
        }
        //first of compound-stmt
        else if(token.equals("{"))  {
            compoundstmt(token);
        }
        //first of selection-stmt
        else if(token.contains("KEYWORD: if"))  {
            selectionstmt(token);
        }
        //first of iteration-stmt
        else if(token.contains("KEYWORD: while"))   {
            iterationstmt(token);
        }
        //first of return-stmt
        else if(token.contains("KEYWORD: return"))   {
            returnstmt(token);
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void selectionstmt(String token)  { 
        System.out.println("invoked selectionstmt");
        System.out.println(token);
        if(token.contains("KEYWORD: if"))   {
            j++;
            if(tokens[j].equals("(")) {
                j++;
                expression(tokens[j]);
                if(tokens[j].equals(")"))   {
                    j++;
                    statement(tokens[j]); // CHECK THIS *******************
                    C(tokens[j]);
                }
                else rejected();   
            }
            else rejected();
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void C(String token)  {
        System.out.println("invoked C");
        System.out.println(token);
        if(token.contains("KEYWORD: else"))     {
            j++;
            statement(tokens[j]);
        }
        // or empty
    }//-------------------------------------------------------------------------
    public static void iterationstmt(String token)  {
        System.out.println("invoked iterationstmt");
        System.out.println(token);
        if(token.contains("KEYWORD: while")) {
            j++;
            if(tokens[j].equals("(")) {
                j++;
                expression(tokens[j]);
                if(tokens[j].equals(")"))   {
                    j++;
                    statement(tokens[j]);
                }
                else rejected();
            }
            else rejected();
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void returnstmt(String token) {
        System.out.println("invoked returnstmt");
        System.out.println(token);
        if(token.contains("KEYWORD: return"))   {
            j++;
            D(tokens[j]);    
        }
        else rejected();
    }
    //-----------------------------------------------------------------------
    public static void D(String token)  {
        System.out.println("invoked D");
        System.out.println(token);
        // first of expression
        if(token.equals("(") || token.contains("ID: ") || token.contains("NUM"))   { 
            expression(token);
            if(tokens[j].equals(";"))   {
                j++;
            }
            else rejected();
        }
        else if(token.equals(";"))
            j++;
        
        else rejected();
    }// ------------------------------------------------------------------------
    public static void expression(String token) { 
        System.out.println("invoked expression");
        System.out.println(token);
        if(token.contains("ID: "))  {
            j++;
            EV(tokens[j]);
        }
        else if(token.equals("(")) {
            j++;
            expression(tokens[j]);
            if(tokens[j].equals(")")) {
                j++;
                term2(tokens[j]);
                additiveexpression2(tokens[j]);
                F(tokens[j]);
            }
            else rejected();
        }
        else if(token.contains("NUM"))  {
            j++;
            term2(tokens[j]);
            additiveexpression2(tokens[j]);
            F(tokens[j]);
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void EV(String token) {
        System.out.println("invoked EV");
        System.out.println(token);
        // first of E
        if(token.equals("["))   {
            E(token);
            EV2(tokens[j]);
        }
        // E is empty, first of EV2
        else if(isMulop(token) || isAddop(token) || isRelop(token) || token.equals("=")){
            EV2(token);
        }
        else if(token.equals("("))  {
            j++;
            args(tokens[j]);
            if(tokens[j].equals(")")) {
                j++;
                term2(tokens[j]);
                additiveexpression2(tokens[j]);
                F(tokens[j]);
            }
            else rejected();
        }
        // ??else rejected(); 
        
    }//-------------------------------------------------------------------------
    public static void EV2(String token)    {
        System.out.println("invoked EV2");
        System.out.println(token);
        //E -> = expression
        if(token.equals("="))   {
            j++;
            expression(tokens[j]);
        }
        //first of term'
        else if(token.equals("*") || token.equals("/")) {
            term2(token);
            additiveexpression2(tokens[j]);
            F(tokens[j]);
        }
        //term' is empty
        else if(token.equals("+") || token.equals("-")) {
            additiveexpression2(tokens[j]);
            F(tokens[j]);
        }
        //additive-expression' is empty
        else if(isRelop(token)) {
            F(tokens[j]);
        }
        //??? else rejected();
    }//-------------------------------------------------------------------------
    public static void E(String token)  {
        System.out.println("invoked E");
        System.out.println(token);
        if(token.equals("["))   {
            j++;
            expression(tokens[j]);
            if(tokens[j].equals("]"))
                j++;
            else rejected();
        }// or empty
    }//-------------------------------------------------------------------------
    public static void F(String token)  {
        System.out.println("invoked F");
        System.out.println(token);
        if(isRelop(token))  {
            relop(token);
            factor(tokens[j]);
            term2(tokens[j]);
            additiveexpression2(tokens[j]);
        }// or empty
    }//-------------------------------------------------------------------------
    public static void relop(String token) {
        System.out.println("invoked relop");
        System.out.println(token);
        if(token.equals("<=") || token.equals("<") || token.equals(">")
            || token.equals(">=") || token.equals("==") || token.equals("!=")) {
            
            j++;
        }
        else rejected();
    }
    // -------------------------------------------------------------------------
    public static void additiveexpression2(String token) {
        System.out.println("invoked additiveexpression2");
        System.out.println(token);
        //first of addop
        if(isAddop(token))  {
            addop(token);
            factor(tokens[j]);
            term2(tokens[j]);
            additiveexpression2(tokens[j]);
        } // or empty
    }//-------------------------------------------------------------------------
    public static void addop(String token) {
        System.out.println("invoked addop");
        System.out.println(token);
        if(token.equals("+") || token.equals("-"))  {
            j++;
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void term2(String token) {
        System.out.println("invoked term2");
        System.out.println(token);
        //first of mulop
        if(isMulop(token))  {
            mulop(token);
            factor(tokens[j]);
            term2(tokens[j]);
        }// or empty
    }//-------------------------------------------------------------------------
    public static void mulop(String token) {
        System.out.println("invoked mulop");
        System.out.println(token);
        if(token.equals("*") || token.equals("/"))  {
            j++;
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void factor(String token) { // CHECK THIS *******************
        System.out.println("invoked factor");
        System.out.println(token);
        
        if(token.equals("("))   {
            j++;
            expression(tokens[j]);
            if(tokens[j].equals(")"))   {
                j++;
            }
            else rejected();
        }
        else if(token.contains("ID: ")) {     
            j++;
            FX(tokens[j]);
        }
        else if(token.contains("NUM"))  {
            j++;
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void FX(String token) {
        System.out.println("invoked FX");
        System.out.println(token);
        //first of E
        if(token.equals("["))   {
            E(token);
        }
        else if(token.equals("("))  {
            j++;
            args(tokens[j]);
            if(tokens[j].equals(")"))   {
                j++;
            } else rejected();
        }// or empty
    }//-------------------------------------------------------------------------
    public static void args(String token) { // good
        System.out.println("invoked args");
        System.out.println(token);
        // first of arg-list
        if(token.equals("(") || token.contains("ID: ") || token.contains("NUM"))   { 
            argslist(token);
        }// or empty
    }//-------------------------------------------------------------------------
    public static void argslist(String token) {
        System.out.println("invoked argslist");
        System.out.println(token);
        //first of expression
        if(token.equals("(") || token.contains("ID: ") || token.contains("NUM")){
            expression(token);
            argslist2(tokens[j]);
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void argslist2(String token) {
        System.out.println("invoked argslist2");
        System.out.println(token);
        // must start with ","
        if(token.equals(","))   {
            j++;
            expression(tokens[j]);
            argslist2(tokens[j]);
        }
    }//-------------------------------------------------------------------------
    public static boolean isTypeSpec(String token)  {
        return token.contains("int") || token.contains("float") || token.contains("void");
    }//-------------------------------------------------------------------------
    public static boolean isKeyword(String x)    {
        switch (x) {
            case "else":
                return true;
            case "if":
                return true;
            case "int":
                return true;
            case "return":
                return true;
            case "void":
                return true;
            default:
                return x.equals("while");
        }
    }//-------------------------------------------------------------------------
    public static boolean isRelop(String x) {
        switch (x) {
            case "<=":
                return true;
            case "<":
                return true;
            case ">":
                return true;
            case ">=":
                return true;
            default:
                return x.equals("==") || x.equals("!=");
        }
    }//-------------------------------------------------------------------------
    public static boolean isMulop(String x) {
        switch (x) {
            case "*":
                return true;
            case "/":
                return true;
            default:
                return false;
        }
    }//-------------------------------------------------------------------------
    public static boolean isAddop(String x) {
        switch (x) {
            case "+":
                return true;
            case "-":
                return true;
            default:
                return false;
        }
    }//-------------------------------------------------------------------------
    public static boolean isSpecial(String x, char[] chars, int index)   {
        String next;
        StringBuilder sb = new StringBuilder(x);
        if (Pattern.compile("[;]").matcher(x).find() && (index+1) < chars.length)  {
            next = Character.toString(chars[index+1]);
            sb.append(next);
            if(next.matches("[\\s]"))   {
                System.out.println();
            }
            else    {
                System.out.println("ERROR: " + sb);
                
            }
        }
        if (Pattern.compile("[+-<>!=*]").matcher(x).find()) {
            if((index+1) < chars.length)   {
                next = Character.toString(chars[index+1]);
                sb.append(next);
                if(next.equals("<") || next.equals(">") || next.equals("="))    {
                    //System.out.println(next);
                }
                
            }
            return true;
        }
        if (Pattern.compile("[();,]").matcher(x).find()) {
            if((index+1) < chars.length)   {
                next = Character.toString(chars[index+1]);
                sb.append(next);
                return true;
                
            }
            return true;
        }
        else return false;
        }//--------------------------------------------------------------------
    public static void output(String token) throws IOException  {
        bw.write(token);
        bw.newLine();
    }//--------------------------------------------------------------------
    public static void isNumber(char[] chars, char x)   {
        if(Character.isDigit(x))    {
            System.out.print("NUM: " + x++);
            while(x < chars.length) {
                switch(chars[x])   {
                    case ' ':
                        System.out.println();
                    default:
                        System.out.print(chars[x]);
                }
            }
        }   
    }//--------------------------------------------------------------------
    public static boolean isEndComment(char[] chars, int index)    {
        while(index < chars.length) {
            if(chars[index] == '*') {
                if(chars[index] == '/') {
                    return true;
                }
            }
            index++;
        }
        return false;
    }//--------------------------------------------------------------------
    
    public static int readToken(int currentScope, String token) throws IOException   {
    if(blockDepth == 0 && lineDepth == 0)  {    
        if(token.matches("[a-zA-Z]+"))  {
            if(token.equals("else"))    {
                System.out.println("KEYWORD: " + token);
                output("KEYWORD: " + token);
                
            }
            else if(token.equals("if")) {
                System.out.println("KEYWORD: " + token);
                output("KEYWORD: " + token);
                
            }
            else if(token.equals("float")) {
                System.out.println("KEYWORD: " + token);
                output("KEYWORD: " + token);
                
            }
            else if(token.equals("int")) {
                System.out.println("KEYWORD: " + token);
                output("KEYWORD: " + token);
                
            }
            else if(token.equals("return")) {
                System.out.println("KEYWORD: " + token);
                output("KEYWORD: " + token);
                
            }
            else if(token.equals("void")) {
                System.out.println("KEYWORD: " + token);
                output("KEYWORD: " + token);
            }
            else if(token.equals("while")) {
                System.out.println("KEYWORD: " + token);
                output("KEYWORD: " + token);
            }
            else if(lineDepth == 0 && blockDepth == 0)   {
                System.out.println("ID: " + token + "   SCOPE: " + currentScope);
                output("ID: " + token);
            }
        }
        if(token.matches("[0-9Ee.]+"))  {
            if(token.contains("E") || token.contains(".")) {
                System.out.println("NUM FLOAT: " + token);
                output("NUM FLOAT: " + token);
            }
            else if(token.contains("e"))    {
                System.out.println("ERROR: " + token);
                //output(token);
            }
            else    {
                System.out.println("NUM INT: " + token);
                output("NUM INT: " + token);
            }
        }  
        if(token.matches("[0-9]") && token.matches("[a-z]"))  {
            System.out.println("ERROR: " + token);
        }
        if(token.matches("[+-]+"))  {
            System.out.println(token);
            output(token);
        }
        if(token.matches("[=]+"))  {
            System.out.println(token);
            output(token);
        }
        if(token.matches("[()]+"))  {
            System.out.println(token);
            output(token);
        }
        if(token.equals("/"))   {
            System.out.println(token);
            output(token);
        }
        if(token.equals("*"))   {
            System.out.println(token);
            output(token);
        }
        if(token.equals(";"))   {
            System.out.println(token);
            output(token);
        }
        if(token.equals(","))   {
            System.out.println(token);
            output(token);
        }
        if(token.equals("[") || token.equals("]"))  {
            System.out.println(token);
            output(token);
        }
        if(token.equals("<") || token.equals(">"))  {
            System.out.println(token);
            output(token);
        }
        if(token.equals("{"))   {
            currentScope += 1;
            System.out.println(token);
            output(token);
        }
        if(token.equals("}"))   {
            currentScope -= 1;
            System.out.println(token);
            output(token);
        }
    }
        return currentScope;
    }//-------------------------------------------------------------------------

    public static void readChar(String line) throws IOException   {
        char[] chars = line.toCharArray();
        tokenLength = 0;
        String c;
        Character ca;

        for(int x = 0; x<=chars.length; x++)    {
            if(x == chars.length)   { //end of the line
                int startIndex = x - tokenLength;
                String s1 = new String(chars, startIndex, tokenLength);
                currentScope = readToken(scope, s1);
                scope = currentScope;
                tokenLength = 0;
                break;
            }
            c = String.valueOf(chars[x]);
            ca = chars[x];
            if(ca == '/')   {  //its a comment
                if(chars[x+1] == '*')   {
                    blockDepth++;
                    String s = new String(chars, x, 2);
                    currentScope = readToken(scope, s);
                    //System.out.println(s + " " + blockDepth);
                    x+=2;
                    //continue;
                }
                else if(chars[x+1] == '/')  {
                    lineDepth++;
                    String s = new String(chars, x, 2);
                    currentScope = readToken(scope, s);
                    x+=2;
                    //continue;
                   
                }
            }
            if(blockDepth == 0 && lineDepth == 0)   {
                /*
            if(ca == ',')   {
                if(chars[x+1] != ' ')   {
                    int startIndex = x - tokenLength;
                    String s4 = new String(chars, startIndex, tokenLength);
                    currentScope = readToken(scope, s4);
                    scope = currentScope;
                    
                    String s = new String(chars, x, 1);
                    System.out.println(s);
                    output(s);
                    //currentScope = readToken(scope, s);
                    x+=1;
                    tokenLength = 0;
                }
            }    
                */
            if(ca == '+' || ca == '-')  {
                if(chars[x+1] == '=')   {
                    int startIndex = x - tokenLength;
                    String s4 = new String(chars, startIndex, tokenLength);
                    currentScope = readToken(scope, s4);
                    scope = currentScope;
                    
                    String s = new String(chars, x, 2);
                    System.out.println(s);
                    output(s);
                    //currentScope = readToken(scope, s);
                    x+=2;
                    tokenLength = 0;
                }
                /* CAUSING ISSUES
                else    {
                    System.out.println(ca);
                    output(ca.toString());
                    tokenLength = 0;
                    x+=1;
                }
                */
            }
            if(ca == '!')  {
                if(chars[x+1] == ' ')   {
                    System.out.println("ERROR: " + ca);
                    x++;
                }
                else if(chars[x+1] == '=')  {
                    //new code *************************************************
                    int startIndex = x - tokenLength;
                    String s4 = new String(chars, startIndex, tokenLength);
                    currentScope = readToken(scope, s4);
                    scope = currentScope;
                    // *********************************************************
                    String s = new String(chars, x, 2);
                    System.out.println(s);
                    output(s);
                    //currentScope = readToken(scope, s);
                    x+=2;
                    tokenLength = 0;
                }
            }
            
            if(ca == '>' || ca == '<')  {
                if(chars[x+1] == '=')  {
                    //new code *************************************************
                    int startIndex = x - tokenLength;
                    String s4 = new String(chars, startIndex, tokenLength);
                    currentScope = readToken(scope, s4);
                    scope = currentScope;
                    // *********************************************************
                    String s = new String(chars, x, 2);
                    System.out.println(s);
                    output(s);
                    //currentScope = readToken(scope, s);
                    x+=2;
                    tokenLength = 0;
                }
                
            }
            
            if(ca == '=')  {
                if(chars[x+1] == '=')  {
                    //new code *************************************************
                    int startIndex = x - tokenLength;
                    String s4 = new String(chars, startIndex, tokenLength);
                    currentScope = readToken(scope, s4);
                    scope = currentScope;
                    // *********************************************************
                    String s = new String(chars, x, 2);
                    System.out.println(s);
                    output(s);
                    //currentScope = readToken(scope, s);
                    x+=2;
                    tokenLength = 0;
                }
            }
            }
            if(ca == '*')   { 
                if(chars[x+1] == '/')   {
                    if(blockDepth == 0) {
                        String s1 = new String(chars, x, 1);
                        String s2 = new String(chars, (x+1), 1);
                        System.out.println(s1);
                        System.out.println(s2);
                        output(s1);
                        output(s2);
                        x+=2;
                        tokenLength = 0;
                    }
                    else    {   //its a block comment end
                        //System.out.println("end block");
                        blockDepth--;
                        lineDepth = 0;
                        if(blockDepth == 0) {
                            String s = new String(chars, x, 2);
                            currentScope = readToken(scope, s);
                            //System.out.println(s + " " + blockDepth);
                            x+=2;
                            tokenLength = 0;
                            continue;
                        }
                    }
                }
                else if(chars[x+1] == '=' && blockDepth == 0)  {
                    String s = new String(chars, x, 2);
                    System.out.println(s);
                    output(s);
                    //currentScope = readToken(scope, s);
                    x+=2;
                    tokenLength = 0;
                }
            }
            if(Character.isDigit(ca) || Character.isLetter(ca) || ca.equals('.'))   {
                tokenLength++;
            }
           
            else    {
                if(x == 0)  { //if nonalphanumeric character at beginning
                    tokenLength = 1;
                    String s2 = new String(chars, x, tokenLength);
                    currentScope = readToken(scope, s2);
                    scope = currentScope;
                    tokenLength = 0;
                }
                else    {
                    if(tokenLength == 0)  { //non alphanumeric character
                        String s3 = Character.toString(chars[x]);
                        currentScope = readToken(scope, s3);
                        scope = currentScope;
                        tokenLength = 0;
                        
                    }
                    else    {
                        int startIndex = x - tokenLength;
                        String s4 = new String(chars, startIndex, tokenLength);
                        currentScope = readToken(scope, s4);
                        scope = currentScope;
                        System.out.println("Testing tokens: " + s4); //testing
                        String s5 = Character.toString(chars[x]);
                        System.out.println("Testing tokens: " +s5); //testing
                        currentScope = readToken(scope, s5);
                        scope = currentScope;
                        tokenLength = 0;
                    }
                }
            }
        }
    }
}

