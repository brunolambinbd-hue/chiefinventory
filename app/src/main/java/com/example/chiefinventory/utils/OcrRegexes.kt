package com.example.chiefinventory.utils

import java.util.regex.Pattern

/**
 * Registre centralisé des expressions régulières pré-compilées du pipeline OCR.
 *
 * Responsabilités :
 * - Pré-compiler et réutiliser les expressions régulières statiques pour éliminer les ré-allocations CPU.
 * - Offrir un cache memoized (`getUnitPattern`) pour les motifs d'unités de mesure dynamiques.
 * - Offrir une documentation KDoc pédagogique complète expliquant chaque motif Regex avec des exemples réels.
 */
object OcrRegexes {

    /**
     * Motifs d'expressions régulières liés aux Quantités et Chiffres.
     */
    object Quantities {
        /**
         * Capture une plage de quantités entre deux nombres.
         *
         * Structure du motif :
         * - `^\s*` : Début de ligne, espaces facultatifs.
         * - `(\d+)` : **Groupe 1** - Premier chiffre/nombre (ex: "10").
         * - `\s*(?:ou|à|-)\s*` : Séparateur de plage ("ou", "à" ou "-").
         * - `(\d+[.,]?\d*)` : **Groupe 2** - Second chiffre/nombre (ex: "15").
         *
         * Exemples :
         * - `"10 à 15 minutes"` ➔ Groupe 1 = `"10"`, Groupe 2 = `"15"`
         * - `"1 ou 2 gousses"` ➔ Groupe 1 = `"1"`, Groupe 2 = `"2"`
         * - `"2-3 cuillères"` ➔ Groupe 1 = `"2"`, Groupe 2 = `"3"`
         */
        val RANGE: Regex = Regex("^\\s*(\\d+)\\s*(?:ou|à|-)\\s*(\\d+[.,]?\\d*)", RegexOption.IGNORE_CASE)

        /**
         * Capture les fractions sous forme numérateur/dénominateur.
         *
         * Structure du motif :
         * - `(\d+)` : **Groupe 1** - Numérateur (ex: "1" dans 1/2).
         * - `/` : Symbole de division.
         * - `(\d+)` : **Groupe 2** - Dénominateur (ex: "2" dans 1/2).
         *
         * Exemples :
         * - `"1/2 verre d'eau"` ➔ Numérateur = `1`, Dénominateur = `2` (valeur calculée = `0.5`)
         * - `"3/4 de litre"` ➔ Numérateur = `3`, Dénominateur = `4` (valeur calculée = `0.75`)
         */
        val FRACTION: Regex = Regex("(\\d+)/(\\d+)")

        /**
         * Extrait la quantité numérique au début d'une ligne d'ingrédient et isole le reste de la chaîne.
         *
         * Structure du motif :
         * - `^\s*` : Début de ligne.
         * - `(\d+[.,]?\d*)` : **Groupe 1** - Nombre entier ou décimal (avec point ou virgule).
         * - `\s*` : Espaces de séparation.
         * - `(.*)$` : **Groupe 2** - Tout le reste du texte (unité + nom de l'aliment).
         *
         * Exemples :
         * - `"250 g de farine"` ➔ Groupe 1 = `"250"`, Groupe 2 = `"g de farine"`
         * - `"1.5 l d'eau"` ➔ Groupe 1 = `"1.5"`, Groupe 2 = `"l d'eau"`
         */
        val STANDARD: Pattern = Pattern.compile("^\\s*(\\d+[.,]?\\d*)\\s*(.*)$")

