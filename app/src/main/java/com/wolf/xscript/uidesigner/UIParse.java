package com.wolf.xscript.uidesigner;

import android.content.Context;

import com.google.devtools.simple.compiler.scanner.Scanner;
import com.google.devtools.simple.compiler.scanner.TokenKind;
import com.wolf.util.Utils;
import com.wolf.xscript.uidesigner.data.ComponentData;
import com.wolf.xscript.uidesigner.data.ComponentProperty;

import java.math.BigDecimal;

/**
 * Created by GreyWolf on 2016/5/21.
 */
public class UIParse {
    private String source;
    private Scanner scanner;//扫描器


    private ComponentData componentData;//解析得到的窗口组件


    public UIParse(String source) {
        this.source = source;
        this.scanner = new Scanner(source);
        this.parseComponent();
        if (this.componentData == null) {
            MsgBox.err("未解析到组件 null");
        } else printAll();
    }


    //***********************************解析窗口组件**************************************************

    /**
     * @return 解析回字符串
     */
    public String getString() {
        return componentData.toString();
    }

    /**
     * 打印解析结果
     */
    private void printAll() {
        //打印解析结果
        Utils.log("---------------------------Success!---PrintAllComponents-----------------------------------");
        printComponent(componentData);
    }

    /**
     * 解析出所有的组件到 componentData
     */
    private ComponentData parseComponent() {
        try {
            scanner.scanPropertiesSection();//扫描属性段
            skipEndOfStatements();//跳过前面一堆结束语句(无用代码)
            acceptAndSkip(TokenKind.TOK_$PROPERTIES);
            acceptAndSkipEndOfStatement();

            // First statement in the properties section must be the $Source statement which determines
            // the rest of the content in the section
            //属性第一个必须是$Source
            acceptAndSkip(TokenKind.TOK_$SOURCE);
            switch (scanner.getToken()) {
                default:
                    MsgBox.err("1");
                    break;

                case TOK_$FORM:
                    componentData = null;
                    //窗体
                    parseFormPropertiesSection();
                    return componentData;
                case TOK_$OBJECT:
                    //对象
                    break;
                case TOK_$INTERFACE:
                    //接口
                    break;
            }

            // End of properties section
            acceptAndSkip(TokenKind.TOK_$END);
            acceptAndSkip(TokenKind.TOK_$PROPERTIES);
            skipEndOfStatements();
            accept(TokenKind.TOK_EOF);
        } catch (Exception e) {
            MsgBox.err("解析异常E1 " + e.toString());
        }
        return null;
    }

    private void printComponent(ComponentData data) {
        Utils.log("ComponentType--->" + data.getType() + "-------Name--->" + data.getName());
//        Utils.log(data.getName() + "--->PropertiesCount--->" + data.getPropertyList().size());
        for (ComponentProperty property : data.getPropertyList()) {
            Utils.log(data.getName() + " : " + property.getPropertyName() + " = " + property.getValue2String());
        }
        if (data.getSubCompentList().size() > 0) {
            Utils.log(data.getName() + "--------subcount-->" + data.getSubCompentList().size());
            for (ComponentData sub : data.getSubCompentList()) {
                Utils.log("--------------------------------------------------------------");
                printComponent(sub);
            }
        }
    }

    /*************
     * 工具方法
     ******************/

    /*
     * Parses the properties section of a Simple form source file.
     * 解析一个Simple源文件的 Form属性段
     */
    private void parseFormPropertiesSection() {
        if (scanner.getToken() == TokenKind.TOK_$FORM) {
            scanner.nextToken();  // Skip '$Form'
            acceptAndSkipEndOfStatement();
            parse$DefineStatement();
        }
    }


    /*
    * Parses a $Define-statement in the properties section of a Simple form source file. The
    * topLevel parameter is true if the component is part of the form container or false if it
    * is nested within another component container.
    *
    * 解析Define-statement美元的一个简单的表单属性部分的源文件。的　　*最高级的参数是真的如果组件是表单容器或假的如果它的一部分　　*是嵌套在另一个组件容器。
    */
    private void parse$DefineStatement() {
        if (scanner.getToken() != TokenKind.TOK_$DEFINE) {
            Utils.log("未发现 $Define");
        }
        componentData = parse$DefineStatementBody();
    }

