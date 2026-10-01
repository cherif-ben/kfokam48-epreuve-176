# Bug #50 : Marquage de présence concurrent — un étudiant disparaît

## Description (rapport utilisateur)
> J'ai ouvert une session ce matin avec deux étudiants côte à côte. Ils ont tapé le code
> presque en même temps et il n'y en a qu'un seul qui apparaît dans ma liste. J'ai réessayé
> une fois, cette fois les deux sont passés. Je ne comprends pas. — Formateur, 25 sept. 2026

## Reproduction
1. Ouvrir une session (obtenir un code de présence).
2. Deux étudiants (ou deux onglets du même étudiant) soumettent `POST /api/presences` avec le
   même code et le même `etudiantId` **presque simultanément**.
3. Un request retourne **201 Created**, l'autre retourne **409 CONFLIT_DONNEES** (au lieu de
   **409 DEJA_PRESENT**).

## Cause racine
Le service `PresenceService.marquer` effectue un **check puis action** (read-then-write) :
1. `existsBySessionIdAndEtudiantId` → false (aucune présence)
2. `save(new Presence(...))`

Entre l'étape 1 et l'étape 2, un autre thread peut passer le même check. Le `save` échoue sur la
contrainte `UNIQUE(session_id, etudiant_id)` et lève `DataIntegrityViolationException`, interceptée
par `GestionnaireErreurs` comme `CONFLIT_DONNEES` au lieu de `DEJA_PRESENT`.

## Comportement attendu
`POST /api/presences` concurrent pour le même couple (session, étudiant) → **409 DEJA_PRESENT**.
