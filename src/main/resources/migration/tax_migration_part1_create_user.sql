--TAX MIGRATION 
--create user MIGRATION
create user MIGRATION identified by TAX;

-- Grant/Revoke role privileges 
grant connect to MIGRATION;
grant resource to MIGRATION;
-- Grant/Revoke system privileges 
grant create any index to MIGRATION;
grant create any view to MIGRATION;
