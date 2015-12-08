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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import lexer.Category;
import lexer.Lexer;
import main.Assembler;

public class Parser {

    private static final int GENERAL_PURPOSE_REGISTER_COUNT = 64;//MAXIMUM OF 64
    private final Assembler mAssembler;
    private final Lexer mLexer;
    private final File mFile;
    private final String mFilename;

    public Parser(Assembler assembler, Lexer lexer, File file) {
        mAssembler = assembler;
        mLexer = lexer;
        mFile = file;
        mFilename = file.getName();
    }

    private int matchRegister() {
        String string = match(Category.LOWERNAME);
        if (string.matches("r[0-9]*")) {
            int val = Integer.parseInt(string.substring(1));
            if (val > GENERAL_PURPOSE_REGISTER_COUNT) {
                error("register index out of bounds");
            }
            return val;
        }
        error("register expected");
        return 0;
    }

    private void move() {
        try {
            mLexer.move();
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private String match(Category c) {
        try {
            return mLexer.match(c);
        } catch (Exception e) {
            error(e.getMessage());
        }
        return "";
    }

    private void error(String string) {
        error(mLexer.getLineNumber(), string);
    }

    private void error(int lineNum, String string) {
        Assembler.syntaxError(mFilename, lineNum, string);
    }

    public void parse() {
        move();
        do {
            if (mLexer.currentIs(Category.INCLUDE)) {
                move();
                StringBuilder sb = new StringBuilder();
                while (!mLexer.currentIs(Category.UPPERNAME)) {
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
            } else if (mLexer.currentIs(Category.PADDING)) {
                move();
                long count = matchLong();
                for (int i = 0; i < count;) {
                    i++;
                    mAssembler.addStatement(new Data(0, "padding " + i + "/" + count));
                }
            } else if (mLexer.currentIs(Category.UPPERNAME)) {
                // scan for constants
                int l = mLexer.getLineNumber();
                String name = mLexer.getCurrentValue();
                move();
                mAssembler.addConstant(name, matchData(), mFilename, l);
            } else {
                mAssembler.addStatement(matchStatement());
            }
        } while (mLexer.getCurrentCategory() != Category.END_OF_FILE);
    }

    public void includeAsm(String fullName) {
        System.out.println("including asm " + fullName);
        mAssembler.include(new File(mFile.getParent(), fullName));
    }

    public void includeData(String fullName) {
        System.out.println("including data " + fullName);
        try {
            Path path = Paths.get(String.format("%s/%s", mFile.getParent(), fullName));
            FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer buf = ByteBuffer.allocate(4);
            try {
                int num = 0;
                fc.position(0);
                while (num >= 0) {
                    buf.clear();
                    num = fc.read(buf);
                    buf.rewind();
                    mAssembler.addStatement(new Data(buf.getInt(), ""));
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

    private AsmStmt matchStatement() {
        int lineNumber = mLexer.getLineNumber();
        String value = mLexer.getCurrentValue();
        if (mLexer.currentIs(Category.LABEL)) {
            move();
            Label label = new Label(value, matchOptionalComment());
            mAssembler.addLabel(label, mFilename, lineNumber);
            return label;
        } else if (mLexer.currentIs(Category.DATA)) {
            move();
            return matchData();
        }
        match(Category.LOWERNAME);
        if (value.startsWith("add")) {
            return new Add(value.endsWith("f"), matchRegister(), matchRegister(), matchRegister(), matchOptionalComment());
        } else if (value.startsWith("sub")) {
            return new Subtract(value.endsWith("f"), matchRegister(), matchRegister(), matchRegister(), matchOptionalComment());
        } else if (value.startsWith("mul")) {
            return new Multiply(value.endsWith("f"), matchRegister(), matchRegister(), matchRegister(), matchOptionalComment());
        } else if (value.startsWith("div")) {
            return new Divide(value.endsWith("f"), matchRegister(), matchRegister(), matchRegister(), matchOptionalComment());
        } else if (value.startsWith("mod")) {
            return new Modulos(value.endsWith("f"), matchRegister(), matchRegister(), matchRegister(), matchOptionalComment());
        } else if (value.startsWith("convert")) {
            return new Convert(value.endsWith("f"), matchRegister(), matchRegister(), matchOptionalComment());
        } else if (value.equals("copy")) {
            return new Copy(matchRegister(), matchRegister(), matchOptionalComment());
        } else if (value.equals("loadc")) {
            if (mLexer.currentIs(Category.LABEL)) {
                String name = mLexer.getCurrentValue();
                move();
                return new LoadC(name, matchRegister(), true, matchOptionalComment());
            } else if (mLexer.currentIs(Category.UPPERNAME)) {
                String name = mLexer.getCurrentValue();
                move();
                return new LoadC(name, matchRegister(), false, matchOptionalComment());
            }
            return new LoadC(matchLong(), matchRegister(), matchOptionalComment());
        } else if (value.equals("load")) {
            int r = matchRegister();
            if (mLexer.currentIs(Category.LABEL)) {
                String name = mLexer.getCurrentValue();
                move();
                return new Load(r, name, matchRegister(), true, matchOptionalComment());
            } else if (mLexer.currentIs(Category.UPPERNAME)) {
                String name = mLexer.getCurrentValue();
                move();
                return new Load(r, name, matchRegister(), false, matchOptionalComment());
            }
            return new Load(r, matchLong(), matchRegister(), matchOptionalComment());
        } else if (value.equals("store")) {
            int r = matchRegister();
            if (mLexer.currentIs(Category.LABEL)) {
                String name = mLexer.getCurrentValue();
                move();
                return new Store(r, name, matchRegister(), true, matchOptionalComment());
            } else if (mLexer.currentIs(Category.UPPERNAME)) {
                String name = mLexer.getCurrentValue();
                move();
                return new Store(r, name, matchRegister(), false, matchOptionalComment());
            }
            return new Store(r, matchLong(), matchRegister(), matchOptionalComment());
        } else if (value.equals("push")) {
            return new Push(matchRegisterList(true), matchOptionalComment());
        } else if (value.equals("pop")) {
            return new Pop(matchRegisterList(false), matchOptionalComment());
        } else if (value.equals("jez")) {
            return new Jez(matchRegister(), matchLabel(), matchOptionalComment());
        } else if (value.equals("jnz")) {
            return new Jnz(matchRegister(), matchLabel(), matchOptionalComment());
        } else if (value.equals("jlz")) {
            return new Jlz(matchRegister(), matchLabel(), matchOptionalComment());
        } else if (value.equals("jle")) {
            return new Jle(matchRegister(), matchLabel(), matchOptionalComment());
        } else if (value.equals("call")) {
            return new Call(matchRegister(), matchOptionalComment());
        } else if (value.equals("ret")) {
            return new Return(matchRegister(), matchOptionalComment());
        } else if (value.equals("break")) {
            return new BreakPoint(matchLong(), matchOptionalComment());
        } else if (value.equals("halt")) {
            return new Halt(matchOptionalComment());
        } else if (value.equals("sleep")) {
            return new Sleep(matchOptionalComment());
        } else if (value.equals("wait")) {
            return new Wait(matchOptionalComment());
        } else if (value.equals("noop")) {
            return new Noop(matchOptionalComment());
        } else if (value.equals("cmd")) {
            if (mLexer.currentIs(Category.UPPERNAME)) {
                return new Command(matchConstant(), matchRegister(), matchOptionalComment());
            }
            return new Command(matchLong(), matchRegister(), matchOptionalComment());
        } else if (value.equals("signal")) {
            return new Signal(matchRegister(), matchOptionalComment());
        } else if (value.equals("intr")) {
            if (mLexer.currentIs(Category.UPPERNAME)) {
                return new Interrupt(matchConstant(), matchOptionalComment());
            }
            return new Interrupt(matchLong(), matchOptionalComment());
        } else if (value.equals("iret")) {
            return new InterruptReturn(matchRegister(), matchOptionalComment());
        } else if (value.equals("lock")) {
            return new Lock(matchOptionalComment());
        } else if (value.equals("unlock")) {
            return new Unlock(matchOptionalComment());
        }
        error(lineNumber, "unrecognised instruction: " + value);
        return null;
    }

    private String matchOptionalComment() {
        if (mLexer.currentIs(Category.COMMENT)) {
            // Get the comment, minus the slashes
            String comment = mLexer.getCurrentValue().substring(2);
            move();
            return comment;
        }
        return "";
    }

    private Data matchData() {
        if (mLexer.currentIs(Category.LABEL)) {
            String value = mLexer.getCurrentValue();
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

    private long matchRegisterList(boolean ascending) {
        long mask = 0;
        int i = ascending ? 3 : GENERAL_PURPOSE_REGISTER_COUNT;

        int register = matchRegister();
        // If the next two tokens are full stops then match range, else match comma separated list
        if (mLexer.currentIs(Category.FSTOP)) {
            match(Category.FSTOP);
            match(Category.FSTOP);
            int endRegister = matchRegister();
            if (ascending && (register <= i || endRegister <= register)) {
                error("register list is not in ascending order");
            } else if (!ascending && (register >= i || endRegister >= register)) {
                error("register list is not in descending order");
            }
            int start = ascending ? register : endRegister;
            int end = ascending ? endRegister : register;
            for (int j = start; j <= end; j++) {
                mask = mask | (1L << (GENERAL_PURPOSE_REGISTER_COUNT - 1 - j));
            }
        } else {
            boolean repeat;
            do {
                if (ascending && register <= i) {
                    error("register list is not in ascending order");
                } else if (!ascending && register >= i) {
                    error("register list is not in descending order");
                }
                i = register;
                mask = mask | (1L << (GENERAL_PURPOSE_REGISTER_COUNT - 1 - i));
                repeat = mLexer.currentIs(Category.COMMA);
                if (repeat) {
                    move();
                }
            } while (repeat);
        }
        return mask;
    }

}
