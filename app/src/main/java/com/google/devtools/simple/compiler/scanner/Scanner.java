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

import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.Compiler;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Scanner for the Simple language.
 * <p>
 * <p>There is one noteworthy thing: the scanner supports two different modes.
 * One for the properties section of the source files (which is supposed to be
 * invisible to the programmer unless the use an external editor other than the
 * one in the IDE) and for the actual source code section.
 * <p>
 * <p>I could have done without the modes, but I felt that it would be cleaner
 * to have the scanner not recognize keywords from either section in the other
 * one. Hence two different modes which only differ in the keywords recognized.
 *
 * @author Herbert Czymontek
 */
public final class Scanner {

    // Compiler instance. Needed for error reporting.
    private Compiler compiler;

    // Index of file currently being scanned.
    private int fileIndex;

    // Starting offset of properties section of source file
    private int propertiesSectionStartOffset;

    // Text buffer containing the source currently being scanned. Note that this does not have to be
    // the complete source file content. Also the number of input characters does not have to equal
    // the length of the source buffer (see bufferEndOffset below).
    private char[] sourceBuffer;

    // Current offset within the source buffer.
    private int sourceOffset;

    // Current line.
    private int sourceLine;

    // Current column.
    private int sourceColumn;

    // End offset within the source buffer.
    private int sourceEndOffset;

    // Current token.
    private TokenKind tokenKind;

    // Offset of the start of the current token.
    private int tokenStartOffset;

    // The line of the start of the current token.
    private int tokenStartLine;

    // The column of the start of the current token.
    private int tokenStartColumn;

    // Value of string constant (TOK_STRINGCONSTANT) and identifier tokens (TOK_IDENTIFIER).
    private String tokenValueString;

    // Value of boolean constant tokens (TOK_BOOLEANCONSTANT).
    private boolean tokenValueBoolean;

    // Value of number tokens (TOK_NUMBERCONSTANT).
    // Note: In the past I always had type specific numeric value holders, but I was thinking
    // of experimenting with a more 'natural' type system for numbers, as in natural, real and
    // complex numbers, more like we all learn in school. Using BigDecimal is very helpful for
    // this and it does not hurt otherwise either because it provides a lot of nice functionality
    // that I don't have to re-implement when using it.
    private BigDecimal tokenValueNumber;

    // Indicates whether the scanner should recognize keywords from the properties section of a
    // Simple source file.
    private boolean propertiesMode;

    // Generates a token for comments if true
    //如果是注释此值为true
    private boolean generateCommentToken;

    // Instance of the keyword recognizer.
    private static final Keywords keywords = new Keywords();

    // Mask for encoding source code positions (stored as 64-bit values). POS_SIZE should be 64.
    static private final int POS_OFFSET_SIZE = 30;        // 1MB
    static private final int POS_FILEINDEX_SIZE = 10;     // 1024
    static private final int POS_LINE_SIZE = 16;          // 65536
    static private final int POS_COLUMN_SIZE = 8;         // 256

    static private final int POS_SIZE =
            POS_OFFSET_SIZE + POS_FILEINDEX_SIZE + POS_LINE_SIZE + POS_COLUMN_SIZE;

    static private final int POS_OFFSET_SHIFT = POS_SIZE - POS_OFFSET_SIZE;
    static private final long POS_OFFSET_MASK = (1L << POS_OFFSET_SIZE) - 1;

    static private final int POS_FILEINDEX_SHIFT = POS_SIZE - POS_OFFSET_SIZE - POS_FILEINDEX_SIZE;
    static private final long POS_FILEINDEX_MASK = (1L << POS_FILEINDEX_SIZE) - 1;

    static private final int POS_LINE_SHIFT =
            POS_SIZE - POS_OFFSET_SIZE - POS_FILEINDEX_SIZE - POS_LINE_SIZE;
    static private final long POS_LINE_MASK = (1L << POS_LINE_SIZE) - 1;

