import os
import json
import urllib.request
import urllib.parse
import re

HEADERS = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"}

SEEDS_DIR = os.path.join("app", "src", "main", "assets", "seeds")
IMAGES_DIR = os.path.join("app", "src", "main", "assets", "recipe_images")

os.makedirs(IMAGES_DIR, exist_ok=True)

TARGET_RECIPES = [
    "Chicons au gratin",
    "Osso bucco",
    "Carbonnade flamande",
    "Panna cotta",
    "Coq au vin"
]

def slugify(text):
    text = text.lower().replace("œ", "oe").replace("æ", "ae")
    text = re.sub(r"[^a-z0-9]", "_", text)
    text = re.sub(r"_+", "_", text).strip("_")
    return text or "recipe_image"

def search_wikimedia_image(query):
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

    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            pages = data.get("query", {}).get("pages", {})
            for _, val in pages.items():
                ii = val.get("imageinfo", [{}])[0]
                thumb = ii.get("thumburl")
                if not thumb:
                    thumb = ii.get("url")
                if thumb:
                    return thumb
    except Exception as e:
        print(f"  ⚠️ Erreur API Wikimedia pour '{query}': {e}")
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
    except Exception as e:
        print(f"  ⚠️ Erreur téléchargement image '{url}': {e}")
    return False

def process_target_recipes():
    print("==========================================")
    print("=== Téléchargement des 5 Photos de Recettes Cibles ===")
    print("==========================================")

    downloaded_map = {}

    for title in TARGET_RECIPES:
        print(f"\nRecherche d'image HD pour : {title}...")
        img_url = search_wikimedia_image(title)
        if img_url:
            slug = slugify(title)
            filename = f"{slug}.jpg"
            target_path = os.path.join(IMAGES_DIR, filename)
            
            if download_file(img_url, target_path):
                asset_uri = f"file:///android_asset/recipe_images/{filename}"
                downloaded_map[title.lower().strip()] = asset_uri
                print(f"  ✅ Image enregistrée : {target_path} -> {asset_uri}")
            else:
                print(f"  ❌ Échec téléchargement pour : {title}")
        else:
            print(f"  ❌ Aucune image trouvée sur Wikimedia pour : {title}")

    # Synchronisation des fichiers JSON seeds
    for fn in ["recipes_seed_be.json", "recipes_seed_it.json", "recipes_seed_fr.json"]:
        filepath = os.path.join(SEEDS_DIR, fn)
        if not os.path.exists(filepath):
            continue

        with open(filepath, "r", encoding="utf-8") as f:
            recipes = json.load(f)

        updated_in_file = 0
        for r in recipes:
            t_clean = r.get("title", "").lower().strip()
            if t_clean in downloaded_map:
                r["imageUrl"] = downloaded_map[t_clean]
                updated_in_file += 1

        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(recipes, f, ensure_ascii=False, indent=2)

        print(f"✅ {fn} mis à jour ({updated_in_file} recettes liées à leur photo locale)")

    # Synchronisation master starter_recipes_seed.json
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
        print(f"✅ Master starter_recipes_seed.json synchronisé ({len(all_master)} recettes total)")

if __name__ == "__main__":
    process_target_recipes()