        /** Règles de détection et normalisation des fractions (ex: "I12" -> "1/2"). */
        val FRACTION_RULES: List<Pair<Regex, String>> = listOf(
            Regex("(?i)(?<![\\p{L}\\p{N}])[1il!|]{2}2(?![\\p{L}\\p{N}])") to "1/2",
            Regex("(?i)(?<![\\p{L}\\p{N}])[1il!|]{2}4(?![\\p{L}\\p{N}])") to "1/4",
            Regex("^1\\s*12\\b") to "1/2",
            Regex("^1\\s*14\\b") to "1/4",
            Regex("(?i)\\b[uw]2\\b") to "1/2",
            Regex("(?i)\\b11?2\\s+([\\p{L}]+)\\b(?!s)") to "1/2 $1"
        )

        /** Corrections des erreurs de chiffres et d'articles (ex: "|c" -> "1 c", "l d'eau" -> "1 l d'eau"). */
        val NUMBER_CORRECTIONS: List<Pair<Regex, String>> = listOf(
            Regex("(?i)^[|il!]c(?=[\\s.àa])") to "1 c",
            Regex("(?i)(?<=\\d)\\s*l\\s*d['’\\s]*eau\\b") to " l d'eau",
            Regex("(?i)(?<![\\p{L}\\d])l\\s*d['’\\s]*eau\\b") to "1 l d'eau",
            Regex("(?i)\\b1 ajout\\b") to "l'ajout",
            Regex("(?i)^[|il!]\\s*(?=\\d)") to "1",
            Regex("(?i)(?<!\\p{L})[|il!](?=/)") to "1",
            Regex("\\s+\\|\\s+") to " 1 ",
            Regex("(?i)\\bdel\\b") to "de 1"
        )
    }

    /**
     * Motifs d'expressions régulières liés à l'Analyse des Ingrédients.
     */
    object Ingredients {
        /**
         * Extrait le texte complémentaire placé entre parenthèses dans une ligne d'ingrédient.
         *
         * Structure du motif :
         * - `\s*\(` : Espace facultatif suivi d'une parenthèse ouvrante.
         * - `(.*?)` : **Groupe 1** - Contenu capturé de manière paresseuse (*lazy*).
         * - `\)` : Parenthèse fermante.
         *
         * Exemples :
         * - `"4 petites tomates (ou 2 grosses)"` ➔ Groupe 1 = `"ou 2 grosses"`
         * - `"120 g de surimi (poisson au goût de crabe)"` ➔ Groupe 1 = `"poisson au goût de crabe"`
         */
        val PARENTHESES: Regex = Regex("\\s*\\((.*?)\\)")

        /**
         * Identifie les quantifieurs vagues ou articles en début de ligne pour les placer en information complémentaire.
         *
         * Structure du motif :
         * - `^` : Début de ligne.
         * - `(quelques|plusieurs|un peu de|...)` : **Groupe 1** - Le mot vague ou l'article.
         * - `\s+` : Un ou plusieurs espaces obligatoires.
         *
         * Exemples :
         * - `"quelques branches de persil"` ➔ Groupe 1 = `"quelques"`
         * - `"un peu de sel"` ➔ Groupe 1 = `"un peu de"`
         * - `"des petites feuilles"` ➔ Groupe 1 = `"des"`
         */
        val VAGUE_QUANTITY: Regex = Regex("^(quelques|plusieurs|un peu de|une poignée de|des|du|de la|de l'|le|la|les|l['’]|un|une)\\s+", RegexOption.IGNORE_CASE)

        /**
         * Capture les adjectifs de taille ou de forme en début de ligne d'ingrédient.
         *
         * Structure du motif :
         * - `^` : Début de ligne.
         * - `(petites?|petits?|grosses?|gros|...)` : **Groupe 1** - Adjectif au singulier/pluriel.
         * - `\s+` : Espace suivant.
         *
         * Exemples :
         * - `"4 petites tomates"` ➔ Adjectif = `"petites"`
         * - `"1 beau chicon"` ➔ Adjectif = `"beau"`
         */
        val ADJECTIVE_PREFIX: Regex = Regex("^(petites?|petits?|grosses?|gros|fines?|fins?|belles?|beau|beaux)\\s+", RegexOption.IGNORE_CASE)

