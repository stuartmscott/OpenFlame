/*
 * The OpenFlame Project <http://stuartmscott.github.io/OpenFlame/>.
 *
 * Copyright (C) 2015 OpenFlame Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package parser;

import internalrep.asm.AsmStmt;
import internalrep.asm.Data;
import internalrep.asm.arithmeticlogic.Add;
import internalrep.asm.arithmeticlogic.Convert;
import internalrep.asm.arithmeticlogic.Divide;
import internalrep.asm.arithmeticlogic.Modulos;
import internalrep.asm.arithmeticlogic.Multiply;
import internalrep.asm.arithmeticlogic.Subtract;
import internalrep.asm.controlflow.Call;
import internalrep.asm.controlflow.Jez;
import internalrep.asm.controlflow.Jle;
import internalrep.asm.controlflow.Jlz;
import internalrep.asm.controlflow.Jnz;
import internalrep.asm.controlflow.Label;
import internalrep.asm.controlflow.Return;
import internalrep.asm.datamovement.Copy;
import internalrep.asm.datamovement.Load;
import internalrep.asm.datamovement.LoadC;
import internalrep.asm.datamovement.Pop;
import internalrep.asm.datamovement.Push;
import internalrep.asm.datamovement.Store;
import internalrep.asm.special.BreakPoint;
import internalrep.asm.special.Command;
import internalrep.asm.special.Halt;
import internalrep.asm.special.Interrupt;
import internalrep.asm.special.InterruptReturn;
import internalrep.asm.special.Lock;
import internalrep.asm.special.Noop;
import internalrep.asm.special.Signal;
import internalrep.asm.special.Sleep;
import internalrep.asm.special.Unlock;
import internalrep.asm.special.Wait;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import lexer.Category;
import lexer.Lexer;
import main.Assembler;

public class Parser {

    private static final int NUM_GP_REGS = 64;//MAXIMUM OF 64
    private final Assembler assembler;
    private final Lexer lex;
    private final String filename;

    public Parser(Assembler assembler, Lexer lex, String filename) {
        this.assembler = assembler;
        this.lex = lex;
        this.filename = filename;
    }

    private int matchReg() {
        String s = match(Category.LOWERNAME);
        if (s.matches("r[0-9]*")) {
            int val = Integer.parseInt(s.substring(1));
            if (val > NUM_GP_REGS) {
                error("register index out of bounds");
            }
            return val;
        }
        error("register expected");
        return 0;
    }

    private void move() {
        try {
            lex.move();
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private String match(Category c) {
        try {
            return lex.match(c);
        } catch (Exception e) {
            error(e.getMessage());
        }
        return "";
    }

    private void error(String string) {
        error(lex.getLineNum(), string);
    }

    private void error(int lineNum, String string) {
        Assembler.syntaxError(filename, lineNum, string);
    }

    public void parse() {
        move();
        do {
            if (lex.currentIs(Category.INCLUDE)) {
                move();
                StringBuilder sb = new StringBuilder();
                while (!lex.currentIs(Category.UPPERNAME)) {
                    sb.append(match(Category.LOWERNAME));
                    match(Category.FSTOP);
                    sb.append(File.separatorChar);
                }
                sb.append(match(Category.UPPERNAME));
                sb.append(match(Category.FSTOP));
                String ext = match(Category.LOWERNAME);
                sb.append(ext);
                matchOptionalComment(); // ignored
                String fullName = sb.toString();
                if (ext.equals("fas")) {
                    includeAsm(fullName);
                } else {
                    includeData(fullName);
                }
            } else if (lex.currentIs(Category.PADDING)) {
                move();
                long count = matchLong();
                for (int i = 0; i < count;) {
                    i++;
                    assembler.addStatement(new Data(0, "padding " + i + "/" + count));
                }
            } else if (lex.currentIs(Category.UPPERNAME)) {
                // scan for constants
                int l = lex.getLineNum();
                String name = lex.getCurrentValue();
                move();
                assembler.addConstant(name, matchData(), filename, l);
            } else {
                assembler.addStatement(matchStmt());
            }
        } while (lex.getCurrentCategory() != Category.EOF);
    }

    public void includeAsm(String fullName) {
        System.out.println("including asm " + fullName);
        assembler.include(fullName);
    }

    public void includeData(String fullName) {
        System.out.println("including data " + fullName);
        try {
            FileChannel fc = FileChannel.open(Paths.get(fullName), StandardOpenOption.READ);
            ByteBuffer buf = ByteBuffer.allocate(4);
            try {
                int num = 0;
                fc.position(0);
                while (num >= 0) {
                    buf.clear();
                    num = fc.read(buf);
                    buf.rewind();
                    assembler.addStatement(new Data(buf.getInt(), ""));
                }
            } catch (IOException e) {
                Assembler.ioError(e);
            } finally {
                fc.close();
            }
        } catch (IOException e) {
            Assembler.ioError(e);
        }
    }

    private AsmStmt matchStmt() {
        int l = lex.getLineNum();
        String val = lex.getCurrentValue();
        if (lex.currentIs(Category.LABEL)) {
            move();
            Label lbl = new Label(val, matchOptionalComment());
            assembler.addLabel(lbl, filename, l);
            return lbl;
        } else if (lex.currentIs(Category.DATA)) {
            move();
            return matchData();
        }
        match(Category.LOWERNAME);
        if (val.startsWith("add")) {
            return new Add(val.endsWith("f"), matchReg(), matchReg(), matchReg(), matchOptionalComment());
        } else if (val.startsWith("sub")) {
            return new Subtract(val.endsWith("f"), matchReg(), matchReg(), matchReg(), matchOptionalComment());
        } else if (val.startsWith("mul")) {
            return new Multiply(val.endsWith("f"), matchReg(), matchReg(), matchReg(), matchOptionalComment());
        } else if (val.startsWith("div")) {
            return new Divide(val.endsWith("f"), matchReg(), matchReg(), matchReg(), matchOptionalComment());
        } else if (val.startsWith("mod")) {
            return new Modulos(val.endsWith("f"), matchReg(), matchReg(), matchReg(), matchOptionalComment());
        } else if (val.startsWith("convert")) {
            return new Convert(val.endsWith("f"), matchReg(), matchReg(), matchOptionalComment());
        } else if (val.equals("copy")) {
            return new Copy(matchReg(), matchReg(), matchOptionalComment());
        } else if (val.equals("loadc")) {
            if (lex.currentIs(Category.LABEL)) {
                String name = lex.getCurrentValue();
                move();
                return new LoadC(name, matchReg(), true, matchOptionalComment());
            } else if (lex.currentIs(Category.UPPERNAME)) {
                String name = lex.getCurrentValue();
                move();
                return new LoadC(name, matchReg(), false, matchOptionalComment());
            }
            return new LoadC(matchLong(), matchReg(), matchOptionalComment());
        } else if (val.equals("load")) {
            int r = matchReg();
            if (lex.currentIs(Category.LABEL)) {
                String name = lex.getCurrentValue();
                move();
                return new Load(r, name, matchReg(), true, matchOptionalComment());
            } else if (lex.currentIs(Category.UPPERNAME)) {
                String name = lex.getCurrentValue();
                move();
                return new Load(r, name, matchReg(), false, matchOptionalComment());
            }
            return new Load(r, matchLong(), matchReg(), matchOptionalComment());
        } else if (val.equals("store")) {
            int r = matchReg();
            if (lex.currentIs(Category.LABEL)) {
                String name = lex.getCurrentValue();
                move();
                return new Store(r, name, matchReg(), true, matchOptionalComment());
            } else if (lex.currentIs(Category.UPPERNAME)) {
                String name = lex.getCurrentValue();
                move();
                return new Store(r, name, matchReg(), false, matchOptionalComment());
            }
            return new Store(r, matchLong(), matchReg(), matchOptionalComment());
        } else if (val.equals("push")) {
            return new Push(matchRegList(true), matchOptionalComment());
        } else if (val.equals("pop")) {
            return new Pop(matchRegList(false), matchOptionalComment());
        } else if (val.equals("jez")) {
            return new Jez(matchReg(), matchLabel(), matchOptionalComment());
        } else if (val.equals("jnz")) {
            return new Jnz(matchReg(), matchLabel(), matchOptionalComment());
        } else if (val.equals("jlz")) {
            return new Jlz(matchReg(), matchLabel(), matchOptionalComment());
        } else if (val.equals("jle")) {
            return new Jle(matchReg(), matchLabel(), matchOptionalComment());
        } else if (val.equals("call")) {
            return new Call(matchReg(), matchOptionalComment());
        } else if (val.equals("ret")) {
            return new Return(matchReg(), matchOptionalComment());
        } else if (val.equals("break")) {
            return new BreakPoint(matchLong(), matchOptionalComment());
        } else if (val.equals("halt")) {
            return new Halt(matchOptionalComment());
        } else if (val.equals("sleep")) {
            return new Sleep(matchOptionalComment());
        } else if (val.equals("wait")) {
            return new Wait(matchOptionalComment());
        } else if (val.equals("noop")) {
            return new Noop(matchOptionalComment());
        } else if (val.equals("cmd")) {
            if (lex.currentIs(Category.UPPERNAME)) {
                return new Command(matchConstant(), matchReg(), matchOptionalComment());
            }
            return new Command(matchLong(), matchReg(), matchOptionalComment());
        } else if (val.equals("signal")) {
            return new Signal(matchReg(), matchOptionalComment());
        } else if (val.equals("intr")) {
            if (lex.currentIs(Category.UPPERNAME)) {
                return new Interrupt(matchConstant(), matchOptionalComment());
            }
            return new Interrupt(matchLong(), matchOptionalComment());
        } else if (val.equals("iret")) {
            return new InterruptReturn(matchReg(), matchOptionalComment());
        } else if (val.equals("lock")) {
            return new Lock(matchOptionalComment());
        } else if (val.equals("unlock")) {
            return new Unlock(matchOptionalComment());
        }
        error(l, "unrecognised instruction: " + val);
        return null;
    }

    private String matchOptionalComment() {
        if (lex.currentIs(Category.COMMENT)) {
            // Get the comment, minus the slashes
            String comment = lex.getCurrentValue().substring(2);
            move();
            return comment;
        }
        return "";
    }

    private Data matchData() {
        if (lex.currentIs(Category.LABEL)) {
            String value = lex.getCurrentValue();
            move();
            return new Data(value, matchOptionalComment());
        }
        return new Data(matchLong(), matchOptionalComment());
    }

    private long matchLong() {
        return Long.parseLong(match(Category.NUMBER_LITERAL));
    }

    private String matchLabel() {
        return match(Category.LABEL);
    }

    private String matchConstant() {
        return match(Category.UPPERNAME);
    }

    private long matchRegList(boolean ascending) {
        long mask = 0;
        int i = ascending ? 3 : NUM_GP_REGS;
        // If the next two tokens are full stops then match range, else match comma separated list
        if (lex.peek().cat.equals(Category.FSTOP)) {
            int regStart = matchReg();
            match(Category.FSTOP);
            match(Category.FSTOP);
            int regEnd = matchReg();
            if (ascending && (regStart <= i || regEnd <= regStart))
                error("register list is not in ascending order");
            else if (!ascending && (regStart >= i || regEnd >= regStart))
                error("register list is not in descending order");
            int start = ascending ? regStart : regEnd;
            int end = ascending ? regEnd : regStart;
            for (int j = start; j <= end; j++) {
                mask = mask | (1L << (NUM_GP_REGS - 1 - j));
            }
        } else {
            boolean repeat;
            do {
                int reg = matchReg();
                if (ascending && reg <= i)
                    error("register list is not in ascending order");
                else if (!ascending && reg >= i)
                    error("register list is not in descending order");
                i = reg;
                mask = mask | (1L << (NUM_GP_REGS - 1 - i));
                repeat = lex.currentIs(Category.COMMA);
                if (repeat)
                    move();
            } while (repeat);
        }
        return mask;
    }

}
