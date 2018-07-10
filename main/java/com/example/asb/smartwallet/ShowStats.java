package com.example.asb.smartwallet;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ShowStats extends AppCompatActivity {
    public static WalletDBHelper plotFromDB;
    Transaction transaction;
    ArrayList<Transaction> arrayList;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Timestamp timestamp;
    Date parsedDate;
    String[] times;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_stats);
        times = new String[1000];
        plotGraphs();
    }
    public void plotGraphs(){
        int currentAmt = 0;
        GraphView transactionHistoryPlot = findViewById(R.id.cashFlow);
        int j;
        j=0;
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{});
        plotFromDB = new WalletDBHelper(this);
        arrayList = plotFromDB.getCompleteTransactions();
        Integer[] y_values = new Integer[arrayList.size()];
        String[] x_values = new String[arrayList.size()];
        for(int i=0; i < arrayList.size(); i++){
            transaction = arrayList.get(i);
            if(transaction.type == 0){
                currentAmt+= transaction.amount;
            } else {
               currentAmt-= transaction.amount;
            }
            y_values[j] = currentAmt;
            x_values[j] = (transaction.timestamp).substring(11,16);
            series.appendData(new DataPoint(j,y_values[j]),true, 5);
            j++;
        }
        transactionHistoryPlot.addSeries(series);

                StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(transactionHistoryPlot);
                staticLabelsFormatter.setHorizontalLabels(x_values);
                transactionHistoryPlot.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
                series.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series, DataPointInterface dataPoint) {
                           // Toast.makeText(getApplicationContext(),"Crap wants me to toast",Toast.LENGTH_SHORT).show();
                    }
                });
        transactionHistoryPlot.setTitle("Transaction history");
        transactionHistoryPlot.getViewport().setMinX(0);
        transactionHistoryPlot.getViewport().setMaxX(10);
        transactionHistoryPlot.getViewport().setMinY(0);
        transactionHistoryPlot.getViewport().setMaxY(2000);
        transactionHistoryPlot.getViewport().setYAxisBoundsManual(true);
        transactionHistoryPlot.getViewport().setXAxisBoundsManual(true);
        transactionHistoryPlot.getViewport().setScalable(true);
        transactionHistoryPlot.getViewport().setScrollable(true);
        transactionHistoryPlot.getViewport().setScalableY(true);
        transactionHistoryPlot.getViewport().setScrollableY(true);
    }

}