        /**
         * Isole les préfixes de préparation de découpe ou d'extraction d'aliment en début de ligne.
         *
         * Structure du motif :
         * - `^(?:le\s+|la\s+|l['’])?` : Article optionnel (*"le "*, *"la "*, *"l'"*).
         * - `(jus|zeste|zestes|morceau|morceaux|tranche|tranches)` : **Groupe 1** - Nom de préparation.
         * - `\s+(?:de\s+|d['’]\s*)` : Préposition de liaison *"de "* ou *"d'"*.
         *
         * Exemples :
         * - `"le jus de 1/2 citron"` ➔ Groupe 1 = `"jus"` (l'aliment reste `"citron"`)
         * - `"zeste de 1 orange"` ➔ Groupe 1 = `"zeste"` (l'aliment reste `"orange"`)
         */
        val PREPARATION_PREFIX: Regex = Regex("^(?:le\\s+|la\\s+|l['’])?(jus|zeste|zestes|morceau|morceaux|tranche|tranches)\\s+(?:de\\s+|d['’]\\s*)", RegexOption.IGNORE_CASE)

        /**
         * Nettoie la préposition de liaison *"de "* ou *"d'"* en début de nom d'aliment.
         *
         * Structure du motif :
         * - `^` : Début de chaîne.
         * - `(?:de\s+|d['’]\s*)` : Préposition *"de "* ou *"d'"*.
         *
         * Exemples :
         * - `"de farine"` ➔ Reste : `"farine"`
         * - `"d'huile d'olive"` ➔ Reste : `"huile d'olive"`
         */
        val DE_PREFIX: Regex = Regex("^(?:de\\s+|d['’]\\s*)", RegexOption.IGNORE_CASE)
    }

    /**
     * Motifs d'expressions régulières liés au Normalisateur de Première Étape ([Ocr1Normalizer]).
     */
    object Normalizer {
        /** Protège les mots-clés de sections ("Ingrédients", "Instructions"). */
        val PROTECTED_KEYWORDS: Regex = Regex("(?i)^(?:i|l)\\s*(ngrédients|nstructions)\\b")

        /** Suppression du préfixe 't' devant un chiffre (ex: "t10 min" -> "10 min"). */
        val PREFIX_T_BEFORE_DIGIT: Regex = Regex("(?i)(?<!\\p{L})t\\s*(?=\\d)")

        /** Suppression du mot de tolérance 'environ' (ex: "environ 250 g" -> "250 g"). */
        val ENVIRON_WORD: Regex = Regex("(?i)\\benviron\\b")

        /** Suppression des puces/symboles parasites devant un chiffre (ex: "! 100 g" -> "100 g"). */
        val SYMBOL_BEFORE_DIGIT: Regex = Regex("(?i)^[!|il!|•\\-*]\\s+(?=\\d)")

        /** Correction de la confusion du chiffre '1' au début des puces. */
        val OCR_ONE_CORRECTION: Regex = Regex("(?i)(^|•\\s*|[\\-*]\\s*|\\()([|!|](?=[\\s\\p{L}])|[il](?=\\s+\\p{L}))")

        /** Suppression du bruit de dates, devises et prix. */
        val NOISE_REMOVAL: List<Pair<Regex, String>> = listOf(
            Regex("\\b\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4}\\b") to "",
            Regex("(?i)\\b\\d+(?:[.,]\\d+)?\\s*(?:f|francs?)\\b") to "",
            Regex("(?i)\\b\\d+(?:[.,]\\d+)?\\s*(?:€|eur\\b|euros?\\b)") to ""
        )

        /** Corrections des articles et élisions (ex: "1 e " -> "le "). */
        val ARTICLE_CORRECTIONS: List<Pair<Regex, String>> = listOf(
            Regex("(?i)^1\\s+e\\s+") to "le ",
            Regex("(?i)^[t1]['’]") to "l'",
            Regex("(?i)\\s+[t1]['’]") to " l'"
        )

