package com.professor.frequenciaescolar.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfGenerator {

    private Context context;
    private ConfiguracoesManager configManager;

    public PdfGenerator(Context context) {
        this.context = context;
        this.configManager = new ConfiguracoesManager(context);
    }

    public void gerarRelatorioAluno(Aluno aluno, Turma turma,
                                    List<Chamada> chamadas,
                                    List<Presenca> presencas,
                                    int totalDias, int presencasCount,
                                    int faltasJustificadas, int faltasNaoJustificadas,
                                    double percentual) {

        try {
            // Criar nome do arquivo
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "relatorio_" + aluno.getNome().replace(" ", "_") + "_" + timestamp + ".pdf";

            // Diretório de download
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File pdfFile = new File(downloadsDir, fileName);
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            // Título
            PdfFont boldFont = PdfFontFactory.createFont();
            Paragraph titulo = new Paragraph("RELATÓRIO DE FREQUÊNCIA - ALUNO")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(titulo);

            // Informações do Aluno
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            infoTable.setWidth(UnitValue.createPercentValue(100));
            infoTable.setMarginBottom(20);

            adicionarLinhaTabela(infoTable, "Aluno:", aluno.getNome());
            adicionarLinhaTabela(infoTable, "Matrícula:", aluno.getMatricula());
            adicionarLinhaTabela(infoTable, "Responsável:", aluno.getResponsavel());
            adicionarLinhaTabela(infoTable, "Telefone:", aluno.getTelefone());
            adicionarLinhaTabela(infoTable, "Turma:", turma.getNome() + " - " + turma.getTurno());
            adicionarLinhaTabela(infoTable, "Período Letivo:", configManager.getDataInicio() + " a " + configManager.getDataFim());

            document.add(infoTable);

            // Resumo de Frequência
            Paragraph resumoTitle = new Paragraph("RESUMO DE FREQUÊNCIA")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(resumoTitle);

            Table resumoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            resumoTable.setWidth(UnitValue.createPercentValue(100));
            resumoTable.setMarginBottom(20);

            adicionarLinhaTabela(resumoTable, "Total de Dias Letivos:", String.valueOf(totalDias));
            adicionarLinhaTabela(resumoTable, "Presenças:", String.valueOf(presencasCount));
            adicionarLinhaTabela(resumoTable, "Faltas Justificadas:", String.valueOf(faltasJustificadas));
            adicionarLinhaTabela(resumoTable, "Faltas Não Justificadas:", String.valueOf(faltasNaoJustificadas));

            Cell percentualCell = new Cell(1, 2)
                    .add(new Paragraph("Percentual de Frequência: " + String.format("%.1f%%", percentual)))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(percentual >= 80 ? ColorConstants.GREEN :
                            (percentual >= 60 ? ColorConstants.ORANGE : ColorConstants.RED))
                    .setFontColor(ColorConstants.WHITE)
                    .setBold();
            resumoTable.addCell(percentualCell);

            document.add(resumoTable);

            // Rodapé
            Paragraph footer = new Paragraph("Documento gerado em " +
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()))
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            document.close();

            // Compartilhar PDF
            compartilharArquivo(pdfFile);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void adicionarLinhaTabela(Table table, String label, String valor) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(valor)));
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