-- V3 — RG13 (Q4) : compteur d'échecs de saisie de code par couple (étudiant, session).
-- Après 5 échecs consécutifs, la saisie est bloquée pendant 2 minutes. Une saisie réussie
-- réinitialise le compteur. Le blocage est propre à chaque couple, pas global.
-- (Le nom de colonne est `blocage_jusqua` — sans apostrophe — pour éviter tout souci de syntaxe
-- SQL sous H2/PostgreSQL.)

CREATE TABLE tentative_code (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT NOT NULL REFERENCES session (id),
    etudiant_id     BIGINT NOT NULL REFERENCES etudiant (id),
    echecs          INTEGER   NOT NULL DEFAULT 0,
    blocage_jusqua  TIMESTAMP,
    dernier_echec   TIMESTAMP NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,

    CONSTRAINT uq_tentative_session_etudiant UNIQUE (session_id, etudiant_id)
);

CREATE INDEX idx_tentative_session ON tentative_code (session_id);
CREATE INDEX idx_tentative_etudiant ON tentative_code (etudiant_id);
CREATE INDEX idx_tentative_blocage ON tentative_code (blocage_jusqua);