        /** Corrections linguistiques générales et liaisons d'apostrophes. */
        val LINGUISTIC_CORRECTIONS: List<Pair<Regex, String>> = listOf(
            Regex("(?i)([a-z])d['’]") to "$1 d'",
            Regex("(?i)d['’]\\s+") to "d'",
            Regex("(?i)\\beuf(s?)\\b") to "oeuf$1",
            Regex("(?i)\\blajout\\b") to "l'ajout",
            Regex("(?i)\\b[1il|!]?ongueur\\b") to "longueur",
            Regex("(?i)\\b[1il|!]\\s*es\\b") to "les",
            Regex("(?i)\\bgri[b6l]\\b") to "gris",
            Regex("(?i)\\b1\\s*oasts?\\b") to "toasts"
        )

        /** Normalisation des abréviations de cuillères (ex: "cas" -> "c. à soupe", "cac" -> "c. à café"). */
        val SPOON_NORMALIZATION: List<Pair<Regex, String>> = listOf(
            Regex("(?i)\\bc\\s+[àa]\\s+f[eé](?![a-z])") to "c. à café",
            Regex("(?i)\\b(supe|sope)(s?)\\b") to "soupe$2",
            Regex("(?i)c\\.?\\s*[àa]\\s*(s|soupe)\\b") to "c. à soupe",
            Regex("(?i)c\\.?\\s*[àa]\\s*(?:c|caf[eé]|f[eé])\\b") to "c. à café",
            Regex("(?i)\\bc[àa]s\\b") to "c. à soupe",
            Regex("(?i)\\bc[àa]c\\b") to "c. à café"
        )
    }

    /**
     * Motifs d'expressions régulières liés au Nettoyeur de Seconde Étape ([Ocr2Cleaner]).
     */
    object Cleaner {
        /** Détecte les dimensions techniques (ex: "5 mm sur 5 mm", "1 cm x 2 cm") pour ne pas les confondre avec des quantités. */
        val TECHNICAL_DIMENSION: Regex = Regex("\\d+\\s*(?:mm|cm|cm2|mm2)\\b", RegexOption.IGNORE_CASE)
    }

    /**
     * Motifs d'expressions régulières liés à l'Extracteur de Titres ([OcrTitleExtractor]).
     */
    object Titles {
        /** Détecte la présence d'une quantité explicite en début de ligne pour disqualifier les candidats titres. */
        val HAS_QUANTITY: Regex = Regex("(?i)^\\d+\\s*(?:g|kg|cl|ml|l|c\\.|cuillè|pincée)")

        /** Efface un numéro de recette ou de fiche en début de titre (ex: "12 Salade grecque" -> "Salade grecque"). */
        val RECIPE_NUMBER_PREFIX: Regex = Regex("(?i)^\\d{1,3}\\s+(?!(?:g|kg|cl|ml|l|c\\.|cuillè|pincée|soupe|café|gousse|gousses|botte|bocal|tranche|tranches)\\b)")

        /** Découpe une ligne d'ingrédients fusionnée contenant "sel et poivre" (ex: "sel et poivre Salade d'avocat"). */
        val SPLIT_SEL_POIVRE: Regex = Regex("(?i)sel et poivre\\s*")

        /** Détecte la présence d'unités d'ingrédients isolées. */
        val INGREDIENT_UNIT_WORD: Regex = Regex("(?i)\\b(?:g|kg|cl|ml|l|c\\.|cuillè|pincée|gousses?)\\b")

        /** Disqualifie un candidat titre commençant par un petit numéro (1 à 20) suivi d'une lettre. */
        val DISQUALIFYING_NUMBER_START: Regex = Regex("(?i)^(?:[1-9]|1[0-9]|20)\\s+[a-zA-Z]")

        /** Disqualifie un candidat titre contenant une fraction. */
        val DISQUALIFYING_FRACTION: Regex = Regex("(?i)\\b\\d/\\d\\b")

