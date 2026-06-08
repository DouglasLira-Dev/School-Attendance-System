package com.professor.frequenciaescolar.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "matriculas")
public class Matricula {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long alunoId;
    private long turmaId;
    private String dataMatricula;
    private String situacao; // "ativa", "transferida", "expulsa", "desistente"

    // Construtor vazio
    public Matricula() {}

    // Construtor com parâmetros
    public Matricula(long alunoId, long turmaId, String dataMatricula, String situacao) {
        this.alunoId = alunoId;
        this.turmaId = turmaId;
        this.dataMatricula = dataMatricula;
        this.situacao = situacao;
    }

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getAlunoId() { return alunoId; }
    public void setAlunoId(long alunoId) { this.alunoId = alunoId; }

    public long getTurmaId() { return turmaId; }
    public void setTurmaId(long turmaId) { this.turmaId = turmaId; }

    public String getDataMatricula() { return dataMatricula; }
    public void setDataMatricula(String dataMatricula) { this.dataMatricula = dataMatricula; }

    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
}