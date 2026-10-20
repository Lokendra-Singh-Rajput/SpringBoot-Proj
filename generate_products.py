import os
import uuid
import subprocess
import datetime
from PIL import Image, ImageDraw, ImageFont

upload_dir = "/Users/lucky/Desktop/SpringBoot-Proj/Search/uploads/images"
os.makedirs(upload_dir, exist_ok=True)

products = [
    {
        "name": "Apple MacBook Pro 16 M3 Max",
        "brand": "Apple",
        "category": "Laptop",
        "price": 349900.00,
        "stock": 12,
        "desc": "16.2-inch Liquid Retina XDR display, M3 Max chip with 16-core CPU and 40-core GPU, 36GB unified memory, 1TB SSD storage. Space Black finish.",
        "release": "2024-01-15",
        "bg_color": (30, 41, 59),
        "accent": (56, 189, 248)
    },
    {
        "name": "Apple MacBook Air 15 M2",
        "brand": "Apple",
        "category": "Laptop",
        "price": 134900.00,
        "stock": 25,
        "desc": "Impossibly thin design with 15.3-inch Liquid Retina display, M2 chip, 8GB memory, 512GB SSD, 1080p FaceTime HD camera, and up to 18 hours of battery life.",
        "release": "2023-06-20",
        "bg_color": (30, 41, 59),
        "accent": (56, 189, 248)
    },
    {
        "name": "Dell XPS 15 OLED",
        "brand": "Dell",
        "category": "Laptop",
        "price": 189990.00,
        "stock": 8,
        "desc": "15.6-inch 3.5K OLED InfinityEdge touch display, Intel Core i7 13th Gen, NVIDIA GeForce RTX 4060, 32GB DDR5 RAM, 1TB NVMe SSD, CNC machined aluminum chassis.",
        "release": "2024-02-10",
        "bg_color": (30, 41, 59),
        "accent": (96, 165, 250)
    },
    {
        "name": "Lenovo ThinkPad X1 Carbon Gen 11",
        "brand": "Lenovo",
        "category": "Laptop",
        "price": 164990.00,
        "stock": 15,
        "desc": "Ultralight business laptop powered by Intel Core i7-1365U, 16GB LPDDR5, 512GB SSD, 14-inch 2.8K OLED display, legendary spill-resistant keyboard.",
        "release": "2023-11-05",
        "bg_color": (30, 41, 59),
        "accent": (239, 68, 68)
    },
    {
        "name": "ASUS ROG Zephyrus G14 Gaming",
        "brand": "ASUS",
        "category": "Laptop",
        "price": 174990.00,
        "stock": 10,
        "desc": "AMD Ryzen 9 8945HS, NVIDIA GeForce RTX 4070 8GB, 14-inch 3K 120Hz OLED ROG Nebula display, 32GB LPDDR5X, 1TB PCIe 4.0 SSD, AniMe Matrix LED lid.",
        "release": "2024-03-01",
        "bg_color": (30, 41, 59),
        "accent": (168, 85, 247)
    },
    {
        "name": "HP Spectre x360 2-in-1",
        "brand": "HP",
        "category": "Laptop",
        "price": 149999.00,
        "stock": 14,
        "desc": "14-inch 2.8K OLED touchscreen 360-degree convertible, Intel Core Ultra 7 155H with AI Boost NPU, 16GB RAM, 1TB SSD, includes rechargeable MPP 2.0 Tilt Pen.",
        "release": "2024-02-18",
        "bg_color": (30, 41, 59),
        "accent": (245, 158, 11)
    },
    {
        "name": "Acer Predator Helios 16",
        "brand": "Acer",
        "category": "Laptop",
        "price": 159990.00,
        "stock": 9,
        "desc": "16-inch WQXGA 240Hz gaming display, Intel Core i9 14th Gen, NVIDIA RTX 4070 8GB, 16GB DDR5, 1TB SSD, 5th Gen AeroBlade 3D fan technology.",
        "release": "2024-04-12",
        "bg_color": (30, 41, 59),
        "accent": (14, 165, 233)
    },
    {
        "name": "Microsoft Surface Laptop 5",
        "brand": "Microsoft",
        "category": "Laptop",
        "price": 109990.00,
        "stock": 18,
        "desc": "13.5-inch PixelSense touchscreen, Intel Core i5 12th Gen, 8GB LPDDR5x RAM, 512GB SSD, sleek Platinum metal finish, up to 18 hours battery.",
        "release": "2023-09-14",
        "bg_color": (30, 41, 59),
        "accent": (59, 130, 246)
    },
    {
        "name": "Razer Blade 16 Dual-Mini-LED",
        "brand": "Razer",
        "category": "Laptop",
        "price": 319999.00,
        "stock": 6,
        "desc": "World first Dual-Mode Mini-LED display (FHD+ 240Hz or 4K 120Hz), Intel Core i9-14900HX, NVIDIA RTX 4080, 32GB DDR5, 1TB SSD, CNC aluminum unibody.",
        "release": "2024-03-25",
        "bg_color": (30, 41, 59),
        "accent": (34, 197, 94)
    },
    {
        "name": "MSI Stealth 14 Studio",
        "brand": "MSI",
        "category": "Laptop",
        "price": 139990.00,
        "stock": 11,
        "desc": "Ultra-portable 1.7kg gaming laptop, 14-inch QHD+ 240Hz IPS, Intel Core i7 13th Gen, RTX 4060, 16GB RAM, 1TB SSD, Vapor Chamber cooling.",
        "release": "2023-12-08",
        "bg_color": (30, 41, 59),
        "accent": (239, 68, 68)
    },

    {
        "name": "Apple iPhone 16 Pro Max",
        "brand": "Apple",
        "category": "Mobile",
        "price": 144900.00,
        "stock": 30,
        "desc": "6.9-inch Super Retina XDR with ProMotion, A18 Pro chip, Grade 5 Titanium design, 48MP Fusion camera with 5x telephoto optical zoom, Camera Control button.",
        "release": "2024-09-20",
        "bg_color": (24, 24, 27),
        "accent": (244, 63, 94)
    },
    {
        "name": "Samsung Galaxy S24 Ultra",
        "brand": "Samsung",
        "category": "Mobile",
        "price": 129999.00,
        "stock": 22,
        "desc": "6.8-inch Dynamic AMOLED 2X flat display, Snapdragon 8 Gen 3 for Galaxy, 200MP camera with Galaxy AI (Circle to Search, Live Translate), built-in S Pen.",
        "release": "2024-01-31",
        "bg_color": (24, 24, 27),
        "accent": (168, 85, 247)
    },
    {
        "name": "Google Pixel 9 Pro XL",
        "brand": "Google",
        "category": "Mobile",
        "price": 124999.00,
        "stock": 16,
        "desc": "6.8-inch Super Actua OLED display, Google Tensor G4 chip, 16GB RAM, Gemini Live integration, pro triple rear camera system with 30x Super Res Zoom.",
        "release": "2024-08-22",
        "bg_color": (24, 24, 27),
        "accent": (34, 197, 94)
    },
    {
        "name": "OnePlus 12 5G",
        "brand": "OnePlus",
        "category": "Mobile",
        "price": 64999.00,
        "stock": 35,
        "desc": "6.82-inch 2K 120Hz ProXDR display, Snapdragon 8 Gen 3, 4th Gen Hasselblad camera for mobile, 5400 mAh battery with 100W SUPERVOOC fast charging.",
        "release": "2024-01-23",
        "bg_color": (24, 24, 27),
        "accent": (239, 68, 68)
    },
    {
        "name": "Xiaomi 14 Ultra",
        "brand": "Xiaomi",
        "category": "Mobile",
        "price": 99999.00,
        "stock": 10,
        "desc": "Quad 50MP Leica optical camera system with 1-inch LYT-900 sensor, stepless variable aperture, Snapdragon 8 Gen 3, 5000 mAh battery with 90W HyperCharge.",
        "release": "2024-03-12",
        "bg_color": (24, 24, 27),
        "accent": (249, 115, 22)
    },
    {
        "name": "Nothing Phone (2)",
        "brand": "Nothing",
        "category": "Mobile",
        "price": 36999.00,
        "stock": 28,
        "desc": "Iconic Glyph interface with customizable LED light patterns, Snapdragon 8+ Gen 1, 6.7-inch flexible LTPO OLED 120Hz display, dual 50MP cameras, Nothing OS 2.5.",
        "release": "2023-07-21",
        "bg_color": (24, 24, 27),
        "accent": (244, 244, 245)
    },
    {
        "name": "Apple iPhone 15 Plus",
        "brand": "Apple",
        "category": "Mobile",
        "price": 79999.00,
        "stock": 20,
        "desc": "6.7-inch Super Retina XDR display with Dynamic Island, 48MP main camera with 2x Telephoto, A16 Bionic chip, all-day battery life, USB-C connectivity.",
        "release": "2023-09-22",
        "bg_color": (24, 24, 27),
        "accent": (56, 189, 248)
    },
    {
        "name": "Samsung Galaxy Z Fold 5",
        "brand": "Samsung",
        "category": "Mobile",
        "price": 154999.00,
        "stock": 8,
        "desc": "7.6-inch foldable Dynamic AMOLED 2X interior screen, zero-gap Flex Hinge, Snapdragon 8 Gen 2 for Galaxy, IPX8 water resistance, advanced multi-window multitasking.",
        "release": "2023-08-11",
        "bg_color": (24, 24, 27),
        "accent": (168, 85, 247)
    },
    {
        "name": "Motorola Edge 50 Ultra",
        "brand": "Motorola",
        "category": "Mobile",
        "price": 59999.00,
        "stock": 19,
        "desc": "Real Nordic Wood back panel, Snapdragon 8s Gen 3, 6.7-inch 144Hz pOLED Pantone-validated display, 50MP periscope telephoto with 100x hybrid zoom, 125W TurboPower.",
        "release": "2024-05-15",
        "bg_color": (24, 24, 27),
        "accent": (234, 179, 8)
    },
    {
        "name": "Vivo X100 Pro",
        "brand": "Vivo",
        "category": "Mobile",
        "price": 89999.00,
        "stock": 12,
        "desc": "ZEISS APO Floating Telephoto Camera with 1-inch main sensor, MediaTek Dimensity 9300 processor, V3 imaging chip, 5400 mAh battery with 100W FlashCharge.",
        "release": "2024-01-04",
        "bg_color": (24, 24, 27),
        "accent": (14, 165, 233)
    },

    {
        "name": "Sony WH-1000XM5 Wireless ANC",
        "brand": "Sony",
        "category": "Headphone",
        "price": 29990.00,
        "stock": 35,
        "desc": "Industry-leading noise cancellation with 8 microphones and Auto NC Optimizer, crystal clear hands-free calling, up to 30 hours battery, lightweight soft-fit leather.",
        "release": "2023-05-20",
        "bg_color": (31, 41, 55),
        "accent": (245, 158, 11)
    },
    {
        "name": "Bose QuietComfort Ultra",
        "brand": "Bose",
        "category": "Headphone",
        "price": 35900.00,
        "stock": 20,
        "desc": "World-class active noise cancellation with breakthrough Bose Immersive Audio, CustomTune technology that personalizes sound to your ears, 24-hour battery life.",
        "release": "2023-10-15",
        "bg_color": (31, 41, 55),
        "accent": (14, 165, 233)
    },
    {
        "name": "Apple AirPods Max",
        "brand": "Apple",
        "category": "Headphone",
        "price": 59900.00,
        "stock": 15,
        "desc": "High-fidelity audio with Apple-designed dynamic driver, Active Noise Cancellation with Transparency mode, Personalized Spatial Audio with dynamic head tracking, knit-mesh canopy.",
        "release": "2023-04-10",
        "bg_color": (31, 41, 55),
        "accent": (244, 63, 94)
    },
    {
        "name": "Sennheiser Momentum 4 Wireless",
        "brand": "Sennheiser",
        "category": "Headphone",
        "price": 24990.00,
        "stock": 28,
        "desc": "Audiophile-inspired 42mm transducer system, outstanding 60-hour battery life, Adaptive Noise Cancellation, Transparency Mode, lightweight fold-flat design.",
        "release": "2023-08-25",
        "bg_color": (31, 41, 55),
        "accent": (234, 179, 8)
    },
    {
        "name": "JBL Tour ONE M2",
        "brand": "JBL",
        "category": "Headphone",
        "price": 19999.00,
        "stock": 40,
        "desc": "True Adaptive Noise Cancelling with Smart Ambient, 40mm dynamic drivers tuned to JBL Pro Sound, 4-mic crystal call technology, up to 50 hours playtime.",
        "release": "2023-07-15",
        "bg_color": (31, 41, 55),
        "accent": (249, 115, 22)
    },
    {
        "name": "Audio-Technica ATH-M50xBT2",
        "brand": "Audio-Technica",
        "category": "Headphone",
        "price": 17990.00,
        "stock": 22,
        "desc": "Legendary M50x studio sound with wireless Bluetooth convenience, proprietary 45mm large-aperture drivers, AK4331 advanced DAC, up to 50 hours continuous use.",
        "release": "2023-03-18",
        "bg_color": (31, 41, 55),
        "accent": (168, 85, 247)
    },
    {
        "name": "Beats Studio Pro Wireless",
        "brand": "Beats",
        "category": "Headphone",
        "price": 37900.00,
        "stock": 18,
        "desc": "Fully custom acoustic platform with UltraPlush leather cushions, lossless audio via USB-C, Personalized Spatial Audio, Active Noise Cancelling, up to 40 hours battery.",
        "release": "2023-09-05",
        "bg_color": (31, 41, 55),
        "accent": (239, 68, 68)
    },
    {
        "name": "Sony WF-1000XM5 True Wireless",
        "brand": "Sony",
        "category": "Headphone",
        "price": 24990.00,
        "stock": 30,
        "desc": "The best noise-cancelling earbuds on the market. Dynamic Driver X for wide-frequency reproduction, Bone conduction sensors for clear calls, IPX4 water resistance.",
        "release": "2023-08-01",
        "bg_color": (31, 41, 55),
        "accent": (245, 158, 11)
    },
    {
        "name": "Apple AirPods Pro 2nd Gen USB-C",
        "brand": "Apple",
        "category": "Headphone",
        "price": 24900.00,
        "stock": 50,
        "desc": "Up to 2x more Active Noise Cancellation, Adaptive Audio that automatically tailors noise control, Transparency mode, MagSafe Charging Case (USB-C) with Precision Finding.",
        "release": "2023-09-22",
        "bg_color": (31, 41, 55),
        "accent": (244, 244, 245)
    },
    {
        "name": "Marshall Major IV Bluetooth",
        "brand": "Marshall",
        "category": "Headphone",
        "price": 12999.00,
        "stock": 32,
        "desc": "Iconic vintage Marshall design delivering explosive signature sound, 80+ solid hours of wireless playtime, ergonomic multidirectional control knob, wireless charging.",
        "release": "2023-04-12",
        "bg_color": (31, 41, 55),
        "accent": (234, 179, 8)
    },

    {
        "name": "Sony PlayStation 5 Slim",
        "brand": "Sony",
        "category": "Electronics",
        "price": 54990.00,
        "stock": 25,
        "desc": "Slimmer console design with 1TB SSD storage, ultra-high speed SSD for near-instant load times, ray tracing support, 4K gaming at up to 120 FPS, DualSense wireless controller.",
        "release": "2024-01-10",
        "bg_color": (15, 23, 42),
        "accent": (59, 130, 246)
    },
    {
        "name": "Xbox Series X 1TB Console",
        "brand": "Microsoft",
        "category": "Electronics",
        "price": 55990.00,
        "stock": 18,
        "desc": "The fastest, most powerful Xbox ever. 12 teraflops of raw graphic processing power, 4K gaming up to 120 FPS, Xbox Velocity Architecture with custom 1TB NVMe SSD.",
        "release": "2023-11-20",
        "bg_color": (15, 23, 42),
        "accent": (34, 197, 94)
    },
    {
        "name": "Nintendo Switch OLED Model",
        "brand": "Nintendo",
        "category": "Electronics",
        "price": 34990.00,
        "stock": 24,
        "desc": "Vibrant 7-inch OLED screen, wide adjustable stand, dock with wired LAN port, 64GB internal storage, enhanced audio in handheld and tabletop modes.",
        "release": "2023-06-15",
        "bg_color": (15, 23, 42),
        "accent": (239, 68, 68)
    },
    {
        "name": "Apple iPad Air 11-inch M2",
        "brand": "Apple",
        "category": "Electronics",
        "price": 59900.00,
        "stock": 28,
        "desc": "11-inch Liquid Retina display with P3 wide color and True Tone, blazing-fast Apple M2 chip, landscape 12MP Ultra Wide front camera, supports Apple Pencil Pro.",
        "release": "2024-05-15",
        "bg_color": (15, 23, 42),
        "accent": (168, 85, 247)
    },
    {
        "name": "Samsung 55 Crystal 4K Smart TV",
        "brand": "Samsung",
        "category": "Electronics",
        "price": 44990.00,
        "stock": 14,
        "desc": "55-inch Ultra HD 4K display with Crystal Processor 4K, HDR10+, PurColor technology, Q-Symphony sound sync, Motion Xcelerator, Tizen OS with all streaming apps.",
        "release": "2024-02-28",
        "bg_color": (15, 23, 42),
        "accent": (56, 189, 248)
    },
    {
        "name": "Dyson V15 Detect Cordless Vacuum",
        "brand": "Dyson",
        "category": "Electronics",
        "price": 62900.00,
        "stock": 12,
        "desc": "Laser reveals microscopic dust on hard floors, piezo sensor continuously sizes and counts dust particles, Digital Motorbar cleaner head with anti-tangle technology.",
        "release": "2023-10-01",
        "bg_color": (15, 23, 42),
        "accent": (245, 158, 11)
    },
    {
        "name": "Fujifilm X-T5 Mirrorless Camera",
        "brand": "Fujifilm",
        "category": "Electronics",
        "price": 169999.00,
        "stock": 7,
        "desc": "40.2MP X-Trans CMOS 5 HR sensor, 5-axis in-body image stabilization with up to 7 stops, 6.2K/30p video, classic retro dial-based operation, 19 Film Simulation modes.",
        "release": "2023-09-10",
        "bg_color": (15, 23, 42),
        "accent": (244, 63, 94)
    },
    {
        "name": "DJI Mini 4 Pro Drone Fly More",
        "brand": "DJI",
        "category": "Electronics",
        "price": 109990.00,
        "stock": 9,
        "desc": "Sub-249g ultra-lightweight drone, Omnidirectional Obstacle Sensing, 4K/60fps HDR True Vertical Shooting, 20km FHD video transmission, 34-min flight time per battery.",
        "release": "2024-01-20",
        "bg_color": (15, 23, 42),
        "accent": (14, 165, 233)
    },
    {
        "name": "Amazon Kindle Paperwhite 16GB",
        "brand": "Amazon",
        "category": "Electronics",
        "price": 14999.00,
        "stock": 35,
        "desc": "6.8-inch glare-free 300 ppi display, adjustable warm light, up to 10 weeks battery life, 20% faster page turns, waterproof (IPX8) for reading by the pool or in the bath.",
        "release": "2023-05-12",
        "bg_color": (15, 23, 42),
        "accent": (234, 179, 8)
    },
    {
        "name": "GoPro HERO12 Black Action Camera",
        "brand": "GoPro",
        "category": "Electronics",
        "price": 37990.00,
        "stock": 22,
        "desc": "Incredible 5.3K60 and 4K120 video, Emmy-winning HyperSmooth 6.0 video stabilization with 360-degree Horizon Lock, waterproof to 33ft, Bluetooth audio support for AirPods.",
        "release": "2023-09-13",
        "bg_color": (15, 23, 42),
        "accent": (59, 130, 246)
    },

    {
        "name": "Nike Air Jordan 1 Retro High OG",
        "brand": "Nike",
        "category": "Fashion",
        "price": 16995.00,
        "stock": 18,
        "desc": "The iconic sneaker that started it all. Premium genuine leather upper, encapsulated Air-Sole unit in the heel for lightweight cushioning, durable rubber cupsole with pivot circle.",
        "release": "2023-11-18",
        "bg_color": (67, 20, 30),
        "accent": (244, 63, 94)
    },
    {
        "name": "Adidas Ultraboost Light Running",
        "brand": "Adidas",
        "category": "Fashion",
        "price": 18999.00,
        "stock": 25,
        "desc": "Experience epic energy return with the lightest Boost midsole ever made. Primeknit+ textile upper hugs your foot, Linear Energy Push system, Continental Rubber outsole.",
        "release": "2024-02-05",
        "bg_color": (67, 20, 30),
        "accent": (249, 115, 22)
    },
    {
        "name": "Levi's 501 Original Fit Jeans",
        "brand": "Levi's",
        "category": "Fashion",
        "price": 4999.00,
        "stock": 45,
        "desc": "The original straight-leg blue jean since 1873. Crafted from 100% heavyweight cotton denim, classic button fly, 5-pocket styling, timeless versatile wash.",
        "release": "2023-08-10",
        "bg_color": (67, 20, 30),
        "accent": (56, 189, 248)
    },
    {
        "name": "Ray-Ban Classic Aviator Polarized",
        "brand": "Ray-Ban",
        "category": "Fashion",
        "price": 11590.00,
        "stock": 30,
        "desc": "Timeless teardrop pilot frame in polished gold with G-15 green polarized mineral glass lenses, providing 100% UV protection and eliminating glare.",
        "release": "2023-04-15",
        "bg_color": (67, 20, 30),
        "accent": (234, 179, 8)
    },
    {
        "name": "Fossil Grant Chronograph Leather",
        "brand": "Fossil",
        "category": "Fashion",
        "price": 12495.00,
        "stock": 26,
        "desc": "Vintage-inspired design with Roman numeral hour markers, 44mm stainless steel case, genuine brown leather strap with buckle closure, quartz chronograph movement.",
        "release": "2023-06-25",
        "bg_color": (67, 20, 30),
        "accent": (245, 158, 11)
    },

    {
        "name": "LEGO Star Wars Millennium Falcon",
        "brand": "LEGO",
        "category": "Toys",
        "price": 14999.00,
        "stock": 14,
        "desc": "Epic 1,351-piece Starship model featuring rotating top and bottom laser turrets, 2 spring-loaded shooters, lowering ramp, opening cockpit, and 7 Star Wars minifigures.",
        "release": "2023-10-12",
        "bg_color": (46, 16, 101),
        "accent": (168, 85, 247)
    },
    {
        "name": "Hot Wheels Super Ultimate Garage",
        "brand": "Hot Wheels",
        "category": "Toys",
        "price": 11999.00,
        "stock": 16,
        "desc": "Over 3 feet tall with parking for 140+ cars. Continuous motorized elevator transports up to 23 cars at once, features massive gorilla that tries to swipe cars off track.",
        "release": "2023-08-20",
        "bg_color": (46, 16, 101),
        "accent": (249, 115, 22)
    },
    {
        "name": "Barbie Dreamhouse 75+ Pieces",
        "brand": "Barbie",
        "category": "Toys",
        "price": 19999.00,
        "stock": 12,
        "desc": "3-story deluxe dollhouse with 10 indoor and outdoor living areas, working elevator, party room with DJ booth, spiral pool slide, lights and sound effects.",
        "release": "2023-09-01",
        "bg_color": (46, 16, 101),
        "accent": (244, 63, 94)
    },
    {
        "name": "Nerf Elite 2.0 Commander Blaster",
        "brand": "Nerf",
        "category": "Toys",
        "price": 1499.00,
        "stock": 60,
        "desc": "Customizable blaster with 3 tactical rails, barrel attachment point, stock attachment point, 6-dart rotating drum, and includes 12 Official Nerf Elite foam darts.",
        "release": "2023-05-18",
        "bg_color": (46, 16, 101),
        "accent": (59, 130, 246)
    },
    {
        "name": "DJI RoboMaster S1 Educational",
        "brand": "DJI",
        "category": "Toys",
        "price": 49999.00,
        "stock": 8,
        "desc": "Educational robot built to unlock the potential in every learner. Features 46 programmable components, 6 AI recognition modules, omnidirectional 4WD chassis, and Scratch/Python coding support.",
        "release": "2023-11-30",
        "bg_color": (46, 16, 101),
        "accent": (34, 197, 94)
    }
]

