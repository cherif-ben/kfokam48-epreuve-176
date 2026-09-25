# Journal de bord — KFOKAM48

Suivi de présence, dépôt d'exercices et relecture entre pairs.
Une entrée par ticket, dans l'ordre de traitement : branche, PR (qui ferme l'issue), ce qui a
été fait, décisions et preuves de vérification.

| Ticket | Titre | Branche | PR |
| --- | --- | --- | --- |
| #25 | [Backend] US-13 Migrations Flyway V1 — schéma initial (B5) | `feat/25-flyway-v1-schema` | #37 |

---

## #25 — [Backend] US-13 Migrations Flyway V1 — schéma initial (B5)

- **Branche** : `feat/25-flyway-v1-schema` · **Commit** : `1d15b8c` · **PR** : #37 (fusionnée) — issue fermée.
- **Fait** : le schéma `V1__schema_initial.sql` (6 tables : `promotion`, `formateur`, `etudiant`,
  `session`, `presence`, `exercice`, `relecture`) est complété par les index manquants sur les clés
  étrangères : `session(formateur_id)`, `presence(etudiant_id)`, `exercice(session_id)`.
- **Vérification D2 ↔ migration** : les tables, colonnes, les trois contraintes `UNIQUE`
  (`presence(session_id, etudiant_id)` = RG12, `exercice(session_id, etudiant_id)` = RG9,
  `relecture(exercice_id)` = RG4) et les `CHECK` (`source IN (ETUDIANT, FORMATEUR)` = Q14,
  `statut IN (...)`, `note BETWEEN 0 AND 20` = RG3) correspondent au diagramme D2.
  `session.code` et `relecture(exercice_id)` sont déjà indexés par leur contrainte `UNIQUE`.
- **Preuve** : `cd backend && ./mvnw -o -B test` → `BUILD SUCCESS`, 2 tests verts ; Flyway joue V1 puis
  V2 sur H2 avec `ddl-auto: validate` (B5), donc le schéma est validé par Hibernate au démarrage.
- **Dette assumée** : la table `tentative_code` (RG13, ticket #23) arrivera en `V3`.

