import os
import json
import urllib.request
import urllib.parse
import re
import time

HEADERS = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"}

# Clés Google Custom Search JSON API (optionnelles)
GOOGLE_API_KEY = os.getenv("GOOGLE_SEARCH_API_KEY", "")
GOOGLE_CX = os.getenv("GOOGLE_SEARCH_CX", "")

SEEDS_DIR = os.path.join("app", "src", "main", "assets", "seeds")
IMAGES_DIR = os.path.join("app", "src", "main", "assets", "recipe_images")

os.makedirs(IMAGES_DIR, exist_ok=True)

def slugify(text):
    text = text.lower().replace("œ", "oe").replace("æ", "ae")
    text = re.sub(r"[^a-z0-9]", "_", text)
    text = re.sub(r"_+", "_", text).strip("_")
    return text or "recipe_image"

def generate_query_candidates(title):
    """Génère des variantes simplifiées du titre d'une recette pour maximiser le taux de succès."""
    candidates = []
    clean = title.replace("Livre de cuisine/", "").replace("Boissons/Cocktails/", "").strip()
    candidates.append(clean)

    # 1. Enlever les parenthèses : "Pizza au chèvre (maison)" -> "Pizza au chèvre"
    no_parens = re.sub(r"\s*\(.*?\)", "", clean).strip()
    if no_parens and no_parens not in candidates:
        candidates.append(no_parens)

    # 2. Enlever les numéros de fin : "Pizza aux poivrons 2" -> "Pizza aux poivrons"
    no_numbers = re.sub(r"\s+\d+$", "", no_parens).strip()
    if no_numbers and no_numbers not in candidates:
        candidates.append(no_numbers)

    # 3. Tronquer au niveau des connecteurs complexes : "Pizza au chèvre et aux lardons" -> "Pizza au chèvre"
    for connector in [" et aux ", " et au ", " et ", " aux ", " au ", " à la ", " à l'", " avec "]:
        if connector in no_numbers:
            parts = no_numbers.split(connector)
            if len(parts) > 1 and len(parts[0].strip()) >= 3:
                # Variante 1 : Garder la première partie + premier connecteur (ex: "Pizza au chèvre")
                simplified = (parts[0].strip() + " " + connector.strip()).strip()
                if len(simplified) >= 4 and simplified not in candidates:
                    candidates.append(simplified)
                # Variante 2 : Garder uniquement le premier groupe nominal (ex: "Pizza")
                first_group = parts[0].strip()
                if len(first_group) >= 4 and first_group not in candidates:
                    candidates.append(first_group)

    return candidates

def search_wikimedia_image(query):
    """Recherche une photo culinaire HD sur Wikimedia Commons API."""
    base_url = "https://commons.wikimedia.org/w/api.php"
    params = {
        "action": "query",
        "generator": "search",
        "gsrsearch": f"{query} filetype:bitmap",
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
            with urllib.request.urlopen(req, timeout=10) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                pages = data.get("query", {}).get("pages", {})
                for _, val in pages.items():
                    ii = val.get("imageinfo", [{}])[0]
                    thumb = ii.get("thumburl") or ii.get("url")
                    if thumb:
                        return thumb
        except Exception:
            time.sleep(0.2)
    return None

def search_google_image(query):
    """Recherche une photo culinaire HD sur Google Custom Search JSON API."""
    if not GOOGLE_API_KEY or not GOOGLE_CX:
        return None

    base_url = "https://www.googleapis.com/customsearch/v1"
    params = {
        "q": f"recette {query}",
        "searchType": "image",
        "imgType": "photo",
        "imgSize": "medium",
        "rights": "cc_publicdomain,cc_attribute,cc_sharealike",
        "key": GOOGLE_API_KEY,
        "cx": GOOGLE_CX,
        "num": 1
    }
    url = f"{base_url}?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(url, headers=HEADERS)

    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            items = data.get("items", [])
            if items:
                return items[0].get("link")
    except Exception as e:
        print(f"  ⚠️ Erreur Google Images API pour '{query}': {e}")
    return None

def find_best_recipe_image(title):
    """Recherche la meilleure photo en testant progressivement les variantes de titres."""
    candidates = generate_query_candidates(title)
    
    # 1. Tester chaque candidat sur Wikimedia Commons
    for cand in candidates:
        img_url = search_wikimedia_image(cand)
        if img_url:
            return img_url

    # 2. Tester chaque candidat sur Google Images API (si clé configurée)
    for cand in candidates:
        img_url = search_google_image(cand)
        if img_url:
            return img_url

    return None

def download_file(url, target_path):
    if os.path.exists(target_path) and os.path.getsize(target_path) > 1000:
        return True
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = resp.read()
            if len(data) > 1000:
                with open(target_path, "wb") as f:
                    f.write(data)
                return True
    except Exception:
        pass
    return False

def process_pack_file(json_filename):
    filepath = os.path.join(SEEDS_DIR, json_filename)
    if not os.path.exists(filepath):
        return

    with open(filepath, "r", encoding="utf-8") as f:
        recipes = json.load(f)

    updated_count = 0
    for r in recipes:
        title = r.get("title", "")
        if not title:
            continue

        slug = slugify(title)
        filename = f"{slug}.jpg"
        target_path = os.path.join(IMAGES_DIR, filename)

        # Si l'image locale existe déjà dans assets/recipe_images/
        if os.path.exists(target_path) and os.path.getsize(target_path) > 1000:
            r["imageUrl"] = f"file:///android_asset/recipe_images/{filename}"
            updated_count += 1
            continue

        # Recherche progressive multi-candidats
        img_url = find_best_recipe_image(title)

        if img_url and download_file(img_url, target_path):
            r["imageUrl"] = f"file:///android_asset/recipe_images/{filename}"
            updated_count += 1
            print(f"  + Image trouvée & enregistrée : '{title}' -> {filename}")
        time.sleep(0.05)

    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(recipes, f, ensure_ascii=False, indent=2)

    print(f"✅ {json_filename} : {updated_count} / {len(recipes)} recettes avec photos locales")

def main():
    print("==========================================")
    print("=== Téléchargement avec Simplification Progressive des Titres ===")
    print("==========================================")

    for fn in ["recipes_seed_be.json", "recipes_seed_it.json", "recipes_seed_fr.json"]:
        process_pack_file(fn)

    master_path = os.path.join("app", "src", "main", "assets", "starter_recipes_seed.json")
    if os.path.exists(master_path):
        all_master = []
        for fn in ["recipes_seed_be.json", "recipes_seed_it.json", "recipes_seed_fr.json"]:
            p = os.path.join(SEEDS_DIR, fn)
            if os.path.exists(p):
                with open(p, "r", encoding="utf-8") as f:
                    all_master.extend(json.load(f))
        with open(master_path, "w", encoding="utf-8") as f:
            json.dump(all_master, f, ensure_ascii=False, indent=2)
        print(f"✅ Master starter_recipes_seed.json synchronisé ({len(all_master)} recettes)")

if __name__ == "__main__":
    main()
