import os
import time
import urllib.request
import mysql.connector

UPLOAD_DIR = "/Users/lucky/Desktop/SpringBoot-Proj/Search/uploads/images"
DB = dict(host="localhost", port=3306, user="root", password=os.getenv("DB_PASSWORD", "your_password"), database="springecom")

PRODUCT_IMAGES = {
    1000: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&q=80",
    1001: "https://images.unsplash.com/photo-1611532736597-de2d4265fba3?w=600&q=80",
    1002: "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=600&q=80",
    1003: "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=600&q=80",
    1004: "https://images.unsplash.com/photo-1587202372634-32705e3bf49c?w=600&q=80",
    1005: "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600&q=80",
    1006: "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=600&q=80",
    1007: "https://images.unsplash.com/photo-1484788984921-03950022c9ef?w=600&q=80",
    1008: "https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?w=600&q=80",
    1009: "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=600&q=80",

    1010: "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&q=80",
    1011: "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=600&q=80",
    1012: "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=600&q=80",
    1013: "https://images.unsplash.com/photo-1574944985070-8f3ebc6b79d2?w=600&q=80",
    1014: "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=600&q=80",
    1015: "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&q=80",
    1016: "https://images.unsplash.com/photo-1663499482523-1c0c1bae4ce1?w=600&q=80",
    1017: "https://images.unsplash.com/photo-1628614407944-5595e4994543?w=600&q=80",
    1018: "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600&q=80",
    1019: "https://images.unsplash.com/photo-1567581935884-3349723552ca?w=600&q=80",

    1020: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&q=80",
    1021: "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&q=80",
    1022: "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=600&q=80",
    1023: "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=600&q=80",
    1024: "https://images.unsplash.com/photo-1589491106922-a8a1e2e96e31?w=600&q=80",
    1025: "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=600&q=80",
    1026: "https://images.unsplash.com/photo-1527814050087-3793815479db?w=600&q=80",
    1027: "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=600&q=80",
    1028: "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=600&q=80",
    1029: "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600&q=80",

    1030: "https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=600&q=80",
    1031: "https://images.unsplash.com/photo-1621259182978-fbf93132d53d?w=600&q=80",
    1032: "https://images.unsplash.com/photo-1578303512597-81e6cc155b3e?w=600&q=80",
    1033: "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&q=80",
    1034: "https://images.unsplash.com/photo-1461151304267-38535e780c79?w=600&q=80",
    1035: "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600&q=80",
    1036: "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&q=80",
    1037: "https://images.unsplash.com/photo-1473968512647-3e447244af8f?w=600&q=80",
    1038: "https://images.unsplash.com/photo-1526040652367-ac003a0475fe?w=600&q=80",
    1039: "https://images.unsplash.com/photo-1502920514313-52581002a659?w=600&q=80",

    1040: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80",
    1041: "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600&q=80",
    1042: "https://images.unsplash.com/photo-1542574271-7f3b92e6c821?w=600&q=80",
    1043: "https://images.unsplash.com/photo-1508296695146-257a814070b4?w=600&q=80",
    1044: "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&q=80",

    1045: "https://images.unsplash.com/photo-1585366119957-e9730b6d0f60?w=600&q=80",
    1046: "https://images.unsplash.com/photo-1558060370-d644479cb6f7?w=600&q=80",
    1047: "https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?w=600&q=80",
    1048: "https://images.unsplash.com/photo-1563453392212-326f5e854473?w=600&q=80",
    1049: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=600&q=80",
}

def download_image(url, filepath, timeout=15, retries=3):
    headers = {
        "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
                      "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
    req = urllib.request.Request(url, headers=headers)
    for attempt in range(retries):
        try:
            with urllib.request.urlopen(req, timeout=timeout) as r:
                data = r.read()
            if len(data) < 5000:
                raise ValueError(f"Image too small ({len(data)} bytes) — likely placeholder")
            with open(filepath, "wb") as f:
                f.write(data)
            return True
        except Exception as e:
            print(f"  ⚠ Attempt {attempt+1}/{retries} failed: {e}")
            time.sleep(1)
    return False

def main():
    conn = mysql.connector.connect(**DB)
    cursor = conn.cursor()
    cursor.execute("SELECT id, name, image_stored_name FROM product ORDER BY id")
    rows = cursor.fetchall()

    ok = 0
    fail = 0
    for pid, name, stored_name in rows:
        url = PRODUCT_IMAGES.get(pid)
        if not url:
            print(f"  [SKIP] id={pid} — no URL defined")
            continue

        dest = os.path.join(UPLOAD_DIR, stored_name)
        print(f"[{pid}] {name}")
        print(f"  → {url}")

        success = download_image(url, dest)
        if success:
            size_kb = os.path.getsize(dest) // 1024
            print(f"  ✔ Saved {size_kb} KB → {stored_name}")
            cursor.execute("UPDATE product SET image_type='image/jpeg' WHERE id=%s", (pid,))
            ok += 1
        else:
            print(f"  ✘ FAILED — keeping old image")
            fail += 1

        time.sleep(0.3)

    conn.commit()
    cursor.close()
    conn.close()
    print(f"\nDone. ✔ {ok} succeeded / ✘ {fail} failed")

if __name__ == "__main__":
    main()
