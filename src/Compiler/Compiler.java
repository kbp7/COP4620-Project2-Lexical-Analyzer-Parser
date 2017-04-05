/*
Kevin Poon
n00900355
 */
package Compiler;

import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.net.URL;
import java.util.regex.*;

class DataItem {                                
    private int val = 0;            
    private int scope = 0;
    private int arrayLength = 0;
    private String identifier = null;
    private String type = null;
    private boolean returnStmt = false;
    private boolean isFunc = false;
    private boolean isVar = false;
    private boolean isArray = false;
    public boolean declared = false;
    private boolean isMain = false;
    private String paramslist = "void";
    
//--------------------------------------------------------------
    public DataItem(String typespec, String token, int currentScope)   { //for symbol table
        type = typespec;
        identifier = token;
        val = currentScope;
    }
//--------------------------------------------------------------
    public void declareVar(String typespec, int arrayL)  {
        isVar = true;
        declared = true;
        arrayLength = arrayL; 
    }
//--------------------------------------------------------------
    public void declareFunc(String typespec, int arrayL, boolean theMain)  {
        isFunc = true;
        arrayLength = arrayL;
        declared = true;
        isMain = theMain;
    }
//--------------------------------------------------------------
    public void declareArray()  {
        isArray = true;
    }
//--------------------------------------------------------------
    public void addFuncParams(String parameter) {
        if(paramslist.equals("none"))    {
            paramslist = parameter;
        }
        else    {
            paramslist += ", " + parameter;
        }
    }
//--------------------------------------------------------------
    public void addReturn(String returnType)  {
        System.out.println("expected type: " + type);
        System.out.println("returnType: " + returnType);
        if(type.contains(returnType))  {
            returnStmt = true;
        }
        else    { //SEMANTICS: return type must match function type
            System.out.println("SEMANTIC ERROR: incorrect return type");
            Compiler.rejected();
        }
    }
    public void checkReturn()  {
        if(!returnStmt && !type.contains("void")) {
            System.out.println("SEMANTIC ERROR: missing return statement");
            Compiler.rejected();
        }
    }
//--------------------------------------------------------------
    public int getVal() { 
        return val; 
    }
    public String getType() {
        return type;
    }
//--------------------------------------------------------------
    public String getID()   {
        return identifier;
    }
//--------------------------------------------------------------
    public void getVariable(int index)   {
        String declaration = null;
        if(isFunc)  {
            declaration = "Function";
        }
        else if(isVar)  {
            declaration = "Variable";
        }
        System.out.printf("[%03d] [%02d] %8s | %-16s | %-16s | ", 
                          index, val, declaration, type, identifier);
        if(arrayLength > 0)
            System.out.printf("Array length: %d\n", arrayLength);
        else if(isFunc) {
            System.out.printf("Params: ");
            System.out.println(paramslist);
           
        }
        else {
            System.out.println();
        }
        //return (type + " | " + identifier + " | " + arrayLength);
    }
 //--------------------------------------------------------------
    public boolean getStatus() {
        return declared;
    }
    public void setID(String id)   {
        identifier = id;
    }
    public void setDeclared()   {
        declared = true;
    }
    public void setType(String itemType)   {
        type = itemType;
    }
    public void setMain()   {
        isMain = true;
    }
   
}//end dataItem
class HashTable
    {
    private final DataItem[] hashArray;    // array holds hash table
    private final int arraySize;
    private int mainCount;
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
       
// -------------------------------------------------------------
    public void insert(DataItem item) // insert a DataItem
    // (assumes table not full)
        {
        String key = item.getID();      // extract key
        System.out.println("test insert: " + key);
        int hashVal = hashFunc(key);  // hash the key         
        
        int j = 0; //for quad probe
        while(hashArray[hashVal] != null) {
            
            ++j;
            hashVal = (hashVal + j * j);  //quadratic probing
            hashVal %= arraySize;         // wraparound
        }
        if (hashArray[hashVal] == null) {
            if(key.contains("main") && mainCount<1)    {
                mainCount += 1;
                Compiler.hasMain = true;
                hashArray[hashVal] = item;    // insert item
            }
            else if(key.contains("main") && mainCount>0)    {
                System.out.println("ERROR: main method already exists");
                Compiler.rejected();
            }
            else
                hashArray[hashVal] = item;    // insert item
            //System.out.println("Stored " + key + " at index " + hashVal + " value: " + value);
        }
        
    }
      // end insert()

// -------------------------------------------------------------
    public int find(int hashVal, String key)    // find item with key
        {
        System.out.println("***finding variable: " + key);
        int j = 0;
        int cellsCheck = 0;
        while((hashArray[hashVal] != null) && (cellsCheck<arraySize))  // until empty cell,
            {                               // found the key?
            //System.out.println(hashArray[hashVal].getKey());
            if(hashArray[hashVal].getID().equals(key)) {
                /*
                System.out.println(key + " found at index " + hashVal
                + " value: " + hashArray[hashVal].getBytes());
                */
                return hashVal;   // yes, return item
                }
            ++j;
            hashVal = (hashVal + j * j);                 // go to next cell
            hashVal %= arraySize;      // wraparound if necessary
            cellsCheck++;
        }
        //System.out.println("index "+hashVal+" contains "+ hashArray[hashVal]);
        return -1;                  // can't find item
    }  
    //--------------------------------------------------------------
    public DataItem searchlocal(String key)    // find item with key
        {
        System.out.println("***Scanning all scoopes for: " + key);
        for(int j = 0; j < arraySize; j++)  {
            if((hashArray[j] != null) && hashArray[j].getID().equals(key))    {
                System.out.println("Found the key");
                return hashArray[j];
            }
        }
        System.out.println("Couldn't find item");
        return null;
    }  
    //-------------------------------------------------------------
    public DataItem getItem(int hashVal)    {
        return hashArray[hashVal];
    }
// -------------------------------------------------------------
    public void update(int hashVal, String key, DataItem item) {
        int index = find(hashVal, key);
        if(!hashArray[index].declared)  {
            hashArray[index] = item;
        }
        else    {
            System.out.println("SEMANTIC ERROR: already declared");
            Compiler.rejected();
        }
    }
    //---------------------------------------------------------------------------
    public void displayTable() 
        {
        System.out.println("================== Symbol Table ====================");
        for(int j=0; j<arraySize; j++)
            {
            if(hashArray[j] != null)
                /*
                System.out.println("[" + j + "]" + hashArray[j].getID()
				   + " [Scope: " + hashArray[j].getVal() + "]");
            */
                hashArray[j].getVariable(j);
                }
        System.out.println();
    }
// -------------------------------------------------------------
}  // end class HashTable

