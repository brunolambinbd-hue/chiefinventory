# 🍳 ChefInventory — Gestionnaire Intelligent de Garde-Manger & Recettes

> **ChefInventory** est une application Android native d'ingénierie culinaire conçue pour les chefs et cuisiniers exigeants. Elle automatise la gestion du Garde-Manger, l'importation et la reconnaissance OCR de recettes, l'appariement intelligent des stocks ("Que Cuisiner ?") et le suivi de la traçabilité Open Food Facts.

---

## 🌟 Fonctionnalités Principales

### 🧊 1. Garde-Manger & Gestion de Stock
- Suivi en temps réel des quantités, unités (*g, L, pièces*) et seuils d'alerte de réassort.
- Classement par emplacements physiques personnalisés (*Frigo, Placard, Surgélateur, Cave*).
- Gestion de la politique de stock (*Permanent* ou *Ponctuel*).

### 🏷️ 2. Intégration Open Food Facts v2 & Catalogue (5 003 Ingrédients)
- **Catalogue Officiel OFF** : Consultation et recherche dans la taxonomie française de 5 003 ingrédients.
- **Classification Écobalyse en Français Clair** : *Fruits & Légumes, Produits Laitiers, Huiles & Graisses, Viandes Rouges, Volailles, Poissons, Épices, Céréales, Boissons, Épicerie*.
- **Fiche Détaillée** : Affichage des caractéristiques officielles (Dénomination Ciqual/Anses, Poids unitaire moyen, Saisonnalité en France, Empreinte Carbone ADEME, Parent ontologique, Synonymes & Pluriels).
- **Scanner Code-Barres Caméra** : Viseur caméra graphique Google Code Scanner / ML Kit avec sélecteur 2 Modes :
  - 🟢 **Mode Entrée (Rangement Courses)**
  - 🔴 **Mode Sortie (Consommation Cuisine)**
- **Édition Autonome Produits Code-Barres** (`openfoodfacts_products` table).

### 📷 3. Moteur OCR & Importation Web de Recettes
- Moteur de reconnaissance optique de recettes par machine à états (`RecipeOcrParser`) en 5 étapes.
- Importation web universelle de recettes avec détection automatique du titre, des portions, des temps, des ingrédients et des instructions.
- Dictionnaire culinaire d'autorité de plus de 250 verbes d'action de la gastronomie française.

### 🍳 4. Rapprochement Intelligent "Que Cuisiner ?"
- Moteur d'appariement qui croise vos stocks réels du Garde-Manger avec les recettes enregistrées pour calculer instantanément le taux de réalisation (100%, 80%, <50%).

---

## 🏛️ Architecture Modulaire des Ressources (`sourceSets`)

Pour garantir une clarté absolue et une étanchéité par domaine fonctionnel, les ressources XML sont découpées sous `app/src/main/` en 4 répertoires distincts :

```text
📂 app/src/main/
│
├── 📂 res/                                <-- 🎨 1. DOMAINE IHM GÉNÉRALE & GRAPHISME
│   └── 📂 values/
│       ├── 📄 strings.xml                 (Toasts, Dialogues généraux, Titres d'activités)
│       ├── 📄 log_strings.xml             (Messages de traçabilité Logcat)
│       ├── 📄 colors.xml                  (Palette de couleurs de l'application)
│       └── 📄 themes.xml                  (Thèmes Material Design & Splashscreen)
│
├── 📂 res-pantry/                         <-- 🧊 2. DOMAINE GARDE-MANGER & STOCK
│   └── 📂 values/
│       └── 📄 strings_pantry_recipes.xml  (Libellés du Garde-Manger, Emplacements, Alertes Stock)
│
├── 📂 res-catalog/                        <-- 🥬 3. DOMAINE CATALOGUES & INITIALISATION
│   └── 📂 values/
│       ├── 📄 standard_catalog_ingredients.xml (~180 ingrédients types de stock initial)
│       └── 📄 symbols.xml                 (Symboles et caractères spéciaux)
│
└── 📂 res-ocr/                            <-- 🤖 4. DOMAINE VOCABULAIRE & DICTIONNAIRES OCR
    └── 📂 values/
        ├── 📄 ocr_shared_ingredients.xml  (Ingrédients courants sans quantité)
        ├── 📄 ocr_shared_step_keywords.xml (Verbes d'action culinaires)
        ├── 📄 ocr_step_keywords_complement.xml (Verbes d'action complémentaires)
        ├── 📄 ocr_shared_units.xml        (Unités de mesure culinaires)
        ├── 📄 ocr_shared_modifiers.xml    (Modificateurs de préparation)
        └── 📄 ocr_step1_*.xml à ocr_step5_*.xml (Règles, Vins, Marques & Exclusions)
```

### Configuration Gradle (`app/build.gradle.kts`)
```kotlin
android {
    ...
    sourceSets {
        getByName("main") {
            res.srcDirs(
                "src/main/res",
                "src/main/res-pantry",
                "src/main/res-catalog",
                "src/main/res-ocr"
            )
        }
    }
}
```

---

## 🛠️ Stack Technique

- **Langage** : Kotlin (JVM 18 / Target SDK 35)
- **Base de Données** : Android Room SQLite (Architecture MVVM + LiveData, Version 37)
- **Réseau & Cache** : API REST Open Food Facts v2 v2, Coil (Chargement images HD)
- **Vision & Caméra** : Google Code Scanner (`play-services-code-scanner`), CameraX, ML Kit, MediaPipe
- **Architecture** : Clean Architecture, Data-Driven Design, Material Components 3

---

## 🟢 Qualimétrie & Couverture de Tests

- **Tests Unitaires** : **752/752 tests validés avec 100% de succès (0 échec)**.
- **Rapport de Couverture** : Exécution via `./gradlew :app:fullCoverageReport` (Rapport JaCoCo XML & HTML).

---

## 📄 Licence
Projet développé sous licence propriétaire pour ChefInventory.
