package com.mycompany.activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mycompany.data.Player;
import com.mycompany.data.Ticket;
import com.mycompany.electronicmonopoly.R;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Set;

public class RegisterActivity extends AppCompatActivity {

    private EditText hostText;
    private EditText nameText;
    private Button joinButton;
    private ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        hostText = (EditText) findViewById(R.id.hostText);
        nameText = (EditText) findViewById(R.id.namePlayerText);
        joinButton = (Button) findViewById(R.id.joinButton);
        joinButtonOnClick();
    }

    public void joinButtonOnClick(){
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    hostText.setError(null);
                    nameText.setError(null);
                    View focus = null;

                    if (TextUtils.isEmpty(hostText.getText().toString())) {
                        hostText.setError(getString(R.string.field_required));
                        focus = hostText;
                        focus.requestFocus();
                    } else if (TextUtils.isEmpty(nameText.getText().toString())) {
                        nameText.setError(getString(R.string.field_required));
                        focus = nameText;
                        focus.requestFocus();
                    } else {
                        showProgress(getString(R.string.joiningMessage), true);
                        register();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void register() throws Exception{
        Player pl = new Player();
        pl.setName(nameText.getText().toString());
        SendRegister registerTask = new SendRegister(hostText.getText().toString(),
                pl);
        registerTask.execute();
    }

    /**
     * show a message in the actual context
     */
    public void showMessage( String nameError, String detail) {
        AlertDialog alert = new AlertDialog.Builder(RegisterActivity.this).create();
        alert.setTitle(nameError);
        alert.setMessage(detail);
        alert.show();
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

    public class SendRegister extends AsyncTask<Void, Void, Void> {

        private String host;
        private Player player;
        private Ticket ticket;

        public SendRegister(String host, Player pl){
            this.host = host;
            player = pl;
        }

        public Player findPlayer(String name){
            Set<Integer> set = ticket.getPlayers().keySet();
            for(Integer i: set){
                if(ticket.getPlayers().get(i).getName().equals(name))
                    return ticket.getPlayers().get(i);
            }
            return null;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Socket socket = null;
            try {
                WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                int ip = wifiInfo.getIpAddress();
                String ipAddress = Formatter.formatIpAddress(ip);
                player.setIp(ipAddress);
                // that port for registering
                socket = new Socket(host,333);
                ObjectOutputStream buffer = new ObjectOutputStream(socket.getOutputStream());
                buffer.writeObject(player);
                ObjectInputStream buffInput = new ObjectInputStream(socket.getInputStream());
                Ticket ticket = (Ticket) buffInput.readObject();
                socket.close();
                if(ticket != null){
                    Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                    this.ticket = ticket;
                    intent.putExtra("ticket",ticket);
                    intent.putExtra("host",host);
                    intent.putExtra("player",findPlayer(player.getName()));
                    startActivity(intent);
                    showProgress(null,false);
                }else{
                    showProgress(null,false);
                    showMessage(getString(R.string.error),getString(R.string.error_joining));
                }
            }catch(ConnectException ex){
                showProgress(null,false);
                //showMessage(getString(R.string.error),getString(R.string.error_restart)); // restart app
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
