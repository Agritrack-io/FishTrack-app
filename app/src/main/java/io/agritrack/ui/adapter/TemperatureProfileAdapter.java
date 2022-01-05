package io.agritrack.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import io.agritrack.R;

public class TemperatureProfileAdapter extends RecyclerView.Adapter<TemperatureProfileAdapter.ViewHolder>{
    private LayoutInflater mLayoutInflater = null;
    public  Context context;
    private ArrayList<String> listOfEPCs = new ArrayList<>();
    private static LineDataSet set1;



//    public TemperatureProfileAdapter(Symbol[] marketSymbol, Context ctx) {
//        this.marketSymbol = marketSymbol;
//        this.context = ctx;
//
//        notifyDataSetChanged();
//    }
//
//    public TemperatureProfileAdapter(Context ctx, ArrayList<MarketFeedBean> arrList) {
//        this.context = ctx;
//        this.arrList = arrList;
//    }
//
//    public TemperatureProfileAdapter(Context ctx, Map<String, MarketFeedBean> watchList) {
//        this.watchList = watchList;
//        this.context = ctx;
//
//        keys.addAll(watchList.keySet());
//    }

    public TemperatureProfileAdapter(Context context) {
        this.context = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        listOfEPCs.add("1410452568525");
        listOfEPCs.add("1410452568520");
    }

    @NonNull
    @Override
    public TemperatureProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.temperature_profile, parent, false);
        return new TemperatureProfileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvBinEPC.setText("14104525685"+position);
        holder.tvHigh.setText("High:5.8C");
        holder.tvAvg.setText("Avg:4.5C");
        holder.tvLow.setText("Low:3.8C");
    }

    @Override
    public int getItemCount() {
        if(listOfEPCs == null){
            return  0;
        }
        else {
            return listOfEPCs.size();
        }
    }

    public synchronized void refill(ArrayList<String> data) {
        this.listOfEPCs.clear();
        this.listOfEPCs.addAll(data);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Context mContext;
        public LineChart temperatureChart;
        public LineData data;
        public CardView cardView;
        public ArrayList<Entry> values;
        public TextView tvBinEPC;
        public TextView tvHigh;
        public TextView tvAvg;
        public TextView tvLow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            cardView = itemView.findViewById(R.id.crdlayout);
            temperatureChart = itemView.findViewById(R.id.tempChart);

            tvBinEPC = itemView.findViewById(R.id.tvBinEPC);
            tvHigh = itemView.findViewById(R.id.tvHigh);
            tvAvg = itemView.findViewById(R.id.tvAvg);
            tvLow = itemView.findViewById(R.id.tvLow);

            setData(10,3f);
            customiseChart();
        }

        private void customiseChart()
        {
            temperatureChart.getDescription().setEnabled(false);
            temperatureChart.setDrawGridBackground(false);
            temperatureChart.setDragEnabled(false);
            temperatureChart.getLegend().setEnabled(false);
            temperatureChart.setScaleEnabled(true);
            temperatureChart.setScaleYEnabled(false);
            temperatureChart.setScaleXEnabled(true);
            temperatureChart.setDrawGridBackground(false);
            temperatureChart.getXAxis().setEnabled(false);
            temperatureChart.getLineData().setDrawValues(false);
            temperatureChart.getXAxis().setDrawGridLines(false);
            temperatureChart.getXAxis().setDrawAxisLine(false);
            temperatureChart.getAxisLeft().setDrawGridLines(false);
            temperatureChart.getAxisRight().setDrawGridLines(false);
            temperatureChart.getAxisRight().setDrawZeroLine(true);
            temperatureChart.getAxisLeft().setDrawZeroLine(true);
            temperatureChart.getAxisRight().setDrawLabels(false);
            temperatureChart.getAxisLeft().setDrawLabels(false);
            temperatureChart.getAxisLeft().setEnabled(false);
            temperatureChart.getAxisRight().setEnabled(true);
            temperatureChart.getAxisRight().setZeroLineColor(Color.BLACK);
            temperatureChart.getAxisRight().setAxisLineColor(Color.BLACK);
            temperatureChart.setMaxHighlightDistance(150);
            temperatureChart.setViewPortOffsets(0, 0, 0, 0);
            temperatureChart.setTouchEnabled(false);
            temperatureChart.setPinchZoom(false);
        }

        private void setData(int count, float range) {

            values = new ArrayList<>();

            values.add(new Entry(1,10f));
            values.add(new Entry(2,12f));
            values.add(new Entry(3,8f));
            values.add(new Entry(4,12f));
            values.add(new Entry(5,3f));
            values.add(new Entry(6,4f));
            values.add(new Entry(7,4f));
            values.add(new Entry(8,5f));
            values.add(new Entry(9,2f));
            values.add(new Entry(10,3f));
            values.add(new Entry(11,9f));
            values.add(new Entry(12,4f));
            values.add(new Entry(13,7f));
            values.add(new Entry(14,10));



            set1 = new LineDataSet(values, "DataSet 1");
            set1.setDrawCircles(false);
            set1.setDrawFilled(true);
            set1.setLineWidth(1f);
            set1.setColor(Color.GREEN);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setDrawFilled(true);
            //set1.setFillDrawable(drawablePositive);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return temperatureChart.getAxisLeft().getAxisMinimum();

                }
            });
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            data = new LineData(dataSets);

            temperatureChart.setData(data);
        }
    }
}
