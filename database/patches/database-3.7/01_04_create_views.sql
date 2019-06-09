-- 3.7-dnovikov-18
			CREATE OR REPLACE FORCE VIEW VW_DEPART_KPP_OKTMO (DEP_ID, DEP_NAME, KPP, OKTMO) AS
			select dep.id dep_id, dep.name dep_name, dc.kpp, oktmo.code oktmo
			from department dep
			join department_config dc on dc.department_id = dep.id
			join ref_book_oktmo oktmo on oktmo.id = dc.oktmo_id;

/
COMMIT;
/
