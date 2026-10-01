-- =====================================================================
-- Données de test — KFOKAM48
-- À exécuter APRÈS les migrations Flyway (db + backend démarrés).
-- Usage :
--   docker compose exec -T db psql -U kfokam48 -d kfokam48 < docs/donnees-test.sql
--
-- Trois scénarios :
--   S1 « Java avancé — promo 2026 »      : OUVERTE, code actif ~13 min (présence en direct)
--   S2 « Bases de données — semaine 38 » : PASSÉE (code expiré), dépôts + relectures variées
--   S3 « Algorithmique — TP noté »       : CLÔTURÉE, notes définitives (RG12)
--
-- Idempotent : ON CONFLICT DO NOTHING partout, ré-exécutable sans erreur.
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- S1 — Session OUVERTE : code actif pour marquer la présence en direct
-- ---------------------------------------------------------------------
INSERT INTO session (id, titre, promotion_id, formateur_id, code, ouverture_at, expiration_at, cloture_at)
VALUES (
  9001, 'Java avancé — promo 2026', 1, 1,
  'JAV742',
  date_trunc('minute', now()) - interval '2 minutes',
  date_trunc('minute', now()) + interval '13 minutes',
  NULL
)
ON CONFLICT (id) DO NOTHING;

-- Étudiants 2..6 déjà présents (pointés par le formateur, EF5)
INSERT INTO presence (session_id, etudiant_id, source, created_at)
SELECT 9001, e.id, 'FORMATEUR', now() - interval '2 minutes'
FROM etudiant e
WHERE e.id BETWEEN 2 AND 6
ON CONFLICT (session_id, etudiant_id) DO NOTHING;

-- ---------------------------------------------------------------------
-- S2 — Session PASSÉE : code expiré (→ CODE_EXPIRE sur saisie), dépôts possibles
-- ---------------------------------------------------------------------
INSERT INTO session (id, titre, promotion_id, formateur_id, code, ouverture_at, expiration_at, cloture_at)
VALUES (
  9002, 'Bases de données — semaine 38', 1, 1,
  'BDD507',
  now() - interval '3 hours',
  now() - interval '2 hours 45 minutes',
  NULL
)
ON CONFLICT (id) DO NOTHING;

-- Étudiants 2..13 présents à S2
INSERT INTO presence (session_id, etudiant_id, source, created_at)
SELECT 9002, e.id, 'ETUDIANT', now() - interval '2 hours 50 minutes'
FROM etudiant e
WHERE e.id BETWEEN 2 AND 13
ON CONFLICT (session_id, etudiant_id) DO NOTHING;

-- Exercices RELUS (étudiants 2..7)
INSERT INTO exercice (session_id, etudiant_id, lien, statut, created_at, updated_at)
SELECT 9002, e.id,
       'https://github.com/etudiant-' || e.id || '/exercice-bdd',
       'RELU',
       now() - interval '2 hours 30 minutes',
       now() - interval '1 hour'
FROM etudiant e
WHERE e.id BETWEEN 2 AND 7
ON CONFLICT (session_id, etudiant_id) DO NOTHING;

-- Exercices EN_ATTENTE_RELECTURE (étudiants 8 et 9)
INSERT INTO exercice (session_id, etudiant_id, lien, statut, created_at, updated_at)
SELECT 9002, e.id,
       'https://github.com/etudiant-' || e.id || '/exercice-bdd',
       'EN_ATTENTE_RELECTURE',
       now() - interval '2 hours',
       now() - interval '2 hours'
FROM etudiant e
WHERE e.id IN (8, 9)
ON CONFLICT (session_id, etudiant_id) DO NOTHING;

-- Exercices DEPOSE sans relecteur (étudiants 10..13)
INSERT INTO exercice (session_id, etudiant_id, lien, statut, created_at, updated_at)
SELECT 9002, e.id,
       'https://github.com/etudiant-' || e.id || '/exercice-bdd',
       'DEPOSE',
       now() - interval '1 hour 30 minutes',
       now() - interval '1 hour 30 minutes'
FROM etudiant e
WHERE e.id BETWEEN 10 AND 13
ON CONFLICT (session_id, etudiant_id) DO NOTHING;

-- Relectures EN_ATTENTE pour les exercices EN_ATTENTE_RELECTURE (relecteur ≠ auteur, parmi les présents)
INSERT INTO relecture (exercice_id, relecteur_id, note, commentaire, statut, created_at, updated_at, rendue_at, modifiee_at)
SELECT ex.id,
       p.etudiant_id,
       NULL,
       NULL,
       'EN_ATTENTE',
       ex.created_at,
       ex.created_at,
       NULL,
       NULL
FROM exercice ex
JOIN presence p
  ON p.session_id = ex.session_id
 AND p.etudiant_id <> ex.etudiant_id
WHERE ex.session_id = 9002
  AND ex.statut = 'EN_ATTENTE_RELECTURE'
  AND p.etudiant_id = (SELECT MIN(p2.etudiant_id) FROM presence p2
                       WHERE p2.session_id = ex.session_id AND p2.etudiant_id <> ex.etudiant_id)
  AND NOT EXISTS (SELECT 1 FROM relecture r WHERE r.exercice_id = ex.id)