        /** Disqualifie un candidat titre contenant une abréviation de cuillère. */
        val DISQUALIFYING_SPOON: Regex = Regex("(?i)\\bc\\.?\\s*[àa]\\s*(?:soupe|café|dessert)")
    }

    /**
     * Motifs d'expressions régulières liés à l'Analyseur de Sources et Adresses ([SourceParser]).
     */
    object Sources {
        /** Détecte un code postal à 4 ou 5 chiffres (ex: "75001 Paris", "CP: 1000"). */
        val ZIP_CODE: Regex = Regex("\\b\\d{4,5}\\b")

        /** Détecte les numéros de téléphone aux formats nationaux et internationaux (ex: "02/542.42.42", "+32 2 542 42 42"). */
        val PHONE_NUMBER: Regex = Regex("(?:(?:\\+|00)\\d{1,3}[\\s./-]?)?(?:\\(0\\)[\\s./-]?)?\\d(?:[\\s./-]*\\d{2,3}){2,5}")

        /** Supprime la ponctuation et espaces résiduels en début de ligne de source (ex: ": 02/542.42.42" -> "02/542.42.42"). */
        val LEADING_PUNCTUATION: Regex = Regex("^[:\\-\\s.,]+")
    }

    /**
     * Motifs d'expressions régulières liés à l'Analyseur de Vins ([WineParser]).
     */
    object Wines {
        /** Détecte un millésime de vin (année 1900-2099). */
        val YEAR: Regex = Regex("\\b(?:19|20)\\d{2}\\b")

        /** Détecte un volume de boisson (ex: "75 cl", "750 ml", "1.5 l", "13.5 vol"). */
        val VOLUME: Regex = Regex("(?i)\\b\\d+(?:[\\s.,]\\d+)?\\s*(?:cl|ml|l|vol)\\b")

        /** Détecte une bouteille standard (ex: "75 cl", "750 ml", "1.5 l", "magnum"). */
        val STANDARD_BOTTLE: Regex = Regex("(?i)\\b(75\\s*cl|750\\s*ml|1\\s*5\\s*l|magnum)\\b")

        /** Efface le préfixe d'en-tête de recommandation de vin (ex: "Accord : Un vin blanc" -> "Un vin blanc"). */
        val HEADER_PREFIX: Regex = Regex("(?i)^(?:accord|boisson|boire|servir avec|suggestion)\\s*:?")

        /** Supprime les titres et en-têtes de section Vin (ex: "Notre vin conseillé :", "Vin suggéré :", "Conseil vin :"). */
        val REMOVE_HEADER_PATTERN: Regex = Regex("(?iu)^(?:notre\\s+)?vin\\s+(?:conseill[éÉėeèÈ]|sugg[éÉėeèÈ]r[éÉėeèÈ]|recommand[éÉėeèÈ])?\\s*:?|^conseil\\s+vin\\s*:?")

        /** Disqualifie une ligne de vin si elle contient des aliments liquides/culinaires. */
        val CULINARY_EXCLUSIONS: Regex = Regex("(?i)\\b(huile|vinaigre|beurre|crème|creme|lait|bouillon|eau|jus|sirop)\\b")

        /** Disqualifie une ligne de vin si elle contient des unités purement culinaires. */
        val CULINARY_UNITS: Regex = Regex("(?i)\\b(\\d+\\s*(?:dl|g|mg|kg)|c\\.?\\s*[àa]\\s*(?:soupe|caf[eé]|dessert))\\b")

        /** Supprime la ponctuation initiale d'une ligne de vin. */
        val LEADING_PUNCTUATION: Regex = Regex("^[:\\-\\s\\.]+")

