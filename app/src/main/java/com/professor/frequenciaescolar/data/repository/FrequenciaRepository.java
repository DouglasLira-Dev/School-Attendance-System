package com.professor.frequenciaescolar.data.repository;

import android.content.Context;

import com.professor.frequenciaescolar.data.database.AppDatabase;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.MovimentacaoAluno;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrequenciaRepository {

    // DAOs
    private final AppDatabase database;
    private final ExecutorService executorService;

    // Singleton
    private static FrequenciaRepository INSTANCE;

    private FrequenciaRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized FrequenciaRepository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new FrequenciaRepository(context);
        }
        return INSTANCE;
    }

    // ==================== TURMA ====================

    public void insertTurma(Turma turma, Runnable onSuccess) {
        executorService.execute(() -> {
            database.turmaDao().insert(turma);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void updateTurma(Turma turma, Runnable onSuccess) {
        executorService.execute(() -> {
            database.turmaDao().update(turma);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void deleteTurma(Turma turma, Runnable onSuccess) {
        executorService.execute(() -> {
            database.turmaDao().delete(turma);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void desativarTurma(long id, Runnable onSuccess) {
        executorService.execute(() -> {
            database.turmaDao().desativarTurma(id);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void getAllTurmasAtivas(OnDataFetched<List<Turma>> callback) {
        executorService.execute(() -> {
            List<Turma> turmas = database.turmaDao().getAllTurmasAtivas();
            if (callback != null) callback.onDataFetched(turmas);
        });
    }

    public void getTurmaById(long id, OnDataFetched<Turma> callback) {
        executorService.execute(() -> {
            Turma turma = database.turmaDao().getTurmaById(id);
            if (callback != null) callback.onDataFetched(turma);
        });
    }

    // ==================== ALUNO ====================

    public void insertAluno(Aluno aluno, Runnable onSuccess) {
        executorService.execute(() -> {
            database.alunoDao().insert(aluno);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void updateAluno(Aluno aluno, Runnable onSuccess) {
        executorService.execute(() -> {
            database.alunoDao().update(aluno);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void deleteAluno(Aluno aluno, Runnable onSuccess) {
        executorService.execute(() -> {
            database.alunoDao().delete(aluno);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void desativarAluno(long id, String status, Runnable onSuccess) {
        executorService.execute(() -> {
            database.alunoDao().desativarAluno(id, status);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void getAllAlunosAtivos(OnDataFetched<List<Aluno>> callback) {
        executorService.execute(() -> {
            List<Aluno> alunos = database.alunoDao().getAllAlunosAtivos();
            if (callback != null) callback.onDataFetched(alunos);
        });
    }

    public void getAlunoById(long id, OnDataFetched<Aluno> callback) {
        executorService.execute(() -> {
            Aluno aluno = database.alunoDao().getAlunoById(id);
            if (callback != null) callback.onDataFetched(aluno);
        });
    }

    public void getAlunoByMatricula(String matricula, OnDataFetched<Aluno> callback) {
        executorService.execute(() -> {
            Aluno aluno = database.alunoDao().getAlunoByMatricula(matricula);
            if (callback != null) callback.onDataFetched(aluno);
        });
    }

    // ==================== MATRICULA ====================

    public void insertMatricula(Matricula matricula, Runnable onSuccess) {
        executorService.execute(() -> {
            database.matriculaDao().insert(matricula);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void getMatriculaAtivaByAluno(long alunoId, OnDataFetched<Matricula> callback) {
        executorService.execute(() -> {
            Matricula matricula = database.matriculaDao().getMatriculaAtivaByAluno(alunoId);
            if (callback != null) callback.onDataFetched(matricula);
        });
    }

    public void getAlunosMatriculadosNaTurma(long turmaId, OnDataFetched<List<Matricula>> callback) {
        executorService.execute(() -> {
            List<Matricula> matriculas = database.matriculaDao().getAlunosMatriculadosNaTurma(turmaId);
            if (callback != null) callback.onDataFetched(matriculas);
        });
    }

    public void desativarMatriculaAtiva(long alunoId, String novaSituacao, Runnable onSuccess) {
        executorService.execute(() -> {
            database.matriculaDao().desativarMatriculaAtiva(alunoId, novaSituacao);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void getHistoricoMatriculasPorAluno(long alunoId, OnDataFetched<List<Matricula>> callback) {
        executorService.execute(() -> {
            List<Matricula> matriculas = database.matriculaDao().getHistoricoMatriculasPorAluno(alunoId);
            if (callback != null) callback.onDataFetched(matriculas);
        });
    }

    // ==================== CHAMADA ====================

    public void insertChamada(Chamada chamada, OnDataFetched<Long> callback) {
        executorService.execute(() -> {
            long id = database.chamadaDao().insert(chamada);
            if (callback != null) callback.onDataFetched(id);
        });
    }

    public void updateChamada(Chamada chamada, Runnable onSuccess) {
        executorService.execute(() -> {
            database.chamadaDao().update(chamada);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void getChamadaByTurmaAndData(long turmaId, String data, OnDataFetched<Chamada> callback) {
        executorService.execute(() -> {
            Chamada chamada = database.chamadaDao().getChamadaByTurmaAndData(turmaId, data);
            if (callback != null) callback.onDataFetched(chamada);
        });
    }

    public void getChamadasPorTurma(long turmaId, OnDataFetched<List<Chamada>> callback) {
        executorService.execute(() -> {
            List<Chamada> chamadas = database.chamadaDao().getChamadasPorTurma(turmaId);
            if (callback != null) callback.onDataFetched(chamadas);
        });
    }

    // ==================== PRESENCA ====================

    public void insertPresenca(Presenca presenca, Runnable onSuccess) {
        executorService.execute(() -> {
            database.presencaDao().insert(presenca);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void insertAllPresencas(List<Presenca> presencas, Runnable onSuccess) {
        executorService.execute(() -> {
            database.presencaDao().insertAll(presencas);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void getPresencasByChamada(long chamadaId, OnDataFetched<List<Presenca>> callback) {
        executorService.execute(() -> {
            List<Presenca> presencas = database.presencaDao().getPresencasByChamada(chamadaId);
            if (callback != null) callback.onDataFetched(presencas);
        });
    }

    public void getTotalPresentesByChamada(long chamadaId, OnDataFetched<Integer> callback) {
        executorService.execute(() -> {
            int total = database.presencaDao().getTotalPresentesByChamada(chamadaId);
            if (callback != null) callback.onDataFetched(total);
        });
    }

    public void getTotalAusentesByChamada(long chamadaId, OnDataFetched<Integer> callback) {
        executorService.execute(() -> {
            int total = database.presencaDao().getTotalAusentesByChamada(chamadaId);
            if (callback != null) callback.onDataFetched(total);
        });
    }

    // ==================== MOVIMENTACAO ====================

    public void insertMovimentacao(MovimentacaoAluno movimentacao, Runnable onSuccess) {
        executorService.execute(() -> {
            database.movimentacaoDao().insert(movimentacao);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void getMovimentacoesByAluno(long alunoId, OnDataFetched<List<MovimentacaoAluno>> callback) {
        executorService.execute(() -> {
            List<MovimentacaoAluno> movimentacoes = database.movimentacaoDao().getMovimentacoesByAluno(alunoId);
            if (callback != null) callback.onDataFetched(movimentacoes);
        });
    }

    // ==================== MÉTODOS ADICIONAIS PARA CHAMADA ====================

    public void updatePresenca(Presenca presenca, Runnable onSuccess) {
        executorService.execute(() -> {
            database.presencaDao().update(presenca);
            if (onSuccess != null) onSuccess.run();
        });
    }

    public void getPresencaByChamadaAndAluno(long chamadaId, long alunoId, OnDataFetched<Presenca> callback) {
        executorService.execute(() -> {
            Presenca presenca = database.presencaDao().getPresencaByChamadaAndAluno(chamadaId, alunoId);
            if (callback != null) callback.onDataFetched(presenca);
        });
    }

    // ==================== CALLBACK INTERFACE ====================

    public interface OnDataFetched<T> {
        void onDataFetched(T data);
    }
}