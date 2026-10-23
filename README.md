# CS240 Assignment5
## Part 1 Assemble Instructions by Hand
```
Convert these MIPS instructions into 32-bit binary machine code by hand:
addi $t0, $zero, 3
addi $t1, $zero, 5
add  $t2, $t0, $t1
sub  $t3, $t1, $t0
For each instruction, identify its instruction format and show the fields used to construct its binary encoding.
```
From ChatGPT, I got some notes:

### Format

_R Type (e.g. ```add```, ```sub```)_
1. rs = first source
2. rt = second source
3. rd = destination

|opcode|rs|rt|rd|shamt|funct|
|---|---|---|---|---|---|
|6 bits|5 bits|5 bits|5 bits|5 bits|6 bits|

_I Type (e.g. ```addi```, ```lw```, ```sw```)_
1. rs = first source
2. rt = destination

|opcode|rs|rt|immediate|
|---|---|---|---|
|6 bits|5 bits|5 bits|16 bits|

_J Type (e.g. ```j```, ```jal```)_
|opcode|target address|
|---|---|
|6 bits|26 bits|

### Register binary reference

|Register|Number|5-bit Binary|
|---|---|---|
|$zero|0|00000|
|$t0|8|01000|
|$t1|9|01001|
|$t2|10|01010|
|$t3|11|01011|
|$t4|12|01100|
|$t5|13|01101|
|$t6|14|01110|
|$t7|15|01111|

### Some MIPS Opcode Reference

|Instruction|Format|Opcode (6 bits)|Funct (R-Type)|
|---|---|---|---|
|add|R|000000|100000|
|sub|R|000000|100010|
|and|R|000000|100100|
|or|R|000000|100101|
|slt|R|000000|101010|
|jr|R|000000|001000|
|addi|I|001000|—|
|lw|I|100011|—|
|sw|I|101011|—|
|beq|I|000100|—|
|bne|I|000101|—|
|j|J|000010|—|
|jal|J|000011|—|

### Converting
#### ```addi $t0, $zero, 3```
|opcode|rs|rt|immediate|
|---|---|---|---|
|addi|$zero|$t0|3|
|001000|00000|01000|0000000000000011|

So the 32 bits machine code for ```addi $t0, $zero, 3``` is ```00100000000010000000000000000011```