        /** Liste de paires de corrections orthographiques œnologiques récurrentes. */
        val SPELLING_CORRECTIONS: List<Pair<Regex, String>> = listOf(
            Regex("(?i)\\bBordeau\\b") to "Bordeaux",
            Regex("(?i)\\bAtinum\\b") to "Atinium",
            Regex("(?i)\\bChậteau\\b") to "Château",
            Regex("(?i)\\b[I!l|](?=[12]?9\\d{2}\\b)") to "1",
            Regex("(?i)\\bI(\\d{3})\\b") to "1$1",
            Regex("(?i)\\b(\\d{2})\\s*d\\b") to "$1 cl",
            Regex("(?i)\\bvìn\\b") to "vin",
            Regex("(?i)\\bM[.\\s]*[0O]\\.?\\b") to "M.O."
        )
    }

    /**
     * Motifs d'expressions régulières liés à l'Analyseur de Méta-Données ([RecipeMetadataParser]).
     */
    object Metadata {
        private const val DURATION_STR = "(\\d+\\s*[hH](?:\\s*\\d+)?|\\d+\\s*(?:mn|min|minute|u|h|heure))"
        private const val TIME_TYPE_STR = "(préparation|cuisson|repos|prép\\.?|prep\\.?|cuis\\.?|rest\\.?)"

        /** Détecte le nombre de portions ou personnes (ex: "pour 4 personnes", "6 personnes", "4 PERS", "A4PERS"). */
        val SERVINGS: Regex = Regex("(?i)pour\\s+(\\d{1,2})\\s+personnes?|(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)(?:\\s*(?:personnes?|portions?|pers\\.?|servings?))?|\\bPOUR\\s+(\\d+)\\b|(?:(?<=\\p{L})|(?<=\\s)|^)(\\d{1,2})\\s*(?:personnes?|portions?|pers\\.?|servings?)\\b")

        /** Détecte l'apport calorique par portion (ex: "250 kcal", "450 kilo calories"). */
        val KCAL: Regex = Regex("(?i)(\\d{2,4})\\s*(?:kcal|kilo\\s*calories?)(?:/por\\.?)?")

        /** Détecte le niveau de difficulté de la recette (ex: "difficulté : facile", "difficile"). */
        val DIFFICULTY: Regex = Regex("(?i)\\b(facile|moyen|difficile|diffcile)\\b")

        /** Détecte le temps combiné préparation + cuisson (ex: "15 min + 45 min de cuisson"). */
        val TIME_COMBINED: Regex = Regex("(?i)$DURATION_STR\\s*\\+\\s*$DURATION_STR\\s+(?:de\\s+)?cuisson")

        /** Détecte le temps avec type en préfixe (ex: "préparation : 15 min"). */
        val TIME_PREFIX: Regex = Regex("(?i)$TIME_TYPE_STR\\s*:?\\s*(?:de\\s+)?$DURATION_STR")

        /** Détecte le temps avec type en suffixe (ex: "15 min de préparation"). */
        val TIME_SUFFIX: Regex = Regex("(?i)$DURATION_STR\\s+(?:de\\s+)?$TIME_TYPE_STR")

        /** Extrait les heures et minutes pour conversion en minutes (ex: "1 h 30 min"). */
        val HOURS_PATTERN: Regex = Regex("(\\d+)\\s*[hH]\\s*(\\d*)")

        /** Extrait les minutes (ex: "45 min", "30 mn"). */
        val MINUTES_PATTERN: Regex = Regex("(\\d+)\\s*(?:mn|min|minute|u)")

        /** Détecte la présence d'un en-tête de valeurs nutritionnelles (ex: "VALEURS NUTRITIONNELLES", "PAR 1000", "POUR 100G"). */
        val NUTRITION_HEADER: Regex = Regex("(?i)\\bvaleurs?\\s*nutritionn?elles?\\b|\\bpar\\s*100\\s*[g0o]\\b|\\bpour\\s*100\\s*[g0o]\\b")

        /** Extrait les calories/énergie (ex: "156 KCAL", "198 NCAL", "K/CAL"). */
        val NUTRITION_KCAL: Regex = Regex("(?i)(\\d+)\\s*(?:kcal|ncal|k/cal|cal)")

