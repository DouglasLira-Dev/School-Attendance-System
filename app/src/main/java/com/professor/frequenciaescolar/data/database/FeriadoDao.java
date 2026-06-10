package com.professor.frequenciaescolar.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.professor.frequenciaescolar.data.entities.Feriado;

import java.util.List;

@Dao
public interface FeriadoDao {

    @Insert
    void insert(Feriado feriado);

    @Delete
    void delete(Feriado feriado);

    @Query("SELECT * FROM feriados ORDER BY data ASC")
    List<Feriado> getAllFeriados();

    @Query("SELECT * FROM feriados WHERE data = :data")
    Feriado getFeriadoByData(String data);

    @Query("DELETE FROM feriados WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM feriados WHERE data BETWEEN :inicio AND :fim")
    List<Feriado> getFeriadosNoPeriodo(String inicio, String fim);
}