grammar FilterTree;

query : ( condition ) + ;

condition : (link_type)? expr # nobrakets
	| (link_type)? '(' condition+ ')' # withbrakets;

link_type : LINK_TYPE_OR
	| LINK_TYPE_AND;

LINK_TYPE_OR : ('o'|'O')('r'|'R');
LINK_TYPE_AND : ('a'|'A')('n'|'N')('d'|'D') ;

expr	: loperand operand_type roperand #standartExpr
	| simpleoperand IS_NULL #isNullExpr;

loperand:	operand;
roperand:	operand;

operand : funcwrap 
	| simpleoperand;

simpleoperand : number
	| alias
	| string;
	
funcwrap : functype '(' simpleoperand ')' | to_date;

functype: LENGTH | LOWER | TO_CHAR | TRUNC;
to_date	: TO_DATE '(' string ')';

LOWER :  'LOWER';
LENGTH 	: 'LENGTH';
TO_CHAR	: 'TO_CHAR';
TRUNC	: 'TRUNC';
TO_DATE	: 'TO_DATE';

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
number : NUMBER;
NUMBER : DIGIT+ | FLOAT ;

/**
* алиас может быть внутренним и расширенным ( расширенный это состоящих из внешних связей)
* т.е. внутренний это для текущего справочника, 
* а внешний для ссылающихся
*/
alias :	internlAlias | eAlias;
// eAlias - extend alias - расширенный алиас, который содержит связь
eAlias : ALIAS ('.' externalAlias)+;
internlAlias : ALIAS;
externalAlias : ALIAS;
ALIAS : ('a'..'z'|'A'..'Z'|'_')+  ('a'..'z'|'A'..'Z'|DIGIT|'_')*;

FLOAT	: DIGIT+ '.' DIGIT+;

string : STRING;
STRING :  '\'' (~('\'') | '\\\'' | ('\'')+('%'))* '\'';
IS_NULL : ('I'|'i')('S'|'s') ' '+ ('N'|'n')('U'|'u')('L'|'l')('L'|'l');

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