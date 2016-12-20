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
import com.mycompany.data.Property;
import com.mycompany.data.Ticket;
import com.mycompany.electronicmonopoly.R;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShowerActivity extends AppCompatActivity {

    private Spinner spinnerProperties;
    private TextView priceView;
    private Button buyButton;
    private ProgressDialog progress;

    private Property property;
    private Ticket ticket;
    private Player player;
    private String host;
    private int port = 334;

    /**
     * 0 = buy
     * 1 = repayment
     */
    private int option;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shower);

        ticket = (Ticket) getIntent().getExtras().getSerializable("ticket");
        player = (Player) getIntent().getExtras().getSerializable("player");
        option = getIntent().getExtras().getInt("option");
        host = getIntent().getExtras().getString("host");

        priceView = (TextView) findViewById(R.id.price);
        spinnerProperties = (Spinner) findViewById(R.id.propertiesSpinner);
        setupSpinner();
        buyButton = (Button) findViewById(R.id.buyButton);
        if(option == 1)
            buyButton.setText(getString(R.string.repaymentButton));
        actionEventBuyButton();
    }

    public void setupSpinner(){
        spinnerProperties.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String prop = parent.getItemAtPosition(position).toString();
                property = getProperty(prop);
                int value = property.getValue();
                if(option == 1)
                    value -= property.getValue()*0.1;
                if(property != null)
                    priceView.setText(value+"");
                else
                    priceView.setText("-");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        List<String> properties = new ArrayList<>();
        if(option == 0) {
            Set<Integer> set = ticket.getPropertiesAvailable().keySet();
            for(Integer i:set)
                properties.add(ticket.getPropertiesAvailable().get(i).getName());
        }else{
            Set<Integer> set = ticket.getPropertiesSold().keySet();
            for(Integer i:set)
                properties.add(ticket.getPropertiesSold().get(i).getName());
        }
        Collections.sort(properties);
        ArrayAdapter<String> propertiesAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,properties);
        propertiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProperties.setAdapter(propertiesAdapter);
    }

    /**
     * show a message in the actual context
     */
    public void showMessage( String nameError, String detail) {
        AlertDialog alert = new AlertDialog.Builder(ShowerActivity.this).create();
        alert.setTitle(nameError);
        alert.setMessage(detail);
        alert.show();
    }

    public void actionEventBuyButton(){
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(property != null) {
                    int value = property.getValue();
                    int currentMoney = player.getMoney();
                    if (option == 1) {
                        showProgress(getString(R.string.waitingMessage),true);
                        ObjectRequest obj = new ObjectRequest();
                        obj.setOperation(5);
                        obj.setObject(ticket);
                        obj.setValue(property.getName());
                        obj.setFromPlayer(idPlayer(player.getName()));
                        SendTask st = new SendTask();
                        st.execute(obj);
                    } else {
                        if (currentMoney >= value) {
                            showProgress(getString(R.string.waitingMessage),true);
                            player.setMoney(currentMoney - value);
                            deleteProperty(property.getName());
                            ObjectRequest obj = new ObjectRequest();
                            obj.setOperation(4);
                            obj.setObject(ticket);
                            obj.setValue(property.getName());
                            obj.setFromPlayer(idPlayer(player.getName()));
                            SendTask st = new SendTask();
                            st.execute(obj);
                        } else {
                            showMessage(getString(R.string.error), getString(R.string.error_not_funds));
                        }
                    }
                }else
                    showMessage(getString(R.string.error),getString(R.string.error_no_properties));
            }
        });
    }

    public int idPlayer(String name){
        Set<Integer> set = ticket.getPlayers().keySet();
        for (Integer i : set ) {
            if(ticket.getPlayers().get(i).getName().equals(name))
                return i;
        }
        return -1;
    }

    public void deleteProperty(String name){
        Map<Integer,Property> map = new HashMap<>(ticket.getPropertiesAvailable());
        Set<Integer> set = map.keySet();
        for(Integer i : set) {
            if (map.get(i).getName().equals(name)) {
                ticket.getPropertiesSold().put(i, map.get(i));
                ticket.getPropertiesAvailable().remove(i);
            }
        }
    }

    public Property getProperty(String name){
        Set<Integer> set = ticket.getPropertiesAvailable().keySet();
        for(Integer i : set){
            if(ticket.getPropertiesAvailable().get(i).getName().equals(name))
                return ticket.getPropertiesAvailable().get(i);
        }
        Set<Integer> setSold = ticket.getPropertiesSold().keySet();
        for(Integer i : setSold){
            if(ticket.getPropertiesSold().get(i).getName().equals(name))
                return ticket.getPropertiesSold().get(i);
        }
        return null;
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

        public SendTask(){
            value = property.getValue();
        }

        @Override
        protected Boolean doInBackground(ObjectRequest... params) {
            try{
                Socket socket = new Socket(host,port);
                ObjectOutputStream bufferOut = new ObjectOutputStream(socket.getOutputStream());
                bufferOut.writeObject(params[0]);
                if(option == 1){
                    ObjectInputStream bufferIn = new ObjectInputStream(socket.getInputStream());
                    responseG = (ObjectRequest) bufferIn.readObject();
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
            if(success && option == 1){
                if((boolean)responseG.getObject()){
                    value -= value * 0.1;
                    player.setMoney(player.getMoney() + value);
                    Intent intent = new Intent(ShowerActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("player", player);
                    startActivity(intent);
                }else
                    showMessage(getString(R.string.error),getString(R.string.error_transaction));
            }else if(success && option == 0){
                Intent intent = new Intent(ShowerActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("player", player);
                intent.putExtra("ticket",ticket);
                startActivity(intent);
            } else if(!success)
                showMessage(getString(R.string.error),getString(R.string.error_sending));
        }
    }

}
