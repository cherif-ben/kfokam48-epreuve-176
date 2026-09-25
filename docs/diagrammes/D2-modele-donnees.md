# D2 — Diagramme de classes / modèle de données

Ce diagramme doit correspondre exactement aux migrations Flyway du backend (B5). Toute
modification du schéma après l'étape 3 devra être répercutée ici, dans un commit qui l'indique.

```mermaid
classDiagram
    class Promotion {
        +Long id
        +String nom
    }

    class Formateur {
        +Long id
        +String nom
    }

    class Etudiant {
        +Long id
        +String nom
        +Long promotionId
    }

    class Session {
        +Long id
        +String titre
        +Long promotionId
        +Long formateurId
        +String code
        +LocalDateTime ouvertureAt
        +LocalDateTime expirationAt
        +LocalDateTime clotureAt
    }

    class Presence {
        +Long id
        +Long sessionId
        +Long etudiantId
        +SourcePresence source
        +LocalDateTime createdAt
    }

    class Exercice {
        +Long id
        +Long sessionId
        +Long etudiantId
        +String lien
        +StatutExercice statut
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class Relecture {
        +Long id
        +Long exerciceId
        +Long relecteurId
        +Integer note
        +String commentaire
        +StatutRelecture statut
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class SourcePresence {
        <<enumeration>>
        ETUDIANT
        FORMATEUR
    }

    class StatutExercice {
        <<enumeration>>
        DEPOSE
        EN_ATTENTE_RELECTURE
        RELU
    }

    class StatutRelecture {
        <<enumeration>>
        EN_ATTENTE
        RENDUE
    }

    Promotion "1" --> "*" Etudiant : regroupe
    Promotion "1" --> "*" Session : concerne
    Formateur "1" --> "*" Session : ouvre
    Session "1" --> "*" Presence : enregistre
    Etudiant "1" --> "*" Presence : marque
    Session "1" --> "*" Exercice : reçoit
    Etudiant "1" --> "*" Exercice : dépose (auteur)
    Exercice "1" --> "1" Relecture : fait l'objet de
    Etudiant "1" --> "*" Relecture : relit (en tant que relecteur)
    Presence ..> SourcePresence : utilise
    Exercice ..> StatutExercice : utilise
    Relecture ..> StatutRelecture : utilise
```

## Notes de conception

- **`Relecture` est créée dès l'assignation** (au moment du dépôt de l'exercice), pas seulement à la
  réponse du relecteur — c'est ce qui permet à `RG8` (« l'exercice reste en attente tant que le
  relecteur n'a pas rendu sa relecture ») d'être vérifiable dans le tableau du formateur (`EF12`), même
  si le relecteur ne répond jamais.
- **Une contrainte d'unicité** `(session_id, etudiant_id)` sur `Presence` garantit `RG12` au niveau base
  de données, pas seulement en code métier.
- **Le relecteur (`Relecture.relecteurId`) référence `Etudiant.id`**, pas une table séparée : conformément
  à la décision de la section 2 du cahier des charges, le relecteur n'est pas un acteur distinct.
- **`Session.clotureAt`** (nullable) porte la règle `RG14` : tant qu'il est `null`, présences, dépôts et
  relectures restent modifiables.