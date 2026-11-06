# 🛡️ Guide de Référence : Ordonnancement Obligatoire "Longest Match First" des Tableaux XML `<string-array>`

Ce document définit la règle d'architecture obligatoire régissant la disposition des éléments au sein de tous les tableaux de ressources XML (`<string-array>`) du projet **ChefInventory**.

---

## 🎯 Règle d'Or d'Ordonnancement : *Longest Match First*

> **Ordonnancement par Longueur et Spécificité Décroissante** :
> Tous les éléments définis dans un tableau `<string-array>` sous les répertoires `res*/values/` doivent impérativement être ordonnés du **plus long / plus spécifique au plus court / plus général**.
> 
> **Il est strictement interdit** de placer un mot court ou un préfixe (ex: `"un"`, `"de"`, `"c"`) avant un mot plus long qui le contient (ex: `"une"`, `"des"`, `"cuillère"`).

---

## 🔍 Justification Technique & Risques

Lorsqu'un algorithme de matching ou une expression régulière parcourt un tableau XML, l'évaluation s'effectue dans **l'ordre séquentiel d'apparition des nœuds `<item>`** :

1. **Piège de la Subsomption par Préfixe** :
   - Si **`"un"`** précède **`"une"`** dans le XML, une recherche de correspondance sur le mot `"une"` validera le préfixe `"un"` en premier et s'arrêtera prématurément, laissant la lettre `"e"` orpheline.
2. **Intégrité de la Reconnaissance OCR** :
   - L'ordonnancement par longueur décroissante garantit que les mots composés et formes complexes sont capturés intégralement avant que les abréviations uninominales ne soient évaluées.

---

## 💡 Subtilité Architecturale : Séparation Cible Canonique vs Variantes de Recherche

> [!IMPORTANT]
> **Pas de Dépendance Implicite à l'Index `[0]` pour le Remplacement** :
> Lorsqu'un tableau de recherche contient des variantes évolutives de longueur variable (ex: `<item>ingrédients nécessaires</item>`, `<item>ce qu'il vous faut</item>`, `<item>ingrédients</item>`), le tri par longueur déplacera l'élément le plus long en position `[0]`.
> 
> Si le code a besoin d'un **mot cible canonique de remplacement** (ex: remplacer `"1ngrédients"` par `"ingrédients"`), il **ne doit jamais lire l'index `[0]` du tableau**, mais utiliser une ressource `<string>` dédiée :
> - `<string name="ocr1_canonical_ingredients_header">ingrédients</string>`
> - `<string name="ocr1_canonical_instructions_header">instructions</string>`
> 
> Cela permet de trier intégralement le tableau `<string-array>` par longueur décroissante sans risque de corrompre la cible de remplacement.

---

## 📋 Hiérarchie Obligatoire des Éléments XML

Chaque tableau `<string-array>` doit respecter la hiérarchie séquentielle suivante :

1. **Expressions Composées Multi-mots en Premier** :
   - Exemple : `<item>cuillères à soupe</item>` avant `<item>cuillère</item>`
   - Exemple : `<item>sel et poivre</item>` avant `<item>sel</item>`
2. **Formes Plurielles avant Formes Singulières** :
   - Exemple : `<item>cuillères</item>` avant `<item>cuillère</item>`
   - Exemple : `<item>gousses</item>` avant `<item>gousse</item>`
3. **Mots Contenant des Préfixes avant Mots Courts** :
   - Exemple : `<item>une</item>` avant `<item>un</item>`
   - Exemple : `<item>des</item>` / `<item>de la</item>` avant `<item>de</item>`
4. **Abréviations Uninominales Courtes et Lettres Isolées en Dernier** :
   - Exemple : `<item>c.</item>`, `<item>c</item>`, `<item>g.</item>`, `<item>g</item>` tout à la fin du tableau.

---

## ❌ Mauvaises vs ✅ Bonnes Pratiques

### ❌ Mauvaise Pratique (Risque de Subsomption & Dépendance Cible `[0]`)
```xml
<string-array name="orphan_noun_number_words">
    <item>un</item>   <!-- ❌ Placé avant "une" : "une" sera tronqué en "un" -->
    <item>une</item>
    <item>deux</item>
</string-array>
```

### ✅ Bonne Pratique (Longest Match First + Cible Canonique Séparée)
```xml
<!-- ✅ Cible canonique de remplacement dédiée -->
<string name="ocr1_canonical_ingredients_header">ingrédients</string>

<!-- ✅ Tableau de recherche trié à 100% par longueur décroissante -->
<string-array name="ocr1_protected_ingredients_headers">
    <item>ingrédients nécessaires</item>
    <item>ce qu\'il vous faut</item>
    <item>ingrédients</item>
    <item>ingrédient</item>
    <item>ingredients</item>
    <item>composition</item>
    <item>ngrédients</item>
</string-array>
```
