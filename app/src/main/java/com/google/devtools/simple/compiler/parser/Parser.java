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

package com.google.devtools.simple.compiler.parser;

import com.google.devtools.simple.compiler.expressions.IsExpression;
import com.google.devtools.simple.compiler.statements.ForEachStatement;
import com.google.devtools.simple.compiler.symbols.DataMemberSymbol;
import com.google.devtools.simple.compiler.symbols.EventHandlerSymbol;
import com.google.devtools.simple.compiler.Compiler;
import com.google.devtools.simple.compiler.Error;
import com.google.devtools.simple.compiler.expressions.AdditionExpression;
import com.google.devtools.simple.compiler.expressions.AliasExpression;
import com.google.devtools.simple.compiler.expressions.AndExpression;
import com.google.devtools.simple.compiler.expressions.AssignmentExpression;
import com.google.devtools.simple.compiler.expressions.CallExpression;
import com.google.devtools.simple.compiler.expressions.NothingExpression;
import com.google.devtools.simple.compiler.expressions.QualifiedIdentifierExpression;
import com.google.devtools.simple.compiler.expressions.ConcatenationExpression;
import com.google.devtools.simple.compiler.expressions.ConstantBooleanExpression;
import com.google.devtools.simple.compiler.expressions.ConstantNumberExpression;
import com.google.devtools.simple.compiler.expressions.ConstantStringExpression;
import com.google.devtools.simple.compiler.expressions.DivisionExpression;
import com.google.devtools.simple.compiler.expressions.EqualExpression;
import com.google.devtools.simple.compiler.expressions.ExponentiationExpression;
import com.google.devtools.simple.compiler.expressions.Expression;
import com.google.devtools.simple.compiler.expressions.GreaterExpression;
import com.google.devtools.simple.compiler.expressions.GreaterOrEqualExpression;
import com.google.devtools.simple.compiler.expressions.IdentifierExpression;
import com.google.devtools.simple.compiler.expressions.IdentityExpression;
import com.google.devtools.simple.compiler.expressions.IntegerDivisionExpression;
import com.google.devtools.simple.compiler.expressions.IsNotExpression;
import com.google.devtools.simple.compiler.expressions.LessExpression;
import com.google.devtools.simple.compiler.expressions.LessOrEqualExpression;
import com.google.devtools.simple.compiler.expressions.LikeExpression;
import com.google.devtools.simple.compiler.expressions.MeExpression;
import com.google.devtools.simple.compiler.expressions.ModuloExpression;
import com.google.devtools.simple.compiler.expressions.MultiplicationExpression;
import com.google.devtools.simple.compiler.expressions.NegationExpression;
import com.google.devtools.simple.compiler.expressions.NewExpression;
import com.google.devtools.simple.compiler.expressions.NotEqualExpression;
import com.google.devtools.simple.compiler.expressions.NotExpression;
import com.google.devtools.simple.compiler.expressions.OrExpression;
import com.google.devtools.simple.compiler.expressions.ShiftLeftExpression;
import com.google.devtools.simple.compiler.expressions.ShiftRightExpression;
import com.google.devtools.simple.compiler.expressions.SimpleIdentifierExpression;
import com.google.devtools.simple.compiler.expressions.SubtractionExpression;
import com.google.devtools.simple.compiler.expressions.TypeOfExpression;
import com.google.devtools.simple.compiler.expressions.XorExpression;
import com.google.devtools.simple.compiler.expressions.synthetic.NewComponentExpression;
import com.google.devtools.simple.compiler.scanner.Scanner;
import com.google.devtools.simple.compiler.scanner.TokenKind;
import com.google.devtools.simple.compiler.scopes.Scope;
import com.google.devtools.simple.compiler.statements.DoUntilStatement;
import com.google.devtools.simple.compiler.statements.DoWhileStatement;
import com.google.devtools.simple.compiler.statements.ExitStatement;
import com.google.devtools.simple.compiler.statements.ExpressionStatement;
import com.google.devtools.simple.compiler.statements.ForNextStatement;
import com.google.devtools.simple.compiler.statements.IfStatement;
import com.google.devtools.simple.compiler.statements.OnErrorStatement;
import com.google.devtools.simple.compiler.statements.RaiseEventStatement;
import com.google.devtools.simple.compiler.statements.SelectStatement;
import com.google.devtools.simple.compiler.statements.Statement;
import com.google.devtools.simple.compiler.statements.StatementBlock;
import com.google.devtools.simple.compiler.statements.WhileStatement;
import com.google.devtools.simple.compiler.statements.OnErrorStatement.OnErrorCaseStatement;
import com.google.devtools.simple.compiler.statements.SelectStatement.SelectCaseStatement;
import com.google.devtools.simple.compiler.statements.synthetic.LocalVariableDefinitionStatement;
import com.google.devtools.simple.compiler.statements.synthetic.MarkerStatement;
import com.google.devtools.simple.compiler.statements.synthetic.RaiseInitializeEventStatement;
import com.google.devtools.simple.compiler.statements.synthetic.RegisterEventHandlersStatement;
import com.google.devtools.simple.compiler.symbols.ConstantDataMemberSymbol;
import com.google.devtools.simple.compiler.symbols.EventSymbol;
import com.google.devtools.simple.compiler.symbols.FunctionSymbol;
import com.google.devtools.simple.compiler.symbols.InstanceDataMemberSymbol;
import com.google.devtools.simple.compiler.symbols.InstanceFunctionSymbol;
import com.google.devtools.simple.compiler.symbols.LocalVariableSymbol;
import com.google.devtools.simple.compiler.symbols.NamespaceSymbol;
import com.google.devtools.simple.compiler.symbols.ObjectDataMemberSymbol;
import com.google.devtools.simple.compiler.symbols.ObjectFunctionSymbol;
import com.google.devtools.simple.compiler.symbols.ObjectSymbol;
import com.google.devtools.simple.compiler.symbols.PropertySymbol;
import com.google.devtools.simple.compiler.types.ArrayType;
import com.google.devtools.simple.compiler.types.BooleanType;
import com.google.devtools.simple.compiler.types.ByteType;
import com.google.devtools.simple.compiler.types.DateType;
import com.google.devtools.simple.compiler.types.DoubleType;
import com.google.devtools.simple.compiler.types.IntegerType;
import com.google.devtools.simple.compiler.types.LongType;
import com.google.devtools.simple.compiler.types.ObjectType;
import com.google.devtools.simple.compiler.types.ShortType;
import com.google.devtools.simple.compiler.types.SingleType;
import com.google.devtools.simple.compiler.types.StringType;
import com.google.devtools.simple.compiler.types.Type;
import com.google.devtools.simple.compiler.types.UnresolvedType;
import com.google.devtools.simple.compiler.types.VariantType;
import com.google.devtools.simple.compiler.types.synthetic.ErrorType;
import com.google.devtools.simple.compiler.util.Signatures;
import com.google.devtools.simple.util.Preconditions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses Simple source files and generates statement and expression
 * trees.
 * <p>
 * <p>For more information about the Simple language and its syntax see
 * <a href="http://code.google.com/google-simple">Simple language definition</a>
 *
 * @author Herbert Czymontek
 */
public final class Parser {

    /*
     * Will be thrown whenever a syntax error is encountered.
     */
    @SuppressWarnings("serial")
    private static final class SyntaxError extends RuntimeException {
    }

    /*
     * Will be thrown whenever the parser encounters an unrecoverable error (e.g. a corrupted source
     * file properties section).
     */
    @SuppressWarnings("serial")
    private static final class FatalError extends RuntimeException {
    }

    // Priorities for expression parser
    private static final int PRIO_ASSIGN = 0;
    private static final int PRIO_LOGICALORBITOPERATION_CONJUNCTION = 1;
    private static final int PRIO_LOGICALORBITOPERATION_DISJUNCTION = 2;
    private static final int PRIO_LOGICALORBITOPERATION_NEGATION = 3;
    private static final int PRIO_COMPARISON = 4;
    private static final int PRIO_SHIFT = 5;
    private static final int PRIO_CONCATENATION = 6;
    private static final int PRIO_ADDITION = 7;
    private static final int PRIO_MODULO = 8;
    private static final int PRIO_INTEGERDIVISION = 9;
    private static final int PRIO_MULTIPLICATION = 10;
    private static final int PRIO_NEGATION = 11;
    private static final int PRIO_EXPONENTIATION = 12;
    private static final int PRIO_DOT = 13;

    // Compiler instance of current compilation
    private final Compiler compiler;

    // Scanner for source file being parsed
    private final Scanner scanner;

    // Namespace symbol for source being parsed
    private final NamespaceSymbol currentNamespace;

    // Object symbol of source file being parsed
    //源文件被提取的Object Symbol (对象标志)
    private final ObjectSymbol currentObjectSymbol;

    // Symbol of function (procedure, property getter/setter, event handler) currently being parsed
    private FunctionSymbol currentFunction;

    // Currently active scope
    //当前活动范围
    private Scope currentScope;

    // Current statement list (only set while parsing functions, procedures, properties and event
    // handlers)
    //当前语句列表(只设置在解析函数、过程、属性和事件　　/ /处理程序)
    private StatementBlock currentStatementList;

    // Special source position for errors without a particular location within the current source file
    private final long fileOnlySourcePosition;

    // Indicates whether the parser is currently processing the source file's properties section
    private boolean parsingPropertiesSection;

    // List of components (only defined by Form source files)
    private List<DataMemberSymbol> components;

