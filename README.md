# 📚 Frequência Escolar - Android

Sistema de controle de frequência escolar para uso do professor, totalmente offline.

## Tecnologias
- **Linguagem:** Java
- **Banco de Dados:** SQLite com Room
- **UI:** XML + Material Design
- **Mínimo SDK:** API 24 (Android 7.0)

## Funcionalidades (implementadas)
- ✅ Estrutura do banco de dados com Room
- ✅ Entidades: Turma, Aluno, Matricula, Chamada, Presenca, MovimentacaoAluno
- ✅ DAOs (interfaces de acesso a dados) para todas as entidades
- ✅ AppDatabase (configuração principal do Room com Singleton)
- ✅ Repository (camada de acesso a dados com callbacks)

## Funcionalidades (planejadas)
- 🔜 CRUD de Turmas
- 🔜 CRUD de Alunos
- 🔜 Matrícula e Transferência (incluindo transferência de escola e expulsão)
- 🔜 Registro de Chamada com Geolocalização
- 🔜 Relatórios PDF e XLSX
- 🔜 Notificações Locais
- 🔜 Autenticação Biométrica

## Como executar
1. Clone o repositório
2. Abra no Android Studio
3. Conecte seu celular com Depuração USB ativada
4. Clique em Run (▶️)

## Versão Atual
`v0.4.0` - AppDatabase e Repository configurados

## Histórico de Versões
| Versão | Data | Descrição |
|--------|------|-----------|
| v0.1.0 | 08/06/2026 | Configuração inicial do projeto |
| v0.2.0 | 08/06/2026 | Criação das entidades Room |
| v0.3.0 | 08/06/2026 | Criação dos DAOs (interfaces de acesso) |
| v0.4.0 | 08/06/2026 | AppDatabase e Repository |