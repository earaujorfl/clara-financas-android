# Arquitetura do Clara Finanças

Projeto Android de módulo único com separação por responsabilidade:

```text
UI (Compose) → ViewModel → Repository → DAO (Room/SQLite)
            ← StateFlow ← Flow ←────────────────
```

- **UI:** estado da navegação, formulários e componentes Material 3.
- **Domain:** parsing/formatação BRL, agregações e resumos.
- **Data:** entidades Room (transações e categorias personalizadas), DAOs e repositório.
- **Categorias:** categorias padrão são definidas em código; categorias do usuário ficam na tabela `custom_categories` e são observadas via `Flow`.
- **Filtros:** função pura `TransactionFilters` normaliza acentos e caixa e filtra apenas o histórico, sem alterar o resumo do dashboard.
- **Migração:** `MIGRATION_1_2` cria tabela e índice para categorias; a tabela de transações permanece intacta.
- **Dinheiro:** valores em centavos (Long), evitando precisão variável de Float/Double.
- **Offline-first:** nenhuma permissão de rede ou autenticação no MVP.
- **Injeção manual:** suficiente para o escopo inicial; Hilt é evolução possível.

## Limitações atuais

- O usuário seleciona datas e pode editar lançamentos desde v0.2.
- Sem contas múltiplas, recorrência, exportação, criptografia adicional ou backup.
- Excluir e renomear categorias personalizadas ainda não estão implementados; filtros limitam-se ao mês escolhido.
- Novas alterações de schema exigirão migrações adicionais, não destrutivas.
- Testes instrumentados de UI e migração em dispositivo ainda estão pendentes.
