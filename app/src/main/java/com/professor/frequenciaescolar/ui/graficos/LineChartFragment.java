package com.professor.frequenciaescolar.ui.graficos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.professor.frequenciaescolar.R;

import java.util.ArrayList;
import java.util.List;

public class LineChartFragment extends Fragment {

    private LineChart lineChart;
    private TextView tvInfo;

    private List<String> meses;
    private List<Float> valores;
    private String alunoNome;

    public LineChartFragment(List<String> meses, List<Float> valores, String alunoNome) {
        this.meses = meses;
        this.valores = valores;
        this.alunoNome = alunoNome;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grafico_linha, container, false);

        lineChart = view.findViewById(R.id.lineChart);
        tvInfo = view.findViewById(R.id.tvInfo);

        configurarGrafico();
        carregarDados();

        tvInfo.setText(String.format("Evolução da frequência de %s por mês", alunoNome));

        return view;
    }

    private void configurarGrafico() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // Configurar eixo X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        // Configurar eixo Y
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setAxisMaximum(100f);
        lineChart.getAxisRight().setEnabled(false);
    }

    private void carregarDados() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < valores.size(); i++) {
            entries.add(new Entry(i, valores.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Frequência (%)");
        dataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSet.setCircleColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSet.setCircleRadius(6f);
        dataSet.setLineWidth(3f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(android.R.color.holo_blue_light));
        dataSet.setFillAlpha(100);

        // Formatter personalizado para os valores
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value);
            }
        });

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Configurar labels dos meses
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(meses));

        // Adicionar linha de 80%
        com.github.mikephil.charting.components.LimitLine limitLine =
                new com.github.mikephil.charting.components.LimitLine(80f, "Mínimo Recomendado (80%)");
        limitLine.setLineWidth(2f);
        limitLine.setLineColor(getResources().getColor(android.R.color.holo_red_dark));
        limitLine.setTextSize(10f);
        lineChart.getAxisLeft().addLimitLine(limitLine);

        lineChart.invalidate();
    }
}