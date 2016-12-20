package com.mycompany.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ASUS on 12/12/2016.
 */
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<Integer,Player> players;
    private Map<Integer,Property> propertiesAvailable;
    private Map<Integer,Property> propertiesSold;

    public Ticket(){
        players = new HashMap<>();
        propertiesAvailable = new HashMap<>();
        propertiesSold = new HashMap<>();
    }

    public Map<Integer, Player> getPlayers() {
        return players;
    }

    public void setPlayers(Map<Integer, Player> players) {
        this.players = players;
    }

    public Map<Integer, Property> getPropertiesAvailable() {
        return propertiesAvailable;
    }

    public void setPropertiesAvailable(Map<Integer, Property> propertiesAvailable) {
        this.propertiesAvailable = propertiesAvailable;
    }

    public Map<Integer, Property> getPropertiesSold() {
        return propertiesSold;
    }

    public void setPropertiesSold(Map<Integer, Property> propertiesSold) {
        this.propertiesSold = propertiesSold;
    }
}
