# Journal de bord — KFOKAM48

Suivi de présence, dépôt d'exercices et relecture entre pairs.
Une entrée par ticket, dans l'ordre de traitement : branche, PR (qui ferme l'issue), ce qui a
été fait, décisions et preuves de vérification.

| Ticket | Titre | Branche | PR |
| --- | --- | --- | --- |
| #20 | [Backend] US-8 Ajouter une présence manuellement | `feat/20-backend-ajouter-presence-manuellelement` | #44 |
| #23 | [Backend] US-11 Bloquer temporairement après 5 échecs de code | `feat/23-backend-blocage-code` | #42 |
| #17 | [Backend] US-5 Assigner automatiquement un relecteur | `feat/17-backend-assigner-relecteur` | #43 |
| #16 | [Backend] US-2 Marquer sa présence avec un code | `feat/16-backend-marquer-presence` | #40 |
| #24 | [Backend] US-12 Gestion centralisée des erreurs (B4) | `feat/24-gestion-erreurs` | #38 |
| #14 | [Backend] US-1 Ouvrir une session et générer un code de présence | `feat/14-backend-ouvrir-session` | #39 |
| #25 | [Backend] US-13 Migrations Flyway V1 — schéma initial (B5) | `feat/25-flyway-v1-schema` | #37 |
| #15 | [Backend] US-3 Déposer le lien de son exercice | `feat/15-backend-deposer-exercice` | #41 |

---

## #20 — [Backend] US-8 Ajouter une présence manuellement (EF5, RG11, Q14)

