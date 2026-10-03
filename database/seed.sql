-- PackFlow: Smart Packaging & Order Management System
-- Database Seed Script for Testing & Demonstration
-- Database: packflow_db

USE packflow_db;

-- Clear any existing records in reverse dependency order
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE dispatches;
TRUNCATE TABLE quality_checks;
TRUNCATE TABLE packaging_tasks;
TRUNCATE TABLE inventory_transactions;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE inventory;
TRUNCATE TABLE packaging_services;
TRUNCATE TABLE products;
TRUNCATE TABLE customers;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert Initial Users
-- Passwords:
-- admin@packflow.com -> admin123
-- manager@packflow.com -> manager123
-- apex@retail.com -> customer123
-- zenith@tech.com -> customer123
-- greenleaf@organics.com -> customer123
INSERT INTO users (user_id, name, email, password_hash, role, status, created_at) VALUES
(1, 'System Administrator', 'admin@packflow.com', '$2a$10$QIdRiwB/J8kf1Zn4rdtZnOJ/7r0awMLGaNb5Ija6GO6bCMGYI.ECK', 'ADMIN', 'ACTIVE', '2026-01-01 10:00:00'),
(2, 'Rajesh Sharma (Operations Manager)', 'manager@packflow.com', '$2a$10$QFDN9/wFTaiI3fqCJUx.h.1KbvHAraYS3TB5UkcHLwiJcPSUy6DBW', 'MANAGER', 'ACTIVE', '2026-01-02 11:30:00'),
(3, 'Anita Desai', 'apex@retail.com', '$2a$10$Qm.3Cu7XohvsDExytMK7BORbwHEwCh9z0me9DImB.2IHS7QwHaiPW', 'CUSTOMER', 'ACTIVE', '2026-01-05 09:15:00'),
(4, 'Vikram Malhotra', 'zenith@tech.com', '$2a$10$Qm.3Cu7XohvsDExytMK7BORbwHEwCh9z0me9DImB.2IHS7QwHaiPW', 'CUSTOMER', 'ACTIVE', '2026-01-10 14:20:00'),
(5, 'Pooja Patel', 'greenleaf@organics.com', '$2a$10$Qm.3Cu7XohvsDExytMK7BORbwHEwCh9z0me9DImB.2IHS7QwHaiPW', 'CUSTOMER', 'ACTIVE', '2026-02-01 12:00:00');

-- 2. Insert Customers Linked to User Accounts
INSERT INTO customers (customer_id, user_id, company_name, contact_person, phone, email, address, created_at) VALUES
(1, 3, 'Apex Retail Solutions', 'Anita Desai', '+91 98765 43210', 'apex@retail.com', 'Unit 402, Trade Tower, Bandra-Kurla Complex, Mumbai, Maharashtra 400051', '2026-01-05 09:30:00'),
(2, 4, 'Zenith Electronics Ltd', 'Vikram Malhotra', '+91 98220 12345', 'zenith@tech.com', 'Plot 18, Electronics City Phase 1, Bengaluru, Karnataka 560100', '2026-01-10 14:35:00'),
(3, 5, 'GreenLeaf Organics Pvt Ltd', 'Pooja Patel', '+91 97111 56789', 'greenleaf@organics.com', 'Survey No 88, Kadi-Kalol Highway, Gandhinagar, Gujarat 382715', '2026-02-01 12:15:00'),
(4, NULL, 'Vibrant Textiles Corp', 'Suresh Reddy', '+91 99000 88776', 'info@vibranttextiles.in', 'Plot 45, Textile Park, Surat, Gujarat 395002', '2026-02-15 16:00:00');

