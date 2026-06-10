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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class XlsxGenerator {

    private Context context;
    private ConfiguracoesManager configManager;

    public XlsxGenerator(Context context) {
        this.context = context;
        this.configManager = new ConfiguracoesManager(context);
    }

    public void gerarRelatorioAlunoExcel(Aluno aluno, Turma turma,
                                         List<Chamada> chamadas,
                                         List<Presenca> presencas,
                                         int totalDias, int presencasCount,
                                         int faltasJustificadas, int faltasNaoJustificadas,
                                         double percentual) {

        try {
            // Criar nome do arquivo
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "relatorio_" + aluno.getNome().replace(" ", "_") + "_" + timestamp + ".xlsx";

            // Diretório de download
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File excelFile = new File(downloadsDir, fileName);
            Workbook workbook = new XSSFWorkbook();

            // Criar estilos
            CellStyle headerStyle = criarEstiloCabecalho(workbook);
            CellStyle titleStyle = criarEstiloTitulo(workbook);
            CellStyle presenteStyle = criarEstiloPresente(workbook);
            CellStyle faltaJustificadaStyle = criarEstiloFaltaJustificada(workbook);
            CellStyle faltaNaoJustificadaStyle = criarEstiloFaltaNaoJustificada(workbook);

            // ==================== ABA 1: RESUMO ====================
            Sheet resumoSheet = workbook.createSheet("Resumo");
            resumoSheet.setColumnWidth(0, 8000);
            resumoSheet.setColumnWidth(1, 6000);

            int rowNum = 0;

            // Título
            Row titleRow = resumoSheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("RELATÓRIO DE FREQUÊNCIA - ALUNO");
            titleCell.setCellStyle(titleStyle);
            resumoSheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

            rowNum++; // Linha em branco

            // Informações do Aluno
            adicionarLinhaResumo(resumoSheet, rowNum++, "INFORMAÇÕES DO ALUNO", "", headerStyle);
            adicionarLinhaResumo(resumoSheet, rowNum++, "Nome:", aluno.getNome(), null);
            adicionarLinhaResumo(resumoSheet, rowNum++, "Matrícula:", aluno.getMatricula(), null);
            adicionarLinhaResumo(resumoSheet, rowNum++, "Responsável:", aluno.getResponsavel(), null);
            adicionarLinhaResumo(resumoSheet, rowNum++, "Telefone:", aluno.getTelefone(), null);
            adicionarLinhaResumo(resumoSheet, rowNum++, "Turma:", turma.getNome() + " - " + turma.getTurno(), null);
            adicionarLinhaResumo(resumoSheet, rowNum++, "Período:", configManager.getDataInicio() + " a " + configManager.getDataFim(), null);

            rowNum++; // Linha em branco

            // Resumo de Frequência
            adicionarLinhaResumo(resumoSheet, rowNum++, "RESUMO DE FREQUÊNCIA", "", headerStyle);
            adicionarLinhaResumo(resumoSheet, rowNum++, "Total de Dias Letivos:", String.valueOf(totalDias), null);
            adicionarLinhaResumo(resumoSheet, rowNum++, "Presenças:", String.valueOf(presencasCount), presenteStyle);
            adicionarLinhaResumo(resumoSheet, rowNum++, "Faltas Justificadas:", String.valueOf(faltasJustificadas), faltaJustificadaStyle);
            adicionarLinhaResumo(resumoSheet, rowNum++, "Faltas Não Justificadas:", String.valueOf(faltasNaoJustificadas), faltaNaoJustificadaStyle);

            rowNum++; // Linha em branco

            // Percentual
            Row percentualRow = resumoSheet.createRow(rowNum);
            Cell labelCell = percentualRow.createCell(0);
            labelCell.setCellValue("PERCENTUAL DE FREQUÊNCIA:");
            labelCell.setCellStyle(headerStyle);
            Cell valueCell = percentualRow.createCell(1);
            valueCell.setCellValue(String.format("%.1f%%", percentual));
            if (percentual >= 80) {
                valueCell.setCellStyle(presenteStyle);
            } else if (percentual >= 60) {
                valueCell.setCellStyle(faltaJustificadaStyle);
            } else {
                valueCell.setCellStyle(faltaNaoJustificadaStyle);
            }

            // ==================== ABA 2: HISTÓRICO ====================
            Sheet historicoSheet = workbook.createSheet("Histórico de Chamadas");

            // Cabeçalho do histórico
            Row headerRow = historicoSheet.createRow(0);
            String[] colunas = {"Data", "Horário", "Status", "Justificativa"};
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
                historicoSheet.setColumnWidth(i, 5000);
            }

            // Preencher histórico
            int linha = 1;
            for (Chamada c : chamadas) {
                for (Presenca p : presencas) {
                    if (p.getAlunoId() == aluno.getId() && p.getChamadaId() == c.getId()) {
                        Row row = historicoSheet.createRow(linha++);
                        row.createCell(0).setCellValue(c.getData());
                        row.createCell(1).setCellValue(c.getHorarioRegistro());

                        if (p.isPresente()) {
                            row.createCell(2).setCellValue("Presente");
                            row.getCell(2).setCellStyle(presenteStyle);
                            row.createCell(3).setCellValue("");
                        } else if (p.getJustificativa() != null && !p.getJustificativa().isEmpty()) {
                            row.createCell(2).setCellValue("Falta Justificada");
                            row.getCell(2).setCellStyle(faltaJustificadaStyle);
                            row.createCell(3).setCellValue(p.getJustificativa());
                        } else {
                            row.createCell(2).setCellValue("Falta Não Justificada");
                            row.getCell(2).setCellStyle(faltaNaoJustificadaStyle);
                            row.createCell(3).setCellValue("");
                        }
                        break;
                    }
                }
            }

            // ==================== ABA 3: ESTATÍSTICAS ====================
            Sheet estatisticasSheet = workbook.createSheet("Estatísticas");
            estatisticasSheet.setColumnWidth(0, 8000);
            estatisticasSheet.setColumnWidth(1, 6000);

            int estRow = 0;
            adicionarLinhaResumo(estatisticasSheet, estRow++, "ESTATÍSTICAS GERAIS", "", headerStyle);
            adicionarLinhaResumo(estatisticasSheet, estRow++, "Data de Geração:",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()), null);
            adicionarLinhaResumo(estatisticasSheet, estRow++, "Total de Chamadas:", String.valueOf(chamadas.size()), null);
            adicionarLinhaResumo(estatisticasSheet, estRow++, "Frequência Mínima Recomendada:", "80%", null);
            adicionarLinhaResumo(estatisticasSheet, estRow++, "Situação do Aluno:",
                    percentual >= 80 ? "Aprovado" : (percentual >= 60 ? "Recuperação" : "Reprovado"), null);

            // Salvar arquivo
            FileOutputStream fileOut = new FileOutputStream(excelFile);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            // Compartilhar arquivo
            compartilharArquivo(excelFile);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Erro ao gerar Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private CellStyle criarEstiloCabecalho(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle criarEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle criarEstiloPresente(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.GREEN.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle criarEstiloFaltaJustificada(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.ORANGE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle criarEstiloFaltaNaoJustificada(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        return style;
    }

    private void adicionarLinhaResumo(Sheet sheet, int rowNum, String label, String valor, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        if (style != null) {
            labelCell.setCellStyle(style);
        } else {
            Font boldFont = sheet.getWorkbook().createFont();
            boldFont.setBold(true);
            CellStyle boldStyle = sheet.getWorkbook().createCellStyle();
            boldStyle.setFont(boldFont);
            labelCell.setCellStyle(boldStyle);
        }

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(valor);
    }

    private void compartilharArquivo(File file) {
        Uri uri = FileProvider.getUriForFile(context,
                context.getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Excel"));
    }
}