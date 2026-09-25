# Cahier des charges — Suivi de présence, dépôt d'exercices et relecture entre pairs (KFOKAM48)

**Auteur :** Faroukou Cherif Ben · 176
**Version :** 1 · **Date :** 25 septembre 2026
**Frontend choisi :** Next.js, parce que le rendu serveur et le routing par fichiers accélèrent la mise en place des trois écrans (formateur, étudiant, relecteur) sans configuration supplémentaire

---

## 1. Contexte et objectif

KFOKAM48 organise des sessions de formation en présentiel. Aujourd'hui, la présence, le dépôt des exercices et la correction entre pairs se font de façon informelle (appel oral, partage de liens en message, corrections improvisées), ce qui ne laisse aucune trace fiable et complique le suivi de la promotion. L'application répond à trois besoins concrets : permettre à un formateur d'ouvrir une session et de savoir immédiatement qui est présent, permettre à un étudiant de déposer son exercice et de se voir attribuer automatiquement un pair correcteur, et donner au formateur un tableau de bord unique montrant, pour chaque étudiant, sa présence, ses dépôts, sa moyenne et les relectures encore en attente. L'enjeu n'est pas la beauté de l'interface, mais la fiabilité des règles (expiration, unicité, impartialité de la relecture) et la traçabilité des décisions.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| Formateur | Ouvrir une session et obtenir un code de présence ; ajouter une présence manuellement ; clôturer une session ; consulter le tableau récapitulatif de sa promotion | Déposer un exercice ; effectuer une relecture ; voir l'identité des relecteurs dans le tableau |
| Étudiant | Marquer sa présence avec un code valide et non expiré ; déposer le lien de son exercice ; remplacer ce lien tant qu'aucune relecture n'a commencé ; consulter sa propre note et le commentaire reçu une fois relu | Marquer sa présence deux fois pour la même session ; relire son propre exercice ; voir qui l'a relu ; modifier une session ou le tableau du formateur |
| Relecteur | Rendre une note (0–20, entière) et un commentaire pour l'exercice qui lui a été assigné ; corriger cette note tant que la session n'est pas clôturée | Choisir l'exercice qu'il relit ; relire l'exercice d'un étudiant absent de la session ; relire son propre exercice ; modifier sa note après clôture de la session |

**Décision — le relecteur est-il un acteur distinct ?** Non. D'après Q7, c'est le système qui choisit « au hasard, parmi les étudiants présents à cette session » : le relecteur n'est donc pas un rôle de compte séparé, mais un **état temporaire d'un étudiant**, attribué automatiquement pour un exercice donné. Conséquence sur le modèle de données : il n'existe pas de table `Role` ni de compte « relecteur » ; la table `Relecture` référence directement un `etudiant_id` comme relecteur, au même titre qu'un `etudiant_id` comme auteur de l'exercice.

## 3. Périmètre

**Inclus dans cette version :**
- Ouverture d'une session par le formateur, avec génération d'un code de présence valide 15 minutes
- Marquage de présence par un étudiant via ce code, avec unicité par session
- Ajout manuel d'une présence par le formateur (source distincte de la présence auto-déclarée)
- Blocage temporaire (2 minutes) après 5 échecs de saisie de code consécutifs pour un même étudiant
- Dépôt du lien d'un exercice par un étudiant, y compris après la fin théorique de la session
- Remplacement du lien déposé, tant qu'aucune relecture n'a débuté
- Assignation automatique et aléatoire d'un relecteur parmi les étudiants présents à la session, à l'exclusion de l'auteur
- Rendu d'une note (0–20, entière) et d'un commentaire par le relecteur, avec possibilité de correction jusqu'à la clôture de la session
- Clôture d'une session par le formateur, verrouillant présences, dépôts et relectures
- Tableau récapitulatif du formateur par promotion (présences, exercices déposés, moyenne, relectures en attente)

