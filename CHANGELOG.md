# 📝 Changelog

Todas as mudanças notáveis neste projeto serão documentadas aqui.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/),
e este projeto adere ao [Semantic Versioning](https://semver.org/).

---

## [v3.2.0] - 2026-06-19

### Corrigido
- **Importação de alunos**: agora impede que um aluno seja matriculado em duas turmas diferentes simultaneamente
- Verificação de matrícula ativa antes de criar nova matrícula
- Mensagem de erro com nome da turma atual do aluno

### Adicionado
- Validação: aluno com matrícula ativa em outra turma é recusado na importação
- Mensagem de feedback: `"❌ Aluno já está matriculado em [Turma]"`

---

## [v3.1.0] - 2026-06-17

### Corrigido
- `TurmaListActivity`: `gerarRelatorioTurma` agora usa Repository (sem acesso direto ao banco)
- Variáveis em lambda usando arrays mutáveis para evitar erro de compilação
- `totalChamadas` movido para fora do loop no `gerarRelatorioTurma`
- `ChamadaActivity`: `carregarAlunos` com controle de concorrência

### Adicionado
- Método `getFeriadosNoPeriodo` no `FrequenciaRepository`
- Salvamento de estado na rotação de tela (`ChamadaActivity`)

---

## [v3.0.0] - 2026-06-17

### Adicionado
- Salvamento de estado da chamada na rotação de tela
- Proteção contra duplo clique ao salvar chamada
- Limite de tentativas de senha (5 tentativas, bloqueio de 30 segundos)
- Requisito mínimo de complexidade para senha (6 caracteres + letras/números)
- Reinício automático do app após restaurar backup
- Upload para Google Drive em thread de background
- Busca de alunos apenas ativos na verificação de matrícula

### Corrigido
- Condição de corrida no carregamento de alunos (`AlunoListActivity`)
- Acesso ao banco centralizado via Repository (`gerarRelatorioTurma`)
- Modelo CSV agora é salvo em pasta **Downloads** (acessível ao usuário)

### Melhorado
- Segurança do login com bloqueio por tentativas
- Experiência do usuário ao restaurar backup

---

## [v2.9.0] - 2026-06-16

### Adicionado
- Funcionalidade **"Esqueci a Senha"** — redefinição sem precisar da senha atual
- Link "Esqueci minha senha" abaixo do campo de senha no login
- Tela de redefinição simplificada (apenas nova senha e confirmação)

### Corrigido
- Menu **Transferência/Expulsão** agora abre `MatriculaActivity`
- Diálogo para selecionar aluno antes de transferir
- Uso de `alunosFiltrados` em vez de `alunos` (que estava vazio)

---

## [v2.8.0] - 2026-06-16

### Adicionado
- Tratamento de erros no Repository com `try/catch` e interface `OnError`
- Validação de **matrícula duplicada** no cadastro de aluno
- Validação de **turma duplicada** (nome + turno) no cadastro
- Confirmação ao sair sem salvar alterações (formulários)
- Fechamento de streams com `try-with-resources` na importação CSV
- Verificação de Activity antes de `runOnUiThread` na geração de relatório
- Exigência de ao menos **um aluno marcado** antes de salvar chamada

### Corrigido
- Remoção de coordenadas hardcoded (São Paulo) no `ChamadaActivity`
- Evita dupla query na abertura do `TurmaListActivity`
- Cálculo de frequência apenas da turma atual no `AlunoDetalheActivity`

---

## [v2.7.3] - 2026-06-16

### Corrigido
- Duplicação de alunos na lista (`AlunoListActivity`)
- Abas dos gráficos não abriam ao clicar
- Métodos de exportação vazios no `TurmaListActivity` (implementados completamente)

### Melhorado
- Exportação mostra caminho completo do arquivo salvo no Toast
- Compartilhamento de arquivos via Intent

---

## [v2.7.2] - 2026-06-16

### Corrigido
- `TurmaListActivity`: verificação de existência da turma ao clicar
- `TurmaListActivity`: método `isTablet()` conflitante com import incorreto
- `TurmaListActivity`: método `carregarAlunos()` removido (não pertencia à classe)
- `AlunoListActivity`: busca por nome e matrícula com filtro em tempo real
- `AlunoListActivity`: tratamento de erros no `onCreate`
- `ConfiguracoesActivity`: integração com "Gerenciar Feriados"
- Exportação: verificação de alunos antes de gerar relatório

### Adicionado
- Verificação de `null` para evitar crashes

---

## [v2.7.1] - 2026-06-16

### Corrigido
- Integração do botão "Gerenciar Feriados" com a tela de feriados
- Verificação de alunos antes de exportar relatório
- Crash ao clicar na turma após inserir aluno
- Tratamento de erros no `AlunoListActivity`

---

## [v2.7.0] - 2026-06-16

### Adicionado
- **Botão "Marcar Todos Presentes"** na tela de chamada
- **Botão "Desmarcar Todos"** na tela de chamada
- **Backup automático** com agendamento diário
- Switch para ativar/desativar backup automático
- `BackupReceiver` para execução agendada

### Removido
- Apache POI (incompatível com `minSdk 24`) — mantida exportação CSV

---

## [v2.6.0] - 2026-06-16

### Adicionado
- **Busca rápida por nome/matrícula** na lista de alunos
- Filtro em tempo real enquanto digita
- **Suporte a Tablet** (layout adaptativo em duas colunas)
- Detalhes da turma no painel direito no tablet
- Arquivos de configuração para tablet (`sw600dp`)

---

## [v2.5.0] - 2026-06-16

### Adicionado
- **Filtro por status do aluno** no relatório (Ativos, Transferidos, Expulsos)
- **Exportação PDF com dados reais**
- **Exportação Excel (XLSX) com dados reais**
- Compartilhamento dos arquivos gerados
- Permissão de armazenamento solicitada em tempo de execução

---

## [v2.4.0] - 2026-06-16

### Adicionado
- **Filtro por status do aluno** nos relatórios
- **Widget na tela inicial** (resumo de frequência)
- **Suporte a Tablet** (layout adaptativo)
- **Backup automático completo** (diário)

---

## [v2.3.0] - 2026-06-16

### Adicionado
- **Exportação real PDF/CSV** com dados da turma
- **Exportação PDF/CSV** da lista de alunos em risco
- **Backup automático para Google Drive** funcional
- Compartilhamento dos arquivos gerados

---

## [v2.2.0] - 2026-06-10

### Adicionado
- Exportação PDF com dados reais da turma
- Exportação CSV com dados reais da turma
- Compartilhamento dos arquivos gerados
- Verificação de feriados no cálculo de dias letivos

### Corrigido
- SwipeRefreshLayout nas listas

---

## [v2.1.1] - 2026-06-10

### Corrigido
- CheckBox sem `layout_height` no `activity_configuracoes`
- `AlunosRiscoActivity`: tipo errado das variáveis de layout
- `AlunoListActivity`: erro de sintaxe no lambda do SwipeRefreshLayout
- Menu popup com fundo escuro e texto branco

---

## [v2.1.0] - 2026-06-10

### Adicionado
- **Exportação de relatório da turma** (PDF/CSV)
- Diálogo com opções de formato e filtro de alunos
- **Swipe to Refresh** nas listas de turmas e alunos

---

## [v2.0.0] - 2026-06-10

### Adicionado
- **Lista de Alunos em Risco** (frequência < 75%)
- **Filtros avançados**: Dia Específico, Semana, Mês, Bimestre
- Cálculo de frequência considerando feriados
- Cores indicativas por nível de risco
- Cards com estatísticas (total alunos, em risco, média da turma)

---

## [v1.9.0] - 2026-06-10

### Adicionado
- **Entidade Feriado** e `FeriadoDao`
- **Tela GerenciarFeriadosActivity**
- Opção de feriado recorrente (todo ano)
- Métodos `isFeriado()` e `calcularDiasLetivos()` no `ConfiguracoesManager`
- Cálculo de dias letivos considera feriados

---

## [v1.8.0] - 2026-06-10

### Adicionado
- **Backup e Restore local** do banco de dados
- **Backup automático** com WorkManager
- **Integração com Google Drive**
- Tela `BackupRestoreActivity` com lista de backups
- Permissões de armazenamento

---

## [v1.7.0] - 2026-06-10

### Adicionado
- **Importação em massa de alunos via CSV**
- Leitura de CSV com colunas: Nome, Matrícula, Responsável, Telefone, Turma
- Preview dos dados antes da importação
- Validação de turmas existentes e matrículas duplicadas
- Barra de progresso durante importação
- Download de modelo CSV para exemplo

---

## [v1.6.2] - 2026-06-10

### Corrigido
- Campos **Responsável** e **Telefone** agora são **opcionais** no cadastro de aluno

---

## [v1.6.1] - 2026-06-10

### Adicionado
- Menu de gráficos no `AlunoDetalheActivity`

---

## [v1.6.0] - 2026-06-10

### Adicionado
- **Gráfico de Barras** (frequência da turma por mês)
- **Gráfico de Linha** (evolução do aluno)
- **Linha de 80%** (mínimo recomendado)
- Dependência MPAndroidChart com JitPack

---

## [v1.5.0] - 2026-06-10

### Adicionado
- **Exportação XLSX/CSV** do relatório do aluno
- Abas no Excel: Resumo, Histórico, Estatísticas
- Compartilhamento dos arquivos gerados

---

## [v1.4.0] - 2026-06-10

### Adicionado
- **Exportação PDF** do relatório do aluno
- Compartilhamento dos arquivos gerados
- Permissão de armazenamento

---

## [v1.3.1] - 2026-06-10

### Corrigido
- Método `getHorario()` renomeado para `getHorarioRegistro()` no `RelatorioAlunoActivity`

---

## [v1.3.0] - 2026-06-10

### Adicionado
- **Relatório por Aluno Individual**
- Histórico completo de chamadas
- Separação de faltas justificadas e não justificadas
- Filtro por período (data início/fim)
- Percentual considerando regras de configuração
- Indicadores visuais por status (verde/laranja/vermelho)

---

## [v1.2.0] - 2026-06-10

### Adicionado
- **Tela de Configurações**
    - Dias Letivos (checkboxes)
    - Período Letivo (data início/fim)
    - Horário do Lembrete
    - Desconsiderar faltas justificadas
- Menu para acessar configurações

---

## [v1.1.0] - 2026-06-08

### Adicionado
- **Editar Aluno**
- **Excluir Aluno** (com confirmação)
- **Excluir Turma** (com confirmação)
- Menu de contexto no clique longo
- Tela `AlunoDetalheActivity` com resumo de frequência

---

## [v1.0.1] - 2026-06-08

### Corrigido
- Carregamento assíncrono de alunos com `AtomicInteger`
- Método `updatePresencaByChamadaAndAluno` no Repository
- Localização padrão quando GPS não disponível
- Tratamento de permissão de localização

---

## [v1.0.0] - 2026-06-08

### Adicionado
- **Autenticação Biométrica** (impressão digital / Face ID)
- **Login com Senha** (primeiro acesso configura a senha)
- `LoginActivity` como tela inicial (`LAUNCHER`)
- `SenhaManager` com criptografia via AndroidKeyStore

---

## [v0.9.0] - 2026-06-08

### Adicionado
- **Notificações e Alertas**
    - Lembrete diário de chamada (horário configurável)
    - Verificação automática de faltas consecutivas
    - `NotificationHelper`, `NotificationScheduler`, `NotificationReceiver`
- Permissões `POST_NOTIFICATIONS` e `SCHEDULE_EXACT_ALARM`

---

## [v0.8.0] - 2026-06-08

### Adicionado
- **Relatórios e Estatísticas**
    - Dashboard com média da turma e total de aulas
    - Lista de alunos com frequência abaixo de 80%
    - Seleção de período (data início/fim)
    - Menu na tela principal para acesso aos relatórios

---

## [v0.7.0] - 2026-06-08

### Adicionado
- **Tela de Chamada**
    - Seleção de turma e data
    - Lista de alunos com switches (Presente/Ausente)
    - Campo de justificativa para faltas
    - Geolocalização (latitude/longitude)
    - Verificação de chamada duplicada (mesma turma/data)
    - Edição de chamada existente

---

## [v0.6.0] - 2026-06-08

### Adicionado
- **CRUD de Alunos** (UI)
- **Matrícula de Alunos**
- **Transferência** entre turmas
- **Expulsão** e **Desistência**
- Tela `AlunoListActivity` (listagem por turma)
- Tela `AlunoFormActivity` (cadastro/edição)
- Tela `MatriculaActivity` (transferência/expulsão/desistência)

---

## [v0.5.0] - 2026-06-08

### Adicionado
- **CRUD de Turmas** (UI)
    - Listagem, cadastro, edição e exclusão
- `TurmaListActivity`, `TurmaFormActivity`, `TurmaAdapter`
- Layouts: `activity_turma_list`, `item_turma`, `activity_turma_form`
- Menu para adicionar turma

---

## [v0.4.0] - 2026-06-08

### Adicionado
- `AppDatabase.java` — Configuração principal do Room (Singleton)
- `FrequenciaRepository.java` — Camada de acesso a dados
- `ExecutorService` para operações em thread separada
- Interface `OnDataFetched<T>` para callbacks assíncronos

---

## [v0.3.0] - 2026-06-08

### Adicionado
- **DAOs** (interfaces de acesso a dados)
    - `TurmaDao`, `AlunoDao`, `MatriculaDao`
    - `ChamadaDao`, `PresencaDao`, `MovimentacaoDao`
- CRUD completo para todas as entidades
- Consultas personalizadas (busca por matrícula, chamada por turma/data, etc.)

---

## [v0.2.0] - 2026-06-08

### Adicionado
- **Entidades Room**
    - `Turma`, `Aluno`, `Matricula`
    - `Chamada`, `Presenca`, `MovimentacaoAluno`
- Anotações Room (`@Entity`, `@PrimaryKey`, `@Ignore`)
- Construtores, getters e setters para todas as entidades

---

## [v0.1.0] - 2026-06-08

### Adicionado
- Estrutura inicial do projeto Android
- Gradle configurado com Room e Java 17
- `AndroidManifest.xml` com permissões (localização, notificações, biometria)
- `MainActivity` e layout básico
- Documentação inicial (README, CHANGELOG, `.gitignore`)
- Configuração do repositório GitHub