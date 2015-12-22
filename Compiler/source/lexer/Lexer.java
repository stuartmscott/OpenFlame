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
package lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import main.Compiler;

public class Lexer {

    private final static ArrayList<Lexem> LEXEMS = new ArrayList<Lexem>();

    private final String mFilename;
    private ArrayList<Token> mTokens;
    private Token mCurrent;
    private int mIndex = 0;
    private int mLineNum = 1;

    public Lexer(File file) {
        mFilename = file.getName();
        try {
            FileReader in = new FileReader(file);
            mTokens = createTokens(in);
            mTokens.add(new Token(Category.END_OF_FILE));
        } catch (FileNotFoundException e) {
            Compiler.error(e.getMessage());
        }
    }

    public void include(File file) {
        try {
            FileReader in = new FileReader(file);
            mTokens.addAll(mIndex, createTokens(in));
        } catch (FileNotFoundException e) {
            Compiler.error(e.getMessage());
        }
    }

    public void insert(ArrayList<Token> ts) {
        mTokens.addAll(mIndex, ts);
    }

    private static ArrayList<Token> createTokens(FileReader in) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        try {
            int input;
            char characterInput;
            String current = "";
            String next;
            while ((input = in.read()) != -1) {
                characterInput = (char) input;
                next = current + characterInput;
                Token tc = getToken(current);
                Token tn = getToken(next);
                if (tc == null) {
                    current = next;
                } else {
                    if (tn == null) {
                        tokens.add(tc);
                        current = characterInput + "";
                    } else {
                        current = next;
                    }
                }
            }
            if (!current.equals("")) {
                tokens.add(getToken(current));
            }
        } catch (IOException e) {
            Compiler.error(e.getMessage());
        }
        return tokens;
    }

    private static Token getToken(String s) {
        for (Lexem l : LEXEMS) {
            if (s.matches(l.mRegex)) {
                return new Token(l.mCategory, s);
            }
        }
        return null;
    }

    public void move() {
        mCurrent = nextToken();
        if (mCurrent == null) {
            error("end of file reached unexpectedly");
        }
    }

    public boolean currentIs(Category c) {
        return mCurrent.mCategory == c;
    }

    public String match(Category c) {
        if (!currentIs(c)) {
            error("Expected \"" + c + "\", found \"" + mCurrent.mCategory + " (" + mCurrent.mValue
                    + ")\" near:\n" + getSurrounding());
        }
        String value = mCurrent.mValue;
        if (!currentIs(Category.END_OF_FILE)) {
            move();
        }
        return value;
    }

    public Token getCurrent() {
        return mCurrent;
    }

    public Category getCurrentCategory() {
        return mCurrent.mCategory;
    }

    public String getCurrentValue() {
        return mCurrent.mValue;
    }

    public int getLineNumber() {
        return mLineNum;
    }

    public ArrayList<Token> getTokens() {
        return mTokens;
    }

    public Token nextToken() {
        Token t = mTokens.get(mIndex++);
        if (t != null) {
            switch (t.mCategory) {
            case COMMENT:
            case NEW_LINE:
                mLineNum++;// fall through
            case WHITESPACE:
                t = nextToken();
                break;
            }
        }
        return t;
    }

    public boolean replaceNext(Category target, Category replacement) {
        for (int i = mIndex; i < mTokens.size(); i++) {
            if (mTokens.get(i).mCategory == target) {
                mTokens.get(i).mCategory = replacement;
                return true;
            }
        }
        return false;
    }

    public String getSurrounding() {
        final int LIMIT = Math.min(mTokens.size(), 100);
        int start = mIndex - (LIMIT / 2);
        int end = start + LIMIT;
        if (start < 0) {
            start = 0;
        } else if (end >= mTokens.size()) {
            start = mTokens.size() - LIMIT;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LIMIT; i++) {
            Token t = mTokens.get(start + i);
            if (t != null) {
                sb.append(t.mValue);
            }
        }
        return sb.toString();
    }

    private void error(String string) {
        Compiler.syntaxError(mFilename, mLineNum, string);
    }

    static {
        LEXEMS.add(new Lexem(Category.NEW_LINE, "\n"));
        //LEXEMS.add(new Lexem(Category.WHITESPACE, " |\t"));
        LEXEMS.add(new Lexem(Category.WHITESPACE, "\\s"));
        LEXEMS.add(new Lexem(Category.ASSIGN, "="));
        LEXEMS.add(new Lexem(Category.COLON, ":"));
        LEXEMS.add(new Lexem(Category.COMMA, ","));
        LEXEMS.add(new Lexem(Category.FULL_STOP, "\\."));
        LEXEMS.add(new Lexem(Category.COMMENT, "//.*\n"));

        LEXEMS.add(new Lexem(Category.OCB, "\\{"));
        LEXEMS.add(new Lexem(Category.CCB, "\\}"));
        LEXEMS.add(new Lexem(Category.ORB, "\\("));
        LEXEMS.add(new Lexem(Category.CRB, "\\)"));
        LEXEMS.add(new Lexem(Category.OSB, "\\["));
        LEXEMS.add(new Lexem(Category.CSB, "\\]"));
        LEXEMS.add(new Lexem(Category.OAB, "\\<"));
        LEXEMS.add(new Lexem(Category.CAB, "\\>"));

        // File Structure
        LEXEMS.add(new Lexem(Category.IMPORT, "import"));
        LEXEMS.add(new Lexem(Category.MODULE, "module"));
        LEXEMS.add(new Lexem(Category.CLASS, "class"));
        LEXEMS.add(new Lexem(Category.PROTOCOL, "protocol"));

        // Creation
        LEXEMS.add(new Lexem(Category.NEW, "new"));

        // Control
        LEXEMS.add(new Lexem(Category.IF, "if"));
        LEXEMS.add(new Lexem(Category.ELSE, "else"));
        LEXEMS.add(new Lexem(Category.DO, "do"));
        LEXEMS.add(new Lexem(Category.WHILE, "while"));
        LEXEMS.add(new Lexem(Category.FOR, "for"));
        LEXEMS.add(new Lexem(Category.MATCH, "match"));
        LEXEMS.add(new Lexem(Category.RETURN, "return"));

        // Assembly
        LEXEMS.add(new Lexem(Category.ASM, "asm"));

        // add support for hex/binary number_literals
        LEXEMS.add(new Lexem(Category.NUMBER_LITERAL, "0|[+-]?[1-9][0-9]*|[+-]?[0-9]*\\.[0-9]+"));
        LEXEMS.add(new Lexem(Category.BOOLEAN_LITERAL, "true|false"));
        // add support for escape characters
        LEXEMS.add(new Lexem(Category.STRING_LITERAL, "\".*\""));
        LEXEMS.add(new Lexem(Category.UPPERNAME, "[A-Z][_a-zA-Z0-9]*"));
        LEXEMS.add(new Lexem(Category.LOWERNAME, "[_a-z][_a-zA-Z0-9]*"));
    }

}