ON CONFLICT (exercice_id) DO NOTHING;

-- Relectures RENDUES pour les exercices RELU, notes variées (RG3 : entiers 0-20)
INSERT INTO relecture (exercice_id, relecteur_id, note, commentaire, statut, created_at, updated_at, rendue_at, modifiee_at)
SELECT ex.id,
       p.etudiant_id,
       CASE (ex.etudiant_id % 5)
         WHEN 0 THEN 12 WHEN 1 THEN 14 WHEN 2 THEN 15 WHEN 3 THEN 16 ELSE 17
       END,
       'Bon travail dans l''ensemble, quelques points à approfondir sur l''indexation.',
       'RENDUE',
       ex.created_at,
       now() - interval '1 hour',
       now() - interval '1 hour',
       NULL
FROM exercice ex
JOIN presence p
  ON p.session_id = ex.session_id
 AND p.etudiant_id <> ex.etudiant_id
WHERE ex.session_id = 9002
  AND ex.statut = 'RELU'
  AND p.etudiant_id = (SELECT MIN(p2.etudiant_id) FROM presence p2
                       WHERE p2.session_id = ex.session_id AND p2.etudiant_id <> ex.etudiant_id)
  AND NOT EXISTS (SELECT 1 FROM relecture r WHERE r.exercice_id = ex.id)
ON CONFLICT (exercice_id) DO NOTHING;

-- V5 / RG11 (Q10) : démo de correction de note — la première relecture RENDUE de S2
-- voit sa note corrigée en 18, avec modifiee_at renseigné.
UPDATE relecture
SET note = 18,
    commentaire = 'Note corrigée après révision : excellent travail sur l''indexation.',
    modifiee_at = now() - interval '30 minutes',
    updated_at = now() - interval '30 minutes'
WHERE id = (
  SELECT r.id
  FROM relecture r
  JOIN exercice ex ON ex.id = r.exercice_id
  WHERE ex.session_id = 9002
    AND r.statut = 'RENDUE'
  ORDER BY r.id
  LIMIT 1
)
AND modifiee_at IS NULL;

-- ---------------------------------------------------------------------
-- S3 — Session CLÔTURÉE : toutes les opérations verrouillées (RG12 / RG14)
-- ---------------------------------------------------------------------
INSERT INTO session (id, titre, promotion_id, formateur_id, code, ouverture_at, expiration_at, cloture_at)
VALUES (
  9003, 'Algorithmique — TP noté', 1, 1,
  'ALGO318',
  now() - interval '26 hours',
  now() - interval '25 hours 45 minutes',
  now() - interval '24 hours'
)
ON CONFLICT (id) DO NOTHING;

-- Étudiants 2..11 présents à S3
INSERT INTO presence (session_id, etudiant_id, source, created_at)
SELECT 9003, e.id, 'ETUDIANT', now() - interval '25 hours 50 minutes'
FROM etudiant e
WHERE e.id BETWEEN 2 AND 11
ON CONFLICT (session_id, etudiant_id) DO NOTHING;

-- 8 exercices (étudiants 2..9), tous relus
INSERT INTO exercice (session_id, etudiant_id, lien, statut, created_at, updated_at)
SELECT 9003, e.id,
       'https://github.com/etudiant-' || e.id || '/tp-algorithmique',
       'RELU',
       now() - interval '25 hours 30 minutes',
       now() - interval '24 hours 30 minutes'
FROM etudiant e
WHERE e.id BETWEEN 2 AND 9
ON CONFLICT (session_id, etudiant_id) DO NOTHING;

-- Relectures RENDUES, notes variées 10-19
INSERT INTO relecture (exercice_id, relecteur_id, note, commentaire, statut, created_at, updated_at, rendue_at, modifiee_at)
SELECT ex.id,
       p.etudiant_id,
       10 + (ex.etudiant_id % 10),
       'Relecture du TP : logique correcte, penser à factoriser les cas répétitifs.',
       'RENDUE',
       ex.created_at,
       now() - interval '24 hours 30 minutes',
       now() - interval '24 hours 30 minutes',
       NULL
FROM exercice ex
JOIN presence p
  ON p.session_id = ex.session_id
 AND p.etudiant_id <> ex.etudiant_id
WHERE ex.session_id = 9003
  AND ex.statut = 'RELU'
  AND p.etudiant_id = (SELECT MIN(p2.etudiant_id) FROM presence p2
                       WHERE p2.session_id = ex.session_id AND p2.etudiant_id <> ex.etudiant_id)
  AND NOT EXISTS (SELECT 1 FROM relecture r WHERE r.exercice_id = ex.id)
ON CONFLICT (exercice_id) DO NOTHING;

COMMIT;

-- =====================================================================
-- Récapitulatif des données injectées (à titre de contrôle) :
--   S1 (ouverte)   : code JAV742, présence possible pour Étudiant 7..60
--   S2 (passée)    : 12 présents, 6 exercices relus (notes 12-18),
--                    2 en attente de relecture, 4 simplement déposés
--   S3 (clôturée)  : 10 présents, 8 exercices relus, notes 10-19 définitives
-- =====================================================================
