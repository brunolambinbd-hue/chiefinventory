# Guide d'Obtention et de Configuration des Clés Google Images API (Creative Commons)

Ce document décrit la procédure complète pour obtenir, configurer et utiliser l'**API Google Images** (*Google Custom Search JSON API*) afin d'alimenter automatiquement le carnet de recettes de ChiefInventory avec des **photos culinaires HD libres de droits** enregistrées localement dans l'application (`app/src/main/assets/recipe_images/`).

---

## 🎯 1. Vue d'Ensemble & Quotas Gratuits

Google met à disposition une API de recherche d'images gratuite offrant un quota journalier de **100 requêtes d'images par jour**.

### Les 2 Identifiants Requis :
1. **`GOOGLE_SEARCH_API_KEY`** : La clé d'authentification auprès des services Google Cloud.
2. **`GOOGLE_SEARCH_CX`** : L'identifiant unique de votre Moteur de Recherche d'Images Programmable.

---

## 🔑 2. Étape 1 : Obtenir la Clé API (`GOOGLE_SEARCH_API_KEY`)

1. Connectez-vous à la **Google Cloud Console** :  
   👉 [console.cloud.google.com](https://console.cloud.google.com/)
2. En haut de la page, ouvrez le sélecteur de projets et cliquez sur **Nouveau Projet** (nommez-le *"ChefInventory"*).
3. Dans le menu de navigation latéral : **APIs et services** ➔ **Bibliothèque** (*Library*).
4. Dans la barre de recherche de la bibliothèque, tapez **`Custom Search API`**, sélectionnez-la et cliquez sur le bouton **Activer** (*Enable*).
5. Une fois activée, allez dans le menu **APIs et services** ➔ **Identifiants** (*Credentials*).
6. Cliquez sur **+ Créer des identifiants** (en haut) ➔ **Clé d'API** (*API Key*).
7. Copiez la clé générée (format : `AIzaSyB...`).  
   👉 C'est votre variable **`GOOGLE_SEARCH_API_KEY`** !

---

## ⚙️ 3. Étape 2 : Créer le Moteur de Recherche d'Images (`GOOGLE_SEARCH_CX`)

1. Connectez-vous à la console du **Moteur de Recherche Programmable Google** :  
   👉 [programmablesearchengine.google.com](https://programmablesearchengine.google.com/)
2. Cliquez sur le bouton **Ajouter** (*Add*).
3. **Nom du moteur de recherche :** *"Recherche Images Recettes ChefInventory"*.
4. **Que rechercher ? :** Cochez l'option **"Rechercher dans tout le Web"** (*Search the entire web*).
5. **Recherche d'images :** Activez le commutateur **"Recherche d'images"** (*Image search*).
6. Cliquez sur **Créer**.
7. Dans la vue d'ensemble du moteur créé, repérez l'**ID du moteur de recherche** (format : `a1b2c3d4e5f6...`).  
   👉 C'est votre variable **`GOOGLE_SEARCH_CX`** !

---

## 💻 4. Étape 3 : Configuration des Variables sur votre PC

Pour que le script Python de téléchargement d'images détecte automatiquement vos clés sans les exposer dans le code source Git, définissez les variables d'environnement sur votre machine.

### Sous Windows (Invite de commande `cmd`) :
```cmd
set GOOGLE_SEARCH_API_KEY=votre_clé_aizasyb...
set GOOGLE_SEARCH_CX=votre_id_cx_a1b2c3...
```

### Sous Windows (PowerShell) :
```powershell
$env:GOOGLE_SEARCH_API_KEY="votre_clé_aizasyb..."
$env:GOOGLE_SEARCH_CX="votre_id_cx_a1b2c3..."
```

### Sous Linux / macOS :
```bash
export GOOGLE_SEARCH_API_KEY="votre_clé_aizasyb..."
export GOOGLE_SEARCH_CX="votre_id_cx_a1b2c3..."
```

---

## 🚀 5. Exécution du Script Python de Téléchargement

Une fois les variables configurées sur votre terminal, lancez le programme Python de téléchargement automatique :

```bash
python tools/download_all_recipe_images.py
```

### Fonctionnalités du Script :
* 🛡️ **Licence Libre Garantie :** Le script applique automatiquement le filtre `rights=cc_publicdomain,cc_attribute,cc_sharealike` et `imgType=photo` pour ne télécharger que de véritables photographies libres de droits.
* 🧠 **Simplification Progressive des Titres :** Si une recherche exacte échoue (ex: *"Pizza au chèvre et aux lardons"*), le script épure le titre (*"Pizza au chèvre"*) pour trouver automatiquement la photo HD.
* ⚡ **100% Mode Hors-Ligne (*Offline-First*) :** Les images sont téléchargées, optimisées et enregistrées dans `app/src/main/assets/recipe_images/` pour s'afficher en 0 ms dans l'application.