    static private final int POS_COLUMN_SHIFT = 0;
    static private final long POS_COLUMN_MASK = (1L << POS_COLUMN_SIZE) - 1;

    /**
     * Special source code position used in situations where there is no source
     * code available.
     */
    public static final long NO_POSITION = 0L;

    /*
     * Encodes a source code offset, line number and column index into a source code position.
     */
    private static long encodePosition(int offset, int fileIndex, int line, int column) {
        return ((long) offset << POS_OFFSET_SHIFT) |
                (fileIndex << POS_FILEINDEX_SHIFT) |
                (line << POS_LINE_SHIFT) |
                (column << POS_COLUMN_SHIFT);
    }

    /*
     * Extracts the source code offset from a source code position.
     */
    private static int decodeOffset(long position) {
        return (int) ((position >> POS_OFFSET_SHIFT) & POS_OFFSET_MASK);
    }

    /*
     *  Extracts the line number offset from a source code position.
     */
    private static short decodeLine(long position) {
        return (short) ((position >> POS_LINE_SHIFT) & POS_LINE_MASK);
    }

    /*
     *  Extracts the file index from a source code position.
     */
    private static short decodeFileIndex(long position) {
        return (short) ((position >> POS_FILEINDEX_SHIFT) & POS_FILEINDEX_MASK);
    }

    /*
     * Extracts the column index from a source code position.
     */
    private static short decodeColumn(long position) {
        return (short) ((position >> POS_COLUMN_SHIFT) & POS_COLUMN_MASK);
    }

    /**
     * Returns the offset within the source file decoded from a source position.
     *
     * @param position encoded source code position
     * @return offset within source code
     */
    public static int getOffset(long position) {
        return decodeOffset(position);
    }

    /**
     * Returns the file index of the source file decoded from a source position.
     *
     * @param position encoded source code position
     * @return file index of source file
     */
    public static short getFileIndex(long position) {
        return decodeFileIndex(position);
    }

    /**
     * Returns the line within the source file decoded from a source position.
     * First line number in a file is 1.
     *
     * @param position encoded source code position
     * @return line within source code
     */
    public static short getLine(long position) {
        return decodeLine(position);
    }

    /**
     * Returns the column within the source file decoded from a source position.
     * First column of a line is 0.
     *
     * @param position encoded source code position
     * @return column within source code
     */
    public static short getColumn(long position) {
        return decodeColumn(position);
    }

    /*
     * Array containing values of hex digits and -1 for characters that are not hex digits.
     * Size of the array is limited to the first 128 characters.
     */
    private static int[] hexValues;

    static {
        hexValues = new int[128];
        // Mark non hex digits with -1
        for (int i = 0; i < hexValues.length; i++) {
            hexValues[i] = -1;
        }
        // Mark hex digits with their actual values
        for (int i = 0; i <= 9; i++) {
            hexValues[i + '0'] = i;
        }
        for (int i = 10; i <= 15; i++) {
            hexValues[i - 10 + 'a'] = i;
            hexValues[i - 10 + 'A'] = i;
        }
    }

    /**
     * Constructs a new scanner instance for source code read from files.
     * Puts it into source section mode by default.
     *
     * @param compiler  compiler instance
     * @param fileIndex index of file to be scanned
     * @param source    source code to be scanned 一个simple文件的源代码
     */
    public Scanner(Compiler compiler, int fileIndex, String source) {
        this.compiler = compiler;
        this.fileIndex = fileIndex;
        this.sourceBuffer = source.toCharArray();

//        得到第一个$Properties的偏移量并赋值给  propertiesSectionStartOffset
        findSectionStarts(source);
        //扫描得到源码的偏移($Properties以上的部分)
        scanSourceSection();
    }

    /**
     * 供UI设计器用的构造器
     *
     * @param source
     */
    public Scanner(String source) {
        this.compiler = null;
        this.fileIndex = 0;
        this.sourceBuffer = source.toCharArray();

//        得到第一个$Properties的偏移量并赋值给  propertiesSectionStartOffset
        findSectionStarts(source);
        //扫描得到源码的偏移($Properties以上的部分)
        scanSourceSection();
    }

