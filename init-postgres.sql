-- PostgreSQL Initialization Script for Order Processing System
-- Default User/Password: admin / admin

-- Create databases for each microservice if they do not exist
SELECT 'CREATE DATABASE order_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'order_db')\gexec

SELECT 'CREATE DATABASE inventory_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'inventory_db')\gexec

SELECT 'CREATE DATABASE payment_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_db')\gexec

-- Grant privileges to admin user
GRANT ALL PRIVILEGES ON DATABASE order_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE payment_db TO admin;
