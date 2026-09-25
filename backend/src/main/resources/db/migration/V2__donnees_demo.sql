-- V2 — Données de démonstration (cahier des charges ENF2 : 60 étudiants)
-- Écrite en SQL portable (CTE récursive) : fonctionne sur PostgreSQL comme sur H2 (B6).

INSERT INTO promotion (nom) VALUES ('KFOKAM48');

INSERT INTO formateur (nom) VALUES
  ('Faroukou Cherif Ben'),
  ('KFOKAM48 Formateur');

INSERT INTO etudiant (nom, promotion_id)
WITH RECURSIVE seq(g) AS (
    SELECT 1
    UNION ALL
    SELECT g + 1 FROM seq WHERE g < 60
)
SELECT 'Etudiant ' || g, p.id
FROM seq
CROSS JOIN promotion p
WHERE p.nom = 'KFOKAM48';
