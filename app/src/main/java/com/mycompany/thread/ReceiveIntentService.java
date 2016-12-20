package com.mycompany.thread;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.mycompany.data.ObjectRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ASUS on 12/13/2016.
 */
public class ReceiveIntentService extends IntentService {

    private int port = 1335;
    private int price;
    private Intent intent;
    private ObjectRequest object;

    public ReceiveIntentService(){
        this(ReceiveIntentService.class.getName());
    }

    public ReceiveIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.intent = intent;
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            while(true){
                Socket client = server.accept();
                ObjectInputStream bufferIn = new ObjectInputStream(client.getInputStream());
                ObjectRequest obj = (ObjectRequest) bufferIn.readObject();
                typeMessage(obj,client);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void typeMessage(ObjectRequest obj, Socket client) throws IOException {
        switch (obj.getOperation()){
            case 2:
                price = Integer.parseInt(obj.getValue());
                Handler handler2 = new Handler(Looper.getMainLooper());
                handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        // run this code in the main thread
                        Messenger msg = (Messenger) intent.getExtras().get("handler");
                        Message msg1 = Message.obtain();

                        msg1.what = 2;
                        msg1.obj = price;

                        try {
                            msg.send(msg1);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case 3:
                this.object = obj;
                Handler handler3 = new Handler(Looper.getMainLooper());
                handler3.post(new Runnable() {
                    @Override
                    public void run() {
                        // run this code in the main thread
                        Messenger msg = (Messenger) intent.getExtras().get("handler");
                        Message msg1 = Message.obtain();

                        msg1.what = 3;
                        msg1.obj = object;

                        try {
                            msg.send(msg1);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case 6:
                this.object = obj;
                Handler handler6 = new Handler(Looper.getMainLooper());
                handler6.post(new Runnable() {
                    @Override
                    public void run() {
                        // run this code in the main thread
                        Messenger msg = (Messenger) intent.getExtras().get("handler");
                        Message msg1 = Message.obtain();

                        msg1.what = 6;
                        msg1.obj = object;

                        try {
                            msg.send(msg1);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            default:
                break;
        }
        client.close();
    }
}
