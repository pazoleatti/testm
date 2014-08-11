---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7519: При развертывании 0.3.9 должны исправляться возможные ошибки в справочнике "Подразделения"
MERGE INTO department a
USING
(
  SELECT connect_by_root id as root_id, id, is_active, level
  FROM department d
  WHERE is_active=1
  START WITH is_active = 0 
  CONNECT BY parent_id = PRIOR id
) b
ON (a.id = b.root_id)
WHEN MATCHED THEN UPDATE SET a.is_active = 1;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7999: Заполнение FORM_TYPE.CODE
UPDATE form_type SET code='852-4' WHERE id=316;
UPDATE form_type SET code='852-5' WHERE id=317;
UPDATE form_type SET code='852-6' WHERE id=318;
UPDATE form_type SET code='852-7' WHERE id=311;
UPDATE form_type SET code='852-8' WHERE id=320;
UPDATE form_type SET code='852-12' WHERE id=364;
UPDATE form_type SET code='852-14' WHERE id=321;
UPDATE form_type SET code='852-16' WHERE id=499;
UPDATE form_type SET code='852-17' WHERE id=501;
UPDATE form_type SET code='852-22' WHERE id=322;
UPDATE form_type SET code='852-23' WHERE id=323;
UPDATE form_type SET code='1111-25' WHERE id=324;
UPDATE form_type SET code='1111-26' WHERE id=325;
UPDATE form_type SET code='1111-27' WHERE id=326;
UPDATE form_type SET code='1210-30' WHERE id=329;
UPDATE form_type SET code='1290-31' WHERE id=328;
UPDATE form_type SET code='1290-32.1' WHERE id=330;
UPDATE form_type SET code='1290-32.2' WHERE id=331;
UPDATE form_type SET code='1290-33' WHERE id=332;
UPDATE form_type SET code='1290-36.1' WHERE id=333;
UPDATE form_type SET code='1290-36.2' WHERE id=315;
UPDATE form_type SET code='1290-38.1' WHERE id=334;
UPDATE form_type SET code='1290-38.2' WHERE id=335;
UPDATE form_type SET code='1290-39.1' WHERE id=336;
UPDATE form_type SET code='1290-39.2' WHERE id=337;
UPDATE form_type SET code='1290-40.1' WHERE id=338;
UPDATE form_type SET code='1290-40.2' WHERE id=339;
UPDATE form_type SET code='852-44' WHERE id=340;
UPDATE form_type SET code='852-45' WHERE id=341;
UPDATE form_type SET code='852-46' WHERE id=342;
UPDATE form_type SET code='852-47' WHERE id=344;
UPDATE form_type SET code='852-48.1' WHERE id=343;
UPDATE form_type SET code='852-48.2' WHERE id=313;
UPDATE form_type SET code='852-49' WHERE id=312;
UPDATE form_type SET code='852-50' WHERE id=365;
UPDATE form_type SET code='1290-51' WHERE id=345;
UPDATE form_type SET code='852-55' WHERE id=348;
UPDATE form_type SET code='852-56' WHERE id=349;
UPDATE form_type SET code='852-57' WHERE id=353;
UPDATE form_type SET code='852-61' WHERE id=352;
UPDATE form_type SET code='852-62' WHERE id=354;
UPDATE form_type SET code='1290-64' WHERE id=355;
UPDATE form_type SET code='852-70.1' WHERE id=504;
UPDATE form_type SET code='852-70.2' WHERE id=357;
UPDATE form_type SET code='852-71.1' WHERE id=356;
UPDATE form_type SET code='852-71.2' WHERE id=503;
UPDATE form_type SET code='852-72' WHERE id=358;
UPDATE form_type SET code='852-75' WHERE id=366;
UPDATE form_type SET code='852-107' WHERE id=502;
UPDATE form_type SET code='852-108' WHERE id=395;
UPDATE form_type SET code='852-110' WHERE id=396;
UPDATE form_type SET code='852-111' WHERE id=367;
UPDATE form_type SET code='1290-112' WHERE id=374;
UPDATE form_type SET code='852-115' WHERE id=369;
UPDATE form_type SET code='852-116' WHERE id=368;
UPDATE form_type SET code='2812-117' WHERE id=370;
UPDATE form_type SET code='2812-118' WHERE id=373;
UPDATE form_type SET code='2812-119' WHERE id=371;
UPDATE form_type SET code='852-120' WHERE id=378;
UPDATE form_type SET code='2263-7.8' WHERE id=362;
UPDATE form_type SET code='1290-53' WHERE id=346;
UPDATE form_type SET code='1290-54' WHERE id=347;
UPDATE form_type SET code='1290-59' WHERE id=350;
UPDATE form_type SET code='1290-60' WHERE id=351;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8007: Преобразовать строковые значения для справочников и НФ
UPDATE string_value SET value = translate(value, '«»', '""') WHERE instr(value, '«')<>0 or instr(value, '»')<>0;
UPDATE department SET tb_index = translate(tb_index, '«»', '""') WHERE instr(tb_index, '«')<>0 or instr(tb_index, '»')<>0;
UPDATE department SET sbrf_code = translate(sbrf_code, '«»', '""') WHERE instr(sbrf_code, '«')<>0 or instr(sbrf_code, '»')<>0;
UPDATE department SET name = translate(name, '«»', '""') WHERE instr(name, '«')<>0 or instr(name, '»')<>0;
UPDATE department SET shortname = translate(shortname, '«»', '""') WHERE instr(shortname, '«')<>0 or instr(shortname, '»')<>0;
UPDATE form_template SET fullname = translate(fullname, '«»', '""') WHERE instr(fullname, '«')<>0 or instr(fullname, '»')<>0;
UPDATE form_template SET header = translate(header, '«»', '""') WHERE instr(header, '«')<>0 or instr(header, '»')<>0;
UPDATE form_template SET name = translate(name, '«»', '""') WHERE instr(name, '«')<>0 or instr(name, '»')<>0;
UPDATE form_type SET name = translate(name, '«»', '""') WHERE instr(name, '«')<>0 or instr(name, '»')<>0;
UPDATE form_type SET code = translate(code, '«»', '""') WHERE instr(code, '«')<>0 or instr(code, '»')<>0;
UPDATE income_101 SET account_name = translate(account_name, '«»', '""') WHERE instr(account_name, '«')<>0 or instr(account_name, '»')<>0;
UPDATE income_101 SET account = translate(account, '«»', '""') WHERE instr(account, '«')<>0 or instr(account, '»')<>0;
UPDATE income_102 SET item_name = translate(item_name, '«»', '""') WHERE instr(item_name, '«')<>0 or instr(item_name, '»')<>0;
UPDATE income_102 SET opu_code = translate(opu_code, '«»', '""') WHERE instr(opu_code, '«')<>0 or instr(opu_code, '»')<>0;
UPDATE ref_book_oktmo SET code = translate(code, '«»', '""') WHERE instr(code, '«')<>0 or instr(code, '»')<>0;
UPDATE ref_book_value SET string_value = translate(string_value, '«»', '""') WHERE instr(string_value, '«')<>0 or instr(string_value, '»')<>0;

---------------------------------------------------------------------------------------------------------
COMMIT;
EXIT;