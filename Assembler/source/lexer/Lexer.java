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

import main.Assembler;

public class Lexer {

    private static ArrayList<Lexem> lexems = new ArrayList<Lexem>();

    private final String filename;
    private ArrayList<Token> tokens;
    private Token current;
    private int index = 0;
    private int lineNum = 1;

    public Lexer(String filename) {
        this.filename = filename;
        try {
            FileReader in = new FileReader(new File(filename));
            tokens = createTokens(in);
            tokens.add(new Token(Category.EOF));
        } catch (FileNotFoundException e) {
            Assembler.error(e.getMessage());
        }
    }

    public void include(File file) {
        try {
            FileReader in = new FileReader(file);
            tokens.addAll(index, createTokens(in));
        } catch (FileNotFoundException e) {
            Assembler.error(e.getMessage());
        }
    }
    
    public void insert(ArrayList<Token> ts) {
        tokens.addAll(index, ts);
    }

    private static ArrayList<Token> createTokens(FileReader in) {
        ArrayList<Token> ts = new ArrayList<Token>();
        try {
            int i;
            String current = "";
            String c;
            String next;
            while ((i = in.read()) != -1) {
                c = (char) i + "";
                next = current + c;
                Token tc = getToken(current);
                Token tn = getToken(next);
                if (tc == null) {
                    current = next;
                } else {
                    if (tn == null) {
                        ts.add(tc);
                        current = c;
                    } else {
                        current = next;
                    }
                }
            }
            if (!current.equals(""))
                ts.add(getToken(current));
        } catch (IOException e) {
            Assembler.error(e.getMessage());
        }
        return ts;
    }

    private static Token getToken(String s) {
        for (Lexem l : lexems)
            if (s.matches(l.regex))
                return new Token(l.cat, s);
        return null;
    }

    public void move() {
        current = nextToken();
        if (current == null) {
            error("end of file reached unexpectedly");
        }
    }

    public boolean currentIs(Category c) {
        return current.cat == c;
    }

    public boolean nextIs(Category c) {
        return peek().cat == c;
    }

    public String match(Category c) {
        if (!currentIs(c)) {
            error("Expected \"" + c + "\", found \"" + current.cat + " (" + current.value + ")\" near:\n" + getSurrounding());
        }
        String value = current.value;
        if (!currentIs(Category.EOF)) {
            move();
        }
        return value;
    }

    public Token getCurrent() {
        return current;
    }

    public Category getCurrentCategory() {
        return current.cat;
    }

    public String getCurrentValue() {
        return current.value;
    }

    public int getLineNum() {
        return lineNum;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public Token nextToken() {
        Token t = tokens.get(index++);
        if (t != null) {
            switch (t.cat) {
            case COMMENT:
            case NEWLINE:
                lineNum++;// fall through
            case WHITESPACE:
                t = nextToken();
                break;
            }
        }
        return t;
    }

    public Token peek() {
        return tokens.get(index);
    }

    public boolean replaceNext(Category target, Category replacement) {
        for (int i = index; i < tokens.size(); i++) {
            if (tokens.get(i).cat == target) {
                tokens.get(i).cat = replacement;
                return true;
            }
        }
        return false;
    }

    public String getSurrounding() {
        final int LIMIT = Math.min(tokens.size(), 100);
        int start = index - (LIMIT / 2);
        int end = start + LIMIT;
        if (start < 0) {
            start = 0;
        } else if (end >= tokens.size()) {
            start = tokens.size() - LIMIT;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LIMIT; i++) {
            Token t = tokens.get(start + i);
            if (t != null) {
                sb.append(t.value);
            }
        }
        return sb.toString();
    }

    private void error(String string) {
        Assembler.syntaxError(filename, lineNum, string);
    }

    static {
        lexems.add(new Lexem(Category.NEWLINE, "\n"));
        //lexems.add(new Lexem(Category.WHITESPACE, " |\t"));
        lexems.add(new Lexem(Category.WHITESPACE, "\\s"));
        lexems.add(new Lexem(Category.ASSIGN, "="));
        lexems.add(new Lexem(Category.COLON, ":"));
        // lexems.add(new Lexem(Category.COMMA, ","));
        lexems.add(new Lexem(Category.FSTOP, "\\."));
        lexems.add(new Lexem(Category.COMMENT, "//.*\n"));

        lexems.add(new Lexem(Category.OCB, "\\{"));
        lexems.add(new Lexem(Category.CCB, "\\}"));
        lexems.add(new Lexem(Category.ORB, "\\("));
        lexems.add(new Lexem(Category.CRB, "\\)"));
        lexems.add(new Lexem(Category.OSB, "\\["));
        lexems.add(new Lexem(Category.CSB, "\\]"));
        lexems.add(new Lexem(Category.OAB, "\\<"));
        lexems.add(new Lexem(Category.CAB, "\\>"));

        // File Structure
        lexems.add(new Lexem(Category.IMPORT, "import"));
        lexems.add(new Lexem(Category.MODULE, "module"));
        lexems.add(new Lexem(Category.CLASS, "class"));
        lexems.add(new Lexem(Category.PROTOCOL, "protocol"));

        // Creation
        lexems.add(new Lexem(Category.NEW, "new"));

        // Control
        lexems.add(new Lexem(Category.IF, "if"));
        lexems.add(new Lexem(Category.ELSE, "else"));
        lexems.add(new Lexem(Category.DO, "do"));
        lexems.add(new Lexem(Category.WHILE, "while"));
        lexems.add(new Lexem(Category.FOR, "for"));
        lexems.add(new Lexem(Category.MATCH, "match"));
        lexems.add(new Lexem(Category.RETURN, "return"));
        lexems.add(new Lexem(Category.BREAK, "break"));

        // Assembly
        lexems.add(new Lexem(Category.PADDING, "padding"));
        lexems.add(new Lexem(Category.INCLUDE, "include"));
        lexems.add(new Lexem(Category.DATA, "data"));
        lexems.add(new Lexem(Category.ASM, "asm"));
        lexems.add(new Lexem(Category.COMMA, ","));
        lexems.add(new Lexem(Category.LABEL, "#[\\.:_a-zA-Z0-9\\(\\)]+"));

        // add support for hex/binary number_literals
        lexems.add(new Lexem(Category.NUMBER_LITERAL, "0|[+-]?[1-9][0-9]*|[+-]?[0-9]*\\.[0-9]+"));
        lexems.add(new Lexem(Category.BOOLEAN_LITERAL, "true|false"));
        // add support for escape characters
        lexems.add(new Lexem(Category.STRING_LITERAL, "\".*\""));
        lexems.add(new Lexem(Category.UPPERNAME, "[A-Z][_a-zA-Z0-9]*"));
        lexems.add(new Lexem(Category.LOWERNAME, "[_a-z][_a-zA-Z0-9]*"));
    }

}