# 📘 Guide Développeur : Extension du Vocabulaire OCR & Gestion des Ressources XML

Ce document décrit la procédure d'ingénierie pas-à-pas pour étendre ou ajouter de nouvelles ressources XML de vocabulaire métier (`<string-array>` et `<string>`) dans l'application **ChefInventory**.

---

## 🏛️ Architecture Data-Driven du Vocabulaire OCR

L'application consomme l'intégralité du vocabulaire métier via la classe de domaine Kotlin pure [OcrVocabulary.kt](file:///C:/Users/bruno/AndroidStudioProjects/ChiefInventory/app/src/main/java/com/chefinventory/app/utils/OcrVocabulary.kt) et son chargeur récursif [OcrVocabularyLoader.kt](file:///C:/Users/bruno/AndroidStudioProjects/ChiefInventory/app/src/main/java/com/chefinventory/app/utils/OcrVocabularyLoader.kt).

Les ressources XML sont ventilées dans 4 dossiers de domaines sous `app/src/main/` :
- 🎨 **`res/values/`** : Textes généraux IHM, Toasts, Thèmes & Couleurs.
- 🧊 **`res-pantry/values/`** : Textes du Garde-Manger & Stock.
- 🥬 **`res-catalog/values/`** : Ingrédients du catalogue initial & symboles.
- 🤖 **`res-ocr/values/`** : Tous les dictionnaires, en-têtes et verbes d'action du pipeline OCR.

---

## 📋 Procédures d'Extension Pas-à-Pas

### 1. Ajout d'un nouvel élément dans un dictionnaire XML existant (`<string-array>`)
1. Ouvrir le fichier XML cible sous `app/src/main/res-ocr/values/` (ex: `ocr_shared_ingredients.xml`, `ocr_step1_spelling_repairs.xml`).
2. Ajouter la nouvelle entrée sous la balise `<string-array>` ou `<string>`.
   - *Note pour les dictionnaires de réparation :* Respecter scrupuleusement le format `"fautif|correct"` (ex: `<item>càs|c. à soupe</item>`).
3. **Aucune modification Kotlin requise !** `OcrVocabularyLoader` lira automatiquement la nouvelle entrée au prochain lancement.

---

### 2. Ajout d'un nouveau Tableau de Chaînes (`<string-array>`)
1. Déclarer le nouveau tableau dans le fichier XML sous `app/src/main/res-ocr/values/` :
   ```xml
   <string-array name="mon_nouveau_tableau">
       <item>valeur1</item>
       <item>valeur2</item>
   </string-array>
   ```
2. Ajouter la propriété dans [OcrVocabulary.kt](file:///C:/Users/bruno/AndroidStudioProjects/ChiefInventory/app/src/main/java/com/chefinventory/app/utils/OcrVocabulary.kt) :
   ```kotlin
   val monNouveauTableau: List<String> = emptyList()
   ```
3. Binder le chargement Android dans `OcrVocabulary.fromResources` :
   ```kotlin
   monNouveauTableau = getStringArraySafely(res, R.array.mon_nouveau_tableau)
   ```
4. Binder le chargement JVM/TDD dans `OcrVocabularyLoader.loadFromProjectResources` :
   ```kotlin
   monNouveauTableau = getList(stringArrays, "mon_nouveau_tableau")
   ```
5. Ajouter l'assertion dans `OcrVocabularyLoaderTest` :
   ```kotlin
   assertThat(vocab.monNouveauTableau).isNotEmpty()
   ```

---

### 3. Ajout d'une Chaîne Simple ou d'un Préfixe (`<string>`)
1. Déclarer la balise dans le fichier XML sous `app/src/main/res-ocr/values/` :
   ```xml
   <string name="mon_prefixe" translatable="false">valeur_prefixe</string>
   ```
2. Ajouter la propriété avec sa valeur par défaut dans [OcrVocabulary.kt](file:///C:/Users/bruno/AndroidStudioProjects/ChiefInventory/app/src/main/java/com/chefinventory/app/utils/OcrVocabulary.kt) :
   ```kotlin
   val monPrefixe: String = "valeur_prefixe"
   ```
3. Binder le chargement Android dans `OcrVocabulary.fromResources` :
   ```kotlin
   monPrefixe = getStringSafely(res, R.string.mon_prefixe)
   ```
4. Binder le chargement JVM/TDD dans `OcrVocabularyLoader.loadFromProjectResources` :
   ```kotlin
   monPrefixe = getStringVal(strings, "mon_prefixe").ifBlank { "valeur_prefixe" }
   ```
5. Mettre à jour l'index de type `String` dans le test de réflexion `OcrVocabularyTest.testOcrVocabularySyntheticDefaultConstructorReflectionCoverage` (ex: `16, 40, 41 -> ""`).
6. Ajouter l'assertion dans `OcrVocabularyLoaderTest` :
   ```kotlin
   assertThat(vocab.monPrefixe).isEqualTo("valeur_prefixe")
   ```

---

### 4. Ajout d'un Nouveau Fichier XML de Ressources (ex: `ocr_step6_custom_rules.xml`)
1. Créer le fichier XML sous `app/src/main/res-ocr/values/ocr_step6_custom_rules.xml`.
2. Définir vos balises `<string-array name="...">` ou `<string name="...">`.
3. Suivre la procédure du **Point 2** ou **Point 3** ci-dessus.
   - *Note importante :* `OcrVocabularyLoader` scanne automatiquement tous les répertoires `res*/values`, il n'y a **aucun chemin de fichier à enregistrer manuellement**.

---

### 5. Mise à jour & Validation des Tests Unitaires
1. Exécuter `./gradlew :app:fullCoverageReport` pour s'assurer que tous les tests unitaires restent à 100% de succès.
2. Si vous avez ajouté une règle de correction orthographique ou diacritique, ajouter un cas de test dédié dans le composant concerné (ex: `Ocr1CorrectorTest`, `Ocr4MergerTest`, `Ocr5BeautifierTest`).
