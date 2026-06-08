# 📝 Changelog

Todas as mudanças notáveis neste projeto serão documentadas aqui.

## [v0.4.0] - 2026-06-08

### Adicionado
- `AppDatabase.java` - Classe principal de configuração do Room
- `FrequenciaRepository.java` - Camada de acesso a dados
- Singleton pattern para AppDatabase e Repository
- ExecutorService para operações em thread separada
- Interface `OnDataFetched<T>` para callbacks assíncronos
- Métodos completos para todas as operações de banco (Turma, Aluno, Matricula, Chamada, Presenca, Movimentacao)

### Estrutura de pastas finalizada
```
data/
├── entities/ (6 entidades)
├── database/ (6 DAOs + AppDatabase)
└── repository/ (FrequenciaRepository)
```

## [v0.3.0] - 2026-06-08

### Adicionado
- DAO para Turma (CRUD completo e consultas)
- DAO para Aluno (CRUD completo e busca por matrícula)
- DAO para Matricula (matrículas ativas e histórico)
- DAO para Chamada (busca por turma/data)
- DAO para Presenca (marcação em lote e totais de presentes/ausentes)
- DAO para MovimentacaoAluno (histórico de transferências e expulsões)

## [v0.2.0] - 2026-06-08

### Adicionado
- Entidade `Turma` (com anotações Room)
- Entidade `Aluno` (com anotações Room)
- Entidade `Matricula` (relação aluno-turma)
- Entidade `Chamada` (com geolocalização)
- Entidade `Presenca` (com justificativa de falta)
- Entidade `MovimentacaoAluno` (transferência e expulsão)

## [v0.1.0] - 2026-06-08

### Adicionado
- Estrutura inicial do projeto Android
- Gradle configurado com Room e Java 17
- AndroidManifest.xml com permissões
- MainActivity e layout básico
- Documentação inicial (README, CHANGELOG, .gitignore)

### Próximas versões planejadas
- v0.5.0: CRUD de turmas (UI)
- v0.6.0: CRUD de alunos e matrícula (UI)
- v0.7.0: Transferência e desligamento de alunos
- v0.8.0: Tela de chamada
- v0.9.0: Relatórios e estatísticas
- v1.0.0: Notificações e alertas
- v1.1.0: Autenticação e segurança
- v2.0.0: Versão final estável