package com.professor.frequenciaescolar.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.professor.frequenciaescolar.data.entities.MovimentacaoAluno;

import java.util.List;

@Dao
public interface MovimentacaoDao {

    @Insert
    void insert(MovimentacaoAluno movimentacao);

    @Query("SELECT * FROM movimentacoes_aluno WHERE alunoId = :alunoId ORDER BY dataMovimentacao DESC")
    List<MovimentacaoAluno> getMovimentacoesByAluno(long alunoId);

    @Query("SELECT * FROM movimentacoes_aluno WHERE tipo = :tipo ORDER BY dataMovimentacao DESC")
    List<MovimentacaoAluno> getMovimentacoesByTipo(String tipo);

    @Query("SELECT * FROM movimentacoes_aluno WHERE alunoId = :alunoId AND tipo = :tipo")
    List<MovimentacaoAluno> getMovimentacoesByAlunoAndTipo(long alunoId, String tipo);
}