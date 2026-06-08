package com.professor.frequenciaescolar.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alunos")
public class Aluno {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String nome;
    private String matricula;
    private String responsavel;
    private String telefone;
    private String status;
    private boolean ativo;

    public Aluno() {}

    public Aluno(String nome, String matricula, String responsavel, String telefone, String status, boolean ativo) {
        this.nome = nome;
        this.matricula = matricula;
        this.responsavel = responsavel;
        this.telefone = telefone;
        this.status = status;
        this.ativo = ativo;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}