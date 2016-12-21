package com.mycompany.activities;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mycompany.data.ObjectRequest;
import com.mycompany.data.Player;
import com.mycompany.data.Ticket;
import com.mycompany.electronicmonopoly.R;
import com.mycompany.thread.ReceiveIntentService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Ticket ticket;
    private String host;
    private Player player;
    private Handler myHandler;
    private int port = 334;

    private TextView currentMoney;
    private Button buyButton;
    private Button repaymentButton;
    private Button payButton;
    private EditText inputText;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ticket = (Ticket) getIntent().getExtras().getSerializable("ticket");
        host = getIntent().getExtras().getString("host");
        player = (Player) getIntent().getExtras().getSerializable("player");

        currentMoney = (TextView) findViewById(R.id.currentMoney);
        currentMoney.setText(player.getMoney()+"");

        inputText = (EditText) findViewById(R.id.textInputMoney);

        payButton = (Button) findViewById(R.id.payButton);
        actionEventPayButton();

        buyButton = (Button) findViewById(R.id.buyButton);
        actionEventBuyButton();

        repaymentButton = (Button) findViewById(R.id.repaymentButton);
        actionEventRepaymentButton();

        if(!isMyServiceRunning(ReceiveIntentService.class)){
            myHandler = new Handler(){
                public void handleMessage(Message msg){
                    switch(msg.what){
                        case 2:
                            setCurrentMoney((getCurrentMoney()+(int)msg.obj)+"");
                            break;
                        case 3:
                            gameover((ObjectRequest)msg.obj);
                            break;
                        case 6:
                            setTicket((Ticket) ((ObjectRequest)msg.obj).getObject());
                            break;
                        default:
                            break;
                    }
                }
            };
            Intent service = new Intent(this, ReceiveIntentService.class);
            service.putExtra("handler",new Messenger(myHandler));
            startService(service);
        }
    }

    public void gameover(ObjectRequest obj){
        int myId = idPlayer(player.getName());
        if(myId == obj.getToPlayer()){
            Intent intent = new Intent(MainActivity.this,GameoverActivity.class);
            startActivity(intent);
            this.finish();
        }else{
            deletePlayer(obj.getToPlayer());
        }
    }

    public void deletePlayer(int id){
        ticket.getPlayers().remove(id);
    }

    public int idPlayer(String name){
        if(name.equals(getString(R.string.bank)))
            return -1;
        Set<Integer> set = ticket.getPlayers().keySet();
        for (Integer i : set ) {
            if(ticket.getPlayers().get(i).getName().equals(name))
                return i;
        }
        return -2;
    }

    /**
     * show a message in the actual context
     */
    public void showMessage( String nameError, String detail) {
        AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
        alert.setTitle(nameError);
        alert.setMessage(detail);
        alert.show();
    }

    public void actionEventPayButton(){
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View focus = null;
                inputText.setError(null);
                int value;

                if (TextUtils.isEmpty(inputText.getText().toString())) {
                    inputText.setError(getString(R.string.field_required));
                    focus = inputText;
                    focus.requestFocus();
                }else{
                    value = Integer.parseInt(inputText.getText().toString());
                    if(value > getCurrentMoney())
                        showMessage(getString(R.string.error),getString(R.string.error_not_funds));
                    else{
                        Intent intent = new Intent(MainActivity.this,PayActivity.class);
                        intent.putExtra("host",host);
                        intent.putExtra("value",value);
                        intent.putExtra("player",player);
                        intent.putExtra("ticket",ticket);
                        intent.putExtra("handler",new Messenger(myHandler));
                        startActivity(intent);
                    }
                }
                inputText.setText("");
            }
        });
    }

    public void setTicket(Ticket ticket){
        this.ticket = ticket;
    }

    @Override
    public void onNewIntent(Intent intent){
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        //this.finish();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void setCurrentMoney(String text){
        player.setMoney(Integer.parseInt(text));
        currentMoney.setText(text);
    }

    public Integer getCurrentMoney(){
        return Integer.parseInt(player.getMoney().toString());
    }

    public void actionEventBuyButton(){
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( TextUtils.isEmpty(inputText.getText().toString())) {
                    Intent intent = new Intent(MainActivity.this, ShowerActivity.class);
                    intent.putExtra("ticket", ticket);
                    intent.putExtra("player", player);
                    intent.putExtra("option", 0);
                    intent.putExtra("host",host);
                    intent.putExtra("handler",new Messenger(myHandler));
                    startActivity(intent);
                }else{
                    showProgress(getString(R.string.waitingMessage),true);
                    int value = Integer.parseInt(inputText.getText().toString());
                    if(value > getCurrentMoney())
                        showMessage(getString(R.string.error),getString(R.string.error_not_funds));
                    else {
                        setCurrentMoney((getCurrentMoney() - value) + "");
                        ObjectRequest obj = new ObjectRequest();
                        obj.setOperation(4);
                        obj.setObject(null);
                        obj.setFromPlayer(idPlayer(player.getName()));
                        SendTask st = new SendTask(value, 0);
                        st.execute(obj);
                    }
                }
                inputText.setText("");
            }
        });
    }

    public void actionEventRepaymentButton(){
        repaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View focus = null;
                if( TextUtils.isEmpty(inputText.getText().toString())) {
                    Intent intent = new Intent(MainActivity.this, ShowerActivity.class);
                    intent.putExtra("ticket", ticket);
                    intent.putExtra("player", player);
                    intent.putExtra("host",host);
                    intent.putExtra("option", 1);
                    intent.putExtra("handler",new Messenger(myHandler));
                    startActivity(intent);
                }else{
                    showProgress(getString(R.string.waitingMessage),true);
                    int value = Integer.parseInt(inputText.getText().toString());
                    value -= (value*0.1);
                    SendTask st = new SendTask(value,1);
                    ObjectRequest obj = new ObjectRequest();
                    obj.setOperation(5);
                    obj.setObject(null);
                    obj.setFromPlayer(idPlayer(player.getName()));
                    st.execute(obj);
                }
                inputText.setText("");
            }
        });
    }

    /**
     * show the progress of the current task
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(String message, boolean show){
        if(!show)
            progress.dismiss();
        else
            progress = ProgressDialog.show(this, null, message, true);
    }

    private class SendTask extends AsyncTask<ObjectRequest,Void,Boolean> {

        private ObjectRequest responseG;
        private int value;
        private int operation;

        public SendTask(int value,int operation){
            this.value = value;
            this.operation = operation;
        }

        @Override
        protected Boolean doInBackground(ObjectRequest... params) {
            try{
                Socket socket = new Socket(host,port);
                ObjectOutputStream bufferOut = new ObjectOutputStream(socket.getOutputStream());
                params[0].setValue(value+"");
                bufferOut.writeObject(params[0]);
                if(operation == 1) {
                    ObjectInputStream bufferIn = new ObjectInputStream(socket.getInputStream());
                    ObjectRequest response = (ObjectRequest) bufferIn.readObject();
                    responseG = response;
                }
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        public void onPostExecute(final Boolean success){
            showProgress(null,false);
            if(success && operation == 1){
                //showProgress(null,false);
                if((boolean) responseG.getObject())
                    setCurrentMoney((getCurrentMoney()+value)+"");
                else
                    showMessage(getString(R.string.error),getString(R.string.error_transaction));
            }else if(!success) {
                //showProgress(null,false);
                showMessage(getString(R.string.error), getString(R.string.error_sending));
            }
        }
    }
}
