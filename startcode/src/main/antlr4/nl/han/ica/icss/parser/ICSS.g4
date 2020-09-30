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

stylesheet: variable_declaration* style_rule+ EOF;
style_rule: selector OPEN_BRACE style_body CLOSE_BRACE;
selector: tag_selector | class_selector | id_selector;

style_body: style_body_line+ ;
style_body_line: style_declaration | if_clause;

style_declaration: property_identifier COLON property_value SEMICOLON;
property_identifier: LOWER_IDENT;

if_clause: IF BOX_BRACKET_OPEN condition BOX_BRACKET_CLOSE OPEN_BRACE conditional_body CLOSE_BRACE else_clause?;
else_clause: ELSE OPEN_BRACE conditional_body CLOSE_BRACE;

condition: variable_identifier;
conditional_body: style_declaration* if_clause*;

property_value: hardcoded_value | variable_identifier | calculation;

calculation: sub_calculation+ ;
sub_calculation: sub_calculation MUL sub_calculation
                | sub_calculation PLUS sub_calculation
                | sub_calculation MIN sub_calculation
                | general_value;

general_value: variable_identifier | hardcoded_value;

hardcoded_value: dimension_size | COLOR | SCALAR;
dimension_size: PIXELSIZE | PERCENTAGE;

tag_selector: LOWER_IDENT | CAPITAL_IDENT;
class_selector: CLASS_IDENT;
id_selector: ID_IDENT;

variable_declaration: variable_identifier ASSIGNMENT_OPERATOR variable_value SEMICOLON;
variable_identifier: CAPITAL_IDENT;
variable_value: TRUE | FALSE | PIXELSIZE | PERCENTAGE | COLOR | SCALAR;




