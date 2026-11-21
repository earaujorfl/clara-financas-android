# Arquitetura do Clara Finanças

Projeto Android de módulo único com separação por responsabilidade:

```text
UI (Compose) → ViewModel → Repository → DAO (Room/SQLite)
            ← StateFlow ← Flow ←────────────────
```

- **UI:** estado da navegação, formulários e componentes Material 3.
- **Domain:** parsing/formatação BRL, agregações e resumos.
- **Data:** entidade Room, consultas DAO e repositório.
- **Dinheiro:** valores em centavos (Long), evitando precisão variável de Float/Double.
- **Offline-first:** nenhuma permissão de rede ou autenticação no MVP.
- **Injeção manual:** suficiente para o escopo inicial; Hilt é evolução possível.

## Limitações atuais

- Todo lançamento recebe a data atual; editar e selecionar datas entram em v0.2.
- Sem contas múltiplas, recorrência, exportação, criptografia adicional ou backup.
- Migrações Room necessárias em qualquer alteração de schema futura.
- Compilação e testes instrumentados ainda não validados com SDK Android neste ambiente.