**Explicitement exclu :**
- Toute authentification par mot de passe (Q1) : l'étudiant est sélectionné dans une liste existante
- La création/administration des comptes formateurs, étudiants et promotions (supposés déjà en base, via données de démonstration)
- Le choix du relecteur par le formateur ou par l'étudiant lui-même (Q7 impose un tirage système)
- Les notifications (email, push) de changement de statut
- La gestion de plusieurs formateurs sur une même session
- Le rendu visuel et le style graphique, non notés dans cette épreuve

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur ouvre une session pour sa promotion et obtient un code de présence | Quand le formateur soumet un titre et une promotion, il reçoit un code, une date d'ouverture et une date d'expiration (+15 min) | Must |
| EF2 | L'étudiant marque sa présence à l'aide d'un code | Quand je saisis un code valide et non expiré, ma présence apparaît dans le tableau du formateur | Must |
| EF3 | Le code de présence expire 15 minutes après l'ouverture de la session | Quand je saisis un code après ce délai, je reçois une erreur `CODE_EXPIRE` (410) | Must |
| EF4 | Un étudiant ne peut marquer sa présence qu'une seule fois par session | Quand je saisis un code valide alors que je suis déjà marqué présent, je reçois `DEJA_PRESENT` (409) | Must |
| EF5 | Le formateur peut ajouter une présence manuellement | Quand le formateur ajoute une présence pour un étudiant, elle apparaît dans le tableau avec la mention « ajouté par le formateur » | Should |
| EF6 | L'étudiant dépose le lien de son exercice pour une session | Quand je soumets un lien valide, mon exercice apparaît avec le statut « en attente de relecture » | Must |
| EF7 | L'étudiant peut remplacer le lien de son exercice | Quand je resoumets un lien avant que quiconque n'ait commencé à me relire, l'ancien lien est remplacé | Should |
| EF8 | Un relecteur est assigné automatiquement, au hasard, parmi les étudiants présents à la session, hors l'auteur | Quand un exercice est déposé, un relecteur différent de l'auteur et présent à la session lui est attribué | Must |
| EF9 | Le relecteur rend une note et un commentaire | Quand un relecteur soumet une note entière comprise entre 0 et 20 avec un commentaire, l'exercice passe au statut « relu » | Must |
| EF10 | Le relecteur peut corriger sa note tant que la session n'est pas clôturée | Quand je resoumets une note avant la clôture de la session, la nouvelle valeur remplace l'ancienne | Should |
| EF11 | Le formateur clôture une session | Quand je clôture une session, plus aucune présence, dépôt ou relecture ne peut y être modifié | Must |
| EF12 | Le formateur consulte un tableau récapitulatif par étudiant | Quand j'ouvre le tableau pour une promotion, je vois pour chaque étudiant sa présence, ses exercices déposés, sa moyenne et ses relectures en attente | Must |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | L'interface de marquage de présence est utilisable sur un téléphone | Test manuel sur viewport mobile (375px) : le formulaire de code reste utilisable sans zoom ni défilement horizontal |
| ENF2 | Le tableau du formateur répond en moins de 2 s pour une promotion de 60 étudiants | Jeu de données de démonstration avec 60 étudiants, mesure du temps de réponse de `GET /api/tableau` |
| ENF3 | Aucune erreur ne renvoie de trace technique au client | Revue de code : toutes les exceptions passent par le `@RestControllerAdvice` et renvoient le format `{code, message}` |
| ENF4 | Le contrat d'API reste la seule source de vérité sur les échanges | Toute modification du contrat fait l'objet d'un commit dédié, jamais mêlé à un commit de code métier |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Un code de présence expire 15 minutes après l'ouverture de la session | Q2 |
| RG2 | Un étudiant ne peut pas relire son propre exercice | Q5 |
| RG3 | Une note est un entier compris entre 0 et 20 | Q9 |
| RG4 | Un seul relecteur est assigné par exercice | Q6 |
| RG5 | Le relecteur est choisi aléatoirement par le système, parmi les étudiants présents à la session | Q7 |
| RG6 | L'étudiant relu voit sa note et le commentaire reçu, jamais l'identité de son relecteur | Q8 |
| RG7 | Le relecteur peut corriger sa note tant que le formateur n'a pas clôturé la session | Q10 (décision retenue, voir section 7) |
| RG8 | Un exercice reste au statut « en attente de relecture » tant que le relecteur ne l'a pas rendue | Q11 |
| RG9 | Un exercice peut être déposé même après la fin théorique de la session, jusqu'à sa clôture par le formateur | Q12 |
| RG10 | Le lien d'un exercice peut être remplacé tant qu'aucune relecture n'a débuté sur cet exercice | Q13 |
| RG11 | Une présence ajoutée par le formateur porte la mention `source = FORMATEUR`, une présence auto-déclarée porte `source = ETUDIANT` | Q14 |
| RG12 | Une présence est unique par couple (session, étudiant) | Déduit du contrat d'API (409 `DEJA_PRESENT`) |
| RG13 | Après 5 échecs consécutifs de saisie de code pour un même étudiant, la saisie est bloquée pendant 2 minutes | Q4 |
| RG14 | Après clôture d'une session par le formateur, plus aucune présence, dépôt ou relecture ne peut y être ajouté ou modifié | Décision retenue pour résoudre Q10/Q12/Q15 (voir section 7) |

