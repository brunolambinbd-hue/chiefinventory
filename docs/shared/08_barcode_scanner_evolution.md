# Spécification Technique & Feuille de Route
## Évolution ChefInventory : Scan de Code-Barres EAN13 & Intégration Open Food Facts

**Référence :** `EVO-2025-01`  
**Statut :** Proposé / Prochaine Évolution Majeure  
**Cible :** Application Android `ChefInventory`  

---

## 1. Synthèse Executive & Valeur Ajoutée Utilisateur

L'objectif de cette évolution est d'ajouter un module de **scan de code-barres (EAN13)** à l'application `ChefInventory`, couplé à l'API publique et gratuite **Open Food Facts**.

### 🌟 Valeur Ajoutée pour l'Utilisateur :
1. **Au Magasin (Pendant les courses - Fonctionnalité Anti-Doublon)** :
   - Flasher un produit en rayon pour savoir immédiatement si l'aliment est déjà présent dans le stock de la maison (*Frigo*, *Placard*, *Surgélateur*, *Cave*).
   - Évite les achats inutiles, réduit le gaspillage et fait faire des économies.
2. **Au Rangement des Courses (Entrée en stock ultra-rapide)** :
   - Réduction du temps de saisie d'un nouvel ingrédient de **30 secondes à 1 seconde**.
   - Pré-remplissage automatique : Nom du produit, marque, catégorie, unité, conditionnement et visuel de l'emballage.
3. **En Cuisine (Recherche de recettes & Matching)** :
   - Alimentation instantanée du stock pour débloquer automatiquement les recettes cuisinables via `MatchRecipeInventoryActivity`.

---

## 2. Cas d'Usage Principaux (User Stories)

### US-01 : Vérification de Stock en Magasin (Anti-Doublon)
> **En tant qu'** utilisateur faisant mes courses en magasin,  
> **Je veux** scanner le code-barres d'un produit avec mon smartphone,  
> **Afin de** savoir immédiatement si j'en ai déjà en stock chez moi et en quelle quantité.

### US-02 : Entrée en Stock Instantanée au Rangement
> **En tant qu'** utilisateur déballant mes courses à la maison,  
> **Je veux** flasher les codes-barres de mes achats,  
> **Afin de** les ajouter automatiquement à mon garde-manger sans rien taper au clavier.

---

## 3. Architecture Technique Proposée

```
[ Caméra Smartphone (CameraX) ]
              │
              ▼ (Flux vidéo temps réel)
[ ML Kit Barcode Scanning (Google) ] ──> Détection EAN13 (< 50ms, hors-ligne)
              │
              ▼ (Code-barres EAN13)
[ OpenFoodFactsRepository ] ───────────> API REST JSON (Open Food Facts v2)
              │
              ▼ (DTO Produit)
[ Mapper ChefInventory ] ──────────────> Ingredient(Name, Category, Unit, Location)
              │
              ▼
[ AppDatabase Room (SQLite) ]
```

### 3.1 Composants Techniques :
1. **Détection du Code-Barres** : `com.google.mlkit:barcode-scanning`
   - Détection en temps réel sur le flux vidéo via **CameraX**.
   - 100% hors-ligne, ultra-rapide et gratuit.
2. **Résolution des Données Produit** : **API Open Food Facts v2**
   - Endpoint : `https://world.openfoodfacts.org/api/v2/product/{code_barre}.json`
   - Données extraites :
     - `product_name_fr` / `product_name` ➔ `Ingredient.name`
     - `categories_tags` ➔ `Ingredient.category` & résolution automatique de l'emplacement (*Frigo*, *Placard*, *Surgélateur*, *Cave*)
     - `quantity` / `product_quantity` ➔ `Ingredient.quantity` & `Ingredient.unit`
     - `image_front_url` ➔ `Ingredient.imageUri`
3. **Pliabilité avec l'existant ChefInventory** :
   - Réutilisation du moteur de résolution des emplacements `IngredientAdapter.resolveLocationName()` pour assigner l'emplacement par défaut.
   - Journalisation défensive et traçabilité via `TraceabilityLib` (`LogManager.uException()`, `@CoveredRequirement`).

---

## 4. Plan d'Implémentation Étape par Étape

### Étape 1 : Dépendances & Configuration Gradle
Ajouter les dépendances dans `app/build.gradle.kts` et `libs.versions.toml` :
- `com.google.mlkit:barcode-scanning`
- `com.squareup.retrofit2:retrofit` & `converter-gson` (ou Ktor client)

### Étape 2 : Création de la `BarcodeScannerActivity` (CameraX + ML Kit)
- Vue caméra plein écran avec cadre de visée.
- Analyse continue du flux vidéo via `ImageAnalysis` CameraX.
- Bip sonore/vibration haptique dès qu'un EAN13 valide est détecté.

### Étape 3 : Service API & Repository `OpenFoodFactsRepository`
- Gestion des requêtes réseau asynchrones (Coroutines Kotlin / Flow).
- Gestion du mode hors-ligne et des erreurs réseau via `LogManager.eException()`.

### Étape 4 : Intégration UI & Boutons d'Action
- **Dans `IngredientListActivity` & `MainActivity`** : Ajout d'un bouton d'action rapide "Scan Code-Barres" dans la barre de recherche ou en FAB.
- **Dans `EditIngredientActivity`** : Ajout d'un bouton de remplissage automatique par scan de code-barres.

### Étape 5 : Tests & Qualité
- Tests unitaires du parser Open Food Facts (`OpenFoodFactsParserTest`).
- Tests de couverture JaCoCo 100% sur les nouveaux Mappers et ViewModels.
- Validation des critères de traçabilité TDO (`@CoveredRequirement`).

---

## 5. Indicateurs de Succès & ROI (KPI)

| Indicateur (KPI) | Avant (Saisie manuelle / OCR) | Après (Scan Code-Barres) |
| :--- | :--- | :--- |
| **Temps d'ajout d'un produit** | ~30 secondes | **~1 à 2 secondes** |
| **Erreurs de saisie** | ~10-15% (fautes de frappe) | **< 1% (données BDD officielles)** |
| **Utilisabilité en magasin** | Faible (trop long) | **Excellente (instant anti-doublon)** |
