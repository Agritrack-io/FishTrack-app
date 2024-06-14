package io.agritrack.philosofish.ui.adapter;

import static io.agritrack.philosofish.fish.state.GlobalState.recLoggerData;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.model.BinInfo;
import io.agritrack.philosofish.data.model.TempSample;
import io.agritrack.philosofish.dialog.DataListener;
import io.agritrack.philosofish.dialog.SetTempDataDialog;
import io.agritrack.philosofish.fish.state.LoggerDataRecord;
import io.agritrack.philosofish.fish.ui.binTurnover.BinTurnoverActivity;

public class TemperatureProfileAdapter extends RecyclerView.Adapter<TemperatureProfileAdapter.ViewHolder> implements DataListener {
    private static LineDataSet set1;
    private static int selectedPos = RecyclerView.NO_POSITION;
    public Context context;
    private LayoutInflater mLayoutInflater = null;
    private ArrayList<String> listOfEPCs = new ArrayList<>();
    private double highT, avgT, lowT;
    private String cageCode;
    private Double weight, fishT, waterT, fishT2;
    private Map<String, LoggerDataRecord.TemperatureModel> mapOfData;
    private SetTempDataDialog setTempDialog;
    private DataListener mListener;

    public TemperatureProfileAdapter(Context ctx, Map<String, LoggerDataRecord.TemperatureModel> data) {
        this.mapOfData = data;
        this.context = ctx;
        this.listOfEPCs = new ArrayList<>(data.keySet());

        DoubleSummaryStatistics stats = data.values().stream().flatMap(y -> y.values.stream().filter(z -> z.isAfterFishing())).mapToDouble(x -> Double.valueOf(x.getSample())).summaryStatistics();
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
        if (mLayoutInflater.getContext() instanceof BinTurnoverActivity) {
            view = mLayoutInflater.inflate(R.layout.temperature_profile_for_turnover, parent, false);
        }
        return new TemperatureProfileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        double _highT = 0.0d, _avgT = 0.0d, _lowT = 0.0d;
        String key = listOfEPCs.get(position);

        LoggerDataRecord.TemperatureModel model = mapOfData!=null ? mapOfData.get(key) : null;

        if (model != null) {
            long measurementsCount = model.values.stream().filter(y -> !"N/A".equalsIgnoreCase(y.getSample())).count();

            if (measurementsCount > 0) {
                DoubleSummaryStatistics stats = model.values.stream().filter(y -> !"N/A".equalsIgnoreCase(y.getSample()) && y.isAfterFishing()).mapToDouble(x -> Double.valueOf(x.getSample().replace(',', '.'))).summaryStatistics();

                _highT = stats.getMax();
                _lowT = stats.getMin();
                _avgT = stats.getAverage();

                holder.setMeasurements(key, model.values);
                holder.tvBinEPC.setText(key.substring(key.length() - 10));
                if (mLayoutInflater.getContext() instanceof BinTurnoverActivity) {
                    holder.tvCageCode.setText(cageCode);
                    holder.tvWeight.setText(String.valueOf(weight));
                    holder.tvFish.setText(fishT != null ? String.valueOf(fishT) : "");
                    holder.tvWater.setText(waterT != null ? String.valueOf(waterT) : "");
                    holder.tvFish2.setText(fishT2 != null ? String.valueOf(fishT2) : "");
                    List<TempSample> values = recLoggerData.getValues(key);

                    if (values != null) {
                        recLoggerData.addDataSetForBin(key, fishT, waterT, fishT2);
                    }
                }
                holder.tvHigh.setText(String.format("%.2f\u2103", _highT));
                holder.tvAvg.setText(String.format("%.2f\u2103", _avgT));
                holder.tvLow.setText(String.format("%.2f\u2103", _lowT));
            } else {
                holder.tvHigh.setText("N/A");
                holder.tvAvg.setText("N/A");
                holder.tvLow.setText("N/A");
            }
        } else {
            if (mLayoutInflater.getContext() instanceof BinTurnoverActivity) {
                holder.tvCageCode.setText(cageCode);
                holder.tvWeight.setText(String.valueOf(weight));
                holder.tvFish.setText(fishT != null ? String.valueOf(fishT) : "");
                holder.tvWater.setText(waterT != null ? String.valueOf(waterT) : "");
                holder.tvFish2.setText(fishT2 != null ? String.valueOf(fishT2) : "");
                recLoggerData.addDataSetForBin(key, fishT, waterT, fishT2);
            }
        }

        holder.infoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = listOfEPCs.get(holder.getAdapterPosition());
                LoggerDataRecord.TemperatureModel model = mapOfData!=null ? mapOfData.get(key) : null;

                List<TempSample> values = model != null ? model.values : null;

                AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
                dlgBuilder.setTitle("Logger Data");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.agri_list_item_12dp);

