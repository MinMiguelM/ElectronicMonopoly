package com.mycompany.data;

import java.io.Serializable;

/**
 * Created by ASUS on 12/12/2016.
 */
public class Property implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Integer value;

    public Property(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
