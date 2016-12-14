package com.mycompany.data;

import java.io.Serializable;

/**
 * Created by ASUS on 12/12/2016.
 */
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String ip;
    private Integer money;

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
