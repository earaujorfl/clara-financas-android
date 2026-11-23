# Continuidade do projeto — Clara Finanças

> **Propósito deste arquivo:** permitir que uma nova conversa do ChatGPT ou outro desenvolvedor retome o trabalho sem depender do histórico do chat. Antes de agir, confira o GitHub e o estado local do Android Studio, pois o código pode ter avançado desde esta anotação.
>
> **Última atualização:** 23/11/2026. **Fonte:** arquivos conferidos diretamente na branch `feature/orcamentos-mensais` do GitHub, mais a validação local informada no desenvolvimento.

## 1. Identidade e links

- **Nome:** Clara Finanças.
- **Objetivo:** aplicativo Android nativo para gestão pessoal de receitas, despesas e planejamento de gastos, usado também como projeto de portfólio.
- **Repositório:** https://github.com/earaujorfl/clara-financas-android
- **Branch estável:** `main` — versão 0.3.0, com busca/filtros e categorias personalizadas.
- **Branch atual de desenvolvimento:** `feature/orcamentos-mensais`.
- **Commit que implementou os orçamentos:** `674f55bda718e10c0ad9d49d42329db6bfca3aec` (`feat: adiciona orcamentos mensais por categoria`).
- **Próxima versão planejada:** 0.4.0, **ainda não publicada/ajustada no Gradle**. O arquivo `app/build.gradle.kts` da branch continua com `versionCode = 3` e `versionName = "0.3.0"`.

## 2. Ambiente e forma de colaboração

- Desenvolvimento local em **Windows** com **Android Studio**, usando emulador Android.
- O projeto já foi clonado e executado com sucesso. Evitar colocar a pasta dentro de caminhos do OneDrive com caracteres acentuados; um caminho usado foi `C:\Projetos\ClaraFinancas`.
- Gradle 8.13, AGP 8.13.2, compilação Kotlin/Java JVM target 17; no Android Studio foi necessário selecionar JDK 21 para a JVM do Gradle em uma instalação.
- Android SDK 36, mínimo Android API 26.
- O usuário prefere orientação **passo a passo no Android Studio**, arquivo por arquivo, com verificações por capturas de tela. **Não alterar a branch `main` diretamente** nem fazer commits surpresa.
- Mudanças feitas diretamente pelo assistente no GitHub devem ser comunicadas; após isso o computador local precisa atualizar a branch via **Git → Pull** (ou `git pull`), preferencialmente com a árvore de trabalho limpa.
- Os arquivos `gradlew`, `gradlew.bat` e `gradle-wrapper.jar` **não estão versionados** neste estado; por isso `.\gradlew.bat` não funciona no PowerShell. Para testes, usar a janela Gradle do Android Studio e a tarefa `:app:testDebugUnitTest`. A CI utiliza Gradle instalado diretamente.
- **Não confundir código com dados de uso:** GitHub preserva arquivos versionados, mas não o banco Room de um emulador/dispositivo; desinstalar o aplicativo pode apagar os lançamentos. Ainda falta exportação/backup de dados.

## 3. Tecnologias e arquitetura

- Kotlin, Jetpack Compose, Material 3.
- MVVM: **UI Compose → FinanceViewModel → FinanceRepository → DAOs Room/SQLite**.
- Kotlin Coroutines, `Flow` e `StateFlow`.
- Dados monetários armazenados em **centavos (`Long`)**, com conversão BRL em `domain/Money.kt`.
- Persistência local/offline, sem autenticação ou backend nesta versão.
- Testes unitários JUnit; pipeline GitHub Actions em `.github/workflows/android.yml`. A CI é disparada em push para `main` ou PR direcionado à `main`, não necessariamente em push isolado na branch de feature.
- Documentação adicional: `README.md`, `docs/ARQUITETURA.md`, `docs/ROADMAP.md`.

## 4. O que está implementado

**Em `main` (base 0.3.0):**
- Cadastro, edição e exclusão de receitas e despesas, inclusive escolha de data.
- Resumo financeiro por mês, saldo e despesas por categoria.
- Busca de lançamentos, filtros por tipo/categoria e categorias personalizadas.
- Room v2 e migração `MIGRATION_1_2`.