    /**
     * Creates a new parser.
     *
     * @param compiler           compiler instance of current compilation
     * @param scanner            scanner for source file to be parsed
     * @param qualifiedClassName qualified class name for source file 限定类名
     */
    public Parser(Compiler compiler, Scanner scanner, String qualifiedClassName) {
        this.compiler = compiler;
        this.scanner = scanner;

        //获取源文件唯一的标识  long类型
        fileOnlySourcePosition = scanner.getSourceFileOnlyPosition();

        // There is no default package in Simple
        String packageName = Signatures.getPackageName(qualifiedClassName);//获取包名
        if (packageName.equals("")) {
            compiler.error(fileOnlySourcePosition, Error.errNoPackage);
            currentNamespace = compiler.getGlobalNamespaceSymbol();
        } else {
            currentNamespace = NamespaceSymbol.getNamespaceSymbol(compiler, packageName);
        }

        // Allocate object defined by the source file
        //分配源文件定义的对象
        currentObjectSymbol = new ObjectSymbol(fileOnlySourcePosition,
                Signatures.getClassName(qualifiedClassName), currentNamespace, null);
        currentObjectSymbol.markAsCompiled();//标志类被编译

        Scope scope = currentNamespace.getScope();//获取作用域
        String className = currentObjectSymbol.getName();
        if (scope.lookupShallow(className) != null) {
            //类名冲突错误
            compiler.error(fileOnlySourcePosition, Error.errSymbolRedefinition, className);
        } else {
            scope.enterSymbol(currentObjectSymbol);
        }

        compiler.addObject(currentObjectSymbol);
        currentScope = currentObjectSymbol.getScope();

        components = new ArrayList<DataMemberSymbol>();
    }

    /*
     * Resyncing after encountering a syntax error: because Simple is a line-based language we will
     * simply skip any tokens until we find an end-of-statement or end-of-file.
     */
    private void resyncAfterSyntaxError() {
        for (; ; ) {
            TokenKind token = scanner.nextToken();
            if (token == TokenKind.TOK_EOS || token == TokenKind.TOK_EOF) {
                skipEndOfStatements();
                break;
            }
        }
    }

    /*
     * Accepts the given token or reports an error otherwise.
     */
    private void accept(TokenKind token) {
        if (scanner.getToken() != token) {
            if (!parsingPropertiesSection) {
                compiler.error(scanner.getTokenStartPosition(), Error.errExpected, token.toString(),
                        scanner.getToken().toString());
            }
            throw new SyntaxError();
        }
    }

    /*
     * Accepts the given token and skips it.
     */
    private TokenKind acceptAndSkip(TokenKind token) {
        accept(token);
        return scanner.nextToken();
    }

    /*
     * Accepts the end of the current statement (end-of-line or ':') and skips it or reports an error.
     */
    private void acceptAndSkipEndOfStatement() {
        TokenKind token = scanner.getToken();
        if (token != TokenKind.TOK_EOS && token != TokenKind.TOK_EOF) {
            if (!parsingPropertiesSection) {
                if (compiler != null)
                    compiler.error(scanner.getTokenStartPosition(), Error.errExpected, "end of statement",
                            scanner.getToken().toString());
            }
            throw new SyntaxError();
        }

        skipEndOfStatements();
    }

    /*
     * Skips as many consecutive end-of-statements as can be found.
     * 跳过尽可能多的被找到的连续end-of-statements
     */
    private void skipEndOfStatements() {
        while (scanner.nextToken() == TokenKind.TOK_EOS) {
            // Skip any other end of statement tokens 跳过前面一堆结束语句(无用代码)
        }
    }

    /*
     * Parses a possible qualified object type.
     */
    private ObjectType parseObjectType() {
        accept(TokenKind.TOK_IDENTIFIER);
        return new UnresolvedType(parseQualifiedIdentifier());
    }

    /*
     * Parses a type name (without array designator).
     */
    private Type parseNonArrayType() {
        switch (scanner.getToken()) {
            default:
                compiler.error(scanner.getTokenStartPosition(), Error.errUnexpected,
                        scanner.getToken().toString());
                throw new SyntaxError();

            case TOK_IDENTIFIER:
                return new UnresolvedType(parseQualifiedIdentifier());

            case TOK_BOOLEAN:
                scanner.nextToken(); // Skip 'Boolean'
                return BooleanType.booleanType;

            case TOK_BYTE:
                scanner.nextToken(); // Skip 'Byte'
                return ByteType.byteType;

            case TOK_SHORT:
                scanner.nextToken(); // Skip 'Short'
                return ShortType.shortType;

            case TOK_INTEGER:
                scanner.nextToken(); // Skip 'Integer'
                return IntegerType.integerType;

            case TOK_LONG:
                scanner.nextToken(); // Skip 'Long'
                return LongType.longType;

            case TOK_SINGLE:
                scanner.nextToken(); // Skip 'Single'
                return SingleType.singleType;

            case TOK_DOUBLE:
                scanner.nextToken(); // Skip 'Double'
                return DoubleType.doubleType;

            case TOK_OBJECT:
                scanner.nextToken(); // Skip 'Object'
                return ObjectType.objectType;

            case TOK_STRING:
                scanner.nextToken(); // Skip 'String'
                return StringType.stringType;

            case TOK_DATE:
                scanner.nextToken(); // Skip 'Date'
                return DateType.dateType;

            case TOK_VARIANT:
                scanner.nextToken(); // Skip 'Variant'
                return VariantType.variantType;
        }
    }

    /*
     * Parses a type name.
     */
    private Type parseType() {
        Type type = parseNonArrayType();

        List<Expression> dimensions = null;
        if (scanner.getToken() == TokenKind.TOK_OPENPARENTHESIS) {
            // Array type
            long position = scanner.getTokenStartPosition();
            dimensions = parseArrayDimensions();
            type = new ArrayType(type, dimensions.size());

            // Array type must be dynamically size
            if (dimensions.get(0) != null) {
                compiler.error(position, Error.errDimensionInDynamicArray);
            }
        }

        return type;
    }

