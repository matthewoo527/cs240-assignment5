li $t0, 1
li $t1, 101

loop:
j PrintItem
PrintItemEnd:
#increment
addi $t0, $t0, 1
bne $t0, $t1, loop

j Exit

PrintItem:
rem $t2, $t0, 3 #Save remainder to $t2, $t2 == $t0 & 3
rem $t3, $t0, 5 #Save remainder to $t3, $t3 == $t0 & 5
#Check 3
beq $t2, $zero, CheckFive  #if 3 pass(can divide by 3), then check if 5 pass(can divide by 5)
#Check 5
beq $t3, $zero, PrintBuzz
#If not dividable by 3 or 5, then PrintInt
j PrintInt
PrintIntEnd:
#Check 5 if Check 3 Pass
CheckFive:
	beq $t3, $zero, PrintFizzBuzz #If yes then PrintFizzBuzz
	j PrintFizz #If not then PrintFizz because it checked 3 and passed
#print new line
# j PrintNewLine
PrintNewLineEnd:
j PrintItemEnd

PrintInt:
#print integer
li $v0, 1
move $a0, $t0
syscall

PrintNewLine:
li $v0, 11
li $a0, '\n'
syscall
j PrintNewLineEnd

PrintFizz:
.data
	Fizz: .asciiz "Fizz"
.text
li $v0, 4
la $a0, Fizz
syscall
j PrintFizzEnd
PrintFizzEnd:
j PrintNewLine

PrintBuzz:
.data
	Buzz: .asciiz "Buzz"
.text
li $v0, 4
la $a0, Buzz
syscall
j PrintBuzzEnd
PrintBuzzEnd:
j PrintNewLine

PrintFizzBuzz:
.data
	FizzBuzz: .asciiz "FizzBuzz"
.text
li $v0, 4
la $a0, FizzBuzz
syscall
j PrintFizzBuzzEnd
PrintFizzBuzzEnd:
j PrintNewLine

Exit:
    li $v0, 10
    syscall