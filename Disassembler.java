import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Scanner;

/*
 * Disassembler for the custom MHC1 files made by Assembler.java.
 * Supports fizzbuzz.mhc-ascii and fizzbuzz.mhc-bin.
 * Run: javac Assembler.java Disassembler.java
 *      java Disassembler fizzbuzz.mhc-bin
 *   or java Disassembler fizzbuzz.mhc-ascii
 * Writes Reconstructed.asm and checks a byte-for-byte round trip.
 * Byte order: big-endian. Delayed branching must be OFF in MARS.
 */
public class Disassembler {
    static final int TEXT_BASE = 0x00400000;
    static final int DATA_BASE = 0x10010000;
    static final String[] REG = {
        "$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
        "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
        "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
        "$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
    };

    static class Sections {
        byte[] text, data;
        Sections(byte[] t, byte[] d) { text = t; data = d; }
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: java Disassembler [fizzbuzz.mhc-bin | fizzbuzz.mhc-ascii]");
            return;
        }
        String input = args.length == 0 ? "fizzbuzz.mhc-bin" : args[0];
        try {
            Sections original;
            if (input.endsWith(".mhc-bin")) {
                original = readBinary(input);
            } else if (input.endsWith(".mhc-ascii")) {
                original = readAscii(input);
            } else {
                throw new IllegalArgumentException("Input must end in .mhc-bin or .mhc-ascii");
            }

            String assembly = disassemble(original);
            // Verify by assembling the reconstructed source with the Part 3 assembler.
            Assembler.Result rebuilt = Assembler.assemble(assembly);
            if (!Arrays.equals(original.text, rebuilt.text) ||
                !Arrays.equals(original.data, rebuilt.data)) {
                throw new IllegalArgumentException("Verification FAILED: assembled bytes do not match input.");
            }

            Files.write(Paths.get("Reconstructed.asm"), assembly.getBytes(StandardCharsets.UTF_8));
            System.out.println("Created Reconstructed.asm");
            System.out.println("Input: " + input);
            System.out.println("Instructions: " + (original.text.length / 4));
            System.out.println("Data bytes: " + original.data.length);
            System.out.println("Verification PASSED: reassembled text and data bytes match exactly.");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Disassembly failed: " + e.getMessage());
        }
    }

    // Read the MHC1 binary header, then instructions and data as raw bytes.
    static Sections readBinary(String filename) throws IOException {
        File file = new File(filename);
        if (!file.isFile()) throw new IOException("Cannot find " + filename + " in " + System.getProperty("user.dir"));
        if (file.length() < 20) throw new IOException("Binary file is too short for the MHC1 header.");
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            byte[] magic = new byte[4];
            in.readFully(magic);
            if (!Arrays.equals(magic, "MHC1".getBytes(StandardCharsets.US_ASCII)))
                throw new IllegalArgumentException("Invalid MHC1 file header.");
            int textBase = in.readInt();
            int textSize = in.readInt();
            int dataBase = in.readInt();
            int dataSize = in.readInt();
            if (textBase != TEXT_BASE || dataBase != DATA_BASE)
                throw new IllegalArgumentException("Unexpected .text or .data starting address.");
            if (textSize < 0 || textSize % 4 != 0 || dataSize < 0 ||
                (long) textSize + dataSize + 20 != file.length())
                throw new IllegalArgumentException("Invalid section sizes in the binary header.");
            byte[] text = new byte[textSize];
            byte[] data = new byte[dataSize];
            in.readFully(text);
            in.readFully(data);
            return new Sections(text, data);
        }
    }

    // Read the text format from Assembler.writeAscii(): 32 bits for code,
    // one 8-bit line per data byte. The section lines distinguish the two.
    static Sections readAscii(String filename) throws IOException {
        File file = new File(filename);
        if (!file.isFile()) throw new IOException("Cannot find " + filename + " in " + System.getProperty("user.dir"));
        ByteArrayOutputStream text = new ByteArrayOutputStream();
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        int section = 0, textSize = -1, dataSize = -1, lineNo = 0;
        try (Scanner scanner = new Scanner(file, "UTF-8")) {
            while (scanner.hasNextLine()) {
                lineNo++;
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("# .text base=")) {
                    if (section != 0) throw new IllegalArgumentException("Unexpected .text section at line " + lineNo);
                    String[] parts = line.split("\\s+");
                    if (parts.length != 4 || !parts[2].equals("base=0x00400000"))
                        throw new IllegalArgumentException("Invalid .text header at line " + lineNo);
                    textSize = readSize(parts[3], lineNo);
                    section = 1;
                } else if (line.startsWith("# .data base=")) {
                    if (section != 1) throw new IllegalArgumentException("Unexpected .data section at line " + lineNo);
                    String[] parts = line.split("\\s+");
                    if (parts.length != 4 || !parts[2].equals("base=0x10010000"))
                        throw new IllegalArgumentException("Invalid .data header at line " + lineNo);
                    dataSize = readSize(parts[3], lineNo);
                    section = 2;
                } else if (line.startsWith("#")) {
                    continue;
                } else if (section == 1 && line.matches("[01]{32}")) {
                    for (int i = 0; i < 32; i += 8) text.write(Integer.parseInt(line.substring(i, i + 8), 2));
                } else if (section == 2 && line.matches("[01]{8}")) {
                    data.write(Integer.parseInt(line, 2));
                } else {
                    throw new IllegalArgumentException("Invalid binary digits or section at line " + lineNo + ": " + line);
                }
            }
        }
        if (section != 2 || textSize != text.size() || dataSize != data.size())
            throw new IllegalArgumentException("Missing section or byte counts do not match the ASCII header.");
        return new Sections(text.toByteArray(), data.toByteArray());
    }

    static int readSize(String token, int line) {
        if (!token.matches("bytes=[0-9]+"))
            throw new IllegalArgumentException("Invalid byte count on line " + line);
        return Integer.parseInt(token.substring(6));
    }

    // Convert each 32-bit word to a real MIPS instruction. Create new label
    // names for branch/jump targets, since original names are not in the bytes.
    static String disassemble(Sections file) {
        int count = file.text.length / 4;
        int[] words = new int[count];
        for (int i = 0; i < count; i++) {
            int p = i * 4;
            words[i] = ((file.text[p] & 255) << 24) |
                       ((file.text[p + 1] & 255) << 16) |
                       ((file.text[p + 2] & 255) << 8) |
                       (file.text[p + 3] & 255);
        }
        Map<Integer, String> labels = new TreeMap<>();
        for (int i = 0; i < count; i++) {
            int word = words[i], op = word >>> 26;
            int pc = TEXT_BASE + i * 4;
            int target = 0;
            if (op == 4 || op == 5) {
                target = pc + 4 + ((short) (word & 65535) * 4);
            } else if (op == 2 || op == 3) {
                target = ((pc + 4) & 0xF0000000) | ((word & 0x03FFFFFF) << 2);
            } else {
                continue;
            }
            if (target < TEXT_BASE || target >= TEXT_BASE + file.text.length || ((target - TEXT_BASE) % 4 != 0))
                throw new IllegalArgumentException("Branch/jump at 0x" + hex(pc) + " targets outside .text: 0x" + hex(target));
            labels.put(target, "L_" + hex(target));
        }

        StringBuilder output = new StringBuilder();
        if (file.data.length > 0) {
            output.append(".data\n");
            int start = 0;
            while (start < file.data.length) {
                int end = start;
                while (end < file.data.length && file.data[end] != 0) end++;
                if (end == file.data.length)
                    throw new IllegalArgumentException("Data at offset " + start + " is missing the .asciiz zero terminator.");
                output.append("data_").append(start).append(": .asciiz \"");
                for (int k = start; k < end; k++) {
                    int c = file.data[k] & 255;
                    if (c == '\n') output.append("\\n");
                    else if (c == '\r') output.append("\\r");
                    else if (c == '\t') output.append("\\t");
                    else if (c == '"') output.append("\\\"");
                    else if (c == '\\') output.append("\\\\");
                    else if (c >= 32 && c <= 126) output.append((char) c);
                    else throw new IllegalArgumentException("Unsupported data byte 0x" +
                            Integer.toHexString(c) + " at data offset " + k);
                }
                output.append("\"\n");
                start = end + 1;
            }
        }
        output.append(".text\n");
        for (int i = 0; i < count; i++) {
            int pc = TEXT_BASE + i * 4;
            if (labels.containsKey(pc)) output.append(labels.get(pc)).append(":\n");
            output.append("    ").append(decode(words[i], pc, labels)).append('\n');
        }
        return output.toString();
    }

    static String decode(int w, int pc, Map<Integer, String> labels) {
        int op = w >>> 26;
        int rs = (w >>> 21) & 31, rt = (w >>> 16) & 31;
        int rd = (w >>> 11) & 31, shamt = (w >>> 6) & 31;
        int funct = w & 63;
        int imm = (short) w;
        switch (op) {
            case 0:
                if (w == 12) return "syscall";
                if (funct == 0x21 && shamt == 0) return "addu " + REG[rd] + ", " + REG[rs] + ", " + REG[rt];
                if (funct == 0x1A && rd == 0 && shamt == 0) return "div " + REG[rs] + ", " + REG[rt];
                if (funct == 0x10 && rs == 0 && rt == 0 && shamt == 0) return "mfhi " + REG[rd];
                if (funct == 0 && rs == 0) return "sll " + REG[rd] + ", " + REG[rt] + ", " + shamt;
                if (funct == 8 && rt == 0 && rd == 0 && shamt == 0) return "jr " + REG[rs];
                break;
            case 8: return "addi " + REG[rt] + ", " + REG[rs] + ", " + imm;
            case 9: return "addiu " + REG[rt] + ", " + REG[rs] + ", " + imm;
            case 13: return "ori " + REG[rt] + ", " + REG[rs] + ", " + (w & 65535);
            case 15:
                if (rs == 0) return "lui " + REG[rt] + ", " + (w & 65535);
                break;
            case 4:
            case 5:
                int branch = pc + 4 + imm * 4;
                return (op == 4 ? "beq " : "bne ") + REG[rs] + ", " + REG[rt] + ", " + labels.get(branch);
            case 2:
            case 3:
                int jump = ((pc + 4) & 0xF0000000) | ((w & 0x03FFFFFF) << 2);
                return (op == 2 ? "j " : "jal ") + labels.get(jump);
            case 35: return "lw " + REG[rt] + ", " + imm + "(" + REG[rs] + ")";
            case 43: return "sw " + REG[rt] + ", " + imm + "(" + REG[rs] + ")";
        }
        throw new IllegalArgumentException("Unsupported MIPS word 0x" + hex(w) + " at 0x" + hex(pc));
    }

    static String hex(int n) { return String.format("%08X", n); }
}