**Na branch `feature/orcamentos-mensais` (além do que existe em `main`):**
- Nova aba **Orçamentos** no `ClaraApp.kt`.
- Cadastro, edição do valor e exclusão de limites mensais por categoria de despesa em `ui/BudgetScreen.kt`.
- Exibição de valor gasto, valor disponível, percentual e barra de progresso; destaque de limite ultrapassado.
- `data/BudgetEntity.kt`: tabela `monthly_budgets` com chave composta `yearMonth` + `category`; limite em `limitCents`.
- `data/BudgetDao.kt`: observação por mês, `save` (REPLACE), `delete`.
- `data/FinanceDatabase.kt`: Room **v3**, preservando `MIGRATION_1_2` e acrescentando `MIGRATION_2_3`. `MainActivity.kt` registra ambas as migrações e passa `budgetDao()` ao repositório.
- `data/FinanceRepository.kt`: `budgetsForMonth`, `saveBudget`, `deleteBudget`.
- `ui/FinanceViewModel.kt`: `observeBudgetProgress(month)` combina orçamentos e lançamentos; funções de gravação e exclusão.
- `BudgetCalculator` calcula despesas da categoria no mês do orçamento e deriva `spentCents`, `remainingCents`, `percentageUsed`, `progress`, `isExceeded`.
- `app/src/test/java/com/portfolio/clara/data/FinanceRepositoryTest.kt` atualizado com `FakeBudgetDao` e teste de criação/atualização/exclusão por mês.

**Atenção à organização:** no commit `674f55b`, o arquivo `BudgetCalculator.kt` está fisicamente em `app/src/main/java/com/portfolio/clara/data/BudgetCalculator.kt`, porém declara o pacote `com.portfolio.clara.domain`. Compila, mas convém movê-lo para `domain/BudgetCalculator.kt` numa futura limpeza de código (mantendo a declaração `package`).

## 5. Estado das validações

- O usuário executou a nova tela de Orçamentos no emulador e confirmou que foi possível cadastrar um limite.
- Na captura do Android Studio de 23/11/2026, a tarefa `:app:testDebugUnitTest` mostrou **14 de 14 testes passando** e `BUILD SUCCESSFUL` localmente.
- O commit `674f55b` foi enviado com sucesso ao GitHub na branch `feature/orcamentos-mensais`.
- **Ainda não foi confirmada uma execução de CI específica dessa feature em pull request** nem testes instrumentados/automáticos da migração Room 2→3 em dispositivo com dados preexistentes.
- Atenção: o arquivo `README.md` e o `docs/ROADMAP.md` na branch ainda descrevem majoritariamente a v0.3 e precisam ser atualizados ao preparar a v0.4.

## 6. Próximas tarefas recomendadas (ordem)

1. Criar **`BudgetCalculatorTest.kt`** em `app/src/test/java/com/portfolio/clara/domain/` com testes para despesas no mês certo, categorias distintas, receitas ignoradas, progresso em 0%, 100% e acima do limite.
2. Melhorar organização: mover `BudgetCalculator.kt` da pasta física `data` para `domain`, sem mudar seu pacote.
3. Verificar manualmente edição/exclusão e o comportamento após trocar o mês, alterar uma despesa ou ultrapassar o limite.
4. Criar teste de migração Room **2→3** e verificar preservação de dados; manter a atualização não destrutiva.
5. Corrigir pequenos ajustes: considerar ignorar `.kotlin/` e arquivos `errors-*.log` no `.gitignore` (um log apareceu como arquivo não versionado no Android Studio; **não incluir logs no Git**).
6. Atualizar `versionCode/versionName` para v0.4.0 **quando a release estiver pronta**; atualizar README, ROADMAP, ARQUITETURA e capturas de tela, se disponíveis.
7. Abrir pull request de `feature/orcamentos-mensais` para `main`, acompanhar os testes da CI, revisar e então integrar. Evitar merge automático sem conferência.
8. Depois: backup/exportação de dados de usuário (CSV etc.), testes instrumentados, outras funcionalidades do roadmap.

## 7. Procedimento para retomar em outro computador ou chat

**No computador novo:**
1. Instalar Android Studio + SDK, autenticar-se no GitHub e clonar o repositório.
2. Selecionar a branch `feature/orcamentos-mensais` enquanto ela for a branch ativa; depois usar a branch correspondente à nova etapa.
3. Sincronizar o Gradle e executar o app em emulador/dispositivo.
4. Conferir alterações locais com Git antes de iniciar; não presumir que mudanças não commitadas foram enviadas ao GitHub.

**Na próxima conversa do ChatGPT, enviar:**

> Estamos desenvolvendo o app Android Clara Finanças. Repositório: https://github.com/earaujorfl/clara-financas-android. Leia `docs/CONTINUIDADE.md` na branch `feature/orcamentos-mensais` (ou na branch de trabalho mais recente), confira o código atual e me oriente passo a passo no Android Studio. Não repita etapas concluídas e não altere a `main` sem combinação prévia.

**Ao final de cada marco importante:** atualizar este documento com data, branch, commit, funcionalidades, testes, pendências e próximo passo. Conferir sempre o código real antes de dar instruções específicas. O documento é a referência de continuidade, não substitui o código-fonte nem o histórico Git.
