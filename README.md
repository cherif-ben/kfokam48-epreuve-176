# KFOKAM48 — Suivi de présence, dépôt d'exercices et relecture entre pairs

Application de démonstration : un **formateur** ouvre une session avec un code de présence,
les **étudiants** marquent leur présence et déposent leurs exercices, et un relecteur
(tiré au hasard parmi les présents) note le travail de son pair.

## Stack

| Brique    | Technologie                                          |
|-----------|------------------------------------------------------|
| Backend   | Java 21, Spring Boot 4.1, Maven (wrapper commité), Flyway, PostgreSQL |
| Frontend  | Next.js (App Router), TypeScript, Tailwind CSS        |
| Infra     | Docker + Docker Compose                               |

## Démarrage rapide (Docker)

Depuis la racine du dépôt :

```bash
docker compose up --build
```

- Frontend : http://localhost:3000
- Backend (API) : http://localhost:8080
- PostgreSQL : localhost:5432 (`kfokam48` / `kfokam48`)

Le schéma et les données de démonstration (1 promotion, 60 étudiants) sont créés
automatiquement par les migrations Flyway au démarrage du backend.

## Démarrage sans Docker

### 1. PostgreSQL

Une base `kfokam48` accessible sur `localhost:5432` avec l'utilisateur `kfokam48` / `kfokam48`
(démarrable seul via `docker compose up db`).

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run
```

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

## Tests

```bash
cd backend
./mvnw test
```

Les tests d'intégration tournent sur H2 (profil `test`), sans dépendance à une base locale.

## Structure

```
api/            Contrat d'API OpenAPI (source de vérité, B2)
backend/        Application Spring Boot (contrôleur / service / repository, DTO, B3)
frontend/       Application Next.js (écrans formateur / étudiant / relecteur)
docs/           Cahier des charges, diagrammes, journal
```

## Contraintes projet

- **B1** Java 17+, Maven, wrapper `mvnw` commité ✔
- **B2** Contrat `api/contrat.yaml` respecté à la lettre
- **B3** Séparation contrôleur / service / repository, DTO uniquement (aucune entité JPA en JSON)
- **B4** Validation + gestion d'erreurs centralisée `@RestControllerAdvice` (format `{code, message}`)
- **B5** Schéma versionné par migrations Flyway (`ddl-auto=update` interdit)
- **F1** Build frontend qui passe ✔
