-- Separate databases per microservice (Database-per-Service pattern).
-- One service must never query another service's tables directly.
CREATE DATABASE user_db;
CREATE DATABASE order_db;
CREATE DATABASE notification_db;
