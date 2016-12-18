package com.mycompany.data;

import java.io.Serializable;

/**
 * Created by ASUS on 12/12/2016.
 */
public class ObjectRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 1. Pagar / Comprar
     * 2. Peaje / premio / pago
     * 3. Game over
     */
    private int operation;

    /**
     * id from the map
     * Bank is id = -1
     * Everybody = -2
     */
    private int toPlayer;
    private int value;

    public ObjectRequest(){
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public int getToPlayer() {
        return toPlayer;
    }

    public void setToPlayer(int toPlayer) {
        this.toPlayer = toPlayer;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
