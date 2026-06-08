package com.professor.frequenciaescolar.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "turmas")
public class Turma {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String nome;
    private String turno;
    private int anoLetivo;
    private boolean ativo;

    public Turma() {}

    public Turma(String nome, String turno, int anoLetivo, boolean ativo){
        this.nome = nome;
        this.turno = turno;
        this.anoLetivo = anoLetivo;
        this.ativo = ativo;
    }

    // GETTERS E SETTERS
    public long getId(){return id;}
    public void setId(long id){this.id = id;}

    public String getNome(){return nome;}
    public void setNome(String nome){this.nome = nome;}

    public String getTurno(){return turno;}
    public void setTurno(String turno){this.turno = turno;}

    public int getAnoLetivo(){return anoLetivo;}
    public void setAnoLetivo(int anoLetivo) {this.anoLetivo = anoLetivo;}

    public boolean isAtivo(){return ativo;}
    public void setAtivo(boolean ativo){this.ativo = ativo;}
}
