package com.professor.frequenciaescolar.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.professor.frequenciaescolar.data.entities.Turma;

import java.util.List;

@Dao
public interface TurmaDao {

    @Insert
    void insert(Turma turma);

    @Update
    void update(Turma turma);

    @Delete
    void delete(Turma turma);

    @Query("SELECT * FROM turmas WHERE ativo = 1 ORDER BY nome ASC")
    List<Turma> getAllTurmasAtivas();

    @Query("SELECT * FROM turmas WHERE id = :id")
    Turma getTurmaById(long id);

    @Query("SELECT * FROM turmas WHERE anoLetivo = :anoLetivo ORDER BY nome ASC")
    List<Turma> getTurmasByAnoLetivo(int anoLetivo);

    @Query("UPDATE turmas SET ativo = 0 WHERE id = :id")
    void desativarTurma(long id);
    @Query("SELECT * FROM turmas WHERE nome = :nome AND turno = :turno AND ativo = 1 LIMIT 1")
    Turma getTurmaPorNomeETurno(String nome, String turno);
}