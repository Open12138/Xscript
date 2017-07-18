/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.simple.compiler.scanner;

/**
 * Simple language tokens (for more information about the Simple language, see
 * the Simple Language Guide).
 *
 * @author Herbert Czymontek
 */
public enum TokenKind {

    // Uninitialized token - this should never be seen by any clients of the scanner.
    TOK_NONE(""),

    // Identifier token.
    //标识符
    TOK_IDENTIFIER("<identifier>"),

    // Keyword tokens.
    //关键字标记。
    TOK_ALIAS("Alias"),
    TOK_AND("And"),
    TOK_AS("As"),
    TOK_BOOLEAN("Boolean"),
    TOK_BYREF("ByRef"),
    TOK_BYTE("Byte"),
    TOK_BYVAL("ByVal"),
    TOK_CASE("Case"),
    TOK_CONST("Const"),
    TOK_DATE("Date"),
    TOK_DIM("Dim"),
    TOK_DO("Do"),
    TOK_DOUBLE("Double"),
    TOK_EACH("Each"),
    TOK_ELSE("Else"),
    TOK_ELSEIF("ElseIf"),
    TOK_END("End"),
    TOK_ERROR("Error"),
    TOK_EVENT("Event"),
    TOK_EXIT("Exit"),
    TOK_FOR("For"),
    TOK_FUNCTION("Function"),
    TOK_GET("Get"),
    TOK_IF("If"),
    TOK_IN("In"),
    TOK_INTEGER("Integer"),
    TOK_IS("Is"),
    TOK_ISNOT("IsNot"),
    TOK_LIKE("Like"),
    TOK_LONG("Long"),
    TOK_ME("Me"),
    TOK_MOD("Mod"),
    TOK_NEXT("Next"),
    TOK_NEW("New"),
    TOK_NOT("Not"),
    TOK_NOTHING("Nothing"),
    TOK_OBJECT("Object"),
    TOK_ON("On"),
    TOK_OR("Or"),
    TOK_PROPERTY("Property"),
    TOK_RAISEEVENT("RaiseEvent"),
    TOK_SELECT("Select"),
    TOK_SET("Set"),
    TOK_SHORT("Short"),
    TOK_SINGLE("Single"),
    TOK_STATIC("Static"),
    TOK_STEP("Step"),
    TOK_STRING("String"),
    TOK_SUB("Sub"),
    TOK_THEN("Then"),
    TOK_TO("To"),
    TOK_TYPEOF("TypeOf"),
    TOK_UNTIL("Until"),
    TOK_VARIANT("Variant"),
    TOK_WHILE("While"),
    TOK_XOR("Xor"),

    // Constant literal tokens.  常数 文字
    TOK_BOOLEANCONSTANT("<boolean constant>"),
    TOK_NULLCONSTANT("<null constant>"),
    TOK_NUMERICCONSTANT("<numeric constant>"),//数值常量
    TOK_STRINGCONSTANT("<string constant>"),//字符串常量

    // Operator tokens.
    TOK_OPENPARENTHESIS("("),
    TOK_CLOSEPARENTHESIS(")"),
    TOK_DOT("."),
    TOK_COMMA(","),
    TOK_LESS("<"),
    TOK_GREATER(">"),
    TOK_LESSEQUAL("<="),
    TOK_GREATEREQUAL(">="),
    TOK_EQUAL("="),
    TOK_EXP("^"),
    TOK_NOTEQUAL("<>"),
    TOK_PLUS("+"),
    TOK_MINUS("-"),
    TOK_TIMES("*"),
    TOK_DIVIDE("/"),
    TOK_INTEGERDIVIDE("\\"),
    TOK_SHIFTLEFT("<<"),
    TOK_SHIFTRIGHT(">>"),
    TOK_AMPERSAND("&"),

    // Special end-of-statement and end-of-file token (note that end-of-statement is not the same as
    // end-of-line - there are line continuations ('_' followed by '/n' and equivalent) and the
    // explicit end-of-statement marker ':').
    TOK_EOS("end-of-statement"),
    TOK_EOF("end-of-file"),

    // The following aren't really keywords but defining tokens for them is quite helpful...
    TOK_TRUE("True"),
    TOK_FALSE("False"),

    // Properties section tokens (these tokens are only supposed to be found in the properties
    // section of a source file which is normally invisible to the programmers - unless they are
    // using any low-level external text editors not supporting the Simple language).
    TOK_$AS("$As"),
    TOK_$DEFINE("$Define"),
    TOK_$END("$End"),
    TOK_$FORM("$Form"),
    TOK_$INTERFACE("$Interface"),
    TOK_$OBJECT("$Object"),
    TOK_$PROPERTIES("$Properties"),
    TOK_$SOURCE("$Source"),

    // For syntax highlighters it is useful to have a scan mode that emits comment tokens
    TOK_COMMENT("'");

    // Token string value
    private final String string;

    /*
     * Creates a new token.
     */
    TokenKind(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}

