package com.professor.frequenciaescolar.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class XlsxGenerator {

    private Context context;

    public XlsxGenerator(Context context) {
        this.context = context;
    }

    public void gerarRelatorioAlunoExcel(Aluno aluno, Turma turma,
                                         List<Chamada> chamadas,
                                         List<Presenca> presencas,
                                         int totalDias, int presencasCount,
                                         int faltasJustificadas, int faltasNaoJustificadas,
                                         double percentual) {

        try {
            // Criar nome do arquivo CSV
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "relatorio_" + aluno.getNome().replace(" ", "_") + "_" + timestamp + ".csv";

            // Diretório de download
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File csvFile = new File(downloadsDir, fileName);
            FileOutputStream fos = new FileOutputStream(csvFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

            // Escrever conteúdo CSV
            StringBuilder sb = new StringBuilder();

            // Título
            sb.append("RELATÓRIO DE FREQUÊNCIA - ALUNO\n\n");

            // Informações do Aluno
            sb.append("INFORMAÇÕES DO ALUNO\n");
            sb.append("Nome;").append(aluno.getNome()).append("\n");
            sb.append("Matrícula;").append(aluno.getMatricula()).append("\n");
            sb.append("Responsável;").append(aluno.getResponsavel()).append("\n");
            sb.append("Telefone;").append(aluno.getTelefone()).append("\n");
            sb.append("Turma;").append(turma.getNome() + " - " + turma.getTurno()).append("\n\n");

            // Resumo de Frequência
            sb.append("RESUMO DE FREQUÊNCIA\n");
            sb.append("Total de Dias Letivos;").append(totalDias).append("\n");
            sb.append("Presenças;").append(presencasCount).append("\n");
            sb.append("Faltas Justificadas;").append(faltasJustificadas).append("\n");
            sb.append("Faltas Não Justificadas;").append(faltasNaoJustificadas).append("\n");
            sb.append("Percentual de Frequência;").append(String.format("%.1f%%", percentual)).append("\n\n");

            // Histórico
            sb.append("HISTÓRICO DE CHAMADAS\n");
            sb.append("Data;Horário;Status;Justificativa\n");

            for (Chamada c : chamadas) {
                for (Presenca p : presencas) {
                    if (p.getAlunoId() == aluno.getId() && p.getChamadaId() == c.getId()) {
                        sb.append(c.getData()).append(";");
                        sb.append(c.getHorarioRegistro()).append(";");

                        if (p.isPresente()) {
                            sb.append("Presente;");
                        } else if (p.getJustificativa() != null && !p.getJustificativa().isEmpty()) {
                            sb.append("Falta Justificada;");
                            sb.append(p.getJustificativa());
                        } else {
                            sb.append("Falta Não Justificada;");
                        }
                        sb.append("\n");
                        break;
                    }
                }
            }

            osw.write(sb.toString());
            osw.flush();
            osw.close();
            fos.close();

            // Compartilhar arquivo
            compartilharArquivo(csvFile);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Erro ao gerar CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void compartilharArquivo(File file) {
        Uri uri = FileProvider.getUriForFile(context,
                context.getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar CSV"));
    }
}