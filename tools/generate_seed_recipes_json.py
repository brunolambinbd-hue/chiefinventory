import urllib.parse
import urllib.request
import json
import re
import time
import os

HEADERS = {"User-Agent": "ChefInventoryCountryPacksGenerator/1.0 (contact: test@example.com)"}

BELGIAN_CATEGORIES = [
    ("Cuisine Belge", "Catégorie:Cuisine belge")
]

ITALIAN_CATEGORIES = [
    ("Cuisine Italienne", "Catégorie:Cuisine italienne"),
    ("Cuisine Sicilienne", "Catégorie:Cuisine sicilienne"),
    ("Pizzas", "Catégorie:Recettes de pizzas")
]

FRENCH_CATEGORIES = [
    ("Cuisine Française", "Catégorie:Cuisine française"),
    ("Cuisine Provençale", "Catégorie:Cuisine provençale"),
    ("Cuisine Alsacienne", "Catégorie:Cuisine alsacienne"),
    ("Cuisine Bretonne", "Catégorie:Cuisine bretonne"),
    ("Cuisine Normande", "Catégorie:Cuisine normande"),
    ("Entrées", "Catégorie:Entrées"),
    ("Plats principaux", "Catégorie:Plat principal"),
    ("Desserts", "Catégorie:Desserts"),
    ("Boissons", "Catégorie:Boissons"),
    ("Accompagnements", "Catégorie:Accompagnements")
]

def get_category_pages(cat_name, limit=100):
    """Récupère la liste des titres de recettes d'une catégorie Wikibooks."""
    base_url = "https://fr.wikibooks.org/w/api.php"
    params = {
        "action": "query",
        "list": "categorymembers",
        "cmtitle": cat_name,
        "cmlimit": limit,
        "cmtype": "page",
        "format": "json"
    }
    url = f"{base_url}?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(url, headers=HEADERS)

    for attempt in range(3):
        try:
            with urllib.request.urlopen(req) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                pages = data.get("query", {}).get("categorymembers", [])
                return [
                    p["title"] for p in pages 
                    if p["title"].startswith("Livre de cuisine/") 
                    and not "/Recettes" in p["title"]
                    and not p["title"].endswith("/Entrées") 
                    and not p["title"].endswith("/Plat principal") 
                    and not p["title"].endswith("/Desserts")
                    and not p["title"].endswith("/Boissons")
                    and not p["title"].endswith("/Accompagnements")
                ]
        except Exception:
            time.sleep(1.5)
    return []

def fetch_wikimedia_image_url(title):
    """Recherche la photo HD d'un plat culinaire sur Wikimedia Commons API."""
    base_url = "https://commons.wikimedia.org/w/api.php"
    clean_name = title.replace("Livre de cuisine/", "").strip()
    params = {
        "action": "query",
        "generator": "search",
        "gsrsearch": f"{clean_name} filetype:bitmap",
        "gsrnamespace": 6,
        "gsrlimit": 1,
        "prop": "imageinfo",
        "iiprop": "url",
        "iiurlwidth": 800,
        "format": "json"
    }
    url = f"{base_url}?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(url, headers=HEADERS)

    for attempt in range(2):
        try:
            with urllib.request.urlopen(req) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                pages = data.get("query", {}).get("pages", {})
                for _, val in pages.items():
                    ii = val.get("imageinfo", [{}])[0]
                    thumb = ii.get("thumburl") or ii.get("url")
                    if thumb:
                        return thumb
        except Exception:
            time.sleep(0.3)
    return None

def get_page_content(title):
    """Récupère le texte brut d'une recette sur Wikibooks avec retry."""
    base_url = "https://fr.wikibooks.org/w/api.php"
    params = {
        "action": "query",
        "prop": "extracts",
        "explaintext": "1",
        "titles": title,
        "format": "json"
    }
    url = f"{base_url}?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(url, headers=HEADERS)

    for attempt in range(3):
        try:
            with urllib.request.urlopen(req) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                pages = data.get("query", {}).get("pages", {})
                for _, val in pages.items():
                    return val.get("extract", "")
        except Exception:
            time.sleep(1.5)
    return ""

