package com.example.asb.smartwallet;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
public class TransactionAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] type;
    private final String[] amount;
    private final String[] tmstp;
    private final Integer[] ids;
    // private final Integer[] transaction_id;

    public TransactionAdapter(Activity context, String[] type,String[] amount, String[] tmstp, Integer[] ids) {
        super(context, R.layout.transaction_list, type);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.type=type;
        this.amount=amount;
        this.tmstp=tmstp;
        this.ids=ids;
      //  this.transaction_id=transaction_id;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.transaction_list, null,true);
        TextView type_text = (TextView) rowView.findViewById(R.id.t_type);
        TextView amt_text = (TextView) rowView.findViewById(R.id.t_amt);
        TextView tmstp_text = (TextView) rowView.findViewById(R.id.t_when);
        type_text.setText(type[position]);
        amt_text.setText(amount[position]);
        tmstp_text.setText(tmstp[position]);
        rowView.setId(ids[position]);
        return rowView;

    };
}