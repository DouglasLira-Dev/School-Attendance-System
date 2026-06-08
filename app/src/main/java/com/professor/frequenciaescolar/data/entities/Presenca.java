package com.professor.frequenciaescolar.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "presencas")
public class Presenca {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long chamadaId;
    private long alunoId;
    private boolean presente;
    private String justificativa;

    // Construtor vazio
    public Presenca() {}

    // Construtor com parâmetros
    public Presenca(long chamadaId, long alunoId, boolean presente, String justificativa) {
        this.chamadaId = chamadaId;
        this.alunoId = alunoId;
        this.presente = presente;
        this.justificativa = justificativa;
    }

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getChamadaId() { return chamadaId; }
    public void setChamadaId(long chamadaId) { this.chamadaId = chamadaId; }

    public long getAlunoId() { return alunoId; }
    public void setAlunoId(long alunoId) { this.alunoId = alunoId; }

    public boolean isPresente() { return presente; }
    public void setPresente(boolean presente) { this.presente = presente; }

    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
}