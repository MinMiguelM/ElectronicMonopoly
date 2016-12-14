package com.mycompany.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
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

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Ticket ticket;
    private String host;
    private Player player;
    private Handler myHandler;

    private TextView currentMoney;
    private Button buyButton;
    private Button repaymentButton;
    private Button payButton;
    private EditText inputText;

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
                        startActivity(intent);
                    }
                }
                inputText.setText("");
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent){
        player = (Player) intent.getExtras().getSerializable("player");
        currentMoney.setText(player.getMoney()+"");
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
        currentMoney.setText(text);
        player.setMoney(Integer.parseInt(text));
    }

    public Integer getCurrentMoney(){
        return Integer.parseInt(player.getMoney().toString());
    }

    public void actionEventBuyButton(){
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View focus = null;
                if( TextUtils.isEmpty(inputText.getText().toString())) {
                    Intent intent = new Intent(MainActivity.this, ShowerActivity.class);
                    intent.putExtra("ticket", ticket);
                    intent.putExtra("player", player);
                    intent.putExtra("option", 0);
                    startActivity(intent);
                }else{
                    int value = Integer.parseInt(inputText.getText().toString());
                    if(value > getCurrentMoney())
                        showMessage(getString(R.string.error),getString(R.string.error_not_funds));
                    else
                        setCurrentMoney((getCurrentMoney()-value)+"");
                    focus = currentMoney;
                    focus.requestFocus();
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
                    intent.putExtra("option", 1);
                    startActivity(intent);
                }else{
                    int value = Integer.parseInt(inputText.getText().toString());
                    value -= (value*0.1);
                    setCurrentMoney((getCurrentMoney()+value)+"");
                    focus = currentMoney;
                    focus.requestFocus();
                }
                inputText.setText("");
            }
        });
    }
}
