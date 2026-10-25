.data
data_0: .asciiz "Fizz"
data_5: .asciiz "Buzz"
data_10: .asciiz "FizzBuzz"
.text
    addiu $t0, $zero, 1
    addiu $t1, $zero, 101
L_00400008:
    j L_00400018
L_0040000C:
    addi $t0, $t0, 1
    bne $t0, $t1, L_00400008
    j L_004000AC
L_00400018:
    addiu $at, $zero, 3
    div $t0, $at
    mfhi $t2
    addiu $at, $zero, 5
    div $t0, $at
    mfhi $t3
    beq $t2, $zero, L_0040003C
    beq $t3, $zero, L_0040007C
    j L_00400048
L_0040003C:
    beq $t3, $zero, L_00400094
    j L_00400064
L_00400044:
    j L_0040000C
L_00400048:
    addiu $v0, $zero, 1
    addu $a0, $t0, $zero
    syscall
L_00400054:
    addiu $v0, $zero, 11
    addiu $a0, $zero, 10
    syscall
    j L_00400044
L_00400064:
    addiu $v0, $zero, 4
    lui $a0, 4097
    ori $a0, $a0, 0
    syscall
    j L_00400078
L_00400078:
    j L_00400054
L_0040007C:
    addiu $v0, $zero, 4
    lui $a0, 4097
    ori $a0, $a0, 5
    syscall
    j L_00400090
L_00400090:
    j L_00400054
L_00400094:
    addiu $v0, $zero, 4
    lui $a0, 4097
    ori $a0, $a0, 10
    syscall
    j L_004000A8
L_004000A8:
    j L_00400054
L_004000AC:
    addiu $v0, $zero, 10
    syscall