def create_product_image(p, filename):
    img = Image.new("RGB", (400, 300), color=p["bg_color"])
    draw = ImageDraw.Draw(img)
    
    draw.rectangle([(0, 0), (400, 8)], fill=p["accent"])
    
    cat_text = f"  {p['category'].upper()}  "
    draw.rectangle([(24, 24), (130, 48)], fill=(255, 255, 255, 30), outline=p["accent"], width=1)
    draw.text((32, 28), p["category"].upper(), fill=p["accent"])
    
    draw.text((24, 68), p["brand"].upper(), fill=(148, 163, 184))
    
    words = p["name"].split()
    lines = []
    current_line = []
    for w in words:
        if len(" ".join(current_line + [w])) <= 20:
            current_line.append(w)
        else:
            lines.append(" ".join(current_line))
            current_line = [w]
    if current_line:
        lines.append(" ".join(current_line))
        
    y = 96
    for line in lines[:2]:
        draw.text((24, y), line, fill=(255, 255, 255))
        y += 28
        
    draw.line([(24, 180), (376, 180)], fill=(71, 85, 105), width=1)
    
    draw.text((24, 200), f"INR {p['price']:,.0f}", fill=p["accent"])
    
    draw.text((24, 240), f"In Stock: {p['stock']} units", fill=(148, 163, 184))
    
    draw.rectangle([(270, 225), (376, 265)], outline=p["accent"], width=1)
    draw.text((285, 238), "100% ORIGINAL", fill=(203, 213, 225))

    file_path = os.path.join(upload_dir, filename)
    img.save(file_path, "PNG")
    return file_path

sql_statements = []

for p in products:
    stored_name = f"{uuid.uuid4()}.png"
    create_product_image(p, stored_name)
    
    clean_desc = p["desc"].replace("'", "''")
    clean_name = p["name"].replace("'", "''")
    clean_brand = p["brand"].replace("'", "''")
    
    sql = f"""
    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('{clean_name}', '{clean_brand}', '{p['category']}', '{clean_desc}', {p['price']}, 1, {p['stock']}, '{p['release']} 00:00:00', '{clean_name}.png', 'image/png', '{stored_name}');
    """
    sql_statements.append(sql)

full_sql = "\n".join(sql_statements)
sql_file_path = "/Users/lucky/Desktop/SpringBoot-Proj/insert_50_products.sql"
with open(sql_file_path, "w") as f:
    f.write(full_sql)

print(f"Generated 50 product images in {upload_dir}")
print(f"Generated SQL script with 50 products in {sql_file_path}")