    /**
     * Constructs a new scanner instance (for expressions or testing).
     * Puts it into source section mode by default.
     *
     * @param compiler compiler instance
     * @param source   source code to be scanned
     */
    public Scanner(Compiler compiler, String source) {
        this(compiler, 0, source);
    }


    /**
     * Makes scanner generate a token for comments it finds.
     *
     * @param commentToken generates comment token if {@code true}
     */
    public void generateCommentToken(boolean commentToken) {
        generateCommentToken = commentToken;
    }

    /**
     * Special source position for errors without a particular location within
     * a source file.
     *
     * @return source file only position
     */
    public long getSourceFileOnlyPosition() {
        return encodePosition(0, fileIndex, 0, 0);
    }

    /**
     * Sets up the scanner for parsing the properties section.
     * 设置解析属性部分的扫描器
     * sourceOffset = getPropertiesSectionStartOffset();//第一个$Properties偏移量
     * sourceEndOffset = getPropertiesSectionEndOffset();//源文件结尾偏移
     */
    public void scanPropertiesSection() {
        propertiesMode = true;
        sourceOffset = getPropertiesSectionStartOffset();//第一个$Properties偏移量
        sourceEndOffset = getPropertiesSectionEndOffset();//源文件结尾偏移
        // reset current line and column
        //重置当前行和列
        sourceLine = 1;
        sourceColumn = 0;
    }

    /**
     * Sets up the scanner for parsing the source code section.
     */
    public void scanSourceSection() {
        propertiesMode = false;
        sourceOffset = getSourceSectionStartOffset();
        sourceEndOffset = getSourceSectionEndOffset();
        // reset current line and column
        sourceLine = 1;
        sourceColumn = 0;
    }

    /**
     * Returns the start offset of the source section within the source code.
     *
     * @return start offset of source code section
     */
    public int getSourceSectionStartOffset() {
        return 0;
    }

    /**
     * 返回第一个$Peoperties的偏移,,此地址以上的部分为源码
     * Returns the end offset of the source section within the source code.
     *
     * @return end offset of source code section
     */
    public int getSourceSectionEndOffset() {
        return propertiesSectionStartOffset;
    }

    /**
     * Returns the starting offset of the properties section within the source
     * code.
     *
     * @return starting offset of properties section
     */
    public int getPropertiesSectionStartOffset() {
        return propertiesSectionStartOffset;
    }

    /**
     * Returns the end offset of the properties section within the source code.
     *
     * @return end offset of properties section
     */
    public int getPropertiesSectionEndOffset() {
        return sourceBuffer.length;
    }

    /**
     * Get the current token.
     *
     * @return current token
     */
    public TokenKind getToken() {
        return tokenKind;
    }

    /**
     * If the current token is {@link TokenKind#TOK_IDENTIFIER} then this method
     * will return the name of the identifier.
     *
     * @return identifier name
     */
    public String getTokenValueIdentifier() {
        return tokenValueString;
    }

    /**
     * If the current token is {@link TokenKind#TOK_STRINGCONSTANT} then this
     * method will return the value of the string constant.
     *
     * @return string constant value
     */
    public String getTokenValueString() {
        return tokenValueString;
    }

    /**
     * If the current token is {@link TokenKind#TOK_BOOLEANCONSTANT} then this
     * method will return the value of the boolean constant.
     *
     * @return boolean constant value
     */
    public boolean getTokenValueBoolean() {
        return tokenValueBoolean;
    }

    /**
     * If the current token is {@link TokenKind#TOK_NUMERICCONSTANT} then this
     * method will return the value of the numerical constant.
     *
     * @return numerical constant value
     */
    public BigDecimal getTokenValueNumber() {
        return tokenValueNumber;
    }

