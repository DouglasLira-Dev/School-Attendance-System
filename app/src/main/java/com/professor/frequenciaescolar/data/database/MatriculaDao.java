package com.professor.frequenciaescolar.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.professor.frequenciaescolar.data.entities.Matricula;

import java.util.List;

@Dao
public interface MatriculaDao {

    @Insert
    void insert(Matricula matricula);

    @Update
    void update(Matricula matricula);

    @Query("SELECT * FROM matriculas WHERE alunoId = :alunoId AND situacao = 'ativa'")
    Matricula getMatriculaAtivaByAluno(long alunoId);

    @Query("SELECT * FROM matriculas WHERE turmaId = :turmaId AND situacao = 'ativa'")
    List<Matricula> getAlunosMatriculadosNaTurma(long turmaId);

    @Query("UPDATE matriculas SET situacao = :novaSituacao WHERE alunoId = :alunoId AND situacao = 'ativa'")
    void desativarMatriculaAtiva(long alunoId, String novaSituacao);

    @Query("SELECT * FROM matriculas WHERE alunoId = :alunoId ORDER BY dataMatricula DESC")
    List<Matricula> getHistoricoMatriculasPorAluno(long alunoId);
}