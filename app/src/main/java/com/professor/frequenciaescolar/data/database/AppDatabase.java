package com.professor.frequenciaescolar.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Feriado;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.MovimentacaoAluno;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;

@Database(
        entities = {
                Turma.class,
                Aluno.class,
                Matricula.class,
                Chamada.class,
                Presenca.class,
                MovimentacaoAluno.class,
                Feriado.class
        },
        version = 2,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    // DAOs
    public abstract TurmaDao turmaDao();
    public abstract AlunoDao alunoDao();
    public abstract MatriculaDao matriculaDao();
    public abstract ChamadaDao chamadaDao();
    public abstract PresencaDao presencaDao();
    public abstract MovimentacaoDao movimentacaoDao();
    public abstract FeriadoDao feriadoDao();  // Adicionado

    // Singleton pattern
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "frequencia_escolar.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Método para fechar a conexão (útil em testes)
    public static void destroyInstance() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}