# Journal de bord — KFOKAM48

Suivi de présence, dépôt d'exercices et relecture entre pairs.
Une entrée par ticket, dans l'ordre de traitement : branche, PR (qui ferme l'issue), ce qui a
été fait, décisions et preuves de vérification.

| Ticket | Titre | Branche | PR |
| --- | --- | --- | --- |
| #24 | [Backend] US-12 Gestion centralisée des erreurs (B4) | `feat/24-gestion-erreurs` | #38 |
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

---

## #24 — [Backend] US-12 Gestion centralisée des erreurs (B4)

- **Branche** : `feat/24-gestion-erreurs` · **Commit** : `b7bbfdc` · **PR** : #38 (fusionnée) — issue fermée.
- **Fait** :
  - `web/GestionnaireErreurs` (`@RestControllerAdvice`) : toutes les réponses d'erreur sortent au
    format imposé `{ code, message }`, jamais de stack trace ni de page d'erreur Spring.
  - `erreur/ErreurMetierException` + 20 sous-classes, chacune portant son `code` et son statut HTTP
    conformes au contrat (`CODE_INCONNU` 400, `CODE_EXPIRE` 410, `DEJA_PRESENT` 409,
    `LIEN_INVALIDE` 400, `EXERCICE_DEJA_DEPOSE` 409, `NOTE_INVALIDE` 400, `AUTO_RELECTURE` 403,
    `SESSION_INCONNUE`/`EXERCICE_INCONNUE`/`PROMOTION_INCONNUE` 404, `RELECTURE_DEJA_COMMENCEE` 409,
    `RELECTURE_DEJA_RENDUE` 409, `SESSION_CLOTUREE` 409, `SESSION_DEJA_CLOTUREE` 409,
    `TROP_DE_TENTATIVES` 429…).
  - Erreurs techniques normalisées : validation → 400 `CHAMP_MANQUANT`, corps illisible → 400
    `CORPS_INVALIDE`, paramètre mal formé → 400 `PARAMETRE_INVALIDE`, contrainte SQL → 409
    `CONFLIT_DONNEES`, erreurs Spring standard (404 `RESSOURCE_INCONNUE`, 405, 415), fallback 500
    `ERREUR_INTERNE`.
  - `pom.xml` : surefire inclut `**/*IT.java` pour que `./mvnw test` soit l'unique commande à lancer
    sur un poste vierge (B6).
- **Décision technique** : l'advice étend `ResponseEntityExceptionHandler` plutôt que de capturer
  `Exception` seul — sinon une méthode HTTP invalide (405) ou un média non supporté (415)
  deviendraient des 500. Le détail technique (`ex` + stack) est journalisé côté serveur
  (`LOG.error`), jamais renvoyé.
- **Adaptation Framework 7** : `handleMethodArgumentTypeMismatch` n'existe plus ; l'override porte
  sur `handleTypeMismatch(TypeMismatchException, …)`.
- **Preuve** : `./mvnw -o -B test` → `BUILD SUCCESS`, 7 tests verts, dont
  `GestionnaireErreursIT` (5 cas : 410 métier, 400 CHAMP_MANQUANT, 400 CORPS_INVALIDE,
  404 RESSOURCE_INCONNUE, 500 sans stack trace).

