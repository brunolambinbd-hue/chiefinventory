import urllib.parse
import urllib.request
import json

HEADERS = {"User-Agent": "RecipeCollectorBot/1.0 (contact: test@example.com)"}

def check_category(cat_title):
    base_url = "https://fr.wikibooks.org/w/api.php"
    params = {
        "action": "query",
        "list": "categorymembers",
        "cmtitle": cat_title,
        "cmlimit": 50,
        "format": "json"
    }
    url = f"{base_url}?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode("utf-8"))
        members = data.get("query", {}).get("categorymembers", [])
        print(f"=== {cat_title} ({len(members)} membres) ===")
        for m in members[:5]:
            print(f"  - {m['title']}")

check_category("Catégorie:Cuisine française")
check_category("Catégorie:Cuisine belge")
check_category("Catégorie:Cuisine alsacienne")
check_category("Catégorie:Cuisine bretonne")
check_category("Catégorie:Cuisine provençale")
check_category("Catégorie:Cuisine normande")
