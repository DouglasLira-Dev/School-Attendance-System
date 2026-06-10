package com.professor.frequenciaescolar.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "feriados")
public class Feriado {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String data; // formato yyyy-MM-dd
    private String nome;
    private boolean recorrente; // se é recorrente todo ano (ex: 25/12)

    public Feriado() {}

    public Feriado(String data, String nome, boolean recorrente) {
        this.data = data;
        this.nome = nome;
        this.recorrente = recorrente;
    }

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isRecorrente() { return recorrente; }
    public void setRecorrente(boolean recorrente) { this.recorrente = recorrente; }
}