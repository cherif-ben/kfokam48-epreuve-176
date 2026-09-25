# D3 — Diagramme de séquence : « marquer sa présence »

Le cas nominal et trois cas d'erreur, alignés exactement sur les codes HTTP du contrat
(`api/contrat.yaml`, opération `POST /api/presences`).

```mermaid
sequenceDiagram
    participant E as Étudiant
    participant F as Frontend
    participant API as PresenceController
    participant S as PresenceService
    participant DB as Base de données

    E->>F: Saisit le code de présence
    F->>API: POST /api/presences { code, etudiantId }
    API->>S: enregistrerPresence(code, etudiantId)
    S->>DB: rechercher la session par code

    alt Code inconnu
        DB-->>S: aucune session ne correspond à ce code
        S-->>API: CodeInconnuException
        API-->>F: 400 { code: "CODE_INCONNU", message: "..." }
        F-->>E: Affiche « code invalide »
    else Code expiré (RG1)
        DB-->>S: session trouvée, expirationAt dépassée
        S-->>API: CodeExpireException
        API-->>F: 410 { code: "CODE_EXPIRE", message: "Le code de présence a expiré." }
        F-->>E: Affiche « code expiré, demande un nouveau code »
    else Déjà présent (RG12)
        DB-->>S: présence déjà enregistrée pour (session, étudiant)
        S-->>API: DejaPresentException
        API-->>F: 409 { code: "DEJA_PRESENT", message: "..." }
        F-->>E: Affiche « tu es déjà marqué présent »
    else Cas nominal
        DB-->>S: session valide, pas de présence existante
        S->>DB: insérer Presence(source = ETUDIANT)
        DB-->>S: Presence créée
        S-->>API: Presence
        API-->>F: 201 { id, sessionId, etudiantId, source: "ETUDIANT" }
        F-->>E: Affiche « présence enregistrée »
    end
```

## Correspondance avec le contrat et les règles

| Branche | Code HTTP | Code d'erreur | Règle de gestion |
|---|---|---|---|
| Code inconnu | 400 | `CODE_INCONNU` | déduit du contrat |
| Code expiré | 410 | `CODE_EXPIRE` | RG1 |
| Déjà présent | 409 | `DEJA_PRESENT` | RG12 |
| Cas nominal | 201 | — | EF2 |

*Le blocage après 5 échecs (RG13, Q4) n'est pas représenté ici : il concerne l'accumulation
d'appels successifs, pas un seul appel — il fera l'objet d'un diagramme d'état ou d'une note
séparée si nécessaire, pas d'une branche de cette séquence.*