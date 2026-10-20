
CREATE TABLE IF NOT EXISTS springecom.product (
  id int NOT NULL AUTO_INCREMENT,
  brand varchar(255) DEFAULT NULL,
  category varchar(255) DEFAULT NULL,
  description text,
  name varchar(255) DEFAULT NULL,
  price decimal(38,2) DEFAULT NULL,
  product_available bit(1) NOT NULL,
  release_date datetime(6) DEFAULT NULL,
  stock_quantity int NOT NULL,
  image_name varchar(255) DEFAULT NULL,
  image_type varchar(255) DEFAULT NULL,
  image_stored_name varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Apple MacBook Pro 16 M3 Max', 'Apple', 'Laptop', '16.2-inch Liquid Retina XDR display, M3 Max chip with 16-core CPU and 40-core GPU, 36GB unified memory, 1TB SSD storage. Space Black finish.', 349900.0, 1, 12, '2024-01-15 00:00:00', 'Apple MacBook Pro 16 M3 Max.png', 'image/png', 'a025cdf0-ca7d-470a-99b9-8b8f9466939e.png');

    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Apple MacBook Air 15 M2', 'Apple', 'Laptop', 'Impossibly thin design with 15.3-inch Liquid Retina display, M2 chip, 8GB memory, 512GB SSD, 1080p FaceTime HD camera, and up to 18 hours of battery life.', 134900.0, 1, 25, '2023-06-20 00:00:00', 'Apple MacBook Air 15 M2.png', 'image/png', 'f61f89fe-7e26-4d31-886f-4acd3eabe6f0.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Dell XPS 15 OLED', 'Dell', 'Laptop', '15.6-inch 3.5K OLED InfinityEdge touch display, Intel Core i7 13th Gen, NVIDIA GeForce RTX 4060, 32GB DDR5 RAM, 1TB NVMe SSD, CNC machined aluminum chassis.', 189990.0, 1, 8, '2024-02-10 00:00:00', 'Dell XPS 15 OLED.png', 'image/png', 'b3c70c4e-82da-4aa9-b8db-9597ccd3bcf5.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Lenovo ThinkPad X1 Carbon Gen 11', 'Lenovo', 'Laptop', 'Ultralight business laptop powered by Intel Core i7-1365U, 16GB LPDDR5, 512GB SSD, 14-inch 2.8K OLED display, legendary spill-resistant keyboard.', 164990.0, 1, 15, '2023-11-05 00:00:00', 'Lenovo ThinkPad X1 Carbon Gen 11.png', 'image/png', 'c579d31b-4dd5-44d0-bacf-caa04399318b.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('ASUS ROG Zephyrus G14 Gaming', 'ASUS', 'Laptop', 'AMD Ryzen 9 8945HS, NVIDIA GeForce RTX 4070 8GB, 14-inch 3K 120Hz OLED ROG Nebula display, 32GB LPDDR5X, 1TB PCIe 4.0 SSD, AniMe Matrix LED lid.', 174990.0, 1, 10, '2024-03-01 00:00:00', 'ASUS ROG Zephyrus G14 Gaming.png', 'image/png', '7af3e2b6-d992-47a5-a420-e5ceec9d8ab9.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('HP Spectre x360 2-in-1', 'HP', 'Laptop', '14-inch 2.8K OLED touchscreen 360-degree convertible, Intel Core Ultra 7 155H with AI Boost NPU, 16GB RAM, 1TB SSD, includes rechargeable MPP 2.0 Tilt Pen.', 149999.0, 1, 14, '2024-02-18 00:00:00', 'HP Spectre x360 2-in-1.png', 'image/png', '8f1e696b-4ecd-44a5-a808-3711c5b9870f.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Acer Predator Helios 16', 'Acer', 'Laptop', '16-inch WQXGA 240Hz gaming display, Intel Core i9 14th Gen, NVIDIA RTX 4070 8GB, 16GB DDR5, 1TB SSD, 5th Gen AeroBlade 3D fan technology.', 159990.0, 1, 9, '2024-04-12 00:00:00', 'Acer Predator Helios 16.png', 'image/png', 'ea30063a-f0cc-4b66-9e17-ae2598a82203.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Microsoft Surface Laptop 5', 'Microsoft', 'Laptop', '13.5-inch PixelSense touchscreen, Intel Core i5 12th Gen, 8GB LPDDR5x RAM, 512GB SSD, sleek Platinum metal finish, up to 18 hours battery.', 109990.0, 1, 18, '2023-09-14 00:00:00', 'Microsoft Surface Laptop 5.png', 'image/png', '26ea8b26-207e-46de-9b90-03685df1ef87.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Razer Blade 16 Dual-Mini-LED', 'Razer', 'Laptop', 'World first Dual-Mode Mini-LED display (FHD+ 240Hz or 4K 120Hz), Intel Core i9-14900HX, NVIDIA RTX 4080, 32GB DDR5, 1TB SSD, CNC aluminum unibody.', 319999.0, 1, 6, '2024-03-25 00:00:00', 'Razer Blade 16 Dual-Mini-LED.png', 'image/png', 'a6ea1d98-f6f1-4e43-adae-69b7b2bec71f.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('MSI Stealth 14 Studio', 'MSI', 'Laptop', 'Ultra-portable 1.7kg gaming laptop, 14-inch QHD+ 240Hz IPS, Intel Core i7 13th Gen, RTX 4060, 16GB RAM, 1TB SSD, Vapor Chamber cooling.', 139990.0, 1, 11, '2023-12-08 00:00:00', 'MSI Stealth 14 Studio.png', 'image/png', 'f11f8ce3-184d-41ee-99b8-488e4883e213.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Apple iPhone 16 Pro Max', 'Apple', 'Mobile', '6.9-inch Super Retina XDR with ProMotion, A18 Pro chip, Grade 5 Titanium design, 48MP Fusion camera with 5x telephoto optical zoom, Camera Control button.', 144900.0, 1, 30, '2024-09-20 00:00:00', 'Apple iPhone 16 Pro Max.png', 'image/png', '8b49cc99-66e5-468a-ad51-e442dd85ed7c.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Samsung Galaxy S24 Ultra', 'Samsung', 'Mobile', '6.8-inch Dynamic AMOLED 2X flat display, Snapdragon 8 Gen 3 for Galaxy, 200MP camera with Galaxy AI (Circle to Search, Live Translate), built-in S Pen.', 129999.0, 1, 22, '2024-01-31 00:00:00', 'Samsung Galaxy S24 Ultra.png', 'image/png', 'a9913e8f-5090-4d75-99e6-e2250ac7de95.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Google Pixel 9 Pro XL', 'Google', 'Mobile', '6.8-inch Super Actua OLED display, Google Tensor G4 chip, 16GB RAM, Gemini Live integration, pro triple rear camera system with 30x Super Res Zoom.', 124999.0, 1, 16, '2024-08-22 00:00:00', 'Google Pixel 9 Pro XL.png', 'image/png', '17ec802d-b52d-4c89-be7f-21280c5f4333.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('OnePlus 12 5G', 'OnePlus', 'Mobile', '6.82-inch 2K 120Hz ProXDR display, Snapdragon 8 Gen 3, 4th Gen Hasselblad camera for mobile, 5400 mAh battery with 100W SUPERVOOC fast charging.', 64999.0, 1, 35, '2024-01-23 00:00:00', 'OnePlus 12 5G.png', 'image/png', '6de47b51-a538-4732-a701-97fc74360c89.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Xiaomi 14 Ultra', 'Xiaomi', 'Mobile', 'Quad 50MP Leica optical camera system with 1-inch LYT-900 sensor, stepless variable aperture, Snapdragon 8 Gen 3, 5000 mAh battery with 90W HyperCharge.', 99999.0, 1, 10, '2024-03-12 00:00:00', 'Xiaomi 14 Ultra.png', 'image/png', 'c072d211-22be-42d6-9a73-0058b79e413e.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Nothing Phone (2)', 'Nothing', 'Mobile', 'Iconic Glyph interface with customizable LED light patterns, Snapdragon 8+ Gen 1, 6.7-inch flexible LTPO OLED 120Hz display, dual 50MP cameras, Nothing OS 2.5.', 36999.0, 1, 28, '2023-07-21 00:00:00', 'Nothing Phone (2).png', 'image/png', 'c7c2a36e-644b-455a-99d4-7986878ea83a.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Apple iPhone 15 Plus', 'Apple', 'Mobile', '6.7-inch Super Retina XDR display with Dynamic Island, 48MP main camera with 2x Telephoto, A16 Bionic chip, all-day battery life, USB-C connectivity.', 79999.0, 1, 20, '2023-09-22 00:00:00', 'Apple iPhone 15 Plus.png', 'image/png', '056e5f94-cc68-4b6c-a10b-28d92435f79b.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Samsung Galaxy Z Fold 5', 'Samsung', 'Mobile', '7.6-inch foldable Dynamic AMOLED 2X interior screen, zero-gap Flex Hinge, Snapdragon 8 Gen 2 for Galaxy, IPX8 water resistance, advanced multi-window multitasking.', 154999.0, 1, 8, '2023-08-11 00:00:00', 'Samsung Galaxy Z Fold 5.png', 'image/png', 'e8ef2a6f-c877-4200-8fea-5d5847ff1ddd.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Motorola Edge 50 Ultra', 'Motorola', 'Mobile', 'Real Nordic Wood back panel, Snapdragon 8s Gen 3, 6.7-inch 144Hz pOLED Pantone-validated display, 50MP periscope telephoto with 100x hybrid zoom, 125W TurboPower.', 59999.0, 1, 19, '2024-05-15 00:00:00', 'Motorola Edge 50 Ultra.png', 'image/png', 'f956b2f1-8367-42c5-b286-747a81bf209c.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Vivo X100 Pro', 'Vivo', 'Mobile', 'ZEISS APO Floating Telephoto Camera with 1-inch main sensor, MediaTek Dimensity 9300 processor, V3 imaging chip, 5400 mAh battery with 100W FlashCharge.', 89999.0, 1, 12, '2024-01-04 00:00:00', 'Vivo X100 Pro.png', 'image/png', 'a5fd9cab-6521-4a73-a307-56ef03d87cb6.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Sony WH-1000XM5 Wireless ANC', 'Sony', 'Headphone', 'Industry-leading noise cancellation with 8 microphones and Auto NC Optimizer, crystal clear hands-free calling, up to 30 hours battery, lightweight soft-fit leather.', 29990.0, 1, 35, '2023-05-20 00:00:00', 'Sony WH-1000XM5 Wireless ANC.png', 'image/png', '1dd87dbd-c4e3-4d4e-b153-a1cbe0b73d19.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Bose QuietComfort Ultra', 'Bose', 'Headphone', 'World-class active noise cancellation with breakthrough Bose Immersive Audio, CustomTune technology that personalizes sound to your ears, 24-hour battery life.', 35900.0, 1, 20, '2023-10-15 00:00:00', 'Bose QuietComfort Ultra.png', 'image/png', '767c44a3-50e4-45cc-bd47-0648187693ce.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Apple AirPods Max', 'Apple', 'Headphone', 'High-fidelity audio with Apple-designed dynamic driver, Active Noise Cancellation with Transparency mode, Personalized Spatial Audio with dynamic head tracking, knit-mesh canopy.', 59900.0, 1, 15, '2023-04-10 00:00:00', 'Apple AirPods Max.png', 'image/png', 'ad6143e5-4bfa-49fc-bcc5-bb4bb8a8ec36.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Sennheiser Momentum 4 Wireless', 'Sennheiser', 'Headphone', 'Audiophile-inspired 42mm transducer system, outstanding 60-hour battery life, Adaptive Noise Cancellation, Transparency Mode, lightweight fold-flat design.', 24990.0, 1, 28, '2023-08-25 00:00:00', 'Sennheiser Momentum 4 Wireless.png', 'image/png', '9a33eed5-8c97-4c02-b2b3-e84aebec4275.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('JBL Tour ONE M2', 'JBL', 'Headphone', 'True Adaptive Noise Cancelling with Smart Ambient, 40mm dynamic drivers tuned to JBL Pro Sound, 4-mic crystal call technology, up to 50 hours playtime.', 19999.0, 1, 40, '2023-07-15 00:00:00', 'JBL Tour ONE M2.png', 'image/png', '2659c0ef-7ca3-4c85-b034-a65996214d92.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Audio-Technica ATH-M50xBT2', 'Audio-Technica', 'Headphone', 'Legendary M50x studio sound with wireless Bluetooth convenience, proprietary 45mm large-aperture drivers, AK4331 advanced DAC, up to 50 hours continuous use.', 17990.0, 1, 22, '2023-03-18 00:00:00', 'Audio-Technica ATH-M50xBT2.png', 'image/png', '96ec8022-40ce-42cb-8da3-418efdab568c.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Beats Studio Pro Wireless', 'Beats', 'Headphone', 'Fully custom acoustic platform with UltraPlush leather cushions, lossless audio via USB-C, Personalized Spatial Audio, Active Noise Cancelling, up to 40 hours battery.', 37900.0, 1, 18, '2023-09-05 00:00:00', 'Beats Studio Pro Wireless.png', 'image/png', '1e7b35f4-694f-4f42-a9a8-677c14e6dcd8.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Sony WF-1000XM5 True Wireless', 'Sony', 'Headphone', 'The best noise-cancelling earbuds on the market. Dynamic Driver X for wide-frequency reproduction, Bone conduction sensors for clear calls, IPX4 water resistance.', 24990.0, 1, 30, '2023-08-01 00:00:00', 'Sony WF-1000XM5 True Wireless.png', 'image/png', '65dbb1ab-d8a9-4cd9-95e5-e4967e5da418.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Apple AirPods Pro 2nd Gen USB-C', 'Apple', 'Headphone', 'Up to 2x more Active Noise Cancellation, Adaptive Audio that automatically tailors noise control, Transparency mode, MagSafe Charging Case (USB-C) with Precision Finding.', 24900.0, 1, 50, '2023-09-22 00:00:00', 'Apple AirPods Pro 2nd Gen USB-C.png', 'image/png', '9c120617-7cad-4a3f-abd8-5a4a671eec19.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Marshall Major IV Bluetooth', 'Marshall', 'Headphone', 'Iconic vintage Marshall design delivering explosive signature sound, 80+ solid hours of wireless playtime, ergonomic multidirectional control knob, wireless charging.', 12999.0, 1, 32, '2023-04-12 00:00:00', 'Marshall Major IV Bluetooth.png', 'image/png', '63a3619f-8395-4e6f-b0dd-be9e22b8a368.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Sony PlayStation 5 Slim', 'Sony', 'Electronics', 'Slimmer console design with 1TB SSD storage, ultra-high speed SSD for near-instant load times, ray tracing support, 4K gaming at up to 120 FPS, DualSense wireless controller.', 54990.0, 1, 25, '2024-01-10 00:00:00', 'Sony PlayStation 5 Slim.png', 'image/png', '7e3019fc-5dce-4e1d-9cce-ee35e0109aa9.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Xbox Series X 1TB Console', 'Microsoft', 'Electronics', 'The fastest, most powerful Xbox ever. 12 teraflops of raw graphic processing power, 4K gaming up to 120 FPS, Xbox Velocity Architecture with custom 1TB NVMe SSD.', 55990.0, 1, 18, '2023-11-20 00:00:00', 'Xbox Series X 1TB Console.png', 'image/png', 'a0dfdbd4-7e52-42aa-9069-907cc26867dd.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Nintendo Switch OLED Model', 'Nintendo', 'Electronics', 'Vibrant 7-inch OLED screen, wide adjustable stand, dock with wired LAN port, 64GB internal storage, enhanced audio in handheld and tabletop modes.', 34990.0, 1, 24, '2023-06-15 00:00:00', 'Nintendo Switch OLED Model.png', 'image/png', 'e5572827-7689-4c7d-86d0-eb2eb3e4f855.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Apple iPad Air 11-inch M2', 'Apple', 'Electronics', '11-inch Liquid Retina display with P3 wide color and True Tone, blazing-fast Apple M2 chip, landscape 12MP Ultra Wide front camera, supports Apple Pencil Pro.', 59900.0, 1, 28, '2024-05-15 00:00:00', 'Apple iPad Air 11-inch M2.png', 'image/png', '86e6dc56-50ae-4fb1-8406-3b6755492d2f.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Samsung 55 Crystal 4K Smart TV', 'Samsung', 'Electronics', '55-inch Ultra HD 4K display with Crystal Processor 4K, HDR10+, PurColor technology, Q-Symphony sound sync, Motion Xcelerator, Tizen OS with all streaming apps.', 44990.0, 1, 14, '2024-02-28 00:00:00', 'Samsung 55 Crystal 4K Smart TV.png', 'image/png', 'aebee80d-4bd1-4164-8c68-6354749d07e6.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Dyson V15 Detect Cordless Vacuum', 'Dyson', 'Electronics', 'Laser reveals microscopic dust on hard floors, piezo sensor continuously sizes and counts dust particles, Digital Motorbar cleaner head with anti-tangle technology.', 62900.0, 1, 12, '2023-10-01 00:00:00', 'Dyson V15 Detect Cordless Vacuum.png', 'image/png', 'b1d6ef3f-4a58-46d1-a49b-f1fdfd2f99d4.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Fujifilm X-T5 Mirrorless Camera', 'Fujifilm', 'Electronics', '40.2MP X-Trans CMOS 5 HR sensor, 5-axis in-body image stabilization with up to 7 stops, 6.2K/30p video, classic retro dial-based operation, 19 Film Simulation modes.', 169999.0, 1, 7, '2023-09-10 00:00:00', 'Fujifilm X-T5 Mirrorless Camera.png', 'image/png', '30d669b9-c296-414a-9587-58dd9d7aace2.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('DJI Mini 4 Pro Drone Fly More', 'DJI', 'Electronics', 'Sub-249g ultra-lightweight drone, Omnidirectional Obstacle Sensing, 4K/60fps HDR True Vertical Shooting, 20km FHD video transmission, 34-min flight time per battery.', 109990.0, 1, 9, '2024-01-20 00:00:00', 'DJI Mini 4 Pro Drone Fly More.png', 'image/png', '26b123f2-ef53-4e86-8e76-918c6c9a81e9.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Amazon Kindle Paperwhite 16GB', 'Amazon', 'Electronics', '6.8-inch glare-free 300 ppi display, adjustable warm light, up to 10 weeks battery life, 20% faster page turns, waterproof (IPX8) for reading by the pool or in the bath.', 14999.0, 1, 35, '2023-05-12 00:00:00', 'Amazon Kindle Paperwhite 16GB.png', 'image/png', '1e237024-3f7e-40fd-b9f0-de428e706b03.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('GoPro HERO12 Black Action Camera', 'GoPro', 'Electronics', 'Incredible 5.3K60 and 4K120 video, Emmy-winning HyperSmooth 6.0 video stabilization with 360-degree Horizon Lock, waterproof to 33ft, Bluetooth audio support for AirPods.', 37990.0, 1, 22, '2023-09-13 00:00:00', 'GoPro HERO12 Black Action Camera.png', 'image/png', '18118160-7932-4c66-a9f3-d5cb0946428b.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Nike Air Jordan 1 Retro High OG', 'Nike', 'Fashion', 'The iconic sneaker that started it all. Premium genuine leather upper, encapsulated Air-Sole unit in the heel for lightweight cushioning, durable rubber cupsole with pivot circle.', 16995.0, 1, 18, '2023-11-18 00:00:00', 'Nike Air Jordan 1 Retro High OG.png', 'image/png', '20b7b6be-2172-4198-b212-a9e8e0a2cbb0.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Adidas Ultraboost Light Running', 'Adidas', 'Fashion', 'Experience epic energy return with the lightest Boost midsole ever made. Primeknit+ textile upper hugs your foot, Linear Energy Push system, Continental Rubber outsole.', 18999.0, 1, 25, '2024-02-05 00:00:00', 'Adidas Ultraboost Light Running.png', 'image/png', '5ec316de-a5c3-4647-8576-5c486df318e5.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Levi''s 501 Original Fit Jeans', 'Levi''s', 'Fashion', 'The original straight-leg blue jean since 1873. Crafted from 100% heavyweight cotton denim, classic button fly, 5-pocket styling, timeless versatile wash.', 4999.0, 1, 45, '2023-08-10 00:00:00', 'Levi''s 501 Original Fit Jeans.png', 'image/png', '209f6eea-ae79-43be-a0e0-ebd293ef34b1.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Ray-Ban Classic Aviator Polarized', 'Ray-Ban', 'Fashion', 'Timeless teardrop pilot frame in polished gold with G-15 green polarized mineral glass lenses, providing 100% UV protection and eliminating glare.', 11590.0, 1, 30, '2023-04-15 00:00:00', 'Ray-Ban Classic Aviator Polarized.png', 'image/png', '5b3730e5-317d-4a67-8773-c6c8d941c4f6.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Fossil Grant Chronograph Leather', 'Fossil', 'Fashion', 'Vintage-inspired design with Roman numeral hour markers, 44mm stainless steel case, genuine brown leather strap with buckle closure, quartz chronograph movement.', 12495.0, 1, 26, '2023-06-25 00:00:00', 'Fossil Grant Chronograph Leather.png', 'image/png', 'd53f46e7-c491-4b45-9caa-2be02f660ca6.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('LEGO Star Wars Millennium Falcon', 'LEGO', 'Toys', 'Epic 1,351-piece Starship model featuring rotating top and bottom laser turrets, 2 spring-loaded shooters, lowering ramp, opening cockpit, and 7 Star Wars minifigures.', 14999.0, 1, 14, '2023-10-12 00:00:00', 'LEGO Star Wars Millennium Falcon.png', 'image/png', 'e12e00ad-aed6-4d52-b4a7-7d9c0e91ef5a.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Hot Wheels Super Ultimate Garage', 'Hot Wheels', 'Toys', 'Over 3 feet tall with parking for 140+ cars. Continuous motorized elevator transports up to 23 cars at once, features massive gorilla that tries to swipe cars off track.', 11999.0, 1, 16, '2023-08-20 00:00:00', 'Hot Wheels Super Ultimate Garage.png', 'image/png', '7fc29d1c-465b-4230-b6cb-6ecd3d5e6fa6.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Barbie Dreamhouse 75+ Pieces', 'Barbie', 'Toys', '3-story deluxe dollhouse with 10 indoor and outdoor living areas, working elevator, party room with DJ booth, spiral pool slide, lights and sound effects.', 19999.0, 1, 12, '2023-09-01 00:00:00', 'Barbie Dreamhouse 75+ Pieces.png', 'image/png', 'f23ea962-0c20-49ef-b61c-fada1e994d5d.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('Nerf Elite 2.0 Commander Blaster', 'Nerf', 'Toys', 'Customizable blaster with 3 tactical rails, barrel attachment point, stock attachment point, 6-dart rotating drum, and includes 12 Official Nerf Elite foam darts.', 1499.0, 1, 60, '2023-05-18 00:00:00', 'Nerf Elite 2.0 Commander Blaster.png', 'image/png', '8807bbd6-3f5b-42be-b53d-5662058ccb73.png');
    

    INSERT INTO springecom.product (name, brand, category, description, price, product_available, stock_quantity, release_date, image_name, image_type, image_stored_name)
    VALUES ('DJI RoboMaster S1 Educational', 'DJI', 'Toys', 'Educational robot built to unlock the potential in every learner. Features 46 programmable components, 6 AI recognition modules, omnidirectional 4WD chassis, and Scratch/Python coding support.', 49999.0, 1, 8, '2023-11-30 00:00:00', 'DJI RoboMaster S1 Educational.png', 'image/png', '33f53003-33e2-4ad7-9162-25c265a605e5.png');
    