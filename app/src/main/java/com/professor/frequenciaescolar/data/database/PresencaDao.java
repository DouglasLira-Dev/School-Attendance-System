package com.professor.frequenciaescolar.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.professor.frequenciaescolar.data.entities.Presenca;

import java.util.List;

@Dao
public interface PresencaDao {

    @Insert
    void insert(Presenca presenca);

    @Insert
    void insertAll(List<Presenca> presencas);

    @Update
    void update(Presenca presenca);

    @Query("SELECT * FROM presencas WHERE chamadaId = :chamadaId")
    List<Presenca> getPresencasByChamada(long chamadaId);

    @Query("SELECT * FROM presencas WHERE alunoId = :alunoId AND chamadaId IN (SELECT id FROM chamadas WHERE data BETWEEN :dataInicio AND :dataFim)")
    List<Presenca> getPresencasAlunoPorPeriodo(long alunoId, String dataInicio, String dataFim);

    @Query("SELECT COUNT(*) FROM presencas WHERE chamadaId = :chamadaId AND presente = 1")
    int getTotalPresentesByChamada(long chamadaId);

    @Query("SELECT COUNT(*) FROM presencas WHERE chamadaId = :chamadaId AND presente = 0")
    int getTotalAusentesByChamada(long chamadaId);

    @Query("SELECT * FROM presencas WHERE chamadaId = :chamadaId AND alunoId = :alunoId")
    Presenca getPresencaByChamadaAndAluno(long chamadaId, long alunoId);
}