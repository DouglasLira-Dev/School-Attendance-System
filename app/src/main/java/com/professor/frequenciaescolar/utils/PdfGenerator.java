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
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfGenerator {

    private static boolean pdfBoxInicializado = false;
    private static final float MARGEM = 50f;
    private static final float ALTURA_LINHA = 18f;

    private Context context;
    private ConfiguracoesManager configManager;

    public PdfGenerator(Context context) {
        this.context = context;
        this.configManager = new ConfiguracoesManager(context);
        if (!pdfBoxInicializado) {
            PDFBoxResourceLoader.init(context.getApplicationContext());
            pdfBoxInicializado = true;
        }
    }

    public void gerarRelatorioAluno(Aluno aluno, Turma turma,
                                    List<Chamada> chamadas,
                                    List<Presenca> presencas,
                                    int totalDias, int presencasCount,
                                    int faltasJustificadas, int faltasNaoJustificadas,
                                    double percentual) {

        PDDocument document = new PDDocument();
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "relatorio_" + aluno.getNome().replace(" ", "_") + "_" + timestamp + ".pdf";

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            File pdfFile = new File(downloadsDir, fileName);

            PDFont fonteNormal = PDType1Font.HELVETICA;
            PDFont fonteNegrito = PDType1Font.HELVETICA_BOLD;

            PDPage pagina = new PDPage(PDRectangle.A4);
            document.addPage(pagina);
            PDPageContentStream cs = new PDPageContentStream(document, pagina);

            float y = PDRectangle.A4.getHeight() - MARGEM;

            // ---------- Título ----------
            y = desenharTextoCentralizado(cs, "RELATÓRIO DE FREQUÊNCIA - ALUNO", fonteNegrito, 16, y);
            y -= 20;

            // ---------- Informações do aluno ----------
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Aluno:", aluno.getNome(), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Matrícula:", aluno.getMatricula(), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Responsável:", aluno.getResponsavel(), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Telefone:", aluno.getTelefone(), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Turma:", turma.getNome() + " - " + turma.getTurno(), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Período Letivo:",
                    configManager.getDataInicio() + " a " + configManager.getDataFim(), y);
            y -= 15;

            // ---------- Resumo de frequência ----------
            cs.beginText();
            cs.setFont(fonteNegrito, 13);
            cs.newLineAtOffset(MARGEM, y);
            cs.showText("RESUMO DE FREQUÊNCIA");
            cs.endText();
            y -= ALTURA_LINHA;

            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Total de Dias Letivos:", String.valueOf(totalDias), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Presenças:", String.valueOf(presencasCount), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Faltas Justificadas:", String.valueOf(faltasJustificadas), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Faltas Não Justificadas:", String.valueOf(faltasNaoJustificadas), y);
            y -= 10;

            // ---------- Faixa de percentual (cor conforme desempenho) ----------
            String textoPercentual = "Percentual de Frequência: " + String.format(Locale.getDefault(), "%.1f%%", percentual);
            if (percentual >= 80) {
                cs.setNonStrokingColor(0.20f, 0.60f, 0.20f); // verde
            } else if (percentual >= 60) {
                cs.setNonStrokingColor(0.90f, 0.55f, 0.10f); // laranja
            } else {
                cs.setNonStrokingColor(0.80f, 0.10f, 0.10f); // vermelho
            }
            float larguraFaixa = PDRectangle.A4.getWidth() - (MARGEM * 2);
            cs.addRect(MARGEM, y - ALTURA_LINHA + 2, larguraFaixa, ALTURA_LINHA + 6);
            cs.fill();

            cs.setNonStrokingColor(1f, 1f, 1f);
            float larguraTexto = fonteNegrito.getStringWidth(textoPercentual) / 1000 * 12;
            float xCentro = MARGEM + (larguraFaixa - larguraTexto) / 2;
            cs.beginText();
            cs.setFont(fonteNegrito, 12);
            cs.newLineAtOffset(xCentro, y - ALTURA_LINHA + 8);
            cs.showText(textoPercentual);
            cs.endText();
            cs.setNonStrokingColor(0f, 0f, 0f);
            y -= (ALTURA_LINHA + 20);

            // ---------- Rodapé ----------
            desenharTextoCentralizado(cs, "Documento gerado em " +
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()), fonteNormal, 8, y);

            cs.close();
            document.save(pdfFile);

            compartilharArquivo(pdfFile);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                document.close();
            } catch (Exception ignored) {
            }
        }
    }

    private float desenharTextoCentralizado(PDPageContentStream cs, String texto, PDFont fonte, float tamanho, float y) throws Exception {
        float larguraTexto = fonte.getStringWidth(texto) / 1000 * tamanho;
        float x = (PDRectangle.A4.getWidth() - larguraTexto) / 2;
        cs.beginText();
        cs.setFont(fonte, tamanho);
        cs.newLineAtOffset(x, y);
        cs.showText(texto);
        cs.endText();
        return y - ALTURA_LINHA;
    }

    private float desenharLinhaInfo(PDPageContentStream cs, PDFont fonteNegrito, PDFont fonteNormal, String label, String valor, float y) throws Exception {
        cs.beginText();
        cs.setFont(fonteNegrito, 10);
        cs.newLineAtOffset(MARGEM, y);
        cs.showText(label);
        cs.endText();

        cs.beginText();
        cs.setFont(fonteNormal, 10);
        cs.newLineAtOffset(MARGEM + 150, y);
        cs.showText(valor != null ? valor : "-");
        cs.endText();

        return y - 16;
    }

    private void compartilharArquivo(File file) {
        Uri uri = FileProvider.getUriForFile(context,
                context.getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar PDF"));
    }
}