package com.mycompany.activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.mycompany.data.ObjectRequest;
import com.mycompany.data.Player;
import com.mycompany.data.Ticket;
import com.mycompany.electronicmonopoly.R;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PayActivity extends AppCompatActivity {

    private String host;
    private int port = 334;
    private int value;
    private Player player;
    private int playerTo;
    private Ticket ticket;

    private Button payButton;
    private TextView valueView;
    private Spinner spinnerPlayers;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        host = getIntent().getExtras().getString("host");
        value = getIntent().getExtras().getInt("value");
        player = (Player) getIntent().getExtras().getSerializable("player");
        ticket = (Ticket) getIntent().getExtras().getSerializable("ticket");

        valueView = (TextView) findViewById(R.id.value);
        valueView.setText(value+"");

        spinnerPlayers = (Spinner) findViewById(R.id.playersSpinner);
        setupSpinner();

        payButton = (Button) findViewById(R.id.payButton);
        actionEventPayButton();
    }

    public int idPlayer(String name){
        if(name.equals(getString(R.string.bank)))
            return -1;
        if(name.equals(getString(R.string.everybody)))
            return -2;
        Set<Integer> set = ticket.getPlayers().keySet();
        for (Integer i : set ) {
            if(ticket.getPlayers().get(i).getName().equals(name))
                return i;
        }
        return -3;
    }

    public void setupSpinner(){
        spinnerPlayers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String namePlayer = parent.getItemAtPosition(position).toString();
                playerTo = idPlayer(namePlayer);
                if(playerTo == -2)
                    value = value * (ticket.getPlayers().size() - 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        List<String> players = new ArrayList<>();
        Set<Integer> set = ticket.getPlayers().keySet();
        players.add(getString(R.string.bank));
        if(set.size() > 2)
            players.add(getString(R.string.everybody));
        for(Integer i : set){
            if(!player.getName().equals(ticket.getPlayers().get(i).getName()))
                players.add(ticket.getPlayers().get(i).getName());
        }
        ArrayAdapter<String> propertiesAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,players);
        propertiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlayers.setAdapter(propertiesAdapter);
    }

    public void actionEventPayButton(){
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playerTo == -2 && value > player.getMoney())
                    showMessage(getString(R.string.error),getString(R.string.error_not_funds));
                else {
                    showProgress(getString(R.string.payingMessage), true);
                    SendPayTask st = new SendPayTask();
                    ObjectRequest obj = new ObjectRequest();
                    obj.setOperation(1);
                    obj.setValue(value+"");
                    obj.setFromPlayer( idPlayer(player.getName()) );
                    obj.setToPlayer(playerTo);
                    st.execute(obj);
                }
            }
        });
    }

    /**
     * show a message in the actual context
     */
    public void showMessage( String nameError, String detail) {
        AlertDialog alert = new AlertDialog.Builder(PayActivity.this).create();
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

    private class SendPayTask extends AsyncTask<ObjectRequest,Void,Boolean>{

        @Override
        protected Boolean doInBackground(ObjectRequest... params) {
            try{
                Socket socket = new Socket(host,port);
                ObjectOutputStream bufferOut = new ObjectOutputStream(socket.getOutputStream());
                bufferOut.writeObject(params[0]);
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        public void onPostExecute(final Boolean success){
            showProgress(null,false);
            if(success){
                player.setMoney(player.getMoney() - value);
                Intent intent = new Intent(PayActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("player", player);
                intent.putExtra("ticket",ticket);
                startActivity(intent);
            }else
                showMessage(getString(R.string.error),getString(R.string.error_sending));
        }
    }

}
