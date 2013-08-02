grammar FilterTree;

query : ( condition ) + ;

condition : (link_type)? expr # nobrakets
	| (link_type)? '(' condition+ ')' # withbrakets;

link_type : LINK_TYPE_OR
	| LINK_TYPE_AND;

LINK_TYPE_OR : ('o'|'O')('r'|'R');
LINK_TYPE_AND : ('a'|'A')('n'|'N')('d'|'D') ;

expr : operand operand_type operand;

operand :NUMBER
	| ALIAS
	| STRING;

operand_type :	EQUAL
	| NOTEQUAL
	| MORE
	| LESS
	| LIKE;

EQUAL 	: '=';
NOTEQUAL : '!=';
MORE 	: '>';
LESS	: '<';
LIKE	: ('L'|'l') ('I'|'i') ('K'|'k') ('E'|'e');


fragment DIGIT : '0'..'9' ;

NUMBER : DIGIT+ | FLOAT ;
ALIAS :	('a'..'z'|'A'..'Z'|'_')+  ('a'..'z'|'A'..'Z'|DIGIT|'_')* ;


FLOAT	: DIGIT+ '.' DIGIT+;

STRING
    :  '\'' ( ESC_SEQ | ~('\\'|'\'') )* '\''
    ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

SPACE : ' ' {skip();};