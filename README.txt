Kevin Poon
n00900355
COP4620 Construction of Language Translators
Dr. Eggen
Due: 03/02/2017
Submitted: 02/28/2017

Project 2:

This project implements a top down recursive descent parser. It reads tokens
from the lexical analyzer and returns ACCEPT or REJECT.

In order for the BNF grammar to be used by the parser, it must first be converted
to LL(1) by eliminating left recursion and left factoring. Copious amounts of
substitution were applied ad nauseum until the grammar satisfied LL(1).
In the C- language, a program consists of a list of declarations with the last
being void main(void). Declarations can be a variable or a function declarations.

The logic of the program follows the syntax indicated by the grammar. The first
non-terminal, program, is called and the token is compared with the first set.
If the token belongs to the first set of one of the non-terminal productions,
it calls the method for it. If the token is a terminal, it moves to the next token.
If a non-terminal goes to empty, the method returns.
At any point where a token is not what is expected in the grammar, the program
rejects the code. If the program reaches $ (end of file) it accepts.

Reduced LL(1) grammar:
--------------------------------------------------------------------------------
program -> type-spec ID VF declaration-list'
declaration-list' -> type-spec ID VF declaration-list' | empty 
VF -> A | AX    
var-declaration -> type-spec ID A   
A -> ; | [ NUM ] ;  
type-spec -> int | float | void     
AX -> ( params ) compound-stmt      //good
params -> int ID B param-list' | float ID B param-list' | void
param-list' -> , type-spec ID B param-list' | empty
B -> [ ] | empty
compound-stmt -> { local-declarations' statement-list' }
local-declarations' -> var-declaration local-declarations' | empty
statement-list' -> statement statement-list' | empty
statement -> D | compound-stmt | selection-stmt | iteration-stmt | return-stmt
selection-stmt -> if ( expression ) statement C
C -> else statement | empty
iteration-stmt -> while ( expression ) statement
return-stmt -> return D
D -> expression ; | ;
expression -> ID EV | ( expression ) term' additive-expression' F | 
              NUM term' additive-expression' F
EV -> E EV2 | (args) term' additive-expression' F | empty?
EV2 -> = expression | term' additive-expression' F | empty?
E -> [ expression] | empty
F -> relop factor term' additive-expression' | empty
relop -> <= | < | > | >= | == | !=
additive-expression' -> addop factor term' additive-expression' | empty
addop -> + | - 
term' -> mulop factor term' | empty 
mulop -> * | /
factor -> ( expression ) | ID FX | NUM
FX -> E | ( args )
args -> arg-list | empty
arg-list -> expression arg-list'
arg-list' -> , expression arg-list' | empty

First sets:
--------------------------------------------------------------------------------
program = INT FLOAT VOID
declaration-list' = INT FLOAT VOID EMPTY
VF = ; [ (
var-declaration = INT FLOAT VOID
A = ; [
type-spec = INT FLOAT VOID
AX = (
params = INT FLOAT VOID
param-list' = , EMPTY
B = [ EMPTY
compound-stmt = {
local-declarations' = INT FLOAT VOID EMPTY
statement-list' = ID ( NUM { IF WHILE RETURN EMPTY
statement = ID ( NUM { IF WHILE RETURN EMPTY
selection-stmt = IF
C = ELSE EMPTY
iteration-stmt = WHILE
return-stmt = RETURN
D = ID ( NUM ;
expression = ID ( NUM
EV = [ = * / + - <= < > >= == != (
EV2 = * / + - <= < > >= == != =
E = [ EMPTY 
F = <= < > >= == != EMPTY
relop = <= < > >= == !=
additive-expression' = + - empty
addop = + -
term' = * / EMPTY
mulop = * /
args = ID ( NUM EMPTY
arg-list = ID ( NUM
arg-list' = , EMPTY

--------------------------------------------------------------------------------
Input files:
.txt files containing C- source code, entered through the Linux command line
tokens.txt // tokens from the lexical analyzer for the parser

Output:
To terminal, ACCEPT or REJECT
tokens.txt  // generated when performing lexical analysis before parsing