-- 3. Insert Products
INSERT INTO products (product_id, customer_id, product_name, product_code, description, unit, status, created_at) VALUES
(1, 1, 'Luxury Cotton Bath Towel Set', 'PRD-APX-001', 'Set of 4 premium 600 GSM combed cotton bath towels', 'SET', 'ACTIVE', '2026-01-06 10:00:00'),
(2, 1, 'Designer Bed Linen Queen', 'PRD-APX-002', '300 TC Satin stripe fitted bed sheet with 2 pillow covers', 'SET', 'ACTIVE', '2026-01-06 10:30:00'),
(3, 2, 'Noise-Cancelling Wireless Earbuds', 'PRD-ZEN-101', 'True wireless stereo earbuds with active noise cancellation and wireless charging case', 'BOX', 'ACTIVE', '2026-01-11 11:00:00'),
(4, 2, 'Smart Fitness Tracker Pro', 'PRD-ZEN-102', 'AMOLED display fitness band with heart rate and SpO2 sensors', 'BOX', 'ACTIVE', '2026-01-11 11:30:00'),
(5, 3, 'Organic Cold-Pressed Almond Oil 500ml', 'PRD-GLO-201', '100% pure organic cold pressed sweet almond oil in glass bottle', 'BTL', 'ACTIVE', '2026-02-02 09:00:00'),
(6, 3, 'Wild Forest Honey 250g', 'PRD-GLO-202', 'Unprocessed raw forest honey with tamper evident seal', 'JAR', 'ACTIVE', '2026-02-02 09:30:00');

-- 4. Insert Packaging Services
INSERT INTO packaging_services (service_id, service_name, description, base_price, status, created_at) VALUES
(1, 'Corrugated Box Packing', 'Industrial grade 3-ply/5-ply corrugated carton box packing with inner cushioning', 15.00, 'ACTIVE', '2026-01-01 10:00:00'),
(2, 'Stand-Up Pouch Packing', 'Hermetically heat-sealed metallic/kraft barrier stand-up pouch packing', 8.50, 'ACTIVE', '2026-01-01 10:00:00'),
(3, 'Barcode & Compliance Labeling', 'GS1 compliant thermal barcode labeling, QR code, and regulatory sticker printing', 3.00, 'ACTIVE', '2026-01-01 10:00:00'),
(4, 'Anti-Shock Bubble Wrap Cushioning', 'Multi-layer bubble wrap protection and tamper-evident edge protection', 6.00, 'ACTIVE', '2026-01-01 10:00:00'),
(5, 'Multi-Product Kitting & Assembly', 'Assembly of multiple product components, manuals, accessories into bundle package', 25.00, 'ACTIVE', '2026-01-01 10:00:00'),
(6, 'Custom Shrink Wrap & Sealing', 'High clarity polyolefin heat-shrink film encapsulation', 10.00, 'ACTIVE', '2026-01-01 10:00:00');

-- 5. Insert Packaging Material Inventory
-- Note: Includes healthy stock, low stock (available <= reorder_level), and out of stock
INSERT INTO inventory (inventory_id, material_name, material_code, quantity_available, quantity_reserved, reorder_level, unit, updated_at) VALUES
(1, 'Medium Corrugated Box (12x10x6 in)', 'MAT-BOX-M', 650, 80, 150, 'PCS', '2026-03-01 10:00:00'),
(2, 'Large Corrugated Master Carton', 'MAT-BOX-L', 95, 20, 100, 'PCS', '2026-03-01 10:00:00'), -- Low Stock (95 <= 100)
(3, 'Barrier Stand-Up Pouch 500g', 'MAT-POUCH-500', 1400, 200, 300, 'PCS', '2026-03-01 10:00:00'),
(4, 'Heavy Duty Air Bubble Roll (100m)', 'MAT-BUBBLE-ROLL', 18, 5, 25, 'ROLL', '2026-03-01 10:00:00'), -- Low Stock (18 <= 25)
(5, 'Thermal Barcode Labels (1000/roll)', 'MAT-LABEL-ROLL', 40, 10, 50, 'ROLL', '2026-03-01 10:00:00'), -- Low Stock (40 <= 50)
(6, 'Reinforced Brown Packing Tape (65m)', 'MAT-TAPE-BRN', 280, 40, 60, 'ROLL', '2026-03-01 10:00:00'),
(7, 'Security Tamper-Proof Seal Stickers', 'MAT-SEAL-SEC', 0, 0, 50, 'PACK', '2026-03-01 10:00:00'); -- Out of Stock (0)