        /** Extrait les lipides/graisses (ex: "LIPIDES 9.7 G", "IPOUS 97 0", "IPO3 9,7"). */
        val NUTRITION_FAT: Regex = Regex("(?i)(?:lipides?|ipous|ipo\\d?)\\s*:?\\s*(\\d+(?:[.,]\\d+)?|\\d{1,2}(?:\\s*0)?)")

        /** Extrait les acides gras saturés (ex: "ACIDES GRAS SATURÉS 1.0 G", "ACIOUS GRAS SATURES L00"). */
        val NUTRITION_SAT_FAT: Regex = Regex("(?i)(?:ac[a-z]*\\s+gras\\s+satun?r[eé]s?|satun?r[eé]s?|satur[eé]s?)\\s*:?\\s*(\\d+(?:[.,]\\d+)?|[lI1][0oO]\\d*|\\d{1,2})")

        /** Extrait les glucides (ex: "GLUCIDES 10.7 G", "QLUCIDES 10,70"). */
        val NUTRITION_CARBS: Regex = Regex("(?i)(?:glucides?|qlucides?)\\s*:?\\s*(\\d+(?:[.,]\\d+)?|\\d{2,3})")

        /** Extrait les sucres (ex: "SUCRES 2.0 G", "SUCRIS20"). */
        val NUTRITION_SUGAR: Regex = Regex("(?i)(?:sucres?|sucris)\\s*:?\\s*(\\d+(?:[.,]\\d+)?|\\d{2})")

        /** Extrait les protéines (ex: "PROTÉINES 8.8 G", "PROTCINES 880"). */
        val NUTRITION_PROTEIN: Regex = Regex("(?i)(?:prot[eéÉc]ines?)\\s*:?\\s*(\\d+(?:[.,]\\d+)?|\\d{2,3})")

        /** Extrait le sel (ex: "SEL 0.40 G", "SL0,40"). */
        val NUTRITION_SALT: Regex = Regex("(?i)\\b(?:sel|sl)\\s*:?\\s*(\\d+(?:[.,]\\d+)?|0,\\d{2}|\\d{2})")

        /** Extrait les fibres (ex: "FIBRES 1.0 G", "toRES LO0", "tIRES"). */
        val NUTRITION_FIBER: Regex = Regex("(?i)(?:fibres?|tores|tires)\\s*:?\\s*(\\d+(?:[.,]\\d+)?|[lI1][0oO]\\d*)")
    }

    /**
     * Motifs d'expressions régulières liés à l'Embellisseur de Présentation ([Ocr5InstructionBeautifier]).
     */
    object Beautifier {
        /** Supprime les numéros d'étapes en début de ligne (ex: "1. ", "2) ", "3- "). */
        val STEP_DIGIT_PREFIX: Regex = Regex("^\\d{1,2}[.)\\-]\\s*")

        /** Supprime un numéro d'étape isolé suivi d'un mot à majuscule initiale (ex: "1 Plongez" -> "Plongez"). */
        val STEP_NUMBER_BEFORE_CAPITAL: Regex = Regex("^\\d{1,2}\\s+(?=[\\p{Lu}])")

        /** Supprime les puces typographiques en début de ligne (ex: "• ", "- ", "* "). */
        val BULLET_PREFIX: Regex = Regex("^[•\\-*]\\s*")

        /** Nettoie les commentaires entre crochets dans les listes de mots-clés. */
        val BRACKET_COMMENT: Regex = Regex("\\[[^\\]]+\\]")
    }

    /** Cache memoized pour les Regexes d'unités culinaires pour ne jamais compiler deux fois le même motif. */
    private val unitRegexCache = mutableMapOf<String, Regex>()

    /** Cache memoized pour les Regexes de préfixes publicitaires pour ne jamais re-compiler le même motif. */
    private val adPrefixCache = mutableMapOf<String, Regex>()

