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
import java.util.DoubleSummaryStatistics;
import java.util.Map;

import io.agritrack.R;
import io.agritrack.fish.state.LoggerDataRecord;

public class TemperatureProfileAdapter extends RecyclerView.Adapter<TemperatureProfileAdapter.ViewHolder>{
    private LayoutInflater mLayoutInflater = null;
    public  Context context;
    private ArrayList<String> listOfEPCs = new ArrayList<>();
    private static LineDataSet set1;
    private double highT, avgT, lowT;
    private Map<String, LoggerDataRecord.TemperatureModel> mapOfData;


    public TemperatureProfileAdapter(Context ctx, Map<String, LoggerDataRecord.TemperatureModel> data) {
        this.mapOfData = data;
        this.context = ctx;
        this.listOfEPCs = new ArrayList<>(data.keySet());

        DoubleSummaryStatistics stats = data.values().stream().flatMap(y->y.values.stream()).mapToDouble(x -> Double.valueOf(x[1])).summaryStatistics();
        this.highT = stats.getMax();
        this.lowT = stats.getMin();
        this.avgT = stats.getAverage();
    }

    public TemperatureProfileAdapter(Context context) {
        this.context = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public TemperatureProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.temperature_profile, parent, false);
        return new TemperatureProfileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        double _highT = 0.0d, _avgT = 0.0d, _lowT = 0.0d;
        String key = listOfEPCs.get(position);
        LoggerDataRecord.TemperatureModel model = mapOfData.get(key);

        if(model!=null) {
            DoubleSummaryStatistics stats = model.values.stream().mapToDouble(x -> Double.valueOf(x[1].replace(',', '.'))).summaryStatistics();

            _highT = stats.getMax();
            _lowT = stats.getMin();
            _avgT = stats.getAverage();

            holder.tvBinEPC.setText(key);
            holder.tvHigh.setText(String.format("%.2f\u2103", _highT));
            holder.tvAvg.setText(String.format("%.2f\u2103", _avgT));
            holder.tvLow.setText(String.format("%.2f\u2103", _lowT));
        }
    }

    @Override
    public int getItemCount() {
        if(listOfEPCs == null){
            return  0;
        } else {
            return listOfEPCs.size();
        }
    }

    public synchronized void refill(Map<String, LoggerDataRecord.TemperatureModel> data) {
        this.listOfEPCs = new ArrayList<>(data.keySet());
        this.mapOfData = data;
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
            temperatureChart.setDrawGridBackground(true);
            temperatureChart.setDragEnabled(false);
            temperatureChart.getLegend().setEnabled(false);
            temperatureChart.setScaleEnabled(true);
            temperatureChart.setScaleYEnabled(false);
            temperatureChart.setScaleXEnabled(true);
            temperatureChart.getXAxis().setEnabled(true);
            temperatureChart.getLineData().setDrawValues(false);
            temperatureChart.getXAxis().setDrawGridLines(false);
            temperatureChart.getXAxis().setDrawAxisLine(false);
            temperatureChart.getAxisLeft().setDrawGridLines(true);
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
