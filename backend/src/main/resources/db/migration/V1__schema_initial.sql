-- V1 — Schéma initial (voir docs/diagrammes/D2-modele-donnees.md)

CREATE TABLE promotion (
    id   BIGSERIAL PRIMARY KEY,
    nom  VARCHAR(255) NOT NULL
);

CREATE TABLE formateur (
    id   BIGSERIAL PRIMARY KEY,
    nom  VARCHAR(255) NOT NULL
);

CREATE TABLE etudiant (
    id           BIGSERIAL PRIMARY KEY,
    nom          VARCHAR(255) NOT NULL,
    promotion_id BIGINT NOT NULL REFERENCES promotion (id)
);

CREATE TABLE session (
    id            BIGSERIAL PRIMARY KEY,
    titre         VARCHAR(255) NOT NULL,
    promotion_id  BIGINT NOT NULL REFERENCES promotion (id),
    formateur_id  BIGINT NOT NULL REFERENCES formateur (id),
    code          VARCHAR(10) NOT NULL UNIQUE,
    ouverture_at  TIMESTAMP NOT NULL,
    expiration_at TIMESTAMP NOT NULL,
    cloture_at    TIMESTAMP
);

CREATE TABLE presence (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT NOT NULL REFERENCES session (id),
    etudiant_id BIGINT NOT NULL REFERENCES etudiant (id),
    source      VARCHAR(20) NOT NULL CHECK (source IN ('ETUDIANT', 'FORMATEUR')),
    created_at  TIMESTAMP NOT NULL,

    CONSTRAINT uq_presence_session_etudiant UNIQUE (session_id, etudiant_id)
);

CREATE TABLE exercice (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT NOT NULL REFERENCES session (id),
    etudiant_id BIGINT NOT NULL REFERENCES etudiant (id),
    lien        VARCHAR(2048) NOT NULL,
    statut      VARCHAR(30) NOT NULL CHECK (statut IN ('DEPOSE', 'EN_ATTENTE_RELECTURE', 'RELU')),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,

    CONSTRAINT uq_exercice_session_etudiant UNIQUE (session_id, etudiant_id)
);

CREATE TABLE relecture (
    id           BIGSERIAL PRIMARY KEY,
    exercice_id  BIGINT NOT NULL UNIQUE REFERENCES exercice (id),
    relecteur_id BIGINT NOT NULL REFERENCES etudiant (id),
    note         INTEGER CHECK (note BETWEEN 0 AND 20),
    commentaire  TEXT,
    statut       VARCHAR(20) NOT NULL CHECK (statut IN ('EN_ATTENTE', 'RENDUE')),
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL
);

CREATE INDEX idx_session_promotion ON session (promotion_id);
CREATE INDEX idx_etudiant_promotion ON etudiant (promotion_id);
CREATE INDEX idx_presence_session ON presence (session_id);
CREATE INDEX idx_exercice_etudiant ON exercice (etudiant_id);
CREATE INDEX idx_relecture_relecteur ON relecture (relecteur_id);

-- Index manquants sur les autres clés étrangères (US-13 : « Index sur les FK et session.code »).
-- session.code est déjà indexé par sa contrainte UNIQUE ; relecture.exercice_id par UNIQUE(exercice_id).
CREATE INDEX idx_session_formateur ON session (formateur_id);
CREATE INDEX idx_presence_etudiant ON presence (etudiant_id);
CREATE INDEX idx_exercice_session ON exercice (session_id);