-- 6. Insert Inventory Initial Purchase Transactions
INSERT INTO inventory_transactions (transaction_id, inventory_id, transaction_type, quantity, reference_type, reference_id, created_at) VALUES
(1, 1, 'PURCHASE', 800, 'PURCHASE_ORDER', 101, '2026-01-02 09:00:00'),
(2, 2, 'PURCHASE', 150, 'PURCHASE_ORDER', 101, '2026-01-02 09:30:00'),
(3, 3, 'PURCHASE', 2000, 'PURCHASE_ORDER', 102, '2026-01-03 10:00:00'),
(4, 4, 'PURCHASE', 50, 'PURCHASE_ORDER', 103, '2026-01-04 11:00:00'),
(5, 5, 'PURCHASE', 100, 'PURCHASE_ORDER', 103, '2026-01-04 11:30:00'),
(6, 6, 'PURCHASE', 350, 'PURCHASE_ORDER', 104, '2026-01-05 14:00:00');

-- 7. Insert Orders with Various Workflow Statuses
-- Workflow: PENDING -> APPROVED -> PROCESSING -> QUALITY_CHECK -> COMPLETED -> DISPATCHED -> DELIVERED (or CANCELLED)
INSERT INTO orders (order_id, order_number, customer_id, order_date, status, subtotal, total_cost, notes, created_at, updated_at) VALUES
(1, 'PKG-2026-00001', 1, '2026-01-15', 'DELIVERED', 1800.00, 1800.00, 'Priority delivery for Q1 promotional launch', '2026-01-15 10:00:00', '2026-01-20 16:30:00'),
(2, 'PKG-2026-00002', 2, '2026-01-25', 'DISPATCHED', 3100.00, 3100.00, 'Handle with extra care - sensitive wireless electronics', '2026-01-25 11:15:00', '2026-01-29 14:00:00'),
(3, 'PKG-2026-00003', 3, '2026-02-10', 'COMPLETED', 1450.00, 1450.00, 'Food-grade certification seal required on each jar', '2026-02-10 09:45:00', '2026-02-14 17:00:00'),
(4, 'PKG-2026-00004', 1, '2026-02-20', 'QUALITY_CHECK', 2250.00, 2250.00, 'Batch inspection for retail chain delivery', '2026-02-20 14:00:00', '2026-02-24 11:20:00'),
(5, 'PKG-2026-00005', 2, '2026-03-01', 'PROCESSING', 3750.00, 3750.00, 'Barcode verification on both inner gift box and outer master carton', '2026-03-01 10:30:00', '2026-03-02 13:45:00'),
(6, 'PKG-2026-00006', 3, '2026-03-02', 'APPROVED', 1275.00, 1275.00, 'Awaiting production line schedule; packaging materials reserved', '2026-03-02 15:00:00', '2026-03-03 09:30:00'),
(7, 'PKG-2026-00007', 1, '2026-03-03', 'PENDING', 900.00, 900.00, 'Customer submitted request, awaiting manager review & quotation approval', '2026-03-03 11:00:00', '2026-03-03 11:00:00'),
(8, 'PKG-2026-00008', 2, '2026-02-05', 'CANCELLED', 1500.00, 1500.00, 'Cancelled due to supplier delay in electronic component supply', '2026-02-05 13:00:00', '2026-02-07 10:00:00');

-- 8. Insert Order Items
INSERT INTO order_items (order_item_id, order_id, product_id, service_id, quantity, unit_price, total_price) VALUES
-- Order 1: 100 sets of towels packed in boxes + barcode
(1, 1, 1, 1, 100, 15.00, 1500.00),
(2, 1, 1, 3, 100, 3.00, 300.00),
-- Order 2: 100 wireless earbuds with assembly & bubble wrap
(3, 2, 3, 5, 100, 25.00, 2500.00),
(4, 2, 3, 4, 100, 6.00, 600.00),
-- Order 3: 100 honey jars in stand-up pouches + bubble wrap
(5, 3, 6, 2, 100, 8.50, 850.00),
(6, 3, 6, 4, 100, 6.00, 600.00),
-- Order 4: 150 bed linen sets in boxes
(7, 4, 2, 1, 150, 15.00, 2250.00),
-- Order 5: 150 fitness trackers with assembly
(8, 5, 4, 5, 150, 25.00, 3750.00),
-- Order 6: 150 almond oil bottles in pouches
(9, 6, 5, 2, 150, 8.50, 1275.00),
-- Order 7: 60 towel sets in boxes
(10, 7, 1, 1, 60, 15.00, 900.00),
-- Order 8: Cancelled order items
(11, 8, 3, 1, 100, 15.00, 1500.00);

