package com.professor.frequenciaescolar.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chamadas")
public class Chamada {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long turmaId;
    private String data;
    private String horarioRegistro;
    private double latitude;
    private double longitude;
    private String metodo; // "manual", "qrcode"

    // Construtor vazio
    public Chamada() {}

    // Construtor com parâmetros
    public Chamada(long turmaId, String data, String horarioRegistro, double latitude, double longitude, String metodo) {
        this.turmaId = turmaId;
        this.data = data;
        this.horarioRegistro = horarioRegistro;
        this.latitude = latitude;
        this.longitude = longitude;
        this.metodo = metodo;
    }

    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTurmaId() { return turmaId; }
    public void setTurmaId(long turmaId) { this.turmaId = turmaId; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getHorarioRegistro() { return horarioRegistro; }
    public void setHorarioRegistro(String horarioRegistro) { this.horarioRegistro = horarioRegistro; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
}