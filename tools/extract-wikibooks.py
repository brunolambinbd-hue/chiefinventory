import urllib.parse
import urllib.request
import json
import time

HEADERS = {"User-Agent": "RecipeCollectorBot/1.0 (contact: test@example.com)"}

CATEGORIES = {
    "ENTREE": "Catégorie:Entrées",
    "PLAT": "Catégorie:Plat principal",
    "DESSERT": "Catégorie:Desserts"
}

def get_category_pages(cat_name, limit=50):
    """Récupère la liste des titres de recettes d'une catégorie."""
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

    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode("utf-8"))
        pages = data.get("query", {}).get("categorymembers", [])
        return [
            p["title"] for p in pages 
            if p["title"].startswith("Livre de cuisine/") 
            and not p["title"].endswith("/Entrées") 
            and not p["title"].endswith("/Plat principal") 
            and not p["title"].endswith("/Desserts")
        ]

def get_page_content(title):
    """Récupère le texte brut d'une recette."""
    base_url = "https://fr.wikibooks.org/w/api.php"
    params = {
        "action": "query",
        "prop": "extracts",
        "explaintext": "1",  # Renvoie du texte brut nettoyé du code HTML
        "titles": title,
        "format": "json"
    }
    url = f"{base_url}?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(url, headers=HEADERS)

    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode("utf-8"))
        pages = data.get("query", {}).get("pages", {})
        for _, val in pages.items():
            return val.get("extract", "")
    return ""

def main():
    all_recipes = []

    for cat_key, cat_name in CATEGORIES.items():
        print(f"Extraction des {cat_key}...")
        titles = get_category_pages(cat_name, limit=30)

        for title in titles:
            clean_title = title.replace("Livre de cuisine/", "").strip()
            content = get_page_content(title)

            recipe_entry = {
                "title": clean_title,
                "category": cat_key,
                "content": content,
                "source_url": f"https://fr.wikibooks.org/wiki/{urllib.parse.quote(title)}",
                "license": "CC-BY-SA 3.0"
            }
            all_recipes.append(recipe_entry)
            print(f"  + Ajouté : {clean_title}")
            time.sleep(0.2)  # Respecter les quotas de requêtes Wikimedia

    # Sauvegarde dans le fichier final dans assets/
    output_path = "app/src/main/assets/recipes_wikibooks.json"
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(all_recipes, f, ensure_ascii=False, indent=2)

    print(f"\nTerminé ! {len(all_recipes)} recettes enregistrées dans {output_path}")

if __name__ == "__main__":
    main()
