# CS240 Assignment5
## Part 1: Assemble Instructions by Hand
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
It is an I type.

|opcode|rs|rt|immediate|
|---|---|---|---|
|addi|$zero|$t0|3|
|001000|00000|01000|0000000000000011|

So the 32 bits machine code for ```addi $t0, $zero, 3``` is ```00100000000010000000000000000011```

#### ```addi $t1, $zero, 5```
It is an I type

|opcode|rs|rt|immediate|
|---|---|---|---|
|addi|$zero|$t1|5|
|001000|00000|01001|0000000000000101|

So the 32 bits machine code for ```addi $t1, $zero, 5``` is ```00100000000010010000000000000101```

#### ```add  $t2, $t0, $t1```
It is an R type.

|opcode|rs|rt|rd|shamt|funct|
|---|---|---|---|---|---|
|add|$t0|$t1|$t2|No shift amount|funct of add|
|000000|01000|01001|01010|00000|100000|

So the 32 bits machine code for ```add  $t2, $t0, $t1``` is ```00000001000010010101000000100000```

#### ```sub  $t3, $t1, $t0```
It is an R type.

|opcode|rs|rt|rd|shamt|funct|
|---|---|---|---|---|---|
|add|$t1|$t0|$t3|No shift amount|funct of sub|
|000000|01001|01000|01011|00000|100010|

So the 32 bits machine code for ```sub  $t3, $t1, $t0``` is ```00000001001010000101100000100010```

## Part 2: Represent Machine Code as Binary Bytes

Using the machine code from Part 1:

1. Show each 32-bit instruction as four 8-bit bytes.
   
   __Instruction 1: ```addi $t0, $zero, 3```__
   
   32 bits machine code for ```addi $t0, $zero, 3``` is ```00100000000010000000000000000011```
   
   |Byte 1|Byte 2|Byte 3|Byte 4|
   |---|---|---|---|
   |00100000|00001000|00000000|00000011|

   __Instruction 2: ```addi $t1, $zero, 5```__

   32 bits machine code for ```addi $t1, $zero, 5``` is ```00100000000010010000000000000101```
   
   |Byte 1|Byte 2|Byte 3|Byte 4|
   |---|---|---|---|
   |00100000|00001001|00000000|00000101|

   __Instruction 3: ```add $t2, $t0, $t1```__

   32 bits machine code for ```add  $t2, $t0, $t1``` is ```00000001000010010101000000100000```

   |Byte 1|Byte 2|Byte 3|Byte 4|
   |---|---|---|---|
   |00000001|00001001|01010000|00100000|

   __Instruction 4: ```sub $t3, $t1, $t0```__

   32 bits machine code for ```sub  $t3, $t1, $t0``` is ```00000001001010000101100000100010```

   |Byte 1|Byte 2|Byte 3|Byte 4|
   |---|---|---|---|
   |00000001|00101000|01011000|00100010|

   
