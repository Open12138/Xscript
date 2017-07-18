package com.wolf.xscript.uidesigner.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GreyWolf on 2016/5/25.
 */
public class ComponentData {
    private String name = "";//组件名字
    private String type = "";//组件类型 全限定名字

    private List<ComponentProperty> property;//属性
    private List<ComponentData> subCompent;//子组件

    public ComponentData(String n, String t) {
        this.name = n;
        this.type = t;
        this.property = new ArrayList<ComponentProperty>();
        this.subCompent = new ArrayList<ComponentData>();
    }

    /**
     * 添加子组件
     *
     * @param cd
     */
    public void addSubComponent(ComponentData cd) {
        this.subCompent.add(cd);
    }

    /**
     * 删除组件
     *
     * @param cd
     */
    public void deleteSubComponent(ComponentData cd) {
        for (int i = 0; i < this.getSubCompentList().size(); i++) {
            ComponentData sub = this.getSubCompentList().get(i);
            if (sub.getName().equals(cd.getName()) && sub.getType().equals(cd.getType())) {
                this.getSubCompentList().remove(i);
                return;
            }
        }
    }

    /**
     * 根据组件名字删除组件
     *
     * @param name
     */
    public void deleteSubComponent(String name) {
        for (int i = 0; i < this.getSubCompentList().size(); i++) {
            ComponentData sub = this.getSubCompentList().get(i);
            if (sub.getName().equals(name)) {
                this.getSubCompentList().remove(i);
                return;
            }
        }
    }

    /**
     * 更新组件属性
     */
    public void upDateComponentProperties(ComponentData cd, ComponentProperty componentProperty) {
        for (int i = 0; i < this.getSubCompentList().size(); i++) {
            ComponentData sub = this.getSubCompentList().get(i);
            if (sub.getName().equals(cd.getName()) && sub.getType().equals(cd.getType())) {
                for (int k = 0; k < sub.getPropertyList().size(); k++) {
                    ComponentProperty subproperty = sub.getPropertyList().get(i);
                    if (subproperty.getPropertyName().equals(componentProperty.getPropertyName())) {

                    }
                }
            }
        }
    }

    /**
     * 获取属性
     *
     * @return
     */
    public List<ComponentProperty> getPropertyList() {
        return this.property;
    }

    /**
     * 获取所有子组件
     *
     * @return
     */
    public List<ComponentData> getSubCompentList() {
        return this.subCompent;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("$Properties\r\n")
                .append("$Source $Form\r\n")
                .append(getString(this))
                .append("$End $Properties");
        return sb.toString();
    }

    private String getString(ComponentData data) {
        StringBuffer sb = new StringBuffer();
        sb.append("$Define " + data.getName() + " $As " + data.getType() + "\r\n");
        for (ComponentProperty property : data.getPropertyList()) {
            sb.append(property.getPropertyName() + " = " + property.getValue2String() + "\r\n");
        }
        if (data.getSubCompentList().size() > 0) {
            for (ComponentData cd : data.getSubCompentList())
                sb.append(getString(cd));
        }
        sb.append("$End $Define\r\n");
        return sb.toString();
    }
}