    /*
     * Parses the actual argument list of a function invocation.
     */
    private List<Expression> parseActualArgumentList() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_OPENPARENTHESIS);

        List<Expression> argList = new ArrayList<Expression>();
        if (scanner.nextToken() == TokenKind.TOK_CLOSEPARENTHESIS) {
            // Empty argument list
            scanner.nextToken();
        } else {
            for (; ; ) {
                argList.add(parseExpression());

                if (scanner.getToken() == TokenKind.TOK_CLOSEPARENTHESIS) {
                    scanner.nextToken();  // Skip ')'
                    break;
                }

                acceptAndSkip(TokenKind.TOK_COMMA);
            }
        }
        return argList;
    }

    /*
     * Parses an array dimensioning expression. It should either consist of a list of comma-separated
     * expressions or a list of commas (possibly none), but no mix of either.
     */
    private List<Expression> parseArrayDimensions() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_OPENPARENTHESIS);

        List<Expression> dimensions = new ArrayList<Expression>();
        switch (scanner.nextToken()) {
            default:
                // One or more specified dimensions
                for (; ; ) {
                    dimensions.add(parseExpression());
                    if (scanner.getToken() == TokenKind.TOK_CLOSEPARENTHESIS) {
                        scanner.nextToken();  // Skip ')'
                        break;
                    }
                    acceptAndSkip(TokenKind.TOK_COMMA);
                }
                break;

            case TOK_COMMA:
                // Multiple unspecified dimensions
                do {
                    dimensions.add(null);
                } while (scanner.nextToken() == TokenKind.TOK_COMMA);
                dimensions.add(null);
                acceptAndSkip(TokenKind.TOK_CLOSEPARENTHESIS);
                break;

            case TOK_CLOSEPARENTHESIS:
                // Single unspecified dimension
                dimensions.add(null);
                scanner.nextToken();  // Skip ')'
                break;
        }

        return dimensions;
    }

    /*
     * Parses a qualified identifier.
     * 解析一个合格的标识符。
     */
    private IdentifierExpression parseQualifiedIdentifier() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_IDENTIFIER);

        // Always starts of with a single identifier
        //总是用一个标识符开始
        IdentifierExpression expr = new SimpleIdentifierExpression(scanner.getTokenStartPosition(),
                currentScope, scanner.getTokenValueIdentifier());

        // If followed by a '.' it is a qualified identifier
        //如果一个 . 紧随其后。这是一个限定的标识符
        if (scanner.nextToken() == TokenKind.TOK_DOT) {
            do {
                scanner.nextToken(); // Skip '.'
                accept(TokenKind.TOK_IDENTIFIER);
                expr = new QualifiedIdentifierExpression(scanner.getTokenStartPosition(), expr,
                        scanner.getTokenValueIdentifier());
            } while (scanner.nextToken() == TokenKind.TOK_DOT);
        }

        return expr;
    }

    /*
     * Parses a 'New' expression.
     */
    private Expression parseNewExpression() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_NEW);

        long exprStartPosition = scanner.getTokenStartPosition();
        scanner.nextToken();  // Skip 'New'

        Type type = parseNonArrayType();

        List<Expression> dimensions = null;
        switch (scanner.getToken()) {
            default:
                return new NewExpression(exprStartPosition, type, null);

            case TOK_OPENPARENTHESIS:
                // Array type
                dimensions = parseArrayDimensions();
                return new NewExpression(exprStartPosition, new ArrayType(type, dimensions.size()),
                        dimensions);

            case TOK_ON:
                // Component on container
                scanner.nextToken();  // Skip 'On'
                return new NewComponentExpression(parseExpression(), type);
        }
    }

    /*
     * Skips the first identity operator and reports an error if there any additional ones following.
     */
    private void skipIdentityOperator() {
        TokenKind tokenKind = scanner.nextToken();  // Skip '-' or '+'

        while (tokenKind == TokenKind.TOK_MINUS || tokenKind == TokenKind.TOK_PLUS) {
            compiler.error(scanner.getTokenStartPosition(), Error.errUnexpected, tokenKind.toString());
            tokenKind = scanner.nextToken();
        }
    }

    /*
     * Parses partial expressions based on their priority. If the parser encounters an operator
     * with a priority higher than the current one, then it will complete parsing of the sub-
     * expression.
     */
    private Expression parseExpression(int priority) {
        Expression expr;

        // Parse the left hand side of an expression first
        TokenKind tokenKind = scanner.getToken();
        long exprStartPosition = scanner.getTokenStartPosition();

        switch (tokenKind) {
            default:
                compiler.error(exprStartPosition, Error.errUnexpected, tokenKind.toString());
                throw new SyntaxError();

            case TOK_BOOLEANCONSTANT:
                expr = new ConstantBooleanExpression(exprStartPosition, scanner.getTokenValueBoolean());
                scanner.nextToken();
                break;

            case TOK_IDENTIFIER:
                expr = parseQualifiedIdentifier();
                if (scanner.getToken() == TokenKind.TOK_OPENPARENTHESIS) {
                    expr = new CallExpression(scanner.getTokenStartPosition(), expr,
                            parseActualArgumentList());
                }
                break;

            case TOK_ME:
                expr = new MeExpression(exprStartPosition, currentObjectSymbol);
                scanner.nextToken();
                // TODO: what about Me.foo.bar?
                break;

            case TOK_MINUS:
                skipIdentityOperator();
                expr = new NegationExpression(exprStartPosition, parseExpression(PRIO_NEGATION));
                break;

            case TOK_NEW:
                expr = parseNewExpression();
                break;

            case TOK_NOT:
                scanner.nextToken(); // Skip 'Not'
                expr = new NotExpression(exprStartPosition,
                        parseExpression(PRIO_LOGICALORBITOPERATION_NEGATION));
                break;

            case TOK_NOTHING:
                scanner.nextToken(); // Skip 'Nothing'
                expr = new NothingExpression(exprStartPosition);
                break;

            case TOK_NUMERICCONSTANT:
                expr = new ConstantNumberExpression(exprStartPosition, scanner.getTokenValueNumber());
                scanner.nextToken();
                break;

            case TOK_OPENPARENTHESIS:
                scanner.nextToken(); // Skip '('
                expr = parseExpression();
                acceptAndSkip(TokenKind.TOK_CLOSEPARENTHESIS);
                break;

            case TOK_PLUS:
                skipIdentityOperator();
                expr = new IdentityExpression(exprStartPosition, parseExpression(PRIO_NEGATION));
                break;

            case TOK_STRINGCONSTANT:
                expr = new ConstantStringExpression(exprStartPosition, scanner.getTokenValueString());
                scanner.nextToken();
                break;

            case TOK_TYPEOF:
                scanner.nextToken(); // Skip 'TypeOf'
                expr = parseExpression(PRIO_COMPARISON);
                exprStartPosition = scanner.getTokenStartPosition();
                acceptAndSkip(TokenKind.TOK_IS);
                expr = new TypeOfExpression(exprStartPosition, expr, parseType());
                break;
        }

        // Then repeatedly parse right hand side sub-expressions as long as their priority is lower
        // (or the same - depending on the operator) as the current expression priority.
        for (; ; ) {
            exprStartPosition = scanner.getTokenStartPosition();
            switch (scanner.getToken()) {
                default:
                    return expr;

                case TOK_AND:
                    if (priority >= PRIO_LOGICALORBITOPERATION_CONJUNCTION) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip 'And'
                    expr = new AndExpression(exprStartPosition, expr,
                            parseExpression(PRIO_LOGICALORBITOPERATION_CONJUNCTION));
                    break;

                case TOK_AMPERSAND:
                    if (priority >= PRIO_CONCATENATION) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '&'
                    expr = new ConcatenationExpression(exprStartPosition, expr,
                            parseExpression(PRIO_CONCATENATION));
                    break;

                case TOK_DIVIDE:
                    if (priority >= PRIO_MULTIPLICATION) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '/'
                    expr = new DivisionExpression(exprStartPosition, expr,
                            parseExpression(PRIO_MULTIPLICATION));
                    break;

                case TOK_DOT:
                    scanner.nextToken(); // Skip '.'
                    accept(TokenKind.TOK_IDENTIFIER);
                    expr = new QualifiedIdentifierExpression(scanner.getTokenStartPosition(), expr,
                            scanner.getTokenValueIdentifier());
                    scanner.nextToken();
                    break;

                case TOK_EQUAL:
                    if (priority >= PRIO_COMPARISON) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '='
                    expr = new EqualExpression(exprStartPosition, expr, parseExpression(PRIO_COMPARISON));
                    break;

                case TOK_EXP:
                    if (priority > PRIO_EXPONENTIATION) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '^'
                    expr = new ExponentiationExpression(exprStartPosition, expr,
                            parseExpression(PRIO_EXPONENTIATION));
                    break;

                case TOK_INTEGERDIVIDE:
                    if (priority >= PRIO_INTEGERDIVISION) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '\'
                    expr = new IntegerDivisionExpression(exprStartPosition, expr,
                            parseExpression(PRIO_INTEGERDIVISION));
                    break;

                case TOK_IS:
                    if (priority >= PRIO_COMPARISON) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip 'Is'
                    expr = new IsExpression(exprStartPosition, expr, parseExpression(PRIO_COMPARISON));
                    break;

                case TOK_ISNOT:
                    if (priority >= PRIO_COMPARISON) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip 'IsNot'
                    expr = new IsNotExpression(exprStartPosition, expr, parseExpression(PRIO_COMPARISON));
                    break;

                case TOK_GREATER:
                    if (priority >= PRIO_COMPARISON) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '>'
                    expr = new GreaterExpression(exprStartPosition, expr, parseExpression(PRIO_COMPARISON));
                    break;

                case TOK_GREATEREQUAL:
                    if (priority >= PRIO_COMPARISON) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '>='
                    expr = new GreaterOrEqualExpression(exprStartPosition, expr,
                            parseExpression(PRIO_COMPARISON));
                    break;

                case TOK_LESS:
                    if (priority >= PRIO_COMPARISON) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '<'
                    expr = new LessExpression(exprStartPosition, expr, parseExpression(PRIO_COMPARISON));
                    break;

                case TOK_LESSEQUAL:
                    if (priority >= PRIO_COMPARISON) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '<='
                    expr = new LessOrEqualExpression(exprStartPosition, expr,
                            parseExpression(PRIO_COMPARISON));
                    break;

                case TOK_LIKE:
                    if (priority >= PRIO_COMPARISON) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip 'Like'
                    expr = new LikeExpression(exprStartPosition, expr, parseExpression(PRIO_COMPARISON));
                    break;

                case TOK_MINUS:
                    if (priority >= PRIO_ADDITION) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '-'
                    expr = new SubtractionExpression(exprStartPosition, expr, parseExpression(PRIO_ADDITION));
                    break;

                case TOK_MOD:
                    if (priority >= PRIO_MODULO) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip 'Mod'
                    expr = new ModuloExpression(exprStartPosition, expr, parseExpression(PRIO_MODULO));
                    break;

                case TOK_NOTEQUAL:
                    if (priority >= PRIO_COMPARISON) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '<>'
                    expr = new NotEqualExpression(exprStartPosition, expr, parseExpression(PRIO_COMPARISON));
                    break;

                case TOK_OR:
                    if (priority >= PRIO_LOGICALORBITOPERATION_DISJUNCTION) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip 'Or'
                    expr = new OrExpression(exprStartPosition, expr,
                            parseExpression(PRIO_LOGICALORBITOPERATION_DISJUNCTION));
                    break;

                case TOK_PLUS:
                    if (priority >= PRIO_ADDITION) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '+'
                    expr = new AdditionExpression(exprStartPosition, expr, parseExpression(PRIO_ADDITION));
                    break;

                case TOK_SHIFTLEFT:
                    if (priority >= PRIO_SHIFT) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '<<'
                    expr = new ShiftLeftExpression(exprStartPosition, expr, parseExpression(PRIO_SHIFT));
                    break;

                case TOK_SHIFTRIGHT:
                    if (priority >= PRIO_SHIFT) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '>>'
                    expr = new ShiftRightExpression(exprStartPosition, expr, parseExpression(PRIO_SHIFT));
                    break;

                case TOK_TIMES:
                    if (priority >= PRIO_MULTIPLICATION) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip '*'
                    expr = new MultiplicationExpression(exprStartPosition, expr,
                            parseExpression(PRIO_MULTIPLICATION));
                    break;

                case TOK_XOR:
                    if (priority >= PRIO_LOGICALORBITOPERATION_DISJUNCTION) {
                        return expr;
                    }

                    scanner.nextToken(); // Skip 'Xor'
                    expr = new XorExpression(exprStartPosition, expr,
                            parseExpression(PRIO_LOGICALORBITOPERATION_DISJUNCTION));
                    break;
            }
        }
    }

    /*
     * Parses a (top-level) expression.
     */
    private Expression parseExpression() {
        return parseExpression(PRIO_ASSIGN);
    }

    /*
     * Parses a Do-Loop statement. Note that there are two varieties: loop while and loop until.
     */
    private Statement parseDoLoopStatement() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_DO);

        scanner.nextToken();  // Skip 'Do'

        acceptAndSkipEndOfStatement();

        // Parse loop body
        StatementBlock savedStatementList = currentStatementList;
        StatementBlock loopStatements = new StatementBlock(currentScope);
        try {
            currentStatementList = loopStatements;
            currentScope = loopStatements.getScope();
            parseStatementList(TokenKind.TOK_DO);
        } finally {
            currentStatementList = savedStatementList;
            currentScope = currentStatementList.getScope();
        }

        // Parse loop condition
        TokenKind loopToken = scanner.getToken();
        long stmtStartPosition = scanner.getTokenStartPosition();
        scanner.nextToken();
        Expression condition = parseExpression();
        acceptAndSkipEndOfStatement();

        switch (loopToken) {
            default:
                // Should not happen! It must be 'While' or 'Until' because
                // otherwise parseStatementList(TokenKind.TOK_DO) wouldn't have returned
                // here - otherwise it would have thrown an exception.
                assert false;
                return null;

            case TOK_UNTIL:
                return new DoUntilStatement(stmtStartPosition, condition, loopStatements);

            case TOK_WHILE:
                return new DoWhileStatement(stmtStartPosition, condition, loopStatements);
        }
    }

    /*
     * Parses an Exit statement. The acceptEndOfStatement parameter must be set to false for
     * sub-statements of the single line version of the If-Else-Statement.
     */
    private Statement parseExitStatement(boolean acceptEndOfStatement) {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_EXIT);

        long stmtStartPosition = scanner.getTokenStartPosition();
        TokenKind token = scanner.nextToken();
        switch (token) {
            default:
                token = TokenKind.TOK_NONE;
                break;

            case TOK_DO:
            case TOK_EVENT:
            case TOK_FOR:
            case TOK_FUNCTION:
            case TOK_PROPERTY:
            case TOK_SUB:
            case TOK_WHILE:
                // These tokens are allowed to follow an Exit statement
                scanner.nextToken();
                break;
        }

        if (acceptEndOfStatement) {
            acceptAndSkipEndOfStatement();
        }

        return new ExitStatement(stmtStartPosition, token);
    }

    /*
     * Parses an expression statement. The acceptEndOfStatement parameter must be set to false for
     * sub-statements of the single line version of the If-Else-Statement.
     */
    private Statement parseExpressionStatement(boolean acceptEndOfStatement) {
        // Parsing an assignment expression is kind of tricky because of the overloaded use of '=' for
        // assignment as well as comparison. We solve this by just parsing a right-hand-side
        // expression (PRIO_DOT) and if it is followed by '=' we assume it is an assignment expression.
        long stmtStartPosition = scanner.getTokenStartPosition();
        Expression expr = parseExpression(PRIO_DOT);
        if (scanner.getToken() == TokenKind.TOK_EQUAL) {
            // This is an assignment expression
            long exprStartPosition = scanner.getTokenStartPosition();
            scanner.nextToken(); // Skip '='
            expr = new AssignmentExpression(exprStartPosition, expr, parseExpression());
        }

        if (acceptEndOfStatement) {
            acceptAndSkipEndOfStatement();
        }

        return new ExpressionStatement(stmtStartPosition, expr);
    }

    /*
     * Parses a For-statement. Note that there are two variations: For-Next-statements and For-Each-
     * statements.
     */
    private Statement parseForStatement() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_FOR);

        long stmtStartPosition = scanner.getTokenStartPosition();
        Statement stmt;

        if (scanner.nextToken() == TokenKind.TOK_EACH) {
            // Parse For...Each statement
            scanner.nextToken();  // Skip 'Each'

            accept(TokenKind.TOK_IDENTIFIER);
            String loopVarName = scanner.getTokenValueIdentifier();
            Expression loopVarExpr = new SimpleIdentifierExpression(scanner.getTokenStartPosition(),
                    currentScope, loopVarName);

            // Parse initialization expression
            scanner.nextToken();  // Skip identifier
            acceptAndSkip(TokenKind.TOK_IN);
            Expression initExpr = parseExpression();
            acceptAndSkipEndOfStatement();

            // Parse the loop body
            StatementBlock savedStatementList = currentStatementList;
            StatementBlock loopStatements = new StatementBlock(currentScope);
            try {
                currentStatementList = loopStatements;
                currentScope = loopStatements.getScope();
                parseStatementList(TokenKind.TOK_FOR);
            } finally {
                currentStatementList = savedStatementList;
                currentScope = currentStatementList.getScope();
            }

            // If there is the optional identifier after the Next keyword then it must match the name of
            // the loop variable.
            if (scanner.getToken() == TokenKind.TOK_IDENTIFIER) {
                if (!scanner.getTokenValueIdentifier().equals(loopVarName)) {
                    compiler.error(scanner.getTokenStartPosition(), Error.errForNextIdentifierMismatch,
                            scanner.getTokenValueIdentifier(), loopVarName);
                }
                scanner.nextToken();  // Skip identifier
            }

            // Return the For statement.
            stmt = new ForEachStatement(stmtStartPosition, loopVarExpr, initExpr, loopStatements);
        } else {
            // Parse For...Next statement
            accept(TokenKind.TOK_IDENTIFIER);
            String loopVarName = scanner.getTokenValueIdentifier();
            Expression loopVarExpr = new SimpleIdentifierExpression(scanner.getTokenStartPosition(),
                    currentScope, loopVarName);

            // Parse initialization expression
            scanner.nextToken();  // Skip identifier
            acceptAndSkip(TokenKind.TOK_EQUAL);
            Expression initExpr = parseExpression();

            // Parse end condition
            acceptAndSkip(TokenKind.TOK_TO);
            Expression endExpr = parseExpression();

            // Parse optional step expression
            Expression stepExpr;
            if (scanner.getToken() == TokenKind.TOK_STEP) {
                scanner.nextToken();  // Skip 'Step'
                stepExpr = parseExpression();
            } else {
                stepExpr = new ConstantNumberExpression(0, BigDecimal.ONE);
            }

            acceptAndSkipEndOfStatement();

            // Parse the loop body
            StatementBlock savedStatementList = currentStatementList;
            StatementBlock loopStatements = new StatementBlock(currentScope);
            try {
                currentStatementList = loopStatements;
                currentScope = loopStatements.getScope();
                parseStatementList(TokenKind.TOK_FOR);
            } finally {
                currentStatementList = savedStatementList;
                currentScope = currentStatementList.getScope();
            }

            // If there is the optional identifier after the Next keyword then it must match the name of
            // the loop variable.
            if (scanner.getToken() == TokenKind.TOK_IDENTIFIER) {
                if (!scanner.getTokenValueIdentifier().equals(loopVarName)) {
                    compiler.error(scanner.getTokenStartPosition(), Error.errForNextIdentifierMismatch,
                            scanner.getTokenValueIdentifier(), loopVarName);
                }
                scanner.nextToken();  // Skip identifier
            }

            // Return the For statement.
            stmt = new ForNextStatement(stmtStartPosition, loopVarExpr, initExpr, endExpr, stepExpr,
                    loopStatements);
        }

        acceptAndSkipEndOfStatement();

        return stmt;
    }

    /*
     * Parses an If-statement. There are plenty of different variations, but the most tricky ones
     * are the single-line versions. They will require us to allow statements without the customary
     * end-of-statement delimiters. Also the statements allowed for the then- and else-parts are
     * limited to expression and exit statements.
     */
    private Statement parseIfStatement() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_IF ||
                scanner.getToken() == TokenKind.TOK_ELSEIF);

        long stmtStartPosition = scanner.getTokenStartPosition();

        scanner.nextToken();
        Expression condition = parseExpression();

        acceptAndSkip(TokenKind.TOK_THEN);

        StatementBlock thenStatements = new StatementBlock(currentScope);
        StatementBlock elseStatements = null;

        TokenKind token = scanner.getToken();
        if (token != TokenKind.TOK_EOS && token != TokenKind.TOK_EOF) {
            // Single line form of If statement
            thenStatements.add(parseSameLineStatement());

            if (scanner.getToken() == TokenKind.TOK_ELSE) {
                scanner.nextToken();  // Skip 'Else'
                elseStatements = new StatementBlock(currentScope);
                elseStatements.add(parseSameLineStatement());
            }

            acceptAndSkipEndOfStatement();
        } else {
            // Complex form of If statement
            acceptAndSkipEndOfStatement();

            StatementBlock savedStatementList = currentStatementList;

            try {
                // Parse statements in Then block
                currentStatementList = thenStatements;
                currentScope = thenStatements.getScope();
                parseStatementList(TokenKind.TOK_IF);

                if (scanner.getToken() == TokenKind.TOK_ELSEIF) {
                    // Parse ElseIf block statements
                    currentStatementList =
                            elseStatements = new StatementBlock(currentScope);
                    currentScope = elseStatements.getScope();
                    elseStatements.add(parseIfStatement());
                } else if (scanner.getToken() == TokenKind.TOK_ELSE) {
                    // Parse Else block statements
                    scanner.nextToken();
                    acceptAndSkipEndOfStatement();

                    currentStatementList =
                            elseStatements = new StatementBlock(currentScope);
                    currentScope = elseStatements.getScope();
                    parseStatementList(TokenKind.TOK_IF);
                }
            } finally {
                currentStatementList = savedStatementList;
                currentScope = currentStatementList.getScope();
            }
        }

        return new IfStatement(stmtStartPosition, condition, thenStatements, elseStatements);
    }

    /*
     * Parses an On Error statement. Note that there can be only one On Error statement per
     * function, procedure, event handler or property getter/setter.
     */
    private void parseOnErrorStatement() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_ON);

        long stmtStartPosition = scanner.getTokenStartPosition();

        scanner.nextToken();
        acceptAndSkip(TokenKind.TOK_ERROR);
        acceptAndSkipEndOfStatement();

        StatementBlock savedStatementList = currentStatementList;

        OnErrorStatement onErrorStmt = new OnErrorStatement(stmtStartPosition);

        try {
            for (; ; ) {
                TokenKind tokenKind = scanner.getToken();
                switch (tokenKind) {
                    default:
                        compiler.error(scanner.getTokenStartPosition(), Error.errUnexpected,
                                tokenKind.toString());
                        throw new SyntaxError();

                    case TOK_END:
                        scanner.nextToken(); // Skip 'End'
                        acceptAndSkip(TokenKind.TOK_ERROR);
                        acceptAndSkipEndOfStatement();
                        break;

                    case TOK_CASE:
                        StatementBlock statements = new StatementBlock(currentScope);
                        OnErrorCaseStatement caseStatement = onErrorStmt.newCaseStatement(
                                scanner.getTokenStartPosition(), statements);

                        if (scanner.nextToken() == TokenKind.TOK_ELSE) {
                            scanner.nextToken();  // Skip 'Else'
                        } else {
                            for (; ; ) {
                                caseStatement.addTypeExpression(parseExpression());

                                if (scanner.getToken() != TokenKind.TOK_COMMA) {
                                    break;
                                }

                                scanner.nextToken(); // Skip ','
                            }
                        }

                        acceptAndSkipEndOfStatement();

                        currentStatementList = statements;
                        currentScope = statements.getScope();
                        parseStatementList(TokenKind.TOK_SELECT);
                        continue;
                }
                break;
            }
        } finally {
            currentStatementList = savedStatementList;
            currentScope = currentStatementList.getScope();
        }

        if (!currentFunction.setOnErrorStatement(onErrorStmt)) {
            compiler.error(onErrorStmt.getPosition(), Error.errMultipleOnErrorStatements);
        }
    }

    /*
     * Parses a RaiseEvent statement. The acceptEndOfStatement parameter must be set to false for
     * sub-statements of the single line version of the If-Else-Statement.
     */
    private Statement parseRaiseEventStatement(boolean acceptEndOfStatement) {
        long stmtStartPosition = scanner.getTokenStartPosition();

        scanner.nextToken();  // Skip 'RaiseEvent'

        accept(TokenKind.TOK_IDENTIFIER);
        String eventName = scanner.getTokenValueIdentifier();
        scanner.nextToken();  // Skip '<identifier>'

        accept(TokenKind.TOK_OPENPARENTHESIS);
        List<Expression> actualArgList = parseActualArgumentList();

        if (acceptEndOfStatement) {
            acceptAndSkipEndOfStatement();
        }

        return new RaiseEventStatement(stmtStartPosition, eventName, actualArgList);
    }

    /*
     * Parses a Select statement.
     */
    private Statement parseSelectStatement() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_SELECT);

        long stmtStartPosition = scanner.getTokenStartPosition();

        scanner.nextToken();
        Expression selector = parseExpression();

        acceptAndSkipEndOfStatement();

        StatementBlock savedStatementList = currentStatementList;

        SelectStatement selectStmt = new SelectStatement(stmtStartPosition, selector);

        try {
            for (; ; ) {
                TokenKind tokenKind = scanner.getToken();
                switch (tokenKind) {
                    default:
                        compiler.error(scanner.getTokenStartPosition(), Error.errUnexpected,
                                tokenKind.toString());
                        throw new SyntaxError();

                    case TOK_END:
                        scanner.nextToken(); // Skip 'End'
                        acceptAndSkip(TokenKind.TOK_SELECT);
                        acceptAndSkipEndOfStatement();
                        break;

                    case TOK_CASE:
                        StatementBlock statements = new StatementBlock(currentScope);
                        SelectCaseStatement caseStatement = selectStmt.newCaseStatement(
                                scanner.getTokenStartPosition(), statements);

                        if (scanner.nextToken() == TokenKind.TOK_ELSE) {
                            scanner.nextToken();  // Skip 'Else'
                        } else {
                            for (; ; ) {
                                switch (scanner.getToken()) {
                                    default:
                                        Expression expression = parseExpression();
                                        if (scanner.getToken() == TokenKind.TOK_TO) {
                                            scanner.nextToken();  // Skip 'To'
                                            caseStatement.addRangeExpression(expression, parseExpression());
                                        } else {
                                            caseStatement.addEqualExpression(expression);
                                        }
                                        break;

                                    case TOK_IS:
                                        tokenKind = scanner.nextToken();
                                        scanner.nextToken();  // Skip comparison token
                                        switch (tokenKind) {
                                            default:
                                                compiler.error(scanner.getTokenStartPosition(), Error.errUnexpected,
                                                        tokenKind.toString());
                                                throw new SyntaxError();

                                            case TOK_EQUAL:
                                                caseStatement.addEqualExpression(parseExpression());
                                                break;

                                            case TOK_NOTEQUAL:
                                                caseStatement.addNotEqualExpression(parseExpression());
                                                break;

                                            case TOK_LESS:
                                                caseStatement.addLessExpression(parseExpression());
                                                break;

                                            case TOK_LESSEQUAL:
                                                caseStatement.addLessOrEqualExpression(parseExpression());
                                                break;

                                            case TOK_GREATER:
                                                caseStatement.addGreaterExpression(parseExpression());
                                                break;

                                            case TOK_GREATEREQUAL:
                                                caseStatement.addGreaterOrEqualExpression(parseExpression());
                                                break;
                                        }
                                        break;
                                }

                                if (scanner.getToken() != TokenKind.TOK_COMMA) {
                                    break;
                                }

                                scanner.nextToken();  // Skip ','
                            }
                        }

                        acceptAndSkipEndOfStatement();

                        currentStatementList = statements;
                        currentScope = statements.getScope();
                        parseStatementList(TokenKind.TOK_SELECT);
                        continue;
                }
                break;
            }
        } finally {
            currentStatementList = savedStatementList;
            currentScope = currentStatementList.getScope();
        }

        return selectStmt;
    }

    /*
     * Parses a While statement.
     */
    private Statement parseWhileStatement() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_WHILE);

        long stmtStartPosition = scanner.getTokenStartPosition();

        scanner.nextToken();
        Expression condition = parseExpression();

        acceptAndSkipEndOfStatement();

        StatementBlock savedStatementList = currentStatementList;
        StatementBlock loopStatements = new StatementBlock(currentScope);
        try {
            currentStatementList = loopStatements;
            currentScope = loopStatements.getScope();
            parseStatementList(TokenKind.TOK_WHILE);
        } finally {
            currentStatementList = savedStatementList;
            currentScope = currentStatementList.getScope();
        }

        return new WhileStatement(stmtStartPosition, condition, loopStatements);
    }

    /*
     * Parses a local variable declaration.
     */
    private void parseLocalVariableDeclaration() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_DIM);

        scanner.nextToken();  // Skip 'Dim'

        for (; ; ) {
            // Parse identifier and type declaration.
            long symStartPosition = scanner.getTokenStartPosition();
            accept(TokenKind.TOK_IDENTIFIER);
            String name = scanner.getTokenValueIdentifier();
            scanner.nextToken();  // Skip '<identifier>'
            acceptAndSkip(TokenKind.TOK_AS);
            Type type = parseNonArrayType();

            List<Expression> dimensions = null;
            if (scanner.getToken() == TokenKind.TOK_OPENPARENTHESIS) {
                // Array type
                dimensions = parseArrayDimensions();
                type = new ArrayType(type, dimensions.size());
            }

            // Declare local variable.
            LocalVariableSymbol local =
                    currentFunction.addLocalVariable(symStartPosition, name, type, currentScope);
            if (local == null) {
                compiler.error(symStartPosition, Error.errSymbolRedefinition, name);
            } else {
                MarkerStatement marker = new LocalVariableDefinitionStatement(local);
                currentStatementList.add(marker);
                local.setBeginScope(marker);
                local.setStaticArrayDimensions(dimensions);
            }

            // Unless there is a comma, we are done.
            if (scanner.getToken() != TokenKind.TOK_COMMA) {
                break;
            }

            scanner.nextToken(); // Skip ','
        }

        acceptAndSkipEndOfStatement();
    }

    /*
     * Parses a block (or list) of statements.
     */
    private void parseStatementList(TokenKind tokenAfterEnd) {
        for (; ; ) {
            // Keep parsing until we hit a token that could indicate an end to the statement list
            switch (scanner.getToken()) {
                default:
                    break;

                case TOK_END:
                    if (tokenAfterEnd != TokenKind.TOK_SELECT && tokenAfterEnd != TokenKind.TOK_ERROR) {
                        scanner.nextToken(); // Skip 'End'
                        acceptAndSkip(tokenAfterEnd);
                        acceptAndSkipEndOfStatement();
                    }
                    return;

                case TOK_NEXT:
                    if (tokenAfterEnd == TokenKind.TOK_FOR) {
                        scanner.nextToken(); // Skip 'Next'
                        return;
                    }
                    break;

                case TOK_UNTIL:
                case TOK_WHILE:
                    if (tokenAfterEnd == TokenKind.TOK_DO) {
                        return;
                    }
                    break;

                case TOK_CASE:
                case TOK_EOF:
                    return;
            }

            try {
                Statement stmt = parseStatement();
                if (stmt == null) {
                    // Possibly encountered an 'Else' or 'ElseIf'
                    TokenKind tokenKind = scanner.getToken();
                    switch (tokenKind) {
                        default:
                            break;

                        case TOK_ELSE:
                        case TOK_ELSEIF:
                            if (tokenAfterEnd == TokenKind.TOK_IF) {
                                return;
                            }
                            compiler.error(scanner.getTokenStartPosition(), Error.errUnexpected,
                                    tokenKind.toString());
                            throw new SyntaxError();
                    }
                } else {
                    currentStatementList.add(stmt);
                }
            } catch (SyntaxError se) {
                resyncAfterSyntaxError();
            }
        }
    }

    /*
     * Parses a statement on the same line as the enclosing statement. The only case where this is
     * actually happening is the single line If-statement. This method does not accept
     * end-of-statement tokens at the end of the statement.
     */
    private Statement parseSameLineStatement() {
        TokenKind tokenKind = scanner.getToken();
        switch (tokenKind) {
            default:
                compiler.error(scanner.getTokenStartPosition(), Error.errUnexpected, tokenKind.toString());
                throw new SyntaxError();

            case TOK_IDENTIFIER:
            case TOK_ME:
                return parseExpressionStatement(false);

            case TOK_EXIT:
                return parseExitStatement(false);

            case TOK_RAISEEVENT:
                return parseRaiseEventStatement(false);
        }
    }

    /*
     * Parses a single statement (will also absorb local variable declarations). Note that this
     * method will return null only if it encounters an 'Else' or 'ElseIf' token or if wants to
     * defer error reporting to its caller.
     */
    private Statement parseStatement() {
        TokenKind tokenKind = scanner.getToken();
        switch (tokenKind) {
            default:
                compiler.error(scanner.getTokenStartPosition(), Error.errUnexpected, tokenKind.toString());
                throw new SyntaxError();

            case TOK_DIM:
                parseLocalVariableDeclaration();
                return null;

            case TOK_DO:
                return parseDoLoopStatement();

            case TOK_ELSE:
            case TOK_ELSEIF:
                return null;

            case TOK_EXIT:
                return parseExitStatement(true);

            case TOK_FOR:
                return parseForStatement();

            case TOK_IDENTIFIER:
            case TOK_ME:
                return parseExpressionStatement(true);

            case TOK_IF:
                return parseIfStatement();

            case TOK_ON:
                parseOnErrorStatement();
                return null;

            case TOK_RAISEEVENT:
                return parseRaiseEventStatement(true);

            case TOK_SELECT:
                return parseSelectStatement();

            case TOK_WHILE:
                return parseWhileStatement();
        }
    }

    /*
     * Parses an alias declaration (only allowed at file-level).
     */
    private void parseAliasDeclaration() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_ALIAS);

        scanner.nextToken();  // Skip 'Alias'
        accept(TokenKind.TOK_IDENTIFIER);
        String alias = scanner.getTokenValueIdentifier();
        scanner.nextToken();  // Skip '<identifier>'

        long exprStartPosition = scanner.getTokenStartPosition();
        acceptAndSkip(TokenKind.TOK_EQUAL);

        currentObjectSymbol.addAlias(new AliasExpression(exprStartPosition, alias,
                parseQualifiedIdentifier()));

        acceptAndSkipEndOfStatement();
    }

    /*
     * Parses a constant declaration.
     */
    private void parseConstantDeclaration() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_CONST);

        for (; ; ) {
            // Parse constant declaration
            scanner.nextToken();  // Skip 'Const'
            long symStartPosition = scanner.getTokenStartPosition();
            accept(TokenKind.TOK_IDENTIFIER);
            String name = scanner.getTokenValueIdentifier();
            scanner.nextToken();  // Skip '<identifier>'
            acceptAndSkip(TokenKind.TOK_AS);
            Type type = parseNonArrayType();

            acceptAndSkip(TokenKind.TOK_EQUAL);
            Expression constExpr = parseExpression();

            // Add a new constant field member to the current class
            ConstantDataMemberSymbol constDataMember = new ConstantDataMemberSymbol(symStartPosition,
                    currentObjectSymbol, name, type, constExpr);
            if (!currentObjectSymbol.addDataMember(constDataMember)) {
                compiler.error(symStartPosition, Error.errSymbolRedefinition, name);
            }

            // Unless there is a comma, we are done.
            if (scanner.getToken() != TokenKind.TOK_COMMA) {
                break;
            }

            scanner.nextToken(); // Skip ','
        }

        acceptAndSkipEndOfStatement();
    }

    /*
     * Parses a field (data member) declaration for either a static (object) or an instance field.
     */
    private void parseFieldDeclaration(boolean isStatic) {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_DIM);

        scanner.nextToken();  // Skip 'Dim'

        for (; ; ) {
            // Parse field member declaration
            long symStartPosition = scanner.getTokenStartPosition();
            accept(TokenKind.TOK_IDENTIFIER);
            String name = scanner.getTokenValueIdentifier();
            scanner.nextToken();  // Skip '<identifier>'
            acceptAndSkip(TokenKind.TOK_AS);
            Type type = parseNonArrayType();

            List<Expression> dimensions = null;
            if (scanner.getToken() == TokenKind.TOK_OPENPARENTHESIS) {
                // Array type
                dimensions = parseArrayDimensions();
                type = new ArrayType(type, dimensions.size());
            }

            // Add a new field member to the current class
            DataMemberSymbol field = isStatic ?
                    new ObjectDataMemberSymbol(symStartPosition, currentObjectSymbol, name, type) :
                    new InstanceDataMemberSymbol(symStartPosition, currentObjectSymbol, name, type);
            if (!currentObjectSymbol.addDataMember(field)) {
                compiler.error(symStartPosition, Error.errSymbolRedefinition, name);
            }
            field.setStaticArrayDimensions(dimensions);

            // Unless there is a comma, we are done.
            if (scanner.getToken() != TokenKind.TOK_COMMA) {
                break;
            }

            scanner.nextToken(); // Skip ','
        }

        acceptAndSkipEndOfStatement();
    }

    /*
     * Parses the formal argument list of a function, procedure, property or event handler.
     */
    private void parseFormalArgumentList(FunctionSymbol functionSymbol) {
        for (; ; ) {
            boolean isReferenceParameter = false;

            switch (scanner.getToken()) {
                default:
                    break;

                case TOK_BYVAL:
                    // Nothing to do - this is the default
                    scanner.nextToken(); // Skip 'ByVal'
                    break;

                case TOK_BYREF:
                    isReferenceParameter = true;
                    scanner.nextToken(); // Skip 'ByRef'
                    break;
            }

            accept(TokenKind.TOK_IDENTIFIER);
            long symStartPosition = scanner.getTokenStartPosition();
            String name = scanner.getTokenValueIdentifier();

            scanner.nextToken();
            acceptAndSkip(TokenKind.TOK_AS);

            if (functionSymbol.addParameter(symStartPosition, name, parseType(),
                    isReferenceParameter) == null) {
                compiler.error(symStartPosition, Error.errSymbolRedefinition, name);
            }

            if (scanner.getToken() == TokenKind.TOK_CLOSEPARENTHESIS) {
                scanner.nextToken(); // Skip ')'
                break;
            }

            acceptAndSkip(TokenKind.TOK_COMMA);
        }
    }

    /*
     * Parses the body of a function, procedure, property or event handler.
     */
    private void parseFunctionBody(FunctionSymbol function, TokenKind afterEndToken) {
        currentStatementList = function.getFunctionStatements();
        currentFunction = function;

        Scope outerScope = currentScope;
        currentScope = currentStatementList.getScope();

        try {
            parseStatementList(afterEndToken);
        } finally {
            currentScope = outerScope;
            currentStatementList = null;
            currentFunction = null;
        }
    }

    /*
     * Parses an event handler declaration.
     */
    private void parseEventHandlerDeclaration() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_EVENT);

        // There are two situations to handle here:
        //
        // 1) An event declaration:
        //
        //    Event <identifier>(<formalArgList>)
        //    End Event
        //
        //    When we get to code generation time, this will simply be an empty virtual method
        //    with an @SimpleEvent attribute
        //
        // 2) An event handler definition:
        //
        //    Event <identifier>.<identifier>(<formalArgList>)
        //      <statements>
        //    End Events
        //
        //    The first identifier can either be a type name (in which case the type name must match
        //    the name of the current object) or a data member name. The second identifier must be the
        //    name of an event.

        FunctionSymbol eventSymbol;
        boolean isEvent = false;
        try {
            scanner.nextToken();  // Skip 'Event'
            accept(TokenKind.TOK_IDENTIFIER);
            long firstIdentifierStartPosition = scanner.getTokenStartPosition();
            String firstIdentifier = scanner.getTokenValueIdentifier();

            if (scanner.nextToken() == TokenKind.TOK_DOT) { // Skip <identifier>
                // Parse an event handler definition
                scanner.nextToken();  // Skip '.'

                accept(TokenKind.TOK_IDENTIFIER);
                String secondIdentifier = scanner.getTokenValueIdentifier();
                scanner.nextToken();  // Skip <identifier>

                // Define EventHandler
                EventHandlerSymbol eventHandler = new EventHandlerSymbol(firstIdentifierStartPosition,
                        currentObjectSymbol, firstIdentifier, secondIdentifier);
                if (!currentObjectSymbol.addEventHandler(eventHandler)) {
                    compiler.error(firstIdentifierStartPosition, Error.errSymbolRedefinition,
                            firstIdentifier + '.' + secondIdentifier);
                }

                eventSymbol = eventHandler;

            } else {
                // Parse an event declaration
                EventSymbol event = new EventSymbol(firstIdentifierStartPosition, currentObjectSymbol,
                        firstIdentifier);
                if (!currentObjectSymbol.addEvent(event)) {
                    compiler.error(firstIdentifierStartPosition, Error.errSymbolRedefinition,
                            firstIdentifier);
                }

                event.setIsCompiled();
                isEvent = true;

                eventSymbol = event;
            }

            // Both event handler and event share the following code: parse formal arguments
            acceptAndSkip(TokenKind.TOK_OPENPARENTHESIS);
            if (scanner.getToken() == TokenKind.TOK_CLOSEPARENTHESIS) {
                // Empty formal argument list
                scanner.nextToken(); // Skip ')'
            } else {
                parseFormalArgumentList(eventSymbol);
            }
            acceptAndSkipEndOfStatement();

        } catch (SyntaxError se) {
            resyncAfterSyntaxError();
            // Allocate a dummy event handler so that we can continue parsing
            eventSymbol = new EventHandlerSymbol(Scanner.NO_POSITION, currentObjectSymbol, "", "");
        }

        if (isEvent) {
            // Events must not have a body
            acceptAndSkip(TokenKind.TOK_END);
            acceptAndSkip(TokenKind.TOK_EVENT);
            acceptAndSkipEndOfStatement();

        } else {
            parseFunctionBody(eventSymbol, TokenKind.TOK_EVENT);
        }
    }

    /*
     * Parses a function declaration (for either a static (object) or instance function).
     */
    private void parseFunctionDeclaration(boolean isStatic) {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_FUNCTION);

        FunctionSymbol function = null;
        try {
            // Parse name part of function declaration
            scanner.nextToken();  // Skip 'Function'
            accept(TokenKind.TOK_IDENTIFIER);
            long symStartPosition = scanner.getTokenStartPosition();
            String functionName = scanner.getTokenValueIdentifier();

            // Declare function symbol
            function = isStatic ?
                    new ObjectFunctionSymbol(symStartPosition, currentObjectSymbol, functionName) :
                    new InstanceFunctionSymbol(symStartPosition, currentObjectSymbol, functionName);
            function.setIsCompiled();

            if (!currentObjectSymbol.addFunction(function)) {
                compiler.error(scanner.getTokenStartPosition(), Error.errSymbolRedefinition, functionName);
            }

            scanner.nextToken();  // Skip <identifier>

            // Parse formal arguments
            acceptAndSkip(TokenKind.TOK_OPENPARENTHESIS);
            if (scanner.getToken() == TokenKind.TOK_CLOSEPARENTHESIS) {
                // Empty formal argument list
                scanner.nextToken(); // Skip ')'
            } else {
                parseFormalArgumentList(function);
            }

            acceptAndSkip(TokenKind.TOK_AS);
            function.setResultType(parseType());

            acceptAndSkipEndOfStatement();

        } catch (SyntaxError se) {
            resyncAfterSyntaxError();
            // Allocate a dummy function symbol so that we can continue parsing
            if (function == null) {
                function = isStatic ?
                        new ObjectFunctionSymbol(Scanner.NO_POSITION, currentObjectSymbol, "") :
                        new InstanceFunctionSymbol(Scanner.NO_POSITION, currentObjectSymbol, "");
                function.setIsCompiled();
                currentObjectSymbol.addFunction(function);
            }
            function.setResultType(ErrorType.errorType);
        }

        // Parse function body (non-interface source files only)
        if (currentObjectSymbol.isInterface()) {
            function.setIsAbstract();
        } else {
            parseFunctionBody(function, TokenKind.TOK_FUNCTION);
        }
    }

    /*
     * Parses a property declaration.
     */
    private void parsePropertyDeclaration() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_PROPERTY);

        String propertyName;
        Type propertyType;
        long symStartPosition;
        try {
            // Parse name and type of property
            scanner.nextToken();  // Skip 'Property'
            accept(TokenKind.TOK_IDENTIFIER);
            symStartPosition = scanner.getTokenStartPosition();
            propertyName = scanner.getTokenValueIdentifier();
            scanner.nextToken();  // Skip <identifier>

            acceptAndSkip(TokenKind.TOK_AS);

            propertyType = parseType();

            acceptAndSkipEndOfStatement();
        } catch (SyntaxError se) {
            resyncAfterSyntaxError();
            propertyName = "";
            propertyType = VariantType.variantType;
            symStartPosition = Scanner.NO_POSITION;
        }

        FunctionSymbol getter = null;
        FunctionSymbol setter = null;

        // Parse getter and setter definitions
        if (scanner.getToken() == TokenKind.TOK_GET) {
            getter = new InstanceFunctionSymbol(scanner.getTokenStartPosition(), currentObjectSymbol,
                    propertyName);
            getter.setIsCompiled();
            getter.setResultType(propertyType);
            getter.setIsProperty();

            scanner.nextToken();  // Skip 'Get'
            acceptAndSkipEndOfStatement();

            parseFunctionBody(getter, TokenKind.TOK_GET);
        }

        if (scanner.getToken() == TokenKind.TOK_SET) {
            // Using the property name for its setter argument is not a problem because properties
            // are not supposed to invoked recursively and don't have a result.
            setter = new InstanceFunctionSymbol(scanner.getTokenStartPosition(), currentObjectSymbol,
                    propertyName);
            setter.setIsCompiled();
            setter.addParameter(symStartPosition, propertyName, propertyType, false);
            setter.setIsProperty();

            scanner.nextToken();  // Skip 'Set'
            acceptAndSkipEndOfStatement();

            parseFunctionBody(setter, TokenKind.TOK_SET);
        }

        acceptAndSkip(TokenKind.TOK_END);
        acceptAndSkip(TokenKind.TOK_PROPERTY);

        if (getter == null && setter == null) {
            compiler.error(symStartPosition, Error.errPropertyWithoutGetterOrSetter, propertyName);
        }

        // Define property symbol
        PropertySymbol property = new PropertySymbol(symStartPosition, currentObjectSymbol,
                propertyName, propertyType);
        if (!currentObjectSymbol.addProperty(property, getter, setter)) {
            compiler.error(symStartPosition, Error.errSymbolRedefinition, propertyName);
        }

        acceptAndSkipEndOfStatement();
    }

    /*
     * Parses a procedure declaration (for either a static (object) or instance procedure).
     */
    private void parseProcedureDeclaration(boolean isStatic) {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_SUB);

        FunctionSymbol procedure;
        try {
            // Parse name part of procedure declaration
            scanner.nextToken();  // Skip 'Sub'
            accept(TokenKind.TOK_IDENTIFIER);
            long symStartPosition = scanner.getTokenStartPosition();
            String procedureName = scanner.getTokenValueIdentifier();
            scanner.nextToken();  // Skip <identifier>

            // Declare procedure symbol
            procedure = isStatic ?
                    new ObjectFunctionSymbol(symStartPosition, currentObjectSymbol, procedureName) :
                    new InstanceFunctionSymbol(symStartPosition, currentObjectSymbol, procedureName);
            procedure.setIsCompiled();

            if (!currentObjectSymbol.addFunction(procedure)) {
                compiler.error(symStartPosition, Error.errSymbolRedefinition, procedureName);
            }

            // Parse formal arguments
            acceptAndSkip(TokenKind.TOK_OPENPARENTHESIS);
            if (scanner.getToken() == TokenKind.TOK_CLOSEPARENTHESIS) {
                // Empty formal argument list
                scanner.nextToken(); // Skip ')'
            } else {
                parseFormalArgumentList(procedure);
            }
            acceptAndSkipEndOfStatement();
        } catch (SyntaxError se) {
            resyncAfterSyntaxError();
            // Allocate a procedure symbol so that we can continue parsing
            procedure = isStatic ?
                    new ObjectFunctionSymbol(Scanner.NO_POSITION, currentObjectSymbol, "") :
                    new InstanceFunctionSymbol(Scanner.NO_POSITION, currentObjectSymbol, "");
            procedure.setIsCompiled();
        }

        // Parse function body (non-interface source files only)
        if (currentObjectSymbol.isInterface()) {
            procedure.setIsAbstract();
        } else {
            parseFunctionBody(procedure, TokenKind.TOK_SUB);
        }
    }

    /*
     * Parses a declaration starting with 'Static'.
     */
    private void parseStaticDeclaration() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_STATIC);

        TokenKind tokenKind = scanner.nextToken();
        switch (tokenKind) {
            default:
                compiler.error(scanner.getTokenStartPosition(), Error.errUnexpected, tokenKind.toString());
                throw new SyntaxError();

            case TOK_DIM:
                parseFieldDeclaration(true);
                break;

            case TOK_FUNCTION:
                parseFunctionDeclaration(true);
                break;

            case TOK_SUB:
                parseProcedureDeclaration(true);
                break;
        }
    }

    /*
     * Parses the body of a $Define-statement which contains properties and nested $Define-
     * statements.
     * 解析一个包含属性和嵌套$Define 的 $Define语句s
     */
    private void parse$DefineStatementBody(Expression componentExpr) {
        Statement stmt;
        for (; ; ) {
            TokenKind tokenKind = scanner.getToken();
            switch (tokenKind) {
                default:
                    compiler.error(fileOnlySourcePosition, Error.errCorruptedSourceFileProperties);
                    throw new FatalError();

                case TOK_$DEFINE:
                    // $Define statements can be nested for nested components.
                    //$Define语句可以嵌套在嵌套组件中
                    parse$DefineStatement(componentExpr);
                    continue;

                case TOK_IDENTIFIER:
                    Expression propertyExpr = new QualifiedIdentifierExpression(
                            scanner.getTokenStartPosition(), componentExpr, scanner.getTokenValueIdentifier());
                    // If followed by a '.' it is a compound property
                    while (scanner.nextToken() == TokenKind.TOK_DOT) {    // Skip <identifier>
                        scanner.nextToken(); // Skip '.'
                        accept(TokenKind.TOK_IDENTIFIER);
                        propertyExpr = new QualifiedIdentifierExpression(scanner.getTokenStartPosition(),
                                propertyExpr, scanner.getTokenValueIdentifier());
                    }

                    long exprStartPosition = scanner.getTokenStartPosition();
                    acceptAndSkip(TokenKind.TOK_EQUAL);
                    stmt = new ExpressionStatement(fileOnlySourcePosition,
                            new AssignmentExpression(exprStartPosition, propertyExpr, parseExpression()));
                    break;

                case TOK_EOS:
                    scanner.nextToken(); // Skip <endofstatement>
                    continue;

                case TOK_$END:
                    scanner.nextToken(); // Skip '$End'
                    acceptAndSkip(TokenKind.TOK_$DEFINE);
                    acceptAndSkipEndOfStatement();
                    return;
            }

            currentStatementList.add(stmt);
        }
    }

    /*
     * Parses a $Define-statement in the properties section of a Simple form source file. The
     * topLevel parameter is true if the component is part of the form container or false if it
     * is nested within another component container.
     *
     * 解析Define-statement美元的一个简单的表单属性部分的源文件。的　　*最高级的参数是真的如果组件是表单容器或假的如果它的一部分　　*是嵌套在另一个组件容器。
     */
    private void parse$DefineStatement(Expression container) {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_$DEFINE);

        scanner.nextToken();
        accept(TokenKind.TOK_IDENTIFIER);
        String componentName = scanner.getTokenValueIdentifier();//组件名字

        scanner.nextToken();  // Skip component name 跳过组件名字
        acceptAndSkip(TokenKind.TOK_$AS); //跳过$AS

        accept(TokenKind.TOK_IDENTIFIER);//类型标识符
        ObjectType componentType = parseObjectType();//提取标识符类型

        // Create field for component
        //创建字段组件
        Expression componentExpr = new SimpleIdentifierExpression(fileOnlySourcePosition, currentScope,
                componentName);

        Expression newExpr;
        DataMemberSymbol component;
        if (container == null) {
            component = new ObjectDataMemberSymbol(fileOnlySourcePosition, currentObjectSymbol,
                    componentName, componentType);
            newExpr = new MeExpression(fileOnlySourcePosition, currentObjectSymbol);
        } else {
            component = new InstanceDataMemberSymbol(fileOnlySourcePosition, currentObjectSymbol,
                    componentName, componentType);
            newExpr = new NewComponentExpression(container, componentType);
        }

        components.add(component);
        if (!currentObjectSymbol.addDataMember(component)) {
            compiler.error(fileOnlySourcePosition, Error.errCorruptedSourceFileProperties);
            throw new FatalError();
        }

        Expression expr = new AssignmentExpression(fileOnlySourcePosition, componentExpr, newExpr);
        Statement stmt = new ExpressionStatement(fileOnlySourcePosition, expr);
        currentStatementList.add(stmt);

        acceptAndSkipEndOfStatement();

        parse$DefineStatementBody(componentExpr);
    }

    /*
     * Parses a $Define-statement for defining a component instance in a Simple form source file.
     * 解析一个$Define-语句 定义一个组件实例在一个简单的源文件形式。
     */
    private void parseTopLevel$DefineStatement(FunctionSymbol define) {
        currentStatementList = define.getFunctionStatements();
        currentFunction = define;

        Scope outerScope = currentScope;
        currentScope = currentStatementList.getScope();

        try {
            parse$DefineStatement(null);

            // Register event handlers
            //注册事件处理程序
            currentStatementList.add(new RegisterEventHandlersStatement(currentObjectSymbol));

            // Invoke initializer events for any components
            //为任何组件调用初始化事件
            for (DataMemberSymbol component : components) {
                currentStatementList.add(new RaiseInitializeEventStatement(component));
            }
        } finally {
            currentScope = outerScope;
            currentStatementList = null;
            currentFunction = null;
        }
    }

    /*
     * Parses the properties section of a Simple form source file.
     * 解析一个Simple源文件的 Form属性段
     */
    private void parseFormPropertiesSection() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_$FORM);

        scanner.nextToken();  // Skip '$Form'
        acceptAndSkipEndOfStatement();

        // Now we know the correct superclass of the current class
        //现在我们知道正确的当前类的超类
        currentObjectSymbol.setBaseObject((ObjectType) ObjectSymbol.getObjectSymbol(compiler,
                Compiler.RUNTIME_ROOT_INTERNAL + "/components/Form").getType());

        // Always need to create a $define() method which will instantiate any components on the form
        //总是需要创建一个$define()方法将实例化表单上的任何组件
        InstanceFunctionSymbol function = new InstanceFunctionSymbol(Scanner.NO_POSITION,
                currentObjectSymbol, "$define");
        function.setIsCompiled();
        function.setIsGenerated();
        currentObjectSymbol.addFunction(function);
        parseTopLevel$DefineStatement(function);
    }

    /*
     * Parses the properties section of a Simple object source file.
     */
    private void parseObjectPropertiesSection() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_$OBJECT);

        scanner.nextToken();  // Skip '$Object'
        acceptAndSkipEndOfStatement();

        // Parse properties, which be BaseObject and ImplementsInterface
        for (; ; ) {
            if (scanner.getToken() != TokenKind.TOK_IDENTIFIER) {
                // Not an identifier. Let's assume we are done
                break;
            }

            String identifier = scanner.getTokenValueIdentifier();
            scanner.nextToken();  // Skip <identifier>

            if (identifier.equals("BaseObject")) {
                acceptAndSkip(TokenKind.TOK_EQUAL);
                // The base object must be a fully qualified name and will be looked up in the global
                // namespace scope (using the current class scope leads to all kinds of problems)
                Scope savedCurrentScope = currentScope;
                currentScope = compiler.getGlobalNamespaceSymbol().getScope();
                try {
                    currentObjectSymbol.setBaseObject(parseObjectType());
                } finally {
                    currentScope = savedCurrentScope;
                }
            } else if (identifier.equals("ImplementsInterface")) {
                acceptAndSkip(TokenKind.TOK_EQUAL);
                for (; ; ) {
                    currentObjectSymbol.addInterface(parseObjectType());
                    if (scanner.getToken() != TokenKind.TOK_COMMA) {
                        break;
                    }
                    scanner.nextToken();  // Skip ','
                }
            } else {
                // This is going to be an error. Let's just return and let the caller report an error.
                break;
            }

            acceptAndSkipEndOfStatement();
        }
    }

    /*
     * Parses the properties section of a Simple interface source file.
     */
    private void parseInterfacePropertiesSection() {
        Preconditions.checkState(scanner.getToken() == TokenKind.TOK_$INTERFACE);

        scanner.nextToken();  // Skip '$Interface'
        acceptAndSkipEndOfStatement();

        currentObjectSymbol.markAsInterface();

        // No properties
    }

    /**
     * Parses a source file.
     * 解析源文件
     */
    public void parse() {
        // Every Simple source file contains with a properties section. This is normally not visible to
        // the user in the source file (this obviously requires support from the editor - if you use vi,
        // emacs or any other low-level editor, you will be able to see this section as well).
        try {
            parsingPropertiesSection = true;//标志正解析属性段
            scanner.scanPropertiesSection();//扫描属性段
            skipEndOfStatements();//跳过前面一堆结束语句(无用代码)

            // Design-time section is enclosed by $Properties ... $End $Properties
            //设计时段 通过 $Properties ... $End $Properties 包围
            try {
                acceptAndSkip(TokenKind.TOK_$PROPERTIES);
                acceptAndSkipEndOfStatement();

                // First statement in the properties section must be the $Source statement which determines
                // the rest of the content in the section
                //属性第一个必须是$Source
                acceptAndSkip(TokenKind.TOK_$SOURCE);
                switch (scanner.getToken()) {
                    default:
                        compiler.error(fileOnlySourcePosition, Error.errCorruptedSourceFileProperties);
                        throw new FatalError();

                    case TOK_$FORM:
                        //窗体
                        parseFormPropertiesSection();
                        break;

                    case TOK_$OBJECT:
                        //对象
                        parseObjectPropertiesSection();
                        break;

                    case TOK_$INTERFACE:
                        //接口
                        parseInterfacePropertiesSection();
                        break;
                }

                // End of properties section
                acceptAndSkip(TokenKind.TOK_$END);
                acceptAndSkip(TokenKind.TOK_$PROPERTIES);
                skipEndOfStatements();
                accept(TokenKind.TOK_EOF);
            } catch (SyntaxError se) {
                compiler.error(fileOnlySourcePosition, Error.errCorruptedSourceFileProperties);
                throw new FatalError();
            }

            // Now start parsing the language section of the Simple source file.
            parsingPropertiesSection = false;
            scanner.scanSourceSection();
            skipEndOfStatements();

            // Interfaces only allow a subset of top-level keywords
            if (currentObjectSymbol.isInterface()) {
                // Parse an interface source file
                for (; ; ) {
                    try {
                        TokenKind tokenKind = scanner.getToken();
                        switch (tokenKind) {
                            default:
                                compiler.error(scanner.getTokenStartPosition(), Error.errUnexpected,
                                        tokenKind.toString());
                                throw new SyntaxError();

                            case TOK_ALIAS:
                                parseAliasDeclaration();
                                break;

                            case TOK_CONST:
                                parseConstantDeclaration();
                                break;

                            case TOK_EOF:
                                return;

                            case TOK_FUNCTION:
                                parseFunctionDeclaration(false);
                                acceptAndSkip(TokenKind.TOK_END);
                                acceptAndSkip(TokenKind.TOK_FUNCTION);
                                acceptAndSkipEndOfStatement();
                                break;

                            case TOK_SUB:
                                parseProcedureDeclaration(false);
                                acceptAndSkip(TokenKind.TOK_END);
                                acceptAndSkip(TokenKind.TOK_SUB);
                                acceptAndSkipEndOfStatement();
                                break;
                        }
                    } catch (SyntaxError se) {
                        resyncAfterSyntaxError();
                    }
                }
            } else {
                // Parse a regular object or form source file
                for (; ; ) {
                    try {
                        TokenKind tokenKind = scanner.getToken();
                        switch (tokenKind) {
                            default:
                                compiler.error(scanner.getTokenStartPosition(), Error.errUnexpected,
                                        tokenKind.toString());
                                throw new SyntaxError();

                            case TOK_ALIAS:
                                parseAliasDeclaration();
                                break;

                            case TOK_CONST:
                                parseConstantDeclaration();
                                break;

                            case TOK_DIM:
                                parseFieldDeclaration(false);
                                break;

                            case TOK_EOF:
                                return;

                            case TOK_EVENT:
                                parseEventHandlerDeclaration();
                                break;

                            case TOK_FUNCTION:
                                parseFunctionDeclaration(false);
                                break;

                            case TOK_PROPERTY:
                                parsePropertyDeclaration();
                                break;

                            case TOK_STATIC:
                                parseStaticDeclaration();
                                break;

                            case TOK_SUB:
                                parseProcedureDeclaration(false);
                                break;
                        }
                    } catch (SyntaxError se) {
                        resyncAfterSyntaxError();
                    }
                }
            }
        } catch (FatalError e) {
            // Error was already reported. Just abort parsing.
        }
    }
}
