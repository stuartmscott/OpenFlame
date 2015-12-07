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

public enum Category {

    EOF, NEWLINE, WHITESPACE, ASSIGN,
    COLON, COMMA, FSTOP, //Special Characters
    COMMENT,
    OCB, CCB, ORB, CRB, OSB, CSB, OAB, CAB, //Brackets
    IMPORT, AS, MODULE, CLASS, PROTOCOL, //File structure
    NEW, //Creation
    IF, ELSE, DO, WHILE, FOR, MATCH, RETURN, BREAK, //Control
    NUMBER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL,//Literals
    UPPERNAME, LOWERNAME,//Identifiers
    INCLUDE, PADDING, DATA, ASM, LABEL,//Assembly
}