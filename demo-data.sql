-- Jeu de données de démonstration — Gestion des prêts de matériel (BCM)
-- À exécuter une fois l'application démarrée au moins une fois (tables créées par Hibernate).
--
-- Usage :
--   /Applications/Postgres.app/Contents/Versions/latest/bin/psql -U postgres -d gestion_prets -f demo-data.sql

-- Réinitialisation (ordre inverse des dépendances), pour pouvoir rejouer ce script plusieurs fois
TRUNCATE TABLE prets RESTART IDENTITY CASCADE;
TRUNCATE TABLE materiels RESTART IDENTITY CASCADE;
TRUNCATE TABLE employes RESTART IDENTITY CASCADE;
TRUNCATE TABLE categories RESTART IDENTITY CASCADE;

-- Catégories (section 4 du sujet)
INSERT INTO categories (code, libelle) VALUES
  ('ORDI', 'Ordinateur portable'),
  ('ECR', 'Écran'),
  ('TEL', 'Téléphone'),
  ('VPJ', 'Vidéoprojecteur'),
  ('DSQ', 'Disque externe');

-- Employés
INSERT INTO employes (matricule, nom, departement, email) VALUES
  ('MAT001', 'Ahmed Sidi', 'Informatique', 'ahmed.sidi@bcm.mr'),
  ('MAT002', 'Fatma Mint Salem', 'Finance', 'fatma.salem@bcm.mr'),
  ('MAT003', 'Mohamed Lemine', 'Ressources Humaines', 'mohamed.lemine@bcm.mr'),
  ('MAT004', 'Aichetou Bilal', 'Audit Interne', 'aichetou.bilal@bcm.mr'),
  ('MAT005', 'Sidi Ould Cheikh', 'Informatique', NULL);

-- Matériels (categorie_id correspond à l'ordre d'insertion ci-dessus : 1=Ordi, 2=Ecran, 3=Tel, 4=VPJ, 5=Disque)
INSERT INTO materiels (numero_inventaire, designation, categorie_id, marque, modele, etat_physique) VALUES
  ('INV001', 'Dell Latitude', 1, 'Dell', 'Latitude 5440', 'Bon'),
  ('INV002', 'HP EliteBook', 1, 'HP', '840 G9', 'Bon'),
  ('INV003', 'Lenovo ThinkPad', 1, 'Lenovo', 'T14', 'Usé'),
  ('INV004', 'Écran Dell 24"', 2, 'Dell', 'P2422H', 'Bon'),
  ('INV005', 'Écran Samsung 27"', 2, 'Samsung', 'S27A600', 'Bon'),
  ('INV006', 'iPhone de service', 3, 'Apple', 'iPhone 13', 'Bon'),
  ('INV007', 'Vidéoprojecteur Epson', 4, 'Epson', 'EB-X06', 'Bon'),
  ('INV008', 'Disque dur externe 2To', 5, 'Seagate', 'Expansion', 'Bon');

-- Prêts : un mélange couvrant tous les cas du sujet (en cours, en retard, retourné)
-- employe_id / materiel_id correspondent à l'ordre d'insertion ci-dessus

-- En cours, dans les temps
INSERT INTO prets (materiel_id, employe_id, date_pret, date_retour_prevue, date_retour_effective, statut, commentaire) VALUES
  (1, 1, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '10 days', NULL, 'EN_COURS', 'Mission terrain');

-- En cours, mais EN RETARD (date prévue dépassée) — pour démontrer /api/prets/en-retard
INSERT INTO prets (materiel_id, employe_id, date_pret, date_retour_prevue, date_retour_effective, statut, commentaire) VALUES
  (6, 4, CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE - INTERVAL '5 days', NULL, 'EN_COURS', 'Audit externe');

-- Retourné (historique)
INSERT INTO prets (materiel_id, employe_id, date_pret, date_retour_prevue, date_retour_effective, statut, commentaire) VALUES
  (7, 2, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '23 days', CURRENT_DATE - INTERVAL '24 days', 'RETOURNE', 'Présentation trimestrielle');

-- Retourné (historique, deuxième exemple sur le même matériel pour illustrer l'historique par matériel)
INSERT INTO prets (materiel_id, employe_id, date_pret, date_retour_prevue, date_retour_effective, statut, commentaire) VALUES
  (7, 5, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '53 days', CURRENT_DATE - INTERVAL '55 days', 'RETOURNE', 'Formation interne');

-- Matériels 2, 3, 4, 5, 8 restent sans prêt actif -> visibles dans /api/materiels/disponibles
