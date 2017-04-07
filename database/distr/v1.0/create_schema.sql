-- Change password for user NDFL
CREATE USER NDFL IDENTIFIED BY ndfl;
GRANT CREATE SESSION TO NDFL;
GRANT CREATE TABLE TO NDFL;
GRANT CREATE PROCEDURE TO NDFL;
GRANT CREATE TRIGGER TO NDFL;
GRANT CREATE VIEW TO NDFL;
GRANT CREATE SEQUENCE TO NDFL;


-- Grant/Revoke role privileges 
grant connect to NDFL;
grant resource to NDFL;
-- Grant/Revoke system privileges 
grant advisor to NDFL;
grant create any view to NDFL;
grant select any sequence to NDFL;
grant select any table to NDFL;