-- 9. Insert Material Reservation / Consumption Transactions for Orders
INSERT INTO inventory_transactions (transaction_id, inventory_id, transaction_type, quantity, reference_type, reference_id, created_at) VALUES
-- Order 1: Consumed
(7, 1, 'RESERVATION', 100, 'ORDER', 1, '2026-01-16 10:00:00'),
(8, 1, 'CONSUMPTION', 100, 'ORDER', 1, '2026-01-18 14:00:00'),
-- Order 2: Consumed
(9, 4, 'RESERVATION', 5, 'ORDER', 2, '2026-01-26 11:00:00'),
(10, 4, 'CONSUMPTION', 5, 'ORDER', 2, '2026-01-28 15:00:00'),
-- Order 5: Currently Reserved (Processing)
(11, 1, 'RESERVATION', 80, 'ORDER', 5, '2026-03-01 10:35:00'),
-- Order 6: Currently Reserved (Approved)
(12, 3, 'RESERVATION', 200, 'ORDER', 6, '2026-03-02 15:05:00'),
-- Order 8: Reserved and Released
(13, 1, 'RESERVATION', 100, 'ORDER', 8, '2026-02-05 13:05:00'),
(14, 1, 'RELEASE', 100, 'ORDER', 8, '2026-02-07 10:05:00');

-- 10. Insert Packaging Tasks
INSERT INTO packaging_tasks (task_id, order_id, assigned_to, quantity, completed_quantity, status, started_at, completed_at) VALUES
(1, 1, 2, 100, 100, 'COMPLETED', '2026-01-17 08:30:00', '2026-01-18 16:00:00'),
(2, 2, 2, 100, 100, 'COMPLETED', '2026-01-27 09:00:00', '2026-01-28 17:30:00'),
(3, 3, 2, 100, 100, 'COMPLETED', '2026-02-12 08:45:00', '2026-02-13 15:00:00'),
(4, 4, 2, 150, 150, 'COMPLETED', '2026-02-22 09:00:00', '2026-02-23 16:30:00'),
(5, 5, 2, 150, 60, 'IN_PROGRESS', '2026-03-02 08:30:00', NULL);

-- 11. Insert Quality Checks
INSERT INTO quality_checks (quality_check_id, order_id, checked_by, quantity_checked, quantity_passed, quantity_failed, remarks, status, checked_at) VALUES
(1, 1, 2, 100, 100, 0, 'All outer boxes securely taped, barcode scans verified without error.', 'PASSED', '2026-01-19 11:00:00'),
(2, 2, 2, 100, 100, 0, 'Bubble cushion test passed. No scratches on earbud charging case.', 'PASSED', '2026-01-29 10:30:00'),
(3, 3, 2, 100, 98, 2, '2 jar seals replaced due to loose seal crimping. Final batch approved.', 'PASSED', '2026-02-14 14:00:00'),
(4, 4, 2, 150, 148, 2, 'Minor stain on 2 outer polybags, quarantined for repacking.', 'PARTIAL', '2026-02-24 11:00:00');

-- 12. Insert Dispatches
INSERT INTO dispatches (dispatch_id, order_id, dispatch_date, delivery_partner, tracking_number, status, delivered_at) VALUES
(1, 1, '2026-01-19', 'Blue Dart Express', 'BD-884920192', 'DELIVERED', '2026-01-20 16:30:00'),
(2, 2, '2026-01-29', 'Delhivery Surface', 'DLH-992384711', 'IN_TRANSIT', NULL);