    /*
     * Parses the body of a $Define-statement which contains properties and nested $Define-
     * statements.
     * 解析一个包含属性和嵌套$Define 的 $Define语句s
     */
    private ComponentData parse$DefineStatementBody() {
        ComponentData currentParseComponentData = null;
        boolean isfushu = false;//是否是负数
        ComponentProperty componentProperty = null;

        /*****/
        scanner.nextToken();
        accept(TokenKind.TOK_IDENTIFIER);
        String componentName = scanner.getTokenValueIdentifier();//组件名字

        scanner.nextToken();  // Skip component name 跳过组件名字
        acceptAndSkip(TokenKind.TOK_$AS); //跳过$AS

        accept(TokenKind.TOK_IDENTIFIER);//类型标识符
        String componentType = parseObjectType();//标识符类型

        currentParseComponentData = new ComponentData(componentName, componentType);
        acceptAndSkipEndOfStatement();
        /*****/
        String propertyName = null;
        for (; ; ) {
            TokenKind tokenKind = scanner.getToken();
            switch (tokenKind) {
                default:
                    MsgBox.err("2 " + tokenKind);
                    break;

                case TOK_$DEFINE:
                    // $Define statements can be nested for nested components.
                    //$Define语句可以嵌套在嵌套组件中
                    currentParseComponentData.getSubCompentList().add(parse$DefineStatementBody());
                    continue;

                case TOK_IDENTIFIER:
                    StringBuffer sb = new StringBuffer();
                    sb.append(scanner.getTokenValueString());
                    // If followed by a '.' it is a compound property
                    while (scanner.nextToken() == TokenKind.TOK_DOT) {    // Skip <identifier>
                        scanner.nextToken(); // Skip '.'
                        accept(TokenKind.TOK_IDENTIFIER);
                        sb.append(".").append(scanner.getTokenValueString());
                    }

                    //属性全限定名字
                    String quanxianding = sb.toString();
                    propertyName = quanxianding;
                    acceptAndSkip(TokenKind.TOK_EQUAL);//跳过 = 号
                    continue;

                case TOK_EOS:
                    scanner.nextToken(); // Skip <endofstatement>
                    continue;

                case TOK_$END:
                    scanner.nextToken(); // Skip '$End'
                    acceptAndSkip(TokenKind.TOK_$DEFINE);
                    acceptAndSkipEndOfStatement();
                    //添加子组件
                    return currentParseComponentData;
                case TOK_MINUS:
                    isfushu = true;
                    scanner.nextToken();
                    break;


                case TOK_BOOLEANCONSTANT:
                    componentProperty = new ComponentProperty(propertyName, ComponentProperty.PropertyType.BOOLEANCONSTANT, scanner.getTokenValueBoolean());
                    currentParseComponentData.getPropertyList().add(componentProperty);
                    skipEndOfStatements();
                    break;
                case TOK_NULLCONSTANT:
                    componentProperty = new ComponentProperty(propertyName, ComponentProperty.PropertyType.NULLCONSTANT, null);
                    currentParseComponentData.getPropertyList().add(componentProperty);
                    skipEndOfStatements();
                    break;
                case TOK_NUMERICCONSTANT:
                    //负数支持
                    BigDecimal bigd = BigDecimal.ZERO;
                    if (isfushu) {
                        isfushu = false;
                        bigd = BigDecimal.ZERO.subtract(scanner.getTokenValueNumber());
                    } else
                        bigd = scanner.getTokenValueNumber();
                    componentProperty = new ComponentProperty(propertyName, ComponentProperty.PropertyType.NUMERICCONSTANT, bigd);
                    currentParseComponentData.getPropertyList().add(componentProperty);
                    skipEndOfStatements();
                    break;
                case TOK_STRINGCONSTANT:
                    componentProperty = new ComponentProperty(propertyName, ComponentProperty.PropertyType.STRINGCONSTANT, scanner.getTokenValueString());
                    currentParseComponentData.getPropertyList().add(componentProperty);
                    skipEndOfStatements();
                    break;
            }
        }
    }


    /*
     * Parses a possible qualified object type.
     */
    private String parseObjectType() {
        accept(TokenKind.TOK_IDENTIFIER);
        if (scanner.getToken() == TokenKind.TOK_IDENTIFIER) {
            StringBuffer sb = new StringBuffer();
            sb.append(scanner.getTokenValueString());
            if (scanner.nextToken() == TokenKind.TOK_DOT) {
                do {
                    scanner.nextToken(); // Skip '.'
                    accept(TokenKind.TOK_IDENTIFIER);
                    sb.append(".").append(scanner.getTokenValueString());
                } while (scanner.nextToken() == TokenKind.TOK_DOT);
            }
            return sb.toString();
        }
        return "";
    }

    /*
     * 跳过尽可能多的被找到的连续end-of-statements
     */
    private void skipEndOfStatements() {
        while (scanner.nextToken() == TokenKind.TOK_EOS) {
            // Skip any other end of statement tokens 跳过前面一堆结束语句(无用代码)
        }
    }

    /*
    * Accepts the end of the current statement (end-of-line or ':') and skips it or reports an error.
    */
    private void acceptAndSkipEndOfStatement() {
        TokenKind token = scanner.getToken();
        if (token != TokenKind.TOK_EOS && token != TokenKind.TOK_EOF) {
            MsgBox.err("3");
            /*if (!parsingPropertiesSection) {
                if (compiler != null)
                    compiler.error(scanner.getTokenStartPosition(), Error.errExpected, "end of statement",
                            scanner.getToken().toString());
            }*/
//            throw new SyntaxError();
        }
        skipEndOfStatements();
    }

    private TokenKind acceptAndSkip(TokenKind token) {
        accept(token);
        return scanner.nextToken();
    }

    private void accept(TokenKind token) {
        if (scanner.getToken() != token) {
            MsgBox.err("4");
            /*if (!parsingPropertiesSection) {
                compiler.error(scanner.getTokenStartPosition(), Error.errExpected, token.toString(),
                        scanner.getToken().toString());
            }
            throw new SyntaxError();*/
        }
    }
    //***********************************解析窗口组件**************************************************
}
