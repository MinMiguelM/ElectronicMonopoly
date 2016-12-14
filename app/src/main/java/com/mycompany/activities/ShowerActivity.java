package com.mycompany.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.mycompany.data.Player;
import com.mycompany.data.Property;
import com.mycompany.data.Ticket;
import com.mycompany.electronicmonopoly.R;

import java.util.ArrayList;
import java.util.List;

public class ShowerActivity extends AppCompatActivity {

    private Spinner spinnerProperties;
    private TextView priceView;
    private Button buyButton;

    private Property property;
    private Ticket ticket;
    private Player player;

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
                property = valueProperty(prop);
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
        for(Property p : ticket.getProperties()){
            properties.add(p.getName());
        }
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
                int value = property.getValue();
                int currentMoney = player.getMoney();
                if(option == 1) {
                    value -= property.getValue()*0.1;
                    player.setMoney(currentMoney + value);
                    Intent intent = new Intent(ShowerActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("player", player);
                    startActivity(intent);
                }else {
                    if (currentMoney >= value) {
                        player.setMoney(currentMoney - value);
                        Intent intent = new Intent(ShowerActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("player", player);
                        startActivity(intent);
                    } else {
                        showMessage(getString(R.string.error), getString(R.string.error_not_funds));
                    }
                }
            }
        });
    }

    public Property valueProperty(String name){
        for(Property p : ticket.getProperties()){
            if(p.getName().equals(name))
                return p;
        }
        return null;
    }

}
