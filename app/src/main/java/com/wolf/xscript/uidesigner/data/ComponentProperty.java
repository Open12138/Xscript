package com.wolf.xscript.uidesigner.data;

import java.math.BigDecimal;

/**
 * Created by GreyWolf on 2016/5/25.
 */
public class ComponentProperty {
    private String propertyName = "";
    private PropertyType propertyType;

    private BigDecimal intValue;
    private boolean booleanValue;
    private String stringValue;


    public ComponentProperty(String name, PropertyType type, Object obj) {
        this.propertyName = name;
        this.propertyType = type;
        switch (type) {
            case BOOLEANCONSTANT:
                booleanValue = (boolean) obj;
                break;
            case NULLCONSTANT:
                break;
            case NUMERICCONSTANT:
                intValue = (BigDecimal) obj;
                break;
            case STRINGCONSTANT:
                stringValue = (String) obj;
                break;
        }
    }

    public String getValue2String() {
        switch (propertyType) {
            case BOOLEANCONSTANT:
                return booleanValue ? "True" : "False";
            case NULLCONSTANT:
                return null;
            case NUMERICCONSTANT:
                return intValue + "";
            case STRINGCONSTANT:
                return "\"" + stringValue + "\"";
        }
        return "";
    }

    public String getPropertyName() {
        return propertyName;
    }

    public PropertyType getType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType t) {
        this.propertyType = t;
    }

    public void setValues(Object obj) {
        switch (propertyType) {
            case BOOLEANCONSTANT:
                booleanValue = (boolean) obj;
                break;
            case NULLCONSTANT:
                break;
            case NUMERICCONSTANT:
                intValue = (BigDecimal) obj;
                break;
            case STRINGCONSTANT:
                stringValue = (String) obj;
                break;
        }
    }

    public Object getValues() {
        switch (propertyType) {
            case BOOLEANCONSTANT:
                return booleanValue;
            case NULLCONSTANT:
                return null;
            case NUMERICCONSTANT:
                return intValue;
            case STRINGCONSTANT:
                return stringValue;
        }
        return null;
    }

    public enum PropertyType {
        BOOLEANCONSTANT,//boolean值
        NULLCONSTANT,//null
        NUMERICCONSTANT,//数值常量
        STRINGCONSTANT//字符串
    }
}