def deduce_recipe_course(title, content, default_cat=""):
    """Déduit automatiquement la catégorie (Entrée, Plat principal, Dessert, Boisson, Accompagnement)."""
    if default_cat == "Boissons":
        return "Boisson"
    elif default_cat == "Accompagnements":
        return "Accompagnement"
    elif default_cat == "Entrées":
        return "Entrée"
    elif default_cat == "Plats principaux":
        return "Plat principal"
    elif default_cat == "Desserts":
        return "Dessert"

    title_lower = title.lower()
    if any(k in title_lower for k in ["boisson", "cocktail", "jus", "sirop", "infusion", "thé", "café", "liqueur", "embuscade", "hypocras", "limonade", "punch"]):
        return "Boisson"
    elif any(k in title_lower for k in ["accompagnement", "sauce", "garniture", "purée", "frite", "mayonnaise", "ketchup"]):
        return "Accompagnement"
    elif any(k in title_lower for k in ["dessert", "gâteau", "gateau", "mousse", "charlotte", "brioche", "cake", "beignet", "biscuit", "confiture", "miel", "flan", "crème brûlée", "creme brulee", "tarte aux", "tarte tatin", "gaufre", "pâtisserie", "patisserie"]):
        return "Dessert"
    elif any(k in title_lower for k in ["entrée", "entree", "salade", "soupe", "potage", "terrine", "croquette", "verrine", "toast", "pâté", "pate"]):
        return "Entrée"
    else:
        return "Plat principal"

def parse_wikitext_to_structured_recipe(title, raw_content, source_url, cuisine_origin="", default_cat=""):
    """Découpe le texte brut Wikibooks en tableaux JSON d'ingrédients et d'instructions nettoyés."""
    clean_title = title.replace("Livre de cuisine/", "").strip()
    category = deduce_recipe_course(clean_title, raw_content, default_cat)
    
    ingredients = []
    instructions = []
    prep_time = None
    cook_time = None
    servings = None

    lines = [l.strip() for l in raw_content.split("\n") if l.strip()]
    current_section = "NONE"

    for line in lines:
        lower = line.lower()
        if any(k in lower for k in ["== ingrédients", "== composition", "ingrédients :", "ingrédients et ustensiles"]):
            current_section = "INGREDIENTS"
            continue
        elif any(k in lower for k in ["== préparation", "== cuisson", "== les cuissons", "== réalisation", "== mode opératoire", "== technique", "== la soupe", "== assemblage", "== dégustation", "feuille de route"]):
            current_section = "INSTRUCTIONS"
            continue
        elif "== recette" in lower:
            current_section = "INGREDIENTS" if not ingredients else "INSTRUCTIONS"
            continue
        elif line.startswith("==") and not line.startswith("==="):
            current_section = "OTHER"
            continue

        if "pour " in lower and "personne" in lower:
            match = re.search(r"pour\s+(\d+)\s+personne", lower)
            if match:
                servings = int(match.group(1))

        if "préparation" in lower and ("min" in lower or "h" in lower):
            match = re.search(r"(\d+)\s*(?:min|minute)", lower)
            if match:
                prep_time = int(match.group(1))
        if "cuisson" in lower and ("min" in lower or "h" in lower):
            match = re.search(r"(\d+)\s*(?:min|minute)", lower)
            if match:
                cook_time = int(match.group(1))

        if current_section == "INGREDIENTS":
            if not lower.startswith("pour ") and not lower.startswith("temps "):
                clean_line = re.sub(r"^[\*\•\-\s]+", "", line).strip()
                if clean_line:
                    if re.search(r"^(fai|mélang|melang|vers|couper|découp|epluch|épluch|fouett|battre|préchauff|prechauff|cuire|frire|piler|garnir|laiss)", clean_line.lower()):
                        current_section = "INSTRUCTIONS"
                        instructions.append(clean_line)
                    else:
                        ingredients.append(clean_line)
        elif current_section == "INSTRUCTIONS":
            clean_inst = re.sub(r"^[\*\•\-\d\.\s]+", "", line).strip()
            if clean_inst and not lower.startswith("temps "):
                instructions.append(clean_inst)

    image_url = fetch_wikimedia_image_url(clean_title)

    return {
        "title": clean_title,
        "category": category,
        "cuisineOrigin": cuisine_origin,
        "prepTimeMinutes": prep_time,
        "cookTimeMinutes": cook_time,
        "servings": servings,
        "imageUrl": image_url,
        "ingredients": ingredients,
        "instructions": instructions,
        "sourceUrl": source_url,
        "license": "CC-BY-SA 3.0"
    }