## 7. Zones d'ombre, hypothèses et contradictions

**Points que la demande ne tranche pas :**

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|
| Qu'est-ce qu'une « clôture de session » côté formateur ? Aucune question ne la définit, alors que Q10, Q12 et Q15 s'y réfèrent tous les trois | Trou — aucune réponse directe | Ajout d'une opération non imposée (`clôturer une session`, réservée au formateur), le contrat autorisant explicitement à « ajouter ses propres opérations » | Nouveau champ `clotureAt` sur `Session`, nouvel endpoint dans `api/contrat.yaml`, RG14 |
| Comment gérer les tentatives répétées de code erroné ? | Q4 | Compteur d'échecs par couple (étudiant, session), blocage de 2 minutes au 5e échec, réinitialisé après le délai | Logique ajoutée dans `PresenceService`, pas de nouvel endpoint (le contrat impose déjà `POST /api/presences`) |
| Le relecteur est-il un compte à part ? | Q7 | Non — état temporaire d'un étudiant (voir section 2) | Pas de table `Role` ; `Relecture.relecteurId` référence `Etudiant.id` |

**Contradictions relevées :**

| Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|
| Q10 (« le relecteur peut corriger sa note tant que le formateur n'a pas clôturé la session ») contre Q15 (« une fois validée, la note est définitive, le relecteur ne peut plus y revenir ») | Q10 fait foi : la note reste modifiable jusqu'à la clôture de la session | Q11 décrit un usage concret et récurrent du tableau du formateur (« je dois voir clairement les relectures en attente »), qui suppose un mécanisme de clôture progressif et explicite. Q10 et Q12 s'appuient tous les deux sur ce même mécanisme (« jusqu'à ce que je clôture la session »), ce qui donne un modèle cohérent. Q15 n'exprime qu'une intention générale de définitif, sans décrire aucun mécanisme : elle est traitée comme une reformulation imprécise de la même idée, que la clôture rend vraie au bon moment plutôt qu'à l'envoi de la note |

## 8. Contraintes techniques

Contraintes imposées par le sujet, reprises telles quelles :
- **B1** — Java 17+, Maven, wrapper `mvnw` commité
- **B2** — Respect à la lettre du contrat `api/contrat.yaml` : chemins, verbes, codes de statut, format d'erreur
- **B3** — Séparation stricte contrôleur / service / repository ; aucune entité JPA exposée en JSON, uniquement des DTO
- **B4** — Validation des entrées et gestion centralisée des erreurs via `@RestControllerAdvice` ; aucune stack trace renvoyée au client
- **B5** — Schéma de base versionné par migrations (Flyway ou Liquibase) ; `ddl-auto=update` interdit hors tests
- **B6** — Au moins un test unitaire sur une règle métier réelle (ex. RG1, RG2 ou RG5) et un test d'intégration sur un endpoint, exécutables sur poste vierge
- **F1** — Framework frontend déclaré et justifié dans le README ; le build doit passer
- **F2** — Trois écrans : formateur, étudiant, relecteur
- **F3** — Appels API centralisés dans une couche dédiée ; états de chargement et d'erreur gérés ; aucun recalcul côté frontend d'une donnée déjà fournie par l'API (ex. la moyenne)

