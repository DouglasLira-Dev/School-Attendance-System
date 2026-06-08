package com.professor.frequenciaescolar.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.professor.frequenciaescolar.data.entities.Chamada;

import java.util.List;

@Dao
public interface ChamadaDao {

    @Insert
    long insert(Chamada chamada);

    @Update
    void update(Chamada chamada);

    @Query("SELECT * FROM chamadas WHERE turmaId = :turmaId AND data = :data")
    Chamada getChamadaByTurmaAndData(long turmaId, String data);

    @Query("SELECT * FROM chamadas WHERE turmaId = :turmaId ORDER BY data DESC")
    List<Chamada> getChamadasPorTurma(long turmaId);

    @Query("SELECT * FROM chamadas WHERE id = :id")
    Chamada getChamadaById(long id);

    @Query("SELECT * FROM chamadas WHERE data BETWEEN :dataInicio AND :dataFim")
    List<Chamada> getChamadasPorPeriodo(String dataInicio, String dataFim);
}