    /**
     * Returns the start position of the current token within the source code.
     *
     * @return start position of current token
     */
    public long getTokenStartPosition() {
        return encodePosition(tokenStartOffset, fileIndex, tokenStartLine, tokenStartColumn);
    }

    /**
     * Returns the end position of the current token within the source code.
     *
     * @return end position of current token
     */
    public long getTokenEndPosition() {
        return encodePosition(sourceOffset, fileIndex, sourceLine, sourceColumn);
    }

    /*
    注意该方假定Properties段没有被多行注释注释掉 返回第一个 $Properties
     * Finds the section start offsets.
     * Note that this method assumes that there are no multi-line comments that
     * could hide the start of the properties section.
     */
    private void findSectionStarts(String source) {
        propertiesSectionStartOffset = source.length();
        // Look for $End $Properties
        int index = source.lastIndexOf("$Properties");
        if (index != -1) {
            index = source.lastIndexOf("$Properties", index - 1);
            if (index != -1) {
                propertiesSectionStartOffset = index;
            }
        }
    }

    /*
     * Convenience method for reporting errors.
     * Uses current token start position.
     */
    private void error(String error) {
        // Forward to compiler error reporting.
        if (this.compiler != null)
            compiler.error(encodePosition(tokenStartOffset, fileIndex, tokenStartLine, tokenStartColumn),
                    error);
    }

    /*
     * To be called after a newline character (combination) was encountered.
     */
    private void newline() {
        // Increase line number.
        sourceLine++;
        // Reset current column
        sourceColumn = 0;
    }

    /*
     * Returns the current character.
     * 返回当前的字符
     */
    private char getchar() {
        // Check whether we have reached the section end
        if (sourceOffset >= sourceEndOffset) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // Return current character in source buffer.
        return sourceBuffer[sourceOffset];
    }

    /*
     * Advances to the next source character
     * 前进到下一个源码字符
     */
    private void advance() {
        sourceOffset++;
        // increase current column, \n case is handled in newLine()
        //增加当前列   \n属于新的一行
        sourceColumn++;
    }

    /*
     * Checks whether the character is a legal character to start a Simple identifier.
     */
    private boolean isSimpleIdentifierStart(char c) {
        return c != '$' && c != '_' && Character.isJavaIdentifierStart(c);
    }

    /*
     * Checks whether the character is a legal character to be part of a Simple identifier.
     */
    private boolean isSimpleIdentifierPart(char c) {
        return c != '$' && Character.isJavaIdentifierPart(c);
    }

