package com.professor.frequenciaescolar.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "movimentacoes_aluno")
public class MovimentacaoAluno {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long alunoId;
    private long turmaOrigemId;
    private long turmaDestinoId;
    private String dataMovimentacao;
    private String tipo; // "transferencia_turma", "transferencia_escola", "expulsao", "desistencia"
    private String escolaDestino;
    private String observacao;

    // Construtor vazio
    public MovimentacaoAluno() {}

    // Construtor com parâmetros
    public MovimentacaoAluno(long alunoId, long turmaOrigemId, long turmaDestinoId,
                             String dataMovimentacao, String tipo, String escolaDestino, String observacao) {
        this.alunoId = alunoId;
        this.turmaOrigemId = turmaOrigemId;
        this.turmaDestinoId = turmaDestinoId;
        this.dataMovimentacao = dataMovimentacao;
        this.tipo = tipo;
        this.escolaDestino = escolaDestino;
        this.observacao = observacao;
    }

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getAlunoId() { return alunoId; }
    public void setAlunoId(long alunoId) { this.alunoId = alunoId; }

    public long getTurmaOrigemId() { return turmaOrigemId; }
    public void setTurmaOrigemId(long turmaOrigemId) { this.turmaOrigemId = turmaOrigemId; }

    public long getTurmaDestinoId() { return turmaDestinoId; }
    public void setTurmaDestinoId(long turmaDestinoId) { this.turmaDestinoId = turmaDestinoId; }

    public String getDataMovimentacao() { return dataMovimentacao; }
    public void setDataMovimentacao(String dataMovimentacao) { this.dataMovimentacao = dataMovimentacao; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEscolaDestino() { return escolaDestino; }
    public void setEscolaDestino(String escolaDestino) { this.escolaDestino = escolaDestino; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}