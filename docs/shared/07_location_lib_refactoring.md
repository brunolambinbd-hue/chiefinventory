# Plan de Refactorisation : LocationManagementLib (Idée 07)

## Contexte
Le besoin de gestion hiérarchique des emplacements est commun à plusieurs projets (`ParaBDCollector`, `Chief Inventory`). L'objectif est d'extraire toute la logique non spécifique à une application dans la bibliothèque partagée.

## Objectifs (30 min)

### 1. Couche Modèle & Contrat (Les Fondations)
- [ ] **Déplacer l'entité `Location`** : Transférer la classe de `ParaBDCollector` vers `LocationManagementLib`.
- [ ] **Déplacer le `LocationDao`** : Transférer l'interface pour garantir un schéma Room identique.
- [ ] **Déplacer les `LocationUtils`** : Toute la logique de calcul de chemins et de vérification d'ancêtres.

### 2. Couche Logique d'Affichage (Réutilisabilité UI)
- [ ] **Déplacer `DisplayLocation`** : Le modèle de vue décoré.
- [ ] **Déplacer `LocationViewModel`** : La logique de transformation des données pour la vue.
- [ ] **Déplacer `LocationAdapter`** : L'adapteur RecyclerView (avec son layout XML si possible, ou via une interface).

### 3. Intégration & Publication
- [ ] **Publier la nouvelle version** de `LocationManagementLib` en Maven Local.
- [ ] **Mettre à jour `ParaBDCollector`** pour utiliser les classes de la bibliothèque.

## Avantages
- Code DRY (Don't Repeat Yourself) entre les projets.
- Tests unitaires de la logique d'arbre centralisés dans la lib.
- Schéma de base de données cohérent pour faciliter les imports/exports futurs entre projets.