                if (values != null) {
                    int idx = 1;
                    for (TempSample value : values) {
                        arrayAdapter.add(String.format("%04d. [%s] --> %s", idx++, value.getTimeStamp(), value.getSample()));
                    }
                    dlgBuilder.setAdapter(arrayAdapter, null);
                    dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
                    dlgBuilder.create().show();
                }
            }
        });

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            setTempDialog = new SetTempDataDialog(activity, fishT, waterT, fishT2);
            setTempDialog.setMyDialogListener(this);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTempDialog.showDialog();

//                AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
//                dlgBuilder.setTitle("Logger Data");
//
//                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.agri_list_item_12dp);
//
//                int idx = 1;
//                for (String[] value : values) {
//                    arrayAdapter.add(String.format("%04d. [%s] --> %s", idx++, value[0], value[1]));
//                }
//                dlgBuilder.setAdapter(arrayAdapter, null);
//                dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
//                dlgBuilder.create().show();
            }
        });
    }

    @Override
    public void onDataPassed(Double fishT, Double waterT, Double fishT2) {
        // Handle the passed data here
        this.fishT = fishT;
        this.waterT = waterT;
        this.fishT2 = fishT2;
        notifyDataSetChanged();
    }

    public String getEpc() {
        return this.listOfEPCs.get(0);
    }

    @Override
    public int getItemCount() {
        if (listOfEPCs == null) {
            return 0;
        } else {
            return listOfEPCs.size();
        }
    }

    public synchronized void refill(Map<String, LoggerDataRecord.TemperatureModel> data) {
        this.listOfEPCs = new ArrayList<>(data.keySet());
        this.mapOfData = data;
        notifyDataSetChanged();
    }

    public synchronized void fill(Map<String, LoggerDataRecord.TemperatureModel> data, BinInfo tmpBin) {
        this.listOfEPCs = new ArrayList<>(data.keySet());
        this.mapOfData = data;
        this.cageCode = tmpBin.cage;
        this.weight = tmpBin.totalWeight;
        notifyDataSetChanged();
    }

    public synchronized void fill(BinInfo tmpBin) {
        ArrayList<String> tt = new ArrayList<>();
        tt.add(tmpBin.rfid);
        this.listOfEPCs = tt;
        this.cageCode = tmpBin.cage;
        this.weight = tmpBin.totalWeight;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Context mContext;
        public LineChart temperatureChart;
        public ConstraintLayout infoLayout;
        public LineData data;
        public CardView cardView;
        public TextView tvBinEPC;
        public TextView tvCageCode;
        public TextView tvWeight;
        public TextView tvHigh;
        public TextView tvAvg;
        public TextView tvLow;
        public TextView tvFish;
        public TextView tvWater;
        public TextView tvFish2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            if (this.mContext instanceof BinTurnoverActivity) {
                cardView = itemView.findViewById(R.id.crdlayoutForTurnover);
                tvFish = itemView.findViewById(R.id.tvFishT);
                tvWater = itemView.findViewById(R.id.tvWaterT);
                tvFish2 = itemView.findViewById(R.id.tvFishT2);
                infoLayout = itemView.findViewById(R.id.infoLayout);
            } else {
                cardView = itemView.findViewById(R.id.crdlayout);
            }
            temperatureChart = itemView.findViewById(R.id.tempChartIn);

            tvBinEPC = itemView.findViewById(R.id.tvBinEPC);
            tvCageCode = itemView.findViewById(R.id.tvCageCode);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvHigh = itemView.findViewById(R.id.tvHigh);
            tvAvg = itemView.findViewById(R.id.tvAvg);
            tvLow = itemView.findViewById(R.id.tvLow);

        }

        private void customiseChart() {
            temperatureChart.getDescription().setEnabled(false);
            temperatureChart.setDrawGridBackground(true);
            temperatureChart.setDragEnabled(false);
            temperatureChart.getLegend().setEnabled(false);
            temperatureChart.setScaleEnabled(true);
            temperatureChart.setScaleYEnabled(true);
            temperatureChart.setScaleXEnabled(true);
            temperatureChart.getXAxis().setEnabled(false);
            temperatureChart.getLineData().setDrawValues(false);
            temperatureChart.getXAxis().setDrawGridLines(false);
            temperatureChart.getXAxis().setDrawAxisLine(true);
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

        public void setMeasurements(String key, List<TempSample> measurements) {
            AtomicInteger idx = new AtomicInteger();
            ArrayList<Entry> values = (ArrayList<Entry>) measurements.stream().map(x -> new Entry(idx.incrementAndGet(), !x.getSample().equalsIgnoreCase("N/A") ? Float.valueOf(x.getSample().replace(',', '.')) : Float.NaN)).collect(Collectors.toList());

            set1 = new LineDataSet(values, key);
            set1.setDrawCircles(false);
            set1.setDrawFilled(true);
            set1.setLineWidth(1f);
            set1.setColor(Color.GREEN);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setDrawFilled(true);
            set1.setFillFormatter((dataSet, dataProvider) -> temperatureChart.getAxisLeft().getAxisMinimum());

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            data = new LineData(dataSets);
            temperatureChart.setData(data);

            customiseChart();
        }
    }
}