    /*
     * Scans a Simple identifier. The character passed as a parameter is the first character of the
     * identifier and has already been checked to be a legal character to start a Simple identifier.
     */
    private void scanIdentifier(char c) {
        // Add all identifier characters to the string buffer as long as they are legal Simple
        // identifier characters.
        StringBuilder identifier = new StringBuilder();
        identifier.append(c);

        try {
            c = getchar();
            while (isSimpleIdentifierPart(c)) {
                identifier.append(c);
                advance();
                c = getchar();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // This happens when we reached the end of the source code. OK to just exit the loop.
        }

        // Set identifier token value.
        tokenValueString = identifier.toString();
    }

    /*
     * Scans a properties section keyword (which must all start with a dollar sign). Returns true
     * if an actual properties section keyword was recognized.
     */
    private boolean scanPropertiesKeyword() {
        // Start scanning an identifier.
        scanIdentifier('$');
        // Check whether the identifier is a keyword.
        tokenKind = keywords.checkForKeyword(tokenValueString);
        return tokenKind != TokenKind.TOK_IDENTIFIER;
    }

    /*
     * Skips a line comment.
     */
    private void skipLineComment() {
        for (; ; ) {
            switch (getchar()) {
                default:
                    advance();
                    break;

                case '\r':      // Carriage Return <CR>
                case '\n':      // Line Feed <LF>
                    return;
            }
        }
    }

    /*
     * Returns the hex value of the character argument or -1 if the character is not a valid hex
     * character.
     */
    private int getHexValue(char c) {
        return c >= hexValues.length ? -1 : hexValues[c];
    }

    /*
     * Scans a hex literal constant.
     */
    private void scanHexLiteral() {
        tokenKind = TokenKind.TOK_NUMERICCONSTANT;
        StringBuilder number = new StringBuilder();

        try {
            // So far we only scanned the hex constant prefix (&H). Now we take a look at the first
            // character of the actual constant value.
            char c = getchar();
            int v = getHexValue(c);
            if (v < 0) {
                // We found a hex constant prefix that wasn't followed by a hex digit.
                error(Error.errMalformedHexConstant);
                tokenValueNumber = BigDecimal.ZERO;
                return;
            }
            advance();
            number.append(c);
        } catch (ArrayIndexOutOfBoundsException e) {
            // We found a hex constant prefix at the end of the source buffer without any additional
            // hex digits.
            error(Error.errMalformedHexConstant);
            tokenValueNumber = BigDecimal.ZERO;
            return;
        }

        // Consume as many hex digits as can be found
        try {
            for (; ; ) {
                char c = getchar();
                int v = getHexValue(c);
                if (v < 0) {
                    break;
                }

                advance();
                number.append(c);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // end-of-file OK in this case
        }

        // Convert the found literal into a numerical value.
        tokenValueNumber = new BigDecimal(new BigInteger(number.toString(), 16));
    }

    /*
     * Scans a sequence of digits.
     */
    private int scanDigits(StringBuilder number) {
        int digits = 0;
        try {
            for (; ; ) {
                char c = getchar();
                if (!Character.isDigit(c)) {
                    break;
                }

                number.append(c);
                advance();
                digits++;
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            // Just exit
        }

        return digits;
    }

    /*
     * Scans a numeric literal constant. Character argument contains the first digit found.
     */
    private void scanDecimalLiteral(char c) {
        tokenKind = TokenKind.TOK_NUMERICCONSTANT;
        StringBuilder number = new StringBuilder();
        number.append(c);

        // Scan integral number
        scanDigits(number);

        try {
            if (getchar() == '.') {
                // Floating point number
                number.append('.');
                advance();

                // Scan fraction
                if (scanDigits(number) == 0) {
                    error(Error.errMalformedFloatConstant);
                    tokenValueNumber = BigDecimal.ZERO;
                    return;
                }

                c = getchar();
                if (c == 'e' || c == 'E') {
                    // Scan exponent
                    number.append(c);
                    advance();

                    c = getchar();
                    if (c == '-' || c == '+') {
                        // Explicit exponent sign
                        number.append(c);
                        advance();
                    }

                    if (scanDigits(number) == 0) {
                        error(Error.errMalformedFloatConstant);
                        tokenValueNumber = BigDecimal.ZERO;
                        return;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            // If we catch this exception here then the number is well-formed
        }

        tokenValueNumber = new BigDecimal(number.toString());
    }

    /*
     * Scans a string literal constant. The '"' has already been consumed. The current character
     * is the first string literal character.
     */
    private void scanStringLiteral() {
        tokenKind = TokenKind.TOK_STRINGCONSTANT;
        try {
            StringBuilder sb = new StringBuilder();
            // As long as there are valid string characters.
            for (; ; ) {
                char c = getchar();
                advance();
                switch (c) {
                    default:
                        // Just a normal character. Append to string literal.
                        sb.append(c);
                        break;

                    case '"':
                        // End of string literal. Convert into string constant token value.
                        tokenValueString = sb.toString();
                        return;

                    case '\n':      // Line Feed <LF>
                    case '\r':      // Carriage Return <CR>
                        // String constants cannot span multiple lines. Report an error.
                        error(Error.errUnterminatedString);
                        return;

                    case '\\':
                        // Escape character sequences
                        c = getchar();
                        advance();
                        switch (c) {
                            default:
                                break;

                            case '\"':
                                c = '\"';
                                break;

                            case '\\':
                                c = '\\';
                                break;

                            case 'n':
                                c = '\n';
                                break;

                            case 'r':
                                c = '\r';
                                break;

                            case 't':
                                c = '\t';
                                break;

                            case 'u':
                                // Backslash + u must be followed by at least 4 hex digits.
                                char c1, c2, c3, c4;
                                try {
                                    c1 = getchar();
                                    advance();
                                    c2 = getchar();
                                    advance();
                                    c3 = getchar();
                                    advance();
                                    c4 = getchar();
                                    advance();
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    // Reached end of source source file - could additionally report an unterminated
                                    // string constant, but punt on that
                                    error(Error.errMalformedUnicodeEscapeSequence);
                                    tokenValueString = sb.toString();
                                    return;
                                }

                                try {
                                    int x1 = hexValues[c1];
                                    int x2 = hexValues[c2];
                                    int x3 = hexValues[c3];
                                    int x4 = hexValues[c4];
                                    if (x1 >= 0 && x2 >= 0 && x3 >= 0 && x4 >= 0) {
                                        c = (char) ((x1 << 12) + (x2 << 8) + (x3 << 4) + x4);
                                    } else {
                                        // Illegal hex character in unicode escape sequence
                                        error(Error.errMalformedUnicodeEscapeSequence);
                                    }
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    // Since we know that there were enough characters to scan the unicode escape
                                    // sequence, this must mean that one of the hex constant characters isn't one.
                                    error(Error.errMalformedUnicodeEscapeSequence);
                                }
                                break;
                        }
                        sb.append(c);
                        break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // If the end of the source file was reached before the string constant was terminated we
            // must report an error.
            error(Error.errUnterminatedString);
            throw e;
        }
    }

    /**
     * Scans the next token.
     *
     * @return kind of token scanned
     */
    @SuppressWarnings("fallthrough")
    public TokenKind nextToken() {
        try {
            // Scanners are just a loop with a big switch statement in it!
            for (; ; ) {
                // Remember potential token start offset.
                tokenStartOffset = sourceOffset;
                tokenStartColumn = sourceColumn;
                tokenStartLine = sourceLine;

                // Look at the current source character and advance to the next.
                char c = getchar();
                advance();
                switch (c) {
                    default:
                        // If this is a legal Simple identifier start character then scan the identifier
                        // (this could also be a keyword or a literal).

                        //如果这是一个合法的Simple标识符的开始字符然后扫描这个标识符
                        //(这也可能是一个关键字或文字)。
                        if (isSimpleIdentifierStart(c)) {
                            scanIdentifier(c);

                            // Check whether the identifier is a keyword (or literal)
                            //检查 是否 标识符是一个关键字(或文字)
                            tokenKind = keywords.checkForKeyword(tokenValueString);

                            // Convert pseudo literal keywords into actual literals.
                            //伪文字关键词转化为实际的文字。
                            switch (tokenKind) {
                                default:
                                    break;

                                case TOK_FALSE:
                                    tokenValueBoolean = false;
                                    tokenKind = TokenKind.TOK_BOOLEANCONSTANT;
                                    break;

                                case TOK_TRUE:
                                    tokenValueBoolean = true;
                                    tokenKind = TokenKind.TOK_BOOLEANCONSTANT;
                                    break;
                            }
                            break;
                        }

                        // If it's not starting an identifier then it is an illegal character.
                        //如果没有已标识符开头，那么它是个非法字符
                        error(Error.errIllegalCharacter);
                        continue;

                    case '_':
                        // Line continuation (must be followed by a newline character (combination) otherwise
                        // it's an error).
                        // 行延续(必须后跟换行符 其他这是一个错误)。
                        c = getchar();
                        if (c == '\r') {
                            advance();
                            c = getchar();
                        }
                        if (c == '\n') {
                            advance();
                            continue;
                        }
                        error(Error.errIllegalCharacter);
                        continue;

                    case '$':
                        // Only a legal character when scanning the properties section of a Simple source file.
                        //只有一个合法的字符当扫描一个Simple的源文件的属性部分
                        if (propertiesMode && scanPropertiesKeyword()) {
                            break;
                        }
                        error(Error.errIllegalCharacter);
                        continue;

                    case '0':
                        // Numerical zero constant.
                        //数值零常数
                        char nc = getchar();
                        if ('0' <= nc && nc <= '9') {
                            error(Error.errMalformedDecimalConstant);
                        }
                        // Fall thru...

                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        // Numerical constant (integer or floating point).
                        //数值常数(整数或浮点数)。
                        scanDecimalLiteral(c);
                        break;

                    case '&':
                        if (getchar() != 'H') {
                            // String concatenation operator.
                            //字符串连接操作符。
                            tokenKind = TokenKind.TOK_AMPERSAND;
                        } else {
                            // Prefix for a hex literal.
                            //十六进制文字的前缀  &H
                            advance();
                            scanHexLiteral();
                        }
                        break;

                    case '.':
                        tokenKind = TokenKind.TOK_DOT;
                        break;

                    case ':':
                        tokenKind = TokenKind.TOK_EOS;
                        break;

                    case '\'':
                        // ' 开头注释  跳过一行
                        skipLineComment();
                        if (!generateCommentToken) {
                            continue;
                        }
                        tokenKind = TokenKind.TOK_COMMENT;
                        break;

                    case '\"':
                        //扫描字符串
                        scanStringLiteral();
                        break;

                    case '\u0009':  // Tab <TAB>
                    case '\u000B':  // Vertical Tab <VT>
                    case '\u000C':  // Form Feed <FF>
                    case '\u0020':  // Space <SP>
                        continue;

                    case '\r':      // Carriage Return <CR>
                        if (getchar() == '\n') {
                            advance();
                        }
                        // Fall thru

                    case '\n':      // Line Feed <LF>
                        newline();
                        tokenKind = TokenKind.TOK_EOS;
                        break;

                    case '(':
                        tokenKind = TokenKind.TOK_OPENPARENTHESIS;
                        break;

                    case ')':
                        tokenKind = TokenKind.TOK_CLOSEPARENTHESIS;
                        break;

                    case ',':
                        tokenKind = TokenKind.TOK_COMMA;
                        break;

                    case '<':
                        switch (getchar()) {
                            default:
                                tokenKind = TokenKind.TOK_LESS;
                                break;

                            case '=':
                                tokenKind = TokenKind.TOK_LESSEQUAL;
                                advance();
                                break;

                            case '<':
                                advance();
                                tokenKind = TokenKind.TOK_SHIFTLEFT;
                                break;

                            case '>':
                                advance();
                                tokenKind = TokenKind.TOK_NOTEQUAL;
                                break;
                        }
                        break;

                    case '>':
                        switch (getchar()) {
                            default:
                                tokenKind = TokenKind.TOK_GREATER;
                                break;

                            case '=':
                                tokenKind = TokenKind.TOK_GREATEREQUAL;
                                advance();
                                break;

                            case '>':
                                advance();
                                tokenKind = TokenKind.TOK_SHIFTRIGHT;
                                break;
                        }
                        break;

                    case '\\':
                        tokenKind = TokenKind.TOK_INTEGERDIVIDE;
                        break;

                    case '/':
                        tokenKind = TokenKind.TOK_DIVIDE;
                        break;

                    case '=':
                        tokenKind = TokenKind.TOK_EQUAL;
                        break;

                    case '+':
                        tokenKind = TokenKind.TOK_PLUS;
                        break;

                    case '-':
                        tokenKind = TokenKind.TOK_MINUS;
                        break;

                    case '*':
                        tokenKind = TokenKind.TOK_TIMES;
                        break;

                    case '^':
                        tokenKind = TokenKind.TOK_EXP;
                        break;
                }

                break;  // for (;;)
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // End of file.
            tokenKind = TokenKind.TOK_EOF;
        }

        return tokenKind;
    }
}
