# Guide d'installation — Gestion des prêts de matériel (BCM)

Application de gestion des prêts de matériel informatique développée dans le cadre du stage Licence 2 — Banque Centrale de Mauritanie.

## 1. Prérequis

| Outil | Version utilisée | Vérifier avec |
|---|---|---|
| Java (JDK) | 21 | `java --version` |
| PostgreSQL | 16+ (via Postgres.app) | icône PostgreSQL dans la barre de menu macOS |
| Node.js | 20+ | `node --version` |
| npm | 10+ | `npm --version` |

Aucune installation de Maven n'est nécessaire : le projet embarque son propre Maven Wrapper (`mvnw`).

## 2. Récupérer le projet

Le projet se trouve dans :
```
~/Downloads/gestion-prets-bcm
```

## 3. Base de données

### 3.1 Démarrer PostgreSQL
Ouvrir **Postgres.app** (icône dans la barre de menu macOS) et s'assurer qu'un serveur est démarré (ex: PostgreSQL 18).

### 3.2 Créer la base (si ce n'est pas déjà fait)
```bash
/Applications/Postgres.app/Contents/Versions/latest/bin/psql -U postgres -c "CREATE DATABASE gestion_prets;"
```

### 3.3 Charger un jeu de données de démonstration (optionnel)
```bash
cd ~/Downloads/gestion-prets-bcm
/Applications/Postgres.app/Contents/Versions/latest/bin/psql -U postgres -d gestion_prets -f demo-data.sql
```
Ce script peut être rejoué à tout moment : il réinitialise proprement les tables avant d'insérer des données cohérentes (employés, catégories, matériels, prêts en cours / en retard / retournés).

La configuration de connexion se trouve dans `src/main/resources/application.properties` (utilisateur/mot de passe `postgres`/`postgres` par défaut — à adapter si votre installation PostgreSQL utilise d'autres identifiants).

## 4. Lancer le backend (API Spring Boot)

```bash
cd ~/Downloads/gestion-prets-bcm
chmod +x mvnw        # une seule fois
./mvnw spring-boot:run
```

Le backend démarre sur **http://localhost:8080**.

Vérification rapide :
```bash
curl http://localhost:8080/api/employes
```
Doit renvoyer une liste JSON (vide ou avec des données si le script de démo a été chargé).

Hibernate crée/actualise automatiquement les tables au démarrage (`spring.jpa.hibernate.ddl-auto=update`), aucune migration manuelle n'est nécessaire au premier lancement.

## 5. Lancer le frontend (optionnel)

```bash
cd ~/Downloads/gestion-prets-bcm/frontend
npm install        # une seule fois, ou après ajout de dépendances
npm run dev
```

Le frontend démarre sur **http://localhost:5173** et communique automatiquement avec l'API sur le port 8080 (proxy configuré dans `vite.config.ts`).

Le backend doit être démarré **avant** le frontend pour que les données s'affichent.

## 6. Tester l'API

Trois méthodes possibles, au choix :

### Option A — fichier `api-tests.http` (recommandé, aucune installation)
Ouvrir `api-tests.http` dans VS Code avec l'extension **REST Client**, et cliquer sur "Send Request" au-dessus de chaque requête.

### Option B — Postman
Importer `postman_collection.json` dans Postman (variable `baseUrl` = `http://localhost:8080`).

### Option C — curl
```bash
curl http://localhost:8080/api/materiels/disponibles
```

## 7. Arrêter l'application

- Backend : `Ctrl+C` dans le terminal où `./mvnw spring-boot:run` tourne
- Frontend : `Ctrl+C` dans le terminal où `npm run dev` tourne
- PostgreSQL : via le menu de Postgres.app (optionnel, peut rester actif en permanence)

## 8. Résumé des ports

| Service | URL |
|---|---|
| API Backend | http://localhost:8080 |
| Frontend | http://localhost:5173 |
| PostgreSQL | localhost:5432 |
