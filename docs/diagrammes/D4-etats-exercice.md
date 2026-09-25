# D4 — États-transitions du cycle de vie d'un exercice (bonus)

```mermaid
stateDiagram-v2
    [*] --> EN_ATTENTE : L'étudiant dépose son exercice<br/>(EF5 — POST /api/exercices)
    
    EN_ATTENTE --> EN_ATTENTE : L'étudiant remplace le lien<br/>(EF15 — Q13, RG10)
    
    EN_ATTENTE --> RELUE : Le relecteur rend sa note<br/>(EF8 — POST /api/relectures/{id})
    
    RELUE --> RELUE : Le relecteur modifie sa note<br/>(EF13 — Q10, RG11)
    
    RELUE --> EN_ATTENTE : Le relecteur retire sa note<br/>(Q10, avant clôture)
    
    EN_ATTENTE --> FIGE : Clôture de la session<br/>(EF12 — trou comblé)
    RELUE --> FIGE : Clôture de la session<br/>(EF12 — RG12)
    
    FIGE --> [*] : Note définitive
    
    note right of EN_ATTENTE
        Visible dans le tableau
        formateur comme
        "en attente" (Q11)
    end note
    
    note right of FIGE
        Aucune modification
        possible (RG12)
    end note