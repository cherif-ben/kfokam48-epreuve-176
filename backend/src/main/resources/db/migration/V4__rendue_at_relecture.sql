-- V4 — US-6 : horodatage de la relecture rendue (rendue_at).
-- La note est définitive dès que le relecteur rend (Q10) ; ce champ enregistre le moment de la reddition.

ALTER TABLE relecture ADD COLUMN rendue_at TIMESTAMP;