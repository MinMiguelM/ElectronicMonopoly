package com.mycompany.thread;

import android.os.AsyncTask;

import com.mycompany.activities.MainActivity;

import java.net.Socket;

/**
 * Created by ASUS on 12/12/2016.
 */
public class UniqueThread extends AsyncTask<Void,Void,Void> {

    private Socket cliente;
    private MainActivity activity;

    public UniqueThread(Socket cliente, MainActivity activity){
        this.cliente = cliente;
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }
}