def process_pack_categories(categories_list, country_code, output_file_name, global_seen_titles, master_recipes, pack_cuisine_label=""):
    print(f"\n==========================================")
    print(f"=== Génération Pack Country [{country_code}] -> {output_file_name} ===")
    print(f"==========================================")

    pack_recipes = []
    pack_seen = set()

    for label, cat_query in categories_list:
        print(f"Extraction {label}...")
        titles = get_category_pages(cat_query, limit=100)

        for title in titles:
            if title in pack_seen:
                continue
            pack_seen.add(title)

            raw_content = get_page_content(title)
            source_url = f"https://fr.wikibooks.org/wiki/{urllib.parse.quote(title)}"
            
            origin = pack_cuisine_label if pack_cuisine_label else label
            recipe = parse_wikitext_to_structured_recipe(title, raw_content, source_url, cuisine_origin=origin, default_cat=label)
            if recipe["title"] and (recipe["ingredients"] or recipe["instructions"]):
                pack_recipes.append(recipe)
                if title not in global_seen_titles:
                    global_seen_titles.add(title)
                    master_recipes.append(recipe)
                print(f"  + [{country_code}] [{recipe['category']}] {recipe['title']} ({len(recipe['ingredients'])} ingr)")
            time.sleep(0.2)

    seeds_dir = os.path.join("app", "src", "main", "assets", "seeds")
    os.makedirs(seeds_dir, exist_ok=True)
    out_path = os.path.join(seeds_dir, output_file_name)

    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(pack_recipes, f, ensure_ascii=False, indent=2)

    print(f"✅ PACK [{country_code}] ÉCRIT : {len(pack_recipes)} recettes dans {out_path}")
    return pack_recipes

def main():
    global_seen_titles = set()
    master_recipes = []

    # 1. Pack Belgique BE
    process_pack_categories(BELGIAN_CATEGORIES, "BE", "recipes_seed_be.json", global_seen_titles, master_recipes, pack_cuisine_label="Cuisine Belge")

    # 2. Pack Italie IT
    process_pack_categories(ITALIAN_CATEGORIES, "IT", "recipes_seed_it.json", global_seen_titles, master_recipes, pack_cuisine_label="Cuisine Italienne")

    # 3. Pack France FR
    process_pack_categories(FRENCH_CATEGORIES, "FR", "recipes_seed_fr.json", global_seen_titles, master_recipes, pack_cuisine_label="Cuisine Française")

    # 4. Master Combined Seed File (Rétrocompatibilité)
    master_path = os.path.join("app", "src", "main", "assets", "starter_recipes_seed.json")
    with open(master_path, "w", encoding="utf-8") as f:
        json.dump(master_recipes, f, ensure_ascii=False, indent=2)

    print(f"\n==========================================")
    print(f"🎉 SUCCÈS GLOBAL ! {len(master_recipes)} recettes générées au total dans les Packs et dans {master_path}")

if __name__ == "__main__":
    main()