- **Branche** : `feat/20-backend-ajouter-presence-manuellelement` · **PR** : #44 — issue fermée.
- **Fait** :
  - `POST /api/presences` accepte un champ optionnel `source` (enum `ETUDIANT | FORMATEUR`) :
    absent ou `null` → `ETUDIANT` (comportement inchangé pour l'US-2) ; `FORMATEUR` → présence
    ajoutée manuellement par le formateur (EF5, RG11).
  - `MarquerPresenceRequest` : ajout du champ `source` + constructeur à 2 args conservé pour
    l'existant ; validation de l'enum par Jackson, erreur 400 `SOURCE_INVALIDE` via
    `GestionnaireErreurs` (au lieu d'un générique `CORPS_INVALIDE`).
  - `PresenceService.marquerParEtudiant` résout la source (défaut `ETUDIANT`) puis délègue à
    `marquer` — l'unicité (session, étudiant) s'applique quelle que soit la source (RG12).
  - **Contrat** : champ `source` documenté dans `api/contrat.yaml` §POST /api/presences.
- **Preuve** : `./mvnw -o -B test` → `BUILD SUCCESS`, **39 tests verts**, dont
  `PresenceServiceTest.marquer_avecSourceFormateur_enregistreUnePresenceFormateur` (unitaire) et
  `PresenceControllerIT` : 201 `source=FORMATEUR` + 400 `SOURCE_INVALIDE` (format `{code, message}`,
  aucune ligne insérée).

---

## #17 — [Backend] US-5 Assigner automatiquement un relecteur (EF8, RG2/4/5, Q5/6/7)

- **Branche** : `feat/17-backend-assigner-relecteur` · **Commit** : `5b20677` · **PR** : #43 — issue fermée.
- **Fait** :
  - Entité `Relecture` (nouvelle, D2) : `exercice_id` (clé étrangère vers `Exercice`), `relecteur_id`
    (clé étrangère vers `Etudiant` — le relecteur n'est pas un acteur distinct, cf. §2 du cahier
    des charges), `note` (Integer, CHECK 0-20), `commentaire` (TEXT), `statut`
    (`StatutRelecture` enum `EN_ATTENTE` | `RENDUE`), `created_at`, `updated_at`.
  - Enum `StatutRelecture` (nouvelle) — correspond à la classe D2.
  - `RelectureRepository` : `findByExerciceId` + `existsByExerciceId`.
  - **RG4 (Q6)** : la contrainte `UNIQUE(exercice_id)` — déjà présente dans `V1` sous forme
    `exercice_id ... UNIQUE REFERENCES` — est garantie en base dès le premier dépôt. Aucune
    migration nécessaire (B5 : le schéma V1 est inchangé).
  - `ExerciceService.deposer` : après la sauvegarde de l'exercice, `assignerRelecteur` récupère les
    présences de la session (`PresenceRepository.findBySessionId`), exclut l'auteur (RG2) et tire au
    hasard un relecteur (RG5, via le bean `Random` injecté dans `ConfigurationAleatoire`). Si aucun
    candidat n'est disponible (Q11), aucune relecture n'est créée et l'exercice reste
    `EN_ATTENTE_RELECTURE`. Aucun endpoint nouveau : le dépôt reste `POST /api/exercices` → 201.
  - `PresenceRepository` : méthode `findBySessionId` déjà existante (US-16), réutilisée telle quelle.
- **Décision technique** : le `Random` est un bean Spring (`ConfigurationAleatoire`) et non
  `ThreadLocalRandom.current()` pour que les tests unitaires puissent figer le tir par un seed
  (`new Random(0)`), sans attendre de hasard réel. L'assignation est **silencieuse** : elle ne
  change pas le contrat de réponse (201 `{ id, statut }`) et n'expose jamais l'identité du relecteur
  (RG6/Q8 — `ExerciceCompletResponse` ne transporte que `note` et `commentaire`).
- **Preuve** : `mvn -B test` → `BUILD SUCCESS`, **38 tests verts** (2 de plus qu'avant, dont les 3
  nouveaux cas d'`ExerciceServiceTest` :
  - `deposer_assigneUnRelecteurDifferentDeLAuteur` (RG2 + RG5 : le relecteur est 3 ou 4, jamais 2) ;
  - `deposer_sansAutrePresent_resteEnAttenteSansRelecture` (Q11 : aucun relecteur éligible →
    `EN_ATTENTE_RELECTURE`, 0 relecture créée, pas d'erreur) ;
  - `deposer_apresExpirationMaisAvantCloture_accepteEtPasseEnAttenteDeRelecture` (Q12, enrichi :
    vérification qu'aucune relecture n'est créée en l'absence de présents).
  Flyway joue V1/V2/V3 sur H2 avec `ddl-auto: validate` (B5) → la table `relecture` (déjà en V1)
  est validée par Hibernate au démarrage ; l'entité `Relecture` correspond donc au schéma.
## #23 — [Backend] US-11 Bloquer temporairement après 5 échecs de code (RG13, Q4)

- **Branche** : `feat/23-backend-blocage-code` · **Commit** : `eefef4d` · **PR** : #42 — issue fermée.
- **Fait** :
  - Table `tentative_code` (Flyway `V3`) : `session_id`, `etudiant_id` (FK vers `session`/`etudiant`),
    `echecs INTEGER NOT NULL DEFAULT 0`, `blocage_jusqua TIMESTAMP`, `dernier_echec TIMESTAMP`,
    `created_at`, `updated_at` + contrainte `UNIQUE(session_id, etudiant_id)` (le blocage est
    propre à chaque couple, pas global) et 3 index (`session_id`, `etudiant_id`, `blocage_jusqua`).
  - Entité `TentativeCode` (RG13) : constantes `SEUIL_ECHECS = 5` et `DELAI_BLOCAGE = 2 min`.
    `estBloquee(now)` → true si `blocageJusqua > now`. `enregistrerEchec(now)` incrémente le
    compteur et pose `blocageJusqua = now + 2 min` au 5e échec. `reinitialiser(now)` remet
    `echecs = 0` et `blocageJusqua = null`.
  - `TentativeCodeRepository` : `findBySessionIdAndEtudiantId` + `reinitialiser` (UPDATE ciblé,
    pas de suppression de ligne — le compteur est réinitialisé, pas recréé).
  - `PresenceService` : le blocage est vérifié **avant** tout traitement, une fois la session
    identifiée (après `findByCode`). Chaque échec (code expiré, session clôturée, étudiant
    inconnu, déjà présent) appelle `enregistrerEchec`. Une saisie réussie appelle `reinitialiser`.
    Aucun endpoint nouveau : le contrat impose déjà `POST /api/presences`.
  - `TropDeTentativesException` (déjà existante, ticket #24) → **429** `TROP_DE_TENTATIVES` avec le
    nombre de secondes restantes dans le message.
- **Décision technique** : stockage en base (`tentative_code`) et non cache in-memory. Justification
  (Cahier des charges §7, Q4) : le blocage doit survivre un redémarrage du serveur et être isolé par
  couple (étudiant, session) — une table avec `UNIQUE(session_id, etudiant_id)` donne ces deux
  garanties sans état partagé à gérer. Le `Clock` injectable (`ConfigurationHorloge`) rend RG13
  testable sans attente réelle.
- **Preuve** : `mvn -B test` → `BUILD SUCCESS`, **36 tests verts** dont
  `PresenceServiceTest` (7 cas : nominal, code expiré, session clôturée, code inconnu, doublon,
  **blocage actif → 429**, **saisie réussie → réinitialisation**) et `PresenceControllerIT`
  (201 `source=ETUDIANT`, 409 `DEJA_PRESENT`, 410 `CODE_EXPIRE` au format `{code, message}`,
  400 `CODE_INCONNU`, 410 après clôture + aucune ligne insérée). Flyway joue V1, V2, V3 sur H2 avec
  `ddl-auto: validate` (B5) → la table `tentative_code` est validée par Hibernate au démarrage.

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
- **Dette assumée** : la table `tentative_code` (RG13, ticket #23) arrive en `V3` — cf. entrée #23.

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

---

## #14 — [Backend] US-1 Ouvrir une session et générer un code de présence

- **Branche** : `feat/14-backend-ouvrir-session` · **Commit** : `44b2631` · **PR** : #39 (fusionnée) — issue fermée.
- **Fait** :
  - `POST /api/sessions` → **201** `{ id, code, ouvertureAt, expirationAt }`, exactement le schéma
    imposé par le contrat. Code de **6 caractères** tiré dans un alphabet sans caractères ambigus
    (`ABCDEFGHJKLMNPQRSTUVWXYZ23456789`, pas de `0/O/1/I/L` — le code est dicté à l'oral), unicité
    garantie par la contrainte `UNIQUE(code)` de `V1` **et** par une relecture `existsByCode` avec
    nouvelle tentative.
  - **RG1 (Q2)** : `expirationAt = ouvertureAt + 15 min`, calculé côté API uniquement.
  - Entités `Promotion`, `Formateur`, `Etudiant`, `Session` + `repository/` + `dto/` (records) —
    aucune entité JPA ne sort en JSON (B3). Les relations sont portées par des identifiants
    (`promotionId`, `formateurId`) comme dans D2, ce qui évite tout chargement paresseux.
  - Endpoints libres ajoutés **et documentés dans `api/contrat.YAML`** :
    `GET /api/sessions/{id}`, `GET /api/sessions?promotionId=&formateurId=`, `GET /api/promotions`,
    `GET /api/etudiants?promotionId=`.
  - `config/ConfigurationHorloge` : `Clock` injectable, pour tester RG1 (et plus tard RG13/RG14)
    sans attente réelle.
- **Décision** : le contrat imposé ne transporte pas de `formateurId` dans `POST /api/sessions` et
  interdit de modifier son schéma — le service rattache donc la session au **premier formateur
  enregistré** (données de démo `V2`). Aucun champ ajouté au schéma imposé.
- **Correction de dettes repérée en passant** : le contrat `api /contrat.YAML` avait `GET /api/exercices`
  et `GET /api/relectures` indentés à la racine (hors de `paths:`) → document OpenAPI invalide.
  Indentation corrigée, revalidée par un parseur YAML (10 chemins sous `paths`).
  *(Le nom du fichier — dossier `api ` avec espace finale et `contrat.YAML` en majuscules — ne
  correspond pas au `api/contrat.yaml` annoncé dans le README ; à trancher avec l'auteur.)*
- **Preuve** : `./mvnw -o -B test` → `BUILD SUCCESS`, **14 tests verts** :
  `SessionServiceTest` (15 min + promotion inconnue) et `SessionControllerIT` (201 + 15 min
  constatés en base, 404 `PROMOTION_INCONNUE`, 400 `CHAMP_MANQUANT`, 404 `SESSION_INCONNUE`, liste).
- **Note outillage** : Spring Boot 4 embarque **Jackson 3** (`tools.jackson.*`) ;
  `com.fasterxml.jackson.databind` n'est plus sur le classpath. Les tests s'appuient donc sur
  `jsonPath` plutôt que sur un `ObjectMapper`.

---

## #15 — [Backend] US-3 Déposer le lien de son exercice (EF6, RG9, Q12)

- **Branche** : `feat/15-backend-deposer-exercice` · **Commit** : `fadbf66` · **PR** : #41 (fusionnée) — issue fermée.
- **Fait** :
  - `POST /api/exercices` → **201** `{ id, statut }`, le statut étant `EN_ATTENTE_RELECTURE`
    (Q11) et non `DEPOSE` : le dépôt déclenche immédiatement l'attente de relecture.
  - **RG9** : unicité par couple (session, étudiant) — `ExerciceRepository.existsBySessionIdAndEtudiantId`,
    protégée par la contrainte `UNIQUE(session_id, etudiant_id)` de `V1`. Doublon → 409
    `EXERCICE_DEJA_DEPOSE`.
  - **Q12** : le dépôt reste possible après `expirationAt` de la session, jusqu'à sa clôture
    (RG14) → 409 `SESSION_CLOTUREE` sinon. Aucun contrôle de présence requis (hypothèse §7).
  - **LIEN_INVALIDE** (400) : validation côté API via `ExerciceService.validerLien` (scheme
    http/https + host non vide) — aucune règle dupliquée côté front (F3).
  - **EF7 / RG10 (Q13)** : `PUT /api/exercices/{id}` remplace le lien, refusé 409
    `RELECTURE_DEJA_COMMENCEE` dès que l'exercice est `RELU` ou que la session est clôturée.
  - **RG6 (Q8)** : `GET /api/exercices?etudiantId=&sessionId=` retourne la liste avec note et
    commentaire si relu, **sans jamais exposer l'identité du relecteur** (DTO
    `ExerciceCompletResponse`, pas l'entité `Relecture`).
  - Entités `Exercice` + `StatutExercice` + `ExerciceRepository` + 4 DTOs — aucune entité JPA en
    JSON (B3). Les relations sont portées par des identifiants (`sessionId`, `etudiantId`).
  - L'assignation automatique du relecteur (US-5, EF8) n'est **pas** branchée ici : elle arrive
    avec le ticket #26 (US-5). L'exercice passe donc en `EN_ATTENTE_RELECTURE` en attendant.
- **Décision** : le statut sortant vaut `EN_ATTENTE_RELECTURE` et non `DEPOSE` : le contrat imposé
  ne listant que `DEPOSE`, `EN_ATTENTE_RELECTURE`, `RELU` (D4), les deux premiers sont équivalents
  à l'issue du dépôt, et `EN_ATTENTE_RELECTURE` est plus explicite quant à l'étape suivante.
- **Preuve** : `./mvnw -o -B test` → `BUILD SUCCESS`, **34 tests verts** (10 de plus qu'avant,
  dont les 5 d'`ExerciceServiceTest` et les 5 d'`ExerciceControllerIT` :
  - 201 `EN_ATTENTE_RELECTURE` + vérification en base ;
  - 409 `EXERCICE_DEJA_DEPOSE` ;
  - 400 `LIEN_INVALIDE` ;
  - 409 `SESSION_CLOTUREE` (Q12) ;
  - `PUT` OK → puis 409 `RELECTURE_DEJA_COMMENCEE` après `marquerRelu`).

## #16 — [Backend] US-2 Marquer sa présence avec un code

- **Branche** : `feat/16-backend-marquer-presence` · **Commit** : `59a2ece` · **PR** : #40 (fusionnée) — issue fermée.
- **Fait** :
  - `POST /api/presences` `{ code, etudiantId }` → **201** `{ id, sessionId, etudiantId, source }`,
    `source = ETUDIANT`. Schéma imposé respecté à la lettre (aucun champ ajouté).
  - Ordre de vérification imposé par le ticket : `CODE_INCONNU` (400) → `CODE_EXPIRE` (410) →
    `ETUDIANT_INCONNU` (404) → `DEJA_PRESENT` (409).
  - **RG1 (Q2)** : validité de 15 min ; **RG14 (Q3)** : après clôture, le marquage renvoie 410
    `CODE_EXPIRE` ; **RG12** : unicité du couple (session, étudiant) vérifiée en service **et** par la
    contrainte `UNIQUE(session_id, etudiant_id)` de `V1`.
  - Entité `Presence` + enum `SourcePresence` (`ETUDIANT` | `FORMATEUR`) + `PresenceRepository`.
  - Le code saisi est normalisé (`trim` + majuscules) côté API : le front n'a aucune règle à dupliquer.
- **Preuve** : `./mvnw -o -B test` → `BUILD SUCCESS`, **24 tests verts** dont
  `PresenceServiceTest` (5 cas : nominal, code expiré, session clôturée, code inconnu, doublon) et
  `PresenceControllerIT` (201 `source=ETUDIANT`, 409 `DEJA_PRESENT`, 410 `CODE_EXPIRE` au format
  `{code, message}`, 400 `CODE_INCONNU`, 410 après clôture + aucune ligne insérée).
- **Note tests** : les entités non persistées ont un `id` nul — les stubs Mockito utilisent donc
  `any()` plutôt que des identifiants littéraux (le mode strict de Mockito l'a signalé).
