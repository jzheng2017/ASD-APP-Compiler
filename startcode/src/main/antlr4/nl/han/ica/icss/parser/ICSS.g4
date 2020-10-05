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




//--- PARSER: ---

stylesheet: variableDeclaration* stylerule+;

stylerule: selector OPEN_BRACE styleBody CLOSE_BRACE;
selector: tagSelector | classSelector | idSelector;

styleBody: styleBodyLine+ ;
styleBodyLine: styleDeclaration | ifClause;

styleDeclaration: propertyIdentifier COLON propertyValue SEMICOLON;
propertyIdentifier: LOWER_IDENT;

ifClause: IF BOX_BRACKET_OPEN condition BOX_BRACKET_CLOSE OPEN_BRACE conditionalBody CLOSE_BRACE elseClause?;
elseClause: ELSE OPEN_BRACE conditionalBody CLOSE_BRACE;

condition: variableIdentifier | TRUE | FALSE;
conditionalBody: styleDeclaration* ifClause*;

propertyValue: hardcodedValue | variableIdentifier | calculation;

calculation: firstSubcalculation consecutiveSubcalculation*;

firstSubcalculation: generalValue MUL generalValue
                   | generalValue (PLUS | MIN) generalValue;

consecutiveSubcalculation:  MUL generalValue
                         | (PLUS | MIN) generalValue;


generalValue: variableIdentifier | hardcodedValue;

hardcodedValue: dimensionSize | COLOR | SCALAR;
dimensionSize: PIXELSIZE | PERCENTAGE;

tagSelector: LOWER_IDENT | CAPITAL_IDENT;
classSelector: CLASS_IDENT;
idSelector: ID_IDENT;

variableDeclaration: variableIdentifier ASSIGNMENT_OPERATOR variableValue SEMICOLON;
variableIdentifier: CAPITAL_IDENT;
variableValue: TRUE | FALSE | PIXELSIZE | PERCENTAGE | COLOR | SCALAR;




