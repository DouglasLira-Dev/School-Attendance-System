package com.professor.frequenciaescolar.ui.graficos;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;
import com.professor.frequenciaescolar.utils.ConfiguracoesManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GraficosFrequenciaActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private FrequenciaRepository repository;
    private ConfiguracoesManager configManager;

    private long turmaId;
    private long alunoId;
    private String alunoNome;
    private Turma turma;

    private List<String> meses = new ArrayList<>();
    private List<Float> valoresTurma = new ArrayList<>();
    private List<Float> valoresAluno = new ArrayList<>();
    private float mediaTurma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficos_frequencia);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        repository = FrequenciaRepository.getInstance(this);
        configManager = new ConfiguracoesManager(this);

        turmaId = getIntent().getLongExtra("turma_id", -1);
        alunoId = getIntent().getLongExtra("aluno_id", -1);
        alunoNome = getIntent().getStringExtra("aluno_nome");

        if (turmaId != -1) {
            carregarTurma();
            carregarDados();
        } else {
            finish();
        }
    }

    private void carregarTurma() {
        repository.getTurmaById(turmaId, turma -> {
            runOnUiThread(() -> {
                this.turma = turma;
                if (getSupportActionBar() != null && turma != null) {
                    getSupportActionBar().setTitle("Gráficos - " + turma.getNome());
                }
            });
        });
    }

    private void carregarDados() {
        // Preparar meses do ano
        for (int i = 1; i <= 12; i++) {
            meses.add(getNomeMes(i));
            valoresTurma.add(0f);
        }

        // Buscar chamadas do ano atual
        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        String dataInicio = anoAtual + "-01-01";
        String dataFim = anoAtual + "-12-31";

        repository.getChamadasPorPeriodo(dataInicio, dataFim, chamadas -> {
            runOnUiThread(() -> {
                if (chamadas != null && !chamadas.isEmpty()) {
                    calcularFrequenciaPorMes(chamadas);
                    configurarViewPager();
                }
            });
        });
    }

    private void calcularFrequenciaPorMes(List<Chamada> chamadas) {
        // Agrupar chamadas por mês
        Map<Integer, List<Chamada>> chamadasPorMes = new HashMap<>();
        for (Chamada c : chamadas) {
            try {
                String[] dataParts = c.getData().split("-");
                int mes = Integer.parseInt(dataParts[1]);
                if (!chamadasPorMes.containsKey(mes)) {
                    chamadasPorMes.put(mes, new ArrayList<>());
                }
                chamadasPorMes.get(mes).add(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Arrays mutáveis para acumular valores
        float[] somaPercentuais = new float[12];
        int[] totalChamadasPorMes = new int[12];

        // Para cada mês com chamadas
        for (Map.Entry<Integer, List<Chamada>> entry : chamadasPorMes.entrySet()) {
            int mes = entry.getKey();
            List<Chamada> chamadasMes = entry.getValue();

            AtomicInteger totalPresencasTurma = new AtomicInteger(0);
            AtomicInteger totalAlunosTurma = new AtomicInteger(0);

            for (Chamada c : chamadasMes) {
                repository.getPresencasByChamada(c.getId(), presencas -> {
                    if (presencas != null) {
                        for (Presenca p : presencas) {
                            if (p.isPresente()) {
                                totalPresencasTurma.incrementAndGet();
                            }
                            totalAlunosTurma.incrementAndGet();
                        }
                    }

                    // Calcular frequência da turma
                    int finalMes = mes;
                    float freqTurma = totalAlunosTurma.get() > 0 ?
                            (totalPresencasTurma.get() * 100f / totalAlunosTurma.get()) : 0;
                    valoresTurma.set(finalMes - 1, freqTurma);
                    somaPercentuais[finalMes - 1] += freqTurma;
                    totalChamadasPorMes[finalMes - 1]++;

                    // Calcular frequência do aluno específico (se existir)
                    if (alunoId != -1) {
                        calcularFrequenciaAluno(chamadasMes, alunoId);
                    }

                    // Calcular média final após todos os meses
                    if (mes == 12) {
                        calcularMediaTurma(somaPercentuais, totalChamadasPorMes);
                        configurarViewPager();
                    }
                });
            }
        }
    }

    private void calcularFrequenciaAluno(List<Chamada> chamadasMes, long alunoId) {
        for (Chamada c : chamadasMes) {
            repository.getPresencasByChamada(c.getId(), presencas -> {
                int presencasAluno = 0;
                int totalAulasAluno = 0;

                for (Presenca p : presencas) {
                    if (p.getAlunoId() == alunoId) {
                        if (p.isPresente()) {
                            presencasAluno++;
                        }
                        totalAulasAluno++;
                        break;
                    }
                }

                // Usar valoresAluno já existente
                // Nota: Esta parte depende de como você quer exibir
            });
        }
    }

    private void calcularMediaTurma(float[] somaPercentuais, int[] totalChamadasPorMes) {
        float soma = 0;
        int count = 0;
        for (int i = 0; i < 12; i++) {
            if (totalChamadasPorMes[i] > 0) {
                soma += somaPercentuais[i] / totalChamadasPorMes[i];
                count++;
            }
        }
        mediaTurma = count > 0 ? soma / count : 0;
    }

    private void configurarViewPager() {
        GraficosPagerAdapter pagerAdapter = new GraficosPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Frequência da Turma");
                    } else {
                        tab.setText("Evolução do Aluno");
                    }
                }
        ).attach();
    }

    private String getNomeMes(int mes) {
        String[] nomes = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
                "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        return nomes[mes - 1];
    }

    private class GraficosPagerAdapter extends FragmentStateAdapter {

        public GraficosPagerAdapter(GraficosFrequenciaActivity activity) {
            super(activity);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new BarChartFragment(meses, valoresTurma, mediaTurma);
            } else {
                String nome = alunoNome != null ? alunoNome : "Aluno";
                return new LineChartFragment(meses, valoresAluno, nome);
            }
        }

        @Override
        public int getItemCount() {
            return alunoId != -1 ? 2 : 1;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}