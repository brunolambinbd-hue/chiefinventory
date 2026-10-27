import urllib.parse
import urllib.request
import json

HEADERS = {"User-Agent": "RecipeCollectorBot/1.0 (contact: test@example.com)"}

TITLES = [
    "Livre de cuisine/Bûche aux marrons",
    "Livre de cuisine/Bûche de Noël",
    "Livre de cuisine/Riz à la Catalane",
    "Livre de cuisine/Rôti de porc",
    "Livre de cuisine/Soupe à l'ail",
    "Livre de cuisine/Fondue au chocolat",
    "Livre de cuisine/Salmis"
]

def fetch_extract(title):
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

    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode("utf-8"))
        pages = data.get("query", {}).get("pages", {})
        for _, val in pages.items():
            print(f"=== {title} ===")
            print(val.get("extract", ""))
            print("\n" + "="*50 + "\n")

for t in TITLES:
    fetch_extract(t)
