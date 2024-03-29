grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

NEGATION_OPERATOR: '!';
LT: '<';
LET: '<=';
EQ: '==';
NQ: '!=';
GET: '>=';
GT: '>';
AND: '&&';
OR: '||';
//--- PARSER: ---

stylesheet: stylesheetLines+ | EOF;
stylesheetLines: variableDeclaration | stylerule;

variableDeclaration: variableIdentifier ASSIGNMENT_OPERATOR variableValue SEMICOLON;
variableReference: CAPITAL_IDENT;
variableIdentifier: CAPITAL_IDENT;
variableValue: variableHardcodedValue | variableReference | calculation | booleanExpressions;

variableHardcodedValue: TRUE | FALSE | PIXELSIZE | PERCENTAGE | COLOR | SCALAR;

stylerule: selector OPEN_BRACE styleBody CLOSE_BRACE;
selector: tagSelector | classSelector | idSelector;

styleBody: styleBodyLine+ ;
styleBodyLine: styleDeclaration | ifClause | variableDeclaration;

styleDeclaration: propertyIdentifier COLON propertyValue SEMICOLON;
propertyIdentifier: LOWER_IDENT;

ifClause: IF BOX_BRACKET_OPEN condition BOX_BRACKET_CLOSE OPEN_BRACE conditionalBody CLOSE_BRACE elseClause?;
elseClause: ELSE OPEN_BRACE conditionalBody CLOSE_BRACE;

condition: variableReference | booleanExpressions;
conditionalBody: conditionalBodyLine+;
conditionalBodyLine: variableDeclaration | styleDeclaration | ifClause;

propertyValue: calculation | hardcodedPropertyValue | variableReference;

calculation: subCalculation;

subCalculation: subCalculation MUL subCalculation
              | subCalculation (PLUS | MIN) subCalculation
              | generalValue;

generalValue: variableReference | hardcodedValue;

hardcodedPropertyValue: hardcodedValue;

hardcodedValue: dimensionSize | SCALAR | COLOR | TRUE | FALSE;

dimensionSize: PIXELSIZE | PERCENTAGE;

booleanExpressions: booleanExpressions AND booleanExpressions
                    | booleanExpressions OR booleanExpressions
                    | booleanExpressions (LT | LET | GET | GT | EQ | NQ ) booleanExpressions
                    | booleanExpression;

booleanExpression: NEGATION_OPERATOR? (TRUE | FALSE | equality | variableReference | subCalculation );
equality: NEGATION_OPERATOR? subCalculation (LT | LET | GET | GT | EQ | NQ) NEGATION_OPERATOR? subCalculation;

tagSelector: LOWER_IDENT;
classSelector: CLASS_IDENT;
idSelector: ID_IDENT;