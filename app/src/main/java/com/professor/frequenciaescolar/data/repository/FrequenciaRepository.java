package com.professor.frequenciaescolar.data.repository;

import android.content.Context;

import com.professor.frequenciaescolar.data.database.AppDatabase;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Feriado;
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

    // ==================== MÉTODO AUXILIAR PARA TRATAMENTO DE ERROS ====================
    private void executar(Runnable acao, OnError onError) {
        executorService.execute(() -> {
            try {
                acao.run();
            } catch (Exception e) {
                android.util.Log.e("Repository", "Erro no banco de dados", e);
                if (onError != null) onError.onError(e.getMessage());
            }
        });
    }

    public static synchronized FrequenciaRepository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new FrequenciaRepository(context);
        }
        return INSTANCE;
    }

    // ==================== TURMA ====================

    public void insertTurma(Turma turma, Runnable onSuccess) {
        executar(() -> {
            database.turmaDao().insert(turma);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao inserir turma: " + erro));
    }

    public void updateTurma(Turma turma, Runnable onSuccess) {
        executar(() -> {
            database.turmaDao().update(turma);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao atualizar turma: " + erro));
    }

    public void deleteTurma(Turma turma, Runnable onSuccess) {
        executar(() -> {
            database.turmaDao().delete(turma);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao deletar turma: " + erro));
    }

    public void desativarTurma(long id, Runnable onSuccess) {
        executar(() -> {
            database.turmaDao().desativarTurma(id);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao desativar turma: " + erro));
    }

    public void getAllTurmasAtivas(OnDataFetched<List<Turma>> callback) {
        executar(() -> {
            List<Turma> turmas = database.turmaDao().getAllTurmasAtivas();
            if (callback != null) callback.onDataFetched(turmas);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar turmas ativas: " + erro));
    }

    public void getTurmaById(long id, OnDataFetched<Turma> callback) {
        executar(() -> {
            Turma turma = database.turmaDao().getTurmaById(id);
            if (callback != null) callback.onDataFetched(turma);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar turma por ID: " + erro));
    }

    // ==================== ALUNO ====================

    public void insertAluno(Aluno aluno, Runnable onSuccess) {
        executar(() -> {
            database.alunoDao().insert(aluno);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao inserir aluno: " + erro));
    }

    public void updateAluno(Aluno aluno, Runnable onSuccess) {
        executar(() -> {
            database.alunoDao().update(aluno);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao atualizar aluno: " + erro));
    }

    public void deleteAluno(Aluno aluno, Runnable onSuccess) {
        executar(() -> {
            database.alunoDao().delete(aluno);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao deletar aluno: " + erro));
    }

    public void desativarAluno(long id, String status, Runnable onSuccess) {
        executar(() -> {
            database.alunoDao().desativarAluno(id, status);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao desativar aluno: " + erro));
    }

    public void getAllAlunosAtivos(OnDataFetched<List<Aluno>> callback) {
        executar(() -> {
            List<Aluno> alunos = database.alunoDao().getAllAlunosAtivos();
            if (callback != null) callback.onDataFetched(alunos);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar alunos ativos: " + erro));
    }

    public void getAlunoById(long id, OnDataFetched<Aluno> callback) {
        executar(() -> {
            Aluno aluno = database.alunoDao().getAlunoById(id);
            if (callback != null) callback.onDataFetched(aluno);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar aluno por ID: " + erro));
    }

    public void getAlunoByMatricula(String matricula, OnDataFetched<Aluno> callback) {
        executar(() -> {
            Aluno aluno = database.alunoDao().getAlunoByMatricula(matricula);
            if (callback != null) callback.onDataFetched(aluno);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar aluno por matrícula: " + erro));
    }

    // ==================== MATRICULA ====================

    public void insertMatricula(Matricula matricula, Runnable onSuccess) {
        executar(() -> {
            database.matriculaDao().insert(matricula);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao inserir matrícula: " + erro));
    }

    public void getMatriculaAtivaByAluno(long alunoId, OnDataFetched<Matricula> callback) {
        executar(() -> {
            Matricula matricula = database.matriculaDao().getMatriculaAtivaByAluno(alunoId);
            if (callback != null) callback.onDataFetched(matricula);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar matrícula ativa: " + erro));
    }

    public void getAlunosMatriculadosNaTurma(long turmaId, OnDataFetched<List<Matricula>> callback) {
        executar(() -> {
            List<Matricula> matriculas = database.matriculaDao().getAlunosMatriculadosNaTurma(turmaId);
            if (callback != null) callback.onDataFetched(matriculas);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar alunos matriculados: " + erro));
    }

    public void desativarMatriculaAtiva(long alunoId, String novaSituacao, Runnable onSuccess) {
        executar(() -> {
            database.matriculaDao().desativarMatriculaAtiva(alunoId, novaSituacao);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao desativar matrícula: " + erro));
    }

    public void getHistoricoMatriculasPorAluno(long alunoId, OnDataFetched<List<Matricula>> callback) {
        executar(() -> {
            List<Matricula> matriculas = database.matriculaDao().getHistoricoMatriculasPorAluno(alunoId);
            if (callback != null) callback.onDataFetched(matriculas);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar histórico de matrículas: " + erro));
    }

    // ==================== CHAMADA ====================

    public void insertChamada(Chamada chamada, OnDataFetched<Long> callback) {
        executar(() -> {
            long id = database.chamadaDao().insert(chamada);
            if (callback != null) callback.onDataFetched(id);
        }, erro -> android.util.Log.e("Repository", "Erro ao inserir chamada: " + erro));
    }

    public void updateChamada(Chamada chamada, Runnable onSuccess) {
        executar(() -> {
            database.chamadaDao().update(chamada);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao atualizar chamada: " + erro));
    }

    public void getChamadaByTurmaAndData(long turmaId, String data, OnDataFetched<Chamada> callback) {
        executar(() -> {
            Chamada chamada = database.chamadaDao().getChamadaByTurmaAndData(turmaId, data);
            if (callback != null) callback.onDataFetched(chamada);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar chamada por turma/data: " + erro));
    }

    public void getChamadasPorTurma(long turmaId, OnDataFetched<List<Chamada>> callback) {
        executar(() -> {
            List<Chamada> chamadas = database.chamadaDao().getChamadasPorTurma(turmaId);
            if (callback != null) callback.onDataFetched(chamadas);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar chamadas por turma: " + erro));
    }

    // ==================== PRESENCA ====================

    public void insertPresenca(Presenca presenca, Runnable onSuccess) {
        executar(() -> {
            database.presencaDao().insert(presenca);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao inserir presença: " + erro));
    }

    public void insertAllPresencas(List<Presenca> presencas, Runnable onSuccess) {
        executar(() -> {
            database.presencaDao().insertAll(presencas);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao inserir múltiplas presenças: " + erro));
    }

    public void getPresencasByChamada(long chamadaId, OnDataFetched<List<Presenca>> callback) {
        executar(() -> {
            List<Presenca> presencas = database.presencaDao().getPresencasByChamada(chamadaId);
            if (callback != null) callback.onDataFetched(presencas);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar presenças: " + erro));
    }

    public void getTotalPresentesByChamada(long chamadaId, OnDataFetched<Integer> callback) {
        executar(() -> {
            int total = database.presencaDao().getTotalPresentesByChamada(chamadaId);
            if (callback != null) callback.onDataFetched(total);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar total de presentes: " + erro));
    }

    public void getTotalAusentesByChamada(long chamadaId, OnDataFetched<Integer> callback) {
        executar(() -> {
            int total = database.presencaDao().getTotalAusentesByChamada(chamadaId);
            if (callback != null) callback.onDataFetched(total);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar total de ausentes: " + erro));
    }

    // ==================== MOVIMENTACAO ====================

    public void insertMovimentacao(MovimentacaoAluno movimentacao, Runnable onSuccess) {
        executar(() -> {
            database.movimentacaoDao().insert(movimentacao);
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao inserir movimentação: " + erro));
    }

    public void getMovimentacoesByAluno(long alunoId, OnDataFetched<List<MovimentacaoAluno>> callback) {
        executar(() -> {
            List<MovimentacaoAluno> movimentacoes = database.movimentacaoDao().getMovimentacoesByAluno(alunoId);
            if (callback != null) callback.onDataFetched(movimentacoes);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar movimentações: " + erro));
    }

    // ==================== MÉTODOS ADICIONAIS PARA CHAMADA ====================

    public void updatePresencaByChamadaAndAluno(long chamadaId, long alunoId, boolean presente, String justificativa, Runnable onSuccess) {
        executar(() -> {
            Presenca presenca = database.presencaDao().getPresencaByChamadaAndAluno(chamadaId, alunoId);
            if (presenca != null) {
                presenca.setPresente(presente);
                presenca.setJustificativa(justificativa != null ? justificativa : "");
                database.presencaDao().update(presenca);
            }
            if (onSuccess != null) onSuccess.run();
        }, erro -> android.util.Log.e("Repository", "Erro ao atualizar presença por chamada/aluno: " + erro));
    }

    public void getPresencaByChamadaAndAluno(long chamadaId, long alunoId, OnDataFetched<Presenca> callback) {
        executar(() -> {
            Presenca presenca = database.presencaDao().getPresencaByChamadaAndAluno(chamadaId, alunoId);
            if (callback != null) callback.onDataFetched(presenca);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar presença por chamada/aluno: " + erro));
    }

    // ==================== MÉTODOS ADICIONAIS PARA RELATÓRIO ===================

    public void getChamadasPorPeriodo(String dataInicio, String dataFim, OnDataFetched<List<Chamada>> callback) {
        executar(() -> {
            List<Chamada> chamadas = database.chamadaDao().getChamadasPorPeriodo(dataInicio, dataFim);
            if (callback != null) callback.onDataFetched(chamadas);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar chamadas por período: " + erro));
    }

    // ==================== NOVO MÉTODO PARA VALIDAR TURMA DUPLICADA ====================

    public void getTurmaPorNomeETurno(String nome, String turno, OnDataFetched<Turma> callback) {
        executorService.execute(() -> {
            Turma turma = database.turmaDao().getTurmaPorNomeETurno(nome, turno);
            if (callback != null) callback.onDataFetched(turma);
        });
    }
    // ==================== FERIADOS ====================
    public void getFeriadosNoPeriodo(String dataInicio, String dataFim, OnDataFetched<List<Feriado>> callback) {
        executar(() -> {
            List<Feriado> feriados = database.feriadoDao().getFeriadosNoPeriodo(dataInicio, dataFim);
            if (callback != null) callback.onDataFetched(feriados);
        }, erro -> android.util.Log.e("Repository", "Erro ao buscar feriados no período: " + erro));
    }
    // ==================== INSERIR ALUNO E RETORNAR ID ====================
    public void insertAlunoAndGetId(Aluno aluno, OnDataFetched<Long> callback) {
        executorService.execute(() -> {
            try {
                long id = database.alunoDao().insert(aluno);
                if (callback != null) {
                    callback.onDataFetched(id);
                }
            } catch (Exception e) {
                android.util.Log.e("Repository", "Erro ao inserir aluno", e);
                if (callback != null) {
                    callback.onDataFetched(-1L);
                }
            }
        });
    }
    // ==================== CALLBACK INTERFACE ====================
    public interface OnDataFetched<T> {
        void onDataFetched(T data);
    }

    public interface OnError {
        void onError(String mensagem);
    }
}