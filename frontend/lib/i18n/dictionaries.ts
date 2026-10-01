/**
 * Dictionnaires i18n — le français est la langue par défaut (public cible).
 * Structure à clés plates « domaine.cle » ; l'anglais doit couvrir exactement
 * les mêmes clés (vérifié par le typage `Record<TranslationKey, string>`).
 */
export const dictionaries = {
  fr: {
    // ——— Commun ———
    "common.loading": "Chargement…",
    "common.cancel": "Annuler",
    "common.confirm": "Confirmer",
    "common.select": "Sélectionner…",
    "common.status": "Statut",
    "common.theme.toLight": "Passer en mode clair",
    "common.theme.toDark": "Passer en mode sombre",

    // ——— Navigation ———
    "nav.home": "Accueil",
    "nav.sessions": "Sessions",
    "nav.dashboard": "Tableau",
    "nav.presence": "Présence",
    "nav.exercises": "Exercices",
    "nav.reviews": "Relectures",
    "nav.formateurArea": "Espace formateur",
    "nav.etudiantArea": "Espace étudiant",

    // ——— Accueil ———
    "home.badge": "Démonstration",
    "home.title": "Suivi de présence & relecture entre pairs",
    "home.titleHighlight": "relecture entre pairs",
    "home.subtitle":
      "Le formateur ouvre une session avec un code de présence, les étudiants marquent leur présence et déposent leurs exercices, et un relecteur tiré au hasard note le travail de son pair.",
    "home.formateur.title": "Formateur",
    "home.formateur.desc":
      "Ouvrir une session, diffuser le code, ajouter des présences, clôturer et consulter le tableau récapitulatif.",
    "home.presence.title": "Étudiant — Présence",
    "home.presence.desc": "Marquer sa présence avec le code fourni par le formateur.",
    "home.exercices.title": "Étudiant — Exercices",
    "home.exercices.desc": "Déposer le lien de son exercice et suivre ses relectures.",
    "home.relectures.title": "Relecteur — Relectures",
    "home.relectures.desc": "Relire le travail d'un pair et attribuer une note.",
    "home.footer": "Démonstration pédagogique — KFOKAM48",

    // ——— Tableau récapitulatif ———
    "tableau.title": "Tableau récapitulatif",
    "tableau.student": "Étudiant",
    "tableau.presences": "Présences",
    "tableau.submissions": "Exercices déposés",
    "tableau.average": "Moyenne",
    "tableau.pending": "Relectures en attente",
    "tableau.empty": "Aucun étudiant dans cette promotion.",

    // ——— Sessions ———
    "sessions.title": "Sessions",
    "sessions.empty": "Aucune session pour cette promotion.",
    "sessions.code": "Code",
    "sessions.openedAt": "Ouverture",
    "sessions.closedAt":
      "Clôturée le {date} — présences, dépôts et relectures verrouillés (RG14).",
    "sessions.close": "Clôturer la session",
    "sessions.closeWarning":
      "Clôturer cette session ? L'action est irréversible : plus aucune présence, dépôt ou relecture ne sera possible.",
    "sessions.addAttendance": "Ajouter une présence manuellement",
    "sessions.add": "Ajouter manuellement",
    "sessions.adding": "Ajout…",
    "sessions.addNote":
      "La présence sera marquée « ajouté par le formateur » (Q14). Un étudiant déjà présent sera refusé (409 DEJA_PRESENT).",
    "sessions.added": "Présence de {nom} ajoutée (marquée « ajouté par le formateur »).",

    // ——— Statuts ———
    "status.open": "Ouverte",
    "status.expired": "Expirée",
    "status.closed": "Clôturée",
    "status.submitted": "Déposé",
    "status.pendingReview": "En attente de relecture",
    "status.reviewed": "Relu",
    "status.pending": "En attente",
    "status.rendered": "Rendue",
    "source.etudiant": "étudiant",
    "source.formateur": "formateur",

    // ——— Nouvelle session ———
    "nouvelle.title": "Ouvrir une session",
    "nouvelle.cardTitle": "Nouvelle session",
    "nouvelle.openedTitle": "Session ouverte",
    "nouvelle.codeLabel": "Code de présence",
    "nouvelle.copy": "Copier le code",
    "nouvelle.copied": "Copié !",
    "nouvelle.validUntil": "Valable jusqu'à {date} (15 minutes après l'ouverture — RG1).",
    "nouvelle.open": "Ouvrir la session",
    "nouvelle.opening": "Ouverture…",
    "nouvelle.loadError": "Impossible de charger les promotions.",

    // ——— Formulaires ———
    "form.title": "Titre de la session",
    "form.titlePlaceholder": "Ex : Algorithmique — Séance 3",
    "form.promotion": "Promotion",
    "form.promotionPlaceholder": "Sélectionner une promotion",
    "form.code": "Code de présence",
    "form.codePlaceholder": "Ex : ABC123",
    "form.yourName": "Votre nom",
    "form.selectStudent": "Sélectionner un étudiant",
    "form.myName": "Mon nom",
    "form.session": "Session (ouvertes uniquement)",
    "form.link": "Lien de l'exercice",

    // ——— Présence étudiant ———
    "presence.title": "Marquer ma présence",
    "presence.myPromotion": "Ma promotion",
    "presence.submit": "Marquer ma présence",
    "presence.sending": "Envoi en cours…",
    "presence.success": "Présence enregistrée (source : {source}). À bientôt !",

    // ——— Exercices étudiant ———
    "exercices.title": "Mes exercices",
    "exercices.submitCard": "Déposer mon exercice",
    "exercices.submit": "Déposer",
    "exercices.submitting": "Dépôt…",
    "exercices.mySubmissions": "Mes dépôts",
    "exercices.pickName": "Sélectionnez votre nom ci-dessus.",
    "exercices.empty": "Aucun exercice déposé pour le moment.",
    "exercices.grade": "Note",
    "exercices.editLink": "Modifier le lien",
    "exercices.confirmReplace": "Confirmer le remplacement",
    "exercices.replaced": "Lien remplacé.",
    "exercices.deposited": "Exercice déposé — statut : {statut}",

    // ——— Relectures étudiant ———
    "relectures.title": "Mes relectures",
    "relectures.whoAmI": "Je suis…",
    "relectures.empty": "Aucune relecture assignée — profitez-en !",
    "relectures.givenGrade": "Note donnée",
    "relectures.review": "Relire",
    "relectures.correct": "Corriger la note",
    "relectures.grade": "Note (entière, 0–20)",
    "relectures.comment": "Commentaire",
    "relectures.send": "Envoyer",
    "relectures.sending": "Envoi…",
    "relectures.done": "Relecture enregistrée (note {note}/20).",
    "relectures.toReview": "Exercice à relire",
  },

  en: {
    // ——— Common ———
    "common.loading": "Loading…",
    "common.cancel": "Cancel",
    "common.confirm": "Confirm",
    "common.select": "Select…",
    "common.status": "Status",
    "common.theme.toLight": "Switch to light mode",
    "common.theme.toDark": "Switch to dark mode",

    // ——— Navigation ———
    "nav.home": "Home",
    "nav.sessions": "Sessions",
    "nav.dashboard": "Dashboard",
    "nav.presence": "Check-in",
    "nav.exercises": "Exercises",
    "nav.reviews": "Peer reviews",
    "nav.formateurArea": "Instructor area",
    "nav.etudiantArea": "Student area",

    // ——— Home ———
    "home.badge": "Demo",
    "home.title": "Attendance tracking & peer review",
    "home.titleHighlight": "peer review",
    "home.subtitle":
      "The instructor opens a session with an attendance code, students check in and submit their exercises, and a randomly assigned peer reviews the work.",
    "home.formateur.title": "Instructor",
    "home.formateur.desc":
      "Open a session, share the code, add attendance, close it and review the dashboard.",
    "home.presence.title": "Student — Check-in",
    "home.presence.desc": "Check in with the code shared by the instructor.",
    "home.exercices.title": "Student — Exercises",
    "home.exercices.desc": "Submit your exercise link and follow your reviews.",
    "home.relectures.title": "Reviewer — Peer reviews",
    "home.relectures.desc": "Review a peer's work and give a grade.",
    "home.footer": "Educational demo — KFOKAM48",

    // ——— Dashboard ———
    "tableau.title": "Overview dashboard",
    "tableau.student": "Student",
    "tableau.presences": "Attendance",
    "tableau.submissions": "Submissions",
    "tableau.average": "Average",
    "tableau.pending": "Pending reviews",
    "tableau.empty": "No students in this cohort.",

    // ——— Sessions ———
    "sessions.title": "Sessions",
    "sessions.empty": "No sessions for this cohort.",
    "sessions.code": "Code",
    "sessions.openedAt": "Opened",
    "sessions.closedAt":
      "Closed on {date} — check-ins, submissions and reviews are locked (RG14).",
    "sessions.close": "Close session",
    "sessions.closeWarning":
      "Close this session? This action is irreversible: no more check-ins, submissions or reviews will be possible.",
    "sessions.addAttendance": "Add attendance manually",
    "sessions.add": "Add manually",
    "sessions.adding": "Adding…",
    "sessions.addNote":
      "Attendance will be marked \"added by the instructor\" (Q14). A student who already checked in will be rejected (409 ALREADY_PRESENT).",
    "sessions.added": "{nom}'s attendance added (marked \"added by the instructor\").",

    // ——— Statuses ———
    "status.open": "Open",
    "status.expired": "Expired",
    "status.closed": "Closed",
    "status.submitted": "Submitted",
    "status.pendingReview": "Awaiting review",
    "status.reviewed": "Reviewed",
    "status.pending": "Pending",
    "status.rendered": "Completed",
    "source.etudiant": "student",
    "source.formateur": "instructor",

    // ——— New session ———
    "nouvelle.title": "Open a session",
    "nouvelle.cardTitle": "New session",
    "nouvelle.openedTitle": "Session opened",
    "nouvelle.codeLabel": "Attendance code",
    "nouvelle.copy": "Copy code",
    "nouvelle.copied": "Copied!",
    "nouvelle.validUntil": "Valid until {date} (15 minutes after opening — RG1).",
    "nouvelle.open": "Open the session",
    "nouvelle.opening": "Opening…",
    "nouvelle.loadError": "Unable to load cohorts.",

    // ——— Forms ———
    "form.title": "Session title",
    "form.titlePlaceholder": "e.g. Algorithms — Session 3",
    "form.promotion": "Cohort",
    "form.promotionPlaceholder": "Select a cohort",
    "form.code": "Attendance code",
    "form.codePlaceholder": "e.g. ABC123",
    "form.yourName": "Your name",
    "form.selectStudent": "Select a student",
    "form.myName": "My name",
    "form.session": "Session (open only)",
    "form.link": "Exercise link",

    // ——— Student check-in ———
    "presence.title": "Check in",
    "presence.myPromotion": "My cohort",
    "presence.submit": "Check in",
    "presence.sending": "Sending…",
    "presence.success": "Attendance saved (source: {source}). See you soon!",

    // ——— Student exercises ———
    "exercices.title": "My exercises",
    "exercices.submitCard": "Submit my exercise",
    "exercices.submit": "Submit",
    "exercices.submitting": "Submitting…",
    "exercices.mySubmissions": "My submissions",
    "exercices.pickName": "Select your name above.",
    "exercices.empty": "No submissions yet.",
    "exercices.grade": "Grade",
    "exercices.editLink": "Edit link",
    "exercices.confirmReplace": "Confirm replacement",
    "exercices.replaced": "Link replaced.",
    "exercices.deposited": "Exercise submitted — status: {statut}",

    // ——— Student peer reviews ———
    "relectures.title": "My peer reviews",
    "relectures.whoAmI": "I am…",
    "relectures.empty": "No reviews assigned — enjoy!",
    "relectures.givenGrade": "Grade given",
    "relectures.review": "Review",
    "relectures.correct": "Update grade",
    "relectures.grade": "Grade (integer, 0–20)",
    "relectures.comment": "Comment",
    "relectures.send": "Send",
    "relectures.sending": "Sending…",
    "relectures.done": "Review saved (grade {note}/20).",
    "relectures.toReview": "Exercise to review",
  },
} as const;

export type Lang = "fr" | "en";
export type TranslationKey = keyof (typeof dictionaries)["fr"];
