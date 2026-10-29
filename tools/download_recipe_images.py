import os
import json
import urllib.request
import urllib.parse
import re

HEADERS = {"User-Agent": "RecipeImageDownloader/1.0 (contact: test@example.com)"}

SEEDS_DIR = os.path.join("app", "src", "main", "assets", "seeds")
IMAGES_DIR = os.path.join("app", "src", "main", "assets", "recipe_images")

os.makedirs(IMAGES_DIR, exist_ok=True)

def slugify(text):
    text = text.lower().replace("œ", "oe").replace("æ", "ae")
    text = re.sub(r"[^a-z0-9]", "_", text)
    text = re.sub(r"_+", "_", text).strip("_")
    return text or "recipe_image"

def download_image(url, target_path):
    if os.path.exists(target_path) and os.path.getsize(target_path) > 1000:
        return True
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = resp.read()
            if len(data) > 1000:
                with open(target_path, "wb") as f:
                    f.write(data)
                return True
    except Exception as e:
        print(f"    ⚠️ Erreur de téléchargement pour {url}: {e}")
    return False

def process_file(json_filename):
    filepath = os.path.join(SEEDS_DIR, json_filename)
    if not os.path.exists(filepath):
        print(f"Fichier introuvable: {filepath}")
        return

    with open(filepath, "r", encoding="utf-8") as f:
        recipes = json.load(f)

    updated_count = 0
    for r in recipes:
        img_url = r.get("imageUrl")
        title = r.get("title", "")
        if img_url and title:
            slug = slugify(title)
            filename = f"{slug}.jpg"
            target_path = os.path.join(IMAGES_DIR, filename)
            
            if download_image(img_url, target_path):
                r["imageUri"] = f"file:///android_asset/recipe_images/{filename}"
                updated_count += 1

    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(recipes, f, ensure_ascii=False, indent=2)

    print(f"✅ {json_filename} : {updated_count} images téléchargées et associées dans assets/recipe_images/")

print("==========================================")
print("=== Téléchargement des Images de Recettes dans Assets ===")
print("==========================================")

for fn in ["recipes_seed_be.json", "recipes_seed_it.json", "recipes_seed_fr.json"]:
    process_file(fn)

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