////////////////////////////////////////////////////////////////
public class Compiler {

    public String[] keywords = {"else", "if", "int", "return", "void", "while"};
    public String[] special;
    public static int scope;
    public static int currentScope;
    public static int totalScope;
    public static int tokenLength;
    public static int blockDepth;   //depth of block comments
    public static int lineDepth;    //depth of line comments
    public static boolean hasMain = false;
    public static boolean exp;
    public static boolean rtrn;
    public static BufferedWriter bw;
    public static String[] tokens;
    public static int j;
    public static HashTable table;
    public static Stack<HashTable> stack;
    
    public static void main(String[] args)  throws IOException  {
        System.out.println("Input file: " + args[0]);
        URL path = Compiler.class.getResource(args[0]);
        File file = new File(path.getFile());  
        File out = new File("tokens.txt");
        FileOutputStream fos = new FileOutputStream(out);
        bw = new BufferedWriter(new OutputStreamWriter(fos));
        String[] input;
       
        // read input and write tokens to file
        BufferedReader buff = new BufferedReader(new FileReader(file));
        input = new String[100];
        String line;
        for (int i=0; ((line = buff.readLine()) != null); i++) {
            line = line + " ";
            input[i] = line;
            if(!line.isEmpty()) {
                System.out.println();
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
        
        //----------------------- Semantics ------------------------------------
        table.displayTable();
        
    }//end main    
//--------------------------------------------------------------------
    public static void parser(File out) throws IOException {
        table = new HashTable(20); //symbol table
        stack = new Stack<HashTable>();
        openScope(table);
        
        BufferedReader br = new BufferedReader(new FileReader(out));
        String line;
        tokens = new String[500];
        for (int i=0; ((line = br.readLine()) != null); i++) {
            tokens[i] = line;
        }

        j = 0;
        currentScope = 0;
        totalScope = 0;
        program(tokens[j]);
        if(!hasMain)    {
            System.out.println("SEMANTIC ERROR: No main function");
            rejected();
        }
        System.out.println("-----------------------------------------------------------");
        System.out.println("ACCEPT");
        
    }//-------------------------------------------------------------------------
    public static void rejected()   {
        //System.out.println("ERROR: " + tokens[j]);
        System.out.println("REJECT");
        System.exit(0);
    }
    //--------------------------------------------------------------------------
    public static void accept() {
        System.out.println("Detected $, finished parsing.");
    }
    //=========================== new ==========================================
    public static void openScope(HashTable ht)  {
        stack.add(ht);
    }  
    public static void closeScope()  {
        if(stack.empty())   {
            System.out.println("ERROR: no scope");
            rejected();
        }
        stack.pop();
    } 
    public static void addSym(DataItem item) {
	if (stack.empty()) {
	    System.out.println("ERROR: nowhere to add");
            rejected();
	}
	stack.peek().insert(item);
    }
    public static DataItem lookup(DataItem item) {
	if (stack.empty()) {
	    System.out.println("ERROR: nowhere to add");
            rejected();
	}
	
	for (int i = stack.size() - 1; i >= 0; i--) {
	    DataItem temp = stack.elementAt(i).searchlocal(item.getID());
	    if (temp != null) {
                return temp;
            }
	}
        System.out.println("Failed to find it");
	return null;
    }
    //==========================================================================
    public static boolean lookup(String token)     {
        if (stack.empty()) {
	    System.out.println("ERROR: nowhere to add");
            rejected();
	}
	
	for (int i = stack.size() - 1; i >= 0; i--) {
	    DataItem temp = stack.elementAt(i).searchlocal(token);
	    if (temp != null) {
                return true;
            }
	}
        System.out.println("Failed to find it");
	return false;
        /*
        int hashval = table.hashFunc(token);
        boolean result = false;
        if(table.find(hashval, token) > 0)  { //declared
                System.out.println("*** Found ID in symtab");
                result = true;
            }
            else    {
                System.out.println("SEMANTIC ERROR: variable not declared");
                rejected();
            }
        return result;
        */
    }
    //--------------------------------------------------------------------------
    public static String lookupType(String token)     {
        if (stack.empty()) {
	    System.out.println("ERROR: nowhere to add");
            rejected();
	}
	
	for (int i = stack.size() - 1; i >= 0; i--) {
	    DataItem temp = stack.elementAt(i).searchlocal(token);
	    if (temp != null) {
                return temp.getType();
            }
	}
        System.out.println("Failed to find it");
	return null;
        
    }
    //--------------------------------------------------------------------------
    public static void program(String token)    {   //good
        System.out.println("invoked program");
        System.out.println(token);
        if(isTypeSpec(token))   {
            j++;
            if(tokens[j].contains("ID: "))  {
                //item with typespec and ID
                DataItem item = new DataItem(token, tokens[j], currentScope);
                j++;
                VF(tokens[j], item);
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
                DataItem item = new DataItem(token, tokens[j], currentScope);
                //table.insert(item);
                j++;
                VF(tokens[j], item);
                declarationlist2(tokens[j]);
            }
            else rejected();
        }
        if(token.equals("$"))  {
            accept();   //accepted by parser
        }
        
    }//-------------------------------------------------------------------------
    public static void VF(String token, DataItem item) { // Variable or Function
        System.out.println("invoked VF");
        System.out.println(token);
        // first of A
        if(token.equals(";") || token.equals("["))  {
            //variable
            A(token, item);
        }
        else if(token.equals("("))  {
            //function
            AX(token, item);
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void vardeclaration(String token)    {
        //all variables must be declared once
        System.out.println("invoked vardeclaration");
        System.out.println(token);
        
        if(isTypeSpec(token))   {
            j++;
            if(tokens[j].contains("ID: ")) {
                DataItem item = new DataItem(token, tokens[j], currentScope);
                j++;
                A(tokens[j], item);
            }
            else rejected();
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void A(String token, DataItem item)  { 
        System.out.println("invoked A");
        System.out.println(token);
        //A -> ; | [NUM]
        if(token.equals(";"))  {
            //variable declaration
            String varID = (tokens[j-1]);
            String varType = (tokens[j-2]);
            //SEMANTICS: void variables ****************************************
            if(varType.contains("void"))    {
                System.out.println("SEMANTIC ERROR: void variable");
                rejected();
            }
            //variable is not an array
            item.declareVar(varType, 0);
            int hashval = table.hashFunc(varID);
            if(table.find(hashval, varID) < 0)  {
                //table.insert(item);
                addSym(item);
            }
            else    {
                table.update(hashval, varID, item);
            }
            j++;
        }
        else if(token.equals("["))  {
            //array declaration
            j++;
            if (tokens[j].contains("NUM FLOAT:"))  {
                    //SEMANTICS: FLOAT cannot be an index for an array *********
                    System.out.println("SEMANTIC ERROR: FLOAT index");
                    rejected();
                }
            else if(tokens[j].contains("NUM INT:")) {
                j++;
                if(tokens[j].equals("]")) {
                    j++;
                    if(tokens[j].equals(";"))   {
                        int size = Integer.parseInt
                                       (tokens[j-2].replaceAll("[^0-9]", ""));
                        String varID = (tokens[j-4]);
                        String varType = (tokens[j-5]);
                        //SEMANTICS: void variables ****************************
                        if(varType.contains("void"))    {
                            System.out.println("SEMANTIC ERROR: void variable");
                            rejected();
                        }
                        
                        //variable is an array
                        item.declareVar(varType, size);
                        //table.insert(item);
                        addSym(item);
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
    public static void AX(String token, DataItem item) { //function declaration
        System.out.println("invoked AX");
        System.out.println(token);
        String funcID = tokens[j-1];
        String funcType = tokens[j-2];
        boolean checkMain = false;
        
        if(funcID.contains("main")) {
            checkMain = true;
        }
        
        if(token.equals("("))   {
            item.declareFunc(funcType, 0, checkMain);
            //might change when to insert item into symtab
            //table.insert(item);
            addSym(item);
            j++;
            params(tokens[j], item);
            if(tokens[j].equals(")")) {
                j++;
                compoundstmt(tokens[j], item);
            }
            else rejected();
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void params(String token, DataItem item) {
        System.out.println("invoked params");
        System.out.println(token);
        //parameter types
        if(token.contains("KEYWORD: int"))  {
            j++;
            if(tokens[j].contains("ID: "))  {
                //SEMANTICS: add parameter to symbol table
                DataItem param = new DataItem(token, tokens[j], currentScope);
                //table.insert(param);
                addSym(param);
                j++;
                B(tokens[j], item);
                paramlist2(tokens[j], item);
            }
            else rejected();
        }
        else if(token.contains("KEYWORD: float"))  {
            j++;
            if(tokens[j].contains("ID: "))  {
                DataItem param = new DataItem(token, tokens[j], currentScope);
                //table.insert(param);
                addSym(param);
                j++;
                B(tokens[j], item);
                paramlist2(tokens[j], item);
            }
            else rejected();
        }
        else if(token.contains("KEYWORD: void"))    {
            j++;
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void paramlist2(String token, DataItem item) {
        System.out.println("invoked paramlist2");
        System.out.println(token);
        if(token.equals(","))   {
            j++;
            if(isTypeSpec(tokens[j]))   {
                j++;
                if(tokens[j].contains("ID: "))  {
                    DataItem param = new DataItem(tokens[j-1], tokens[j], currentScope);
                    //table.insert(param);
                    addSym(param);
                    j++;
                    B(tokens[j], item);
                    paramlist2(tokens[j], item);
                }
                else rejected();
            }
            else rejected();
        }// goes to empty
    }//-------------------------------------------------------------------------
    public static void B(String token, DataItem item)  {
        System.out.println("invoked B");
        System.out.println(token);
        String typespec = tokens[j-2];
        String varID = tokens[j-1];
        String parameter;
        if(token.equals("[")) {
            j++;
            if(tokens[j].equals("]"))   {
                //array parameter
                parameter = (typespec + " " + varID + "[]");
                item.addFuncParams(parameter);
                j++;
            }
            else rejected();
        } // or empty
        else {
            //regular parameter
            parameter = (typespec + " " + varID);
            item.addFuncParams(parameter);
        }
    }//-------------------------------------------------------------------------
    public static void compoundstmt(String token, DataItem function)  {
        //compound statement consists of local declarations and statements
        System.out.println("invoked compound");
        System.out.println(token);
        boolean defined = false;
        if(!tokens[j+1].equals("}"))    {
            
            defined = true;
        }
        if(token.equals("{"))   {
            openScope(new HashTable(20));
            totalScope++;
            currentScope++;
            j++;
            localdeclarations2(tokens[j]);
            statementlist2(tokens[j], function);
            if(tokens[j].equals("}")) {
                System.out.println("******** testing checkReturn: " + function.getType());
                if(currentScope == function.getVal() && !function.getType().contains("void"))  {
                    System.out.println(function.getID());
                    function.checkReturn();
                }
                closeScope();
                currentScope--;
                j++;
                
            }
            else rejected();
        }
        else rejected();
        
        if(!defined)    {
            System.out.println("SEMANTIC ERROR: function not defined");
            rejected();
        }
        //SEMANTICS: check for a return statement
        function.checkReturn();
        
            
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
    public static void statementlist2(String token, DataItem function) {
        System.out.println("invoked statementlist2");
        System.out.println(token);
        //first of statement
        if(token.contains("ID: ") || token.contains("NUM") ||
            token.contains("KEYWORD: if") || token.contains("KEYWORD: while") ||
            token.contains("KEYWORD: return") || token.equals("(") ||
            token.equals("{")) {
            statement(token, function);
            statementlist2(tokens[j], function);
        }
        // or empty
    }//-------------------------------------------------------------------------
    public static void statement(String token, DataItem function)  {
        System.out.println("invoked statement");
        System.out.println(token);
        //first of expression-stmt
        if(token.contains("ID: ") || token.contains("NUM") || token.equals("(") ||
           token.equals(";")) {
            
            D(token, function);
        }
        //first of compound-stmt
        else if(token.equals("{"))  {
            compoundstmt(token, function);
        }
        //first of selection-stmt
        else if(token.contains("KEYWORD: if"))  {
            selectionstmt(token, function);
        }
        //first of iteration-stmt
        else if(token.contains("KEYWORD: while"))   {
            iterationstmt(token, function);
        }
        //first of return-stmt
        else if(token.contains("KEYWORD: return"))   {
            returnstmt(token, function);
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void selectionstmt(String token, DataItem function)  { 
        System.out.println("invoked selectionstmt");
        System.out.println(token);
        if(token.contains("KEYWORD: if"))   {
            j++;
            if(tokens[j].equals("(")) {
                j++;
                expression(tokens[j]);
                if(tokens[j].equals(")"))   {
                    j++;
                    statement(tokens[j], function); // CHECK THIS *******************
                    C(tokens[j], function);
                }
                else rejected();   
            }
            else rejected();
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void C(String token, DataItem function)  {
        System.out.println("invoked C");
        System.out.println(token);
        if(token.contains("KEYWORD: else"))     {
            j++;
            statement(tokens[j], function);
        }
        // or empty
    }//-------------------------------------------------------------------------
    public static void iterationstmt(String token, DataItem function)  {
        System.out.println("invoked iterationstmt");
        System.out.println(token);
        if(token.contains("KEYWORD: while")) {
            j++;
            if(tokens[j].equals("(")) {
                j++;
                expression(tokens[j]);
                if(tokens[j].equals(")"))   {
                    j++;
                    statement(tokens[j], function);
                }
                else rejected();
            }
            else rejected();
        }
        else rejected();
    }//-------------------------------------------------------------------------
    public static void returnstmt(String token, DataItem function) {
        System.out.println("invoked returnstmt");
        System.out.println(token);
       
        //SEMANTICS: void functions cannot return values 
        if(function.getType().contains("void")) {
            if(token.contains("return") && tokens[j+1].contains(";")) {
                function.addReturn("void");
            }
            else    {
                System.out.println("SEMANTIC ERROR: void cannot return a value");
                rejected();
            }
        }
        if(token.contains("KEYWORD: return"))   {
            j++;
            D(tokens[j], function);    
        }
        else rejected();
    }
    //-----------------------------------------------------------------------
    public static void D(String token, DataItem function)  {
        System.out.println("invoked D");
        System.out.println(token);
        String ftype = function.getType();
        //SEMANTICS: int/float returns corresponding numbers
        if(token.contains("NUM") && tokens[j+1].equals(";"))    {
            if(token.contains("INT"))   {
                function.addReturn("int");
            }
            else if(token.contains("FLOAT"))    {
                function.addReturn("float");
            }
        }
        //SEMANTICS: return type - simple variable
        if(token.contains("ID: ") && tokens[j+1].equals(";"))   {
            if(ftype.contains(lookupType(token)))  {
                if(token.contains("INT"))   {
                    function.addReturn("int");
                }
                else if(token.contains("FLOAT"))    {
                function.addReturn("float");
                }
            }
            else rejected();
        }
        if(token.equals("(") || token.contains("ID: ") || token.contains("NUM"))   { 
            //rtrn = true;
            expression(token);
            //rtrn = false;
            if(tokens[j].equals(";"))   {
                j++;
            }
            else rejected();
        }
        else if(token.equals(";"))  {
            //SEMANTICS: if not void type, must return a value
            if(!function.getType().contains("void"))    {
                System.out.println("SEMANTIC ERROR: missing return value");
                rejected();
            }
            j++;
        }
        else rejected();
    }// ------------------------------------------------------------------------
    public static void expression(String token) { 
        System.out.println("invoked expression");
        System.out.println(token);
        if(token.contains("ID: "))  {
            lookup(token);
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
            lookup(token);
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
    int hashval = 0;
    
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
                /*
                DataItem item = new DataItem(token, currentScope);
                table.insert(item);
                */
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
                
            }
            if(ca == '!')  {
                if(chars[x+1] == ' ')   {
                    System.out.println("ERROR: " + ca);
                    x++;
                }
                else if(chars[x+1] == '=')  {
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
            }
            
            if(ca == '>' || ca == '<')  {
                if(chars[x+1] == '=')  {
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
                
            }
            
            if(ca == '=')  {
                if(chars[x+1] == '=')  {
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
            ca = chars[x]; 
            if(Character.isDigit(chars[x]) || Character.isLetter(chars[x]) || ca.equals('.'))   {
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
                        
                        String s5 = Character.toString(chars[x]);
                        
                        currentScope = readToken(scope, s5);
                        scope = currentScope;
                        tokenLength = 0;
                    }
                }
            }
        }
    }
}

