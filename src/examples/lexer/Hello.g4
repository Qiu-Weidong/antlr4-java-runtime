lexer grammar Hello;


fragment Digit: [0-9];
fragment Letter: [a-zA-Z_];
Id: Letter (Letter | Digit) * ;
Number: Digit+;
Add: '+';
Sub: '-';
Mul: '*';
Div: '/';
WS: [ \t\r\n] -> skip;
Comment: '/*' .*? '*/' -> channel(HIDDEN);