Contraintes que je m'impose en plus :
- Base de données **PostgreSQL**, pour un environnement proche de la production
- Migrations **Flyway**, un fichier par changement de schéma, y compris ceux introduits à l'étape 3
- Tests d'intégration exécutés sur une base **H2** ou via **Testcontainers**, sans dépendance à une base locale déjà peuplée
- `.gitignore` Java + JS posé avant le premier commit de code (exclusion de `target/`, `node_modules/`, `dist/`, fichiers `.env`)

## 9. Livrables

- `docs/CAHIER_DES_CHARGES.md` — ce document
- `docs/diagrammes/` — D1 (cas d'utilisation), D2 (classes/modèle de données), D3 (séquence « marquer sa présence »), et éventuellement D4 (bonus, états-transitions d'un exercice)
- `docs/JOURNAL.md` — journal de bord, une entrée par étape
- Issues GitHub — backlog priorisé, chacune reliée à une exigence EFx ou une règle RGx
- `api/contrat.yaml` — contrat d'API complété (5 opérations imposées + opérations propres, dont la clôture de session)
- `backend/` — application Spring Boot (Java 17+, Maven, migrations Flyway commitées)
- `frontend/` — application React/Angular/Next.js avec les trois écrans (formateur, étudiant, relecteur)
- `README.md` — installation et démarrage testés depuis un clone vierge, avec données de démonstration
- `CHANGELOG.md` — cohérent avec l'historique Git
- Trois commits `[JALON]` (`analyse`, `v0.1`, `v1.0`), dans cet ordre

## 10. Démarche prévue

1. **Étape 1 — Analyse (aujourd'hui, en premier) :** ce cahier des charges, les diagrammes Mermaid, le backlog en issues, le contrat d'API complété. Rien n'est codé avant le commit `[JALON] analyse`.
2. **Étape 2 — Première version :** uniquement les stories *Must* (EF1, EF2, EF3, EF4, EF6, EF8, EF9, EF11, EF12), une branche et une PR par ticket. Commit `[JALON] v0.1`.
3. **Étape 3 — Enveloppe :** ouverture après le jalon v0.1 ; le schéma de base doit déjà être versionné par migrations pour absorber sans douleur le changement annoncé (bug + évolution de besoin touchant BDD, contrat et frontend). Mise à jour du cahier des charges et des diagrammes dans un commit dédié qui l'indique explicitement.
4. **Étape 4 — Version finale :** stories *Should* restantes selon le temps disponible, `CHANGELOG.md`, README testé depuis un clone vierge, backlog restant trié. Commit `[JALON] v1.0`.
5. **Étape 5 — Épreuve Git :** traitée comme une activité indépendante, sur un dépôt séparé, une fois l'étape 4 poussée.
6. **Étape 6 — Soumission :** vérification des deux dépôts en navigation privée, relevé des hash complets, remplissage de `SOUMISSION.md`, dépôt sur la plateforme avant 18h00.

**Si je prends du retard :** je sacrifie d'abord les stories *Could*, puis les *Should* (EF5, EF7, EF10), jamais les *Must* ni les trois jalons — un produit incomplet mais annoncé comme tel vaut mieux qu'une promesse non tenue.

**Definition of Done — un ticket est terminé quand :**
- Le code respecte la séparation contrôleur/service/repository et passe par des DTO (aucune entité JPA en JSON)
- Les codes HTTP et le format d'erreur du ticket sont conformes au contrat (`api/contrat.yaml`)
- Au moins un test (unitaire ou d'intégration) couvre la règle de gestion citée dans le ticket
- La branche est fusionnée via une PR qui ferme l'issue correspondante
- Le README reste à jour si une commande de démarrage a changé

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | 25 septembre 2026 | Version initiale, rédigée avant tout code (étape 1) |

*L'étape 3 rendra une partie de ce document faux. Reviens le corriger et note-le ici — un cahier des charges périmé est un cahier des charges mort.*
