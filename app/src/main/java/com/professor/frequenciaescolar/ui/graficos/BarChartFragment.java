package com.professor.frequenciaescolar.ui.graficos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.professor.frequenciaescolar.R;

import java.util.ArrayList;
import java.util.List;

public class BarChartFragment extends Fragment {

    private BarChart barChart;
    private TextView tvMedia;

    private List<String> meses;
    private List<Float> valores;
    private float media;

    public BarChartFragment(List<String> meses, List<Float> valores, float media) {
        this.meses = meses;
        this.valores = valores;
        this.media = media;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grafico_barras, container, false);

        barChart = view.findViewById(R.id.barChart);
        tvMedia = view.findViewById(R.id.tvMedia);

        configurarGrafico();
        carregarDados();

        tvMedia.setText(String.format("Média da Turma: %.1f%%", media));

        return view;
    }

    private void configurarGrafico() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(true);

        // Configurar eixo X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        // Configurar eixo Y
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setAxisMaximum(100f);
        barChart.getAxisRight().setEnabled(false);
    }

    private void carregarDados() {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < valores.size(); i++) {
            entries.add(new BarEntry(i, valores.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Frequência (%)");
        dataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSet.setValueTextSize(10f);

        // Formatter personalizado para os valores
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 80) {
                    return String.format("%.1f%% (Bom)", value);
                } else if (value >= 60) {
                    return String.format("%.1f%% (Regular)", value);
                } else {
                    return String.format("%.1f%% (Risco)", value);
                }
            }
        });

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Configurar labels dos meses
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(meses));

        barChart.invalidate();
    }
    public void atualizarDados(List<String> novosMeses, List<Float> novosValores, float novaMedia) {
        this.meses = novosMeses;
        this.valores = novosValores;
        this.media = novaMedia;

        // Atualizar o gráfico
        carregarDados();
        tvMedia.setText(String.format("Média da Turma: %.1f%%", media));
    }
}