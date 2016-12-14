package com.mycompany.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ASUS on 12/12/2016.
 */
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<Integer,Player> players;
    private List<Property> properties;

    public Ticket(){
        players = new HashMap<>();
        properties = new ArrayList<>();
    }

    public Map<Integer, Player> getPlayers() {
        return players;
    }

    public void setPlayers(Map<Integer, Player> players) {
        this.players = players;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }
}
