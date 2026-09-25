-- V5 — RG11 (Q10) : horodatage de la dernière correction de note.
-- US-7 : un relecteur peut corriger sa note tant que la session n'est pas clôturée.
-- La première reddition (rendue_at, V4) est préservée ; modifieeAt trace seulement la dernière
-- correction. La contrainte RG12 (la note devient définitive à la clôture) est métier, pas base.

ALTER TABLE relecture ADD COLUMN modifiee_at TIMESTAMP;