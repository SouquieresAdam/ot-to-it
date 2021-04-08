package com.cgi.connect.converter;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeElement {

    private TreeElement parent;
    private String id;
    private String type;
    private final Set<TreeElement> children = new HashSet<>();

    public void setParent(TreeElement parent) {
        this.parent = parent;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public static TreeElement of(String id) {
        return new TreeElement(id);
    }


    private TreeElement(String id) {
        this.id = id;
    }

    public TreeElement addChild(TreeElement child) {
        this.children.add(child);
        return this;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public TreeElement getParent() {
        return parent;
    }

    public Set<TreeElement> getChildren() {
        return this.children;
    }
}
