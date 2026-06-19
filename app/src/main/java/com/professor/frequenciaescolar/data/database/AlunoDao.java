package com.professor.frequenciaescolar.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.professor.frequenciaescolar.data.entities.Aluno;

import java.util.List;

@Dao
public interface AlunoDao {

    @Insert
    long insert(Aluno aluno);  // Deve retornar long, não void

    @Update
    void update(Aluno aluno);

    @Delete
    void delete(Aluno aluno);

    @Query("SELECT * FROM alunos WHERE ativo = 1 ORDER BY nome ASC")
    List<Aluno> getAllAlunosAtivos();

    @Query("SELECT * FROM alunos WHERE id = :id")
    Aluno getAlunoById(long id);

    @Query("SELECT * FROM alunos WHERE matricula = :matricula")
    Aluno getAlunoByMatricula(String matricula);

    @Query("UPDATE alunos SET ativo = 0, status = :status WHERE id = :id")
    void desativarAluno(long id, String status);

    @Query("SELECT * FROM alunos WHERE status = :status")
    List<Aluno> getAlunosByStatus(String status);
}