    /** Cache memoized pour les Regexes de préfixes téléphoniques pour ne jamais re-compiler le même motif. */
    private val phonePrefixCache = mutableMapOf<String, Regex>()

    /** Cache memoized pour les Regexes de préfixes d'instructions pour ne jamais re-compiler le même motif dynamique. */
    private val instructionPrefixCache = mutableMapOf<String, Regex>()

    /**
     * Retourne le motif Regex pré-compilé et mis en cache pour les préfixes de consignes basés sur les verbes d'action.
     *
     * @param stepActionKeywords La liste des verbes d'action culinaires issue de `step_keywords.xml`.
     * @return L'expression régulière compilée correspondante.
     */
    fun getInstructionPrefixPattern(stepActionKeywords: List<String>): Regex {
        val cacheKey = stepActionKeywords.joinToString(",")
        return instructionPrefixCache.getOrPut(cacheKey) {
            val actionVerbsRegex = if (stepActionKeywords.isNotEmpty()) {
                val distinctKeywords = stepActionKeywords
                    .map { it.replace(Beautifier.BRACKET_COMMENT, "") }
                    .filter { it.length >= 3 && it.lowercase() !in listOf("minutes", "heures", "secondes", "environ") }
                distinctKeywords.joinToString("|") { Regex.escape(it) }
            } else {
                "(?!minutes\\b|heures\\b)[\\p{Lu}][\\p{Ll}]+"
            }
            Regex("^(?:[\\p{L}]{1,3}\\s+)?(?:\\d{1,2}[.)\\-]?\\s+)*(?=(?:$actionVerbsRegex)\\b)", RegexOption.IGNORE_CASE)
        }
    }

    /**
     * Retourne le motif Regex pré-compilé et mis en cache pour un préfixe téléphonique donné.
     *
     * @param prefix Le préfixe téléphonique (ex: "Tel", "Fax", "GSM").
     * @return L'expression régulière compilée correspondante.
     */
    fun getPhonePrefixPattern(prefix: String): Regex {
        return phonePrefixCache.getOrPut(prefix) {
            Regex("(?i)(?<!\\p{L})$prefix\\b\\s*[:\\-.]?")
        }
    }

    /**
     * Retourne le motif Regex pré-compilé et mis en cache pour un motif publicitaire donné.
     *
     * @param rawPattern La chaîne du motif publicitaire issu de `ads_exclusions.xml`.
     * @return L'expression régulière compilée correspondante.
     */
    fun getAdPrefixPattern(rawPattern: String): Regex {
        return adPrefixCache.getOrPut(rawPattern) {
            Regex("(?i)^.*?" + rawPattern + "\\s*")
        }
    }

    /**
     * Retourne le motif Regex pré-compilé et mis en cache pour une unité culinaire donnée.
     *
     * Structure du motif généré :
     * - `^` : Début de la chaîne après la quantité numérique.
     * - `Pattern.quote(unit)` : Échappe les caractères spéciaux de l'unité (comme le point dans `c.`).
     * - `(?:\s+|de\s+|d['’]\s*|\.|\b)` : Exige un espace, une préposition *"de"*, un point ou une limite de mot.
     *
     * Exemples :
     * - Pour `unit = "c. à soupe"`, matche `"c. à soupe de farine"` ou `"c. à soupe. de mayonnaise"`.
     * - Pour `unit = "g"`, matche `"g de sucre"` ou `"g. de beurre"`.
     *
     * @param unit Le nom ou l'abréviation de l'unité culinaire (ex: "c. à soupe", "g", "cl").
     * @return L'expression régulière compilée correspondante.
     */
    fun getUnitPattern(unit: String): Regex {
        return unitRegexCache.getOrPut(unit) {
            Regex("^${Pattern.quote(unit)}(?:\\s+|de\\s+|d['’]\\s*|\\.|\\b)", RegexOption.IGNORE_CASE)
        }
    }
}
