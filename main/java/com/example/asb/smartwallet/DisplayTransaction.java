package com.example.asb.smartwallet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
public class DisplayTransaction extends AppCompatActivity {
    private WalletDBHelper mydb ;
    TextView amount ;
    TextView type;
    TextView timestamp;
    int id_To_Update = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_transaction);
        amount = (TextView) findViewById(R.id.amount);
        type = (TextView) findViewById(R.id.type);
        timestamp = (TextView) findViewById(R.id.time);
        mydb = new WalletDBHelper(this);

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            Integer Value = extras.getInt(WalletDBHelper.TRANSACTIONS_COLUMN_ID);
            Log.i("Wallet","The ID to serach for is "+Value.toString());
            if(Value>0){
               // means this is the view part not the add contact part.
                Cursor rs = mydb.getDataById(Value);
                rs.moveToFirst();
                Log.i("Wallet",rs.getString(rs.getColumnIndex(WalletDBHelper.TRANSACTIONS_COLUMN_ID)));
                id_To_Update = Value;
                rs.moveToFirst();
                Integer amt = rs.getInt(rs.getColumnIndex(WalletDBHelper.TRANSACTIONS_COLUMN_AMOUNT));
                Integer typ = rs.getInt(rs.getColumnIndex(WalletDBHelper.TRANSACTIONS_COLUMN_TYPE));
                String tmstp = rs.getString(rs.getColumnIndex(WalletDBHelper.TRANSACTIONS_COLUMN_TIMESTAMP));

                if (!rs.isClosed())  {
                    rs.close();
                }
                Button b = (Button)findViewById(R.id.button1);
                b.setVisibility(View.INVISIBLE);

                amount.setText(amt.toString());
                amount.setFocusable(false);
                amount.setClickable(false);

                type.setText(typ.toString());
                type.setFocusable(false);
                type.setClickable(false);

                timestamp.setText(tmstp);
                timestamp.setFocusable(false);
                timestamp.setClickable(false);
            }
    }
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Bundle extras = getIntent().getExtras();

        if(extras !=null) {
            int Value = extras.getInt(WalletDBHelper.TRANSACTIONS_COLUMN_ID);
            if(Value>0){
                getMenuInflater().inflate(R.menu.display_transaction, menu);
            } else{
                getMenuInflater().inflate(R.menu.main_menu, menu);
            }
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case R.id.Edit_Transaction:
                Button b = (Button)findViewById(R.id.button1);
                b.setVisibility(View.VISIBLE);
                amount.setEnabled(true);
                amount.setFocusableInTouchMode(true);
                amount.setClickable(true);

                type.setEnabled(true);
                type.setFocusableInTouchMode(true);
                type.setClickable(true);

                timestamp.setEnabled(true);
                timestamp.setFocusableInTouchMode(true);
                timestamp.setClickable(true);
                return true;
            case R.id.Delete_Transaction:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.deleteTransaction)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mydb.deleteTransaction(id_To_Update);
                                Toast.makeText(getApplicationContext(), "Deleted Successfully",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                AlertDialog d = builder.create();
                d.setTitle("Are you sure");
                d.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void run(View view) {
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            int Value = extras.getInt(WalletDBHelper.TRANSACTIONS_COLUMN_ID);
            if(Value>0){
                try{
                    mydb.updateTransaction(id_To_Update,Integer.parseInt(type.getText().toString()), Integer.parseInt(amount.getText().toString()),timestamp.getText().toString());
                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//                    startActivity(intent);
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Not Updated", Toast.LENGTH_SHORT).show();
                }
            } else{
                try{
                    mydb.insertTransaction(Integer.parseInt(type.getText().toString()), Integer.parseInt(amount.getText().toString()),timestamp.getText().toString());
                    Toast.makeText(getApplicationContext(), "done",Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    e.printStackTrace();
                   Toast.makeText(getApplicationContext(), "not done",Toast.LENGTH_SHORT).show();
                }
                //Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                finish();
                //startActivity(intent);
            }
        }
    }
}