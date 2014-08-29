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
-- http://jira.aplana.com/browse/SBRFACCTAX-8586: Исправить записи в справочнике ОКТМО
UPDATE ref_book_oktmo SET code = substr(code, 1, 8) WHERE substr(code, 9, 3) = '000';

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8569: Заполнение SBRF_CODE по постановке
UPDATE department SET sbrf_code = null WHERE sbrf_code = '99_9006_00';

UPDATE department SET sbrf_code = '00__' WHERE id = 0;
UPDATE department SET sbrf_code = '13_0000_00' WHERE id = 102;
UPDATE department SET sbrf_code = '13_0001_00' WHERE id = 103;
UPDATE department SET sbrf_code = '16_0000_00' WHERE id = 97;
UPDATE department SET sbrf_code = '16_0001_00' WHERE id = 98;
UPDATE department SET sbrf_code = '18_0000_00' WHERE id = 4;
UPDATE department SET sbrf_code = '18_0001_00' WHERE id = 5;
UPDATE department SET sbrf_code = '31_0000_00' WHERE id = 16;
UPDATE department SET sbrf_code = '31_0001_00' WHERE id = 17;
UPDATE department SET sbrf_code = '36_0000_00' WHERE id = 59;
UPDATE department SET sbrf_code = '36_0001_00' WHERE id = 60;
UPDATE department SET sbrf_code = '38_0000_00' WHERE id = 37;
UPDATE department SET sbrf_code = '38_0001_00' WHERE id = 38;
UPDATE department SET sbrf_code = '38_0002_00' WHERE id = 138;
UPDATE department SET sbrf_code = '38_0002_01' WHERE id = 139;
UPDATE department SET sbrf_code = '40_0000_00' WHERE id = 88;
UPDATE department SET sbrf_code = '40_0001_00' WHERE id = 89;
UPDATE department SET sbrf_code = '42_0000_00' WHERE id = 8;
UPDATE department SET sbrf_code = '42_0001_00' WHERE id = 9;
UPDATE department SET sbrf_code = '44_0000_00' WHERE id = 82;
UPDATE department SET sbrf_code = '44_0001_00' WHERE id = 83;
UPDATE department SET sbrf_code = '49_0000_00' WHERE id = 32;
UPDATE department SET sbrf_code = '49_0001_00' WHERE id = 33;
UPDATE department SET sbrf_code = '52_0000_00' WHERE id = 109;
UPDATE department SET sbrf_code = '52_0001_00' WHERE id = 110;
UPDATE department SET sbrf_code = '54_0000_00' WHERE id = 44;
UPDATE department SET sbrf_code = '54_0000_01' WHERE id = 45;
UPDATE department SET sbrf_code = '55_0000_00' WHERE id = 64;
UPDATE department SET sbrf_code = '55_0001_00' WHERE id = 65;
UPDATE department SET sbrf_code = '60_0000_00' WHERE id = 72;
UPDATE department SET sbrf_code = '60_0001_00' WHERE id = 73;
UPDATE department SET sbrf_code = '67_0000_00' WHERE id = 27;
UPDATE department SET sbrf_code = '67_0001_00' WHERE id = 28;
UPDATE department SET sbrf_code = '70_0000_00' WHERE id = 20;
UPDATE department SET sbrf_code = '70_0001_00' WHERE id = 21;
UPDATE department SET sbrf_code = '77_0000_00' WHERE id = 52;
UPDATE department SET sbrf_code = '77_0001_00' WHERE id = 53;
UPDATE department SET sbrf_code = '99_0000_00' WHERE id = 113;
UPDATE department SET sbrf_code = '99_1000_00' WHERE id = 163;
UPDATE department SET sbrf_code = '99_1010_00' WHERE id = 164;
UPDATE department SET sbrf_code = '99_1020_00' WHERE id = 165;
UPDATE department SET sbrf_code = '99_1030_00' WHERE id = 166;
UPDATE department SET sbrf_code = '99_1040_00' WHERE id = 167;
UPDATE department SET sbrf_code = '99_1050_00' WHERE id = 168;
UPDATE department SET sbrf_code = '99_1060_00' WHERE id = 169;
UPDATE department SET sbrf_code = '99_1070_00' WHERE id = 170;
UPDATE department SET sbrf_code = '99_1080_00' WHERE id = 171;
UPDATE department SET sbrf_code = '99_1090_00' WHERE id = 172;
UPDATE department SET sbrf_code = '99_1100_00' WHERE id = 173;
UPDATE department SET sbrf_code = '99_11701_00' WHERE id = 176;
UPDATE department SET sbrf_code = '99_11701_01' WHERE id = 177;
UPDATE department SET sbrf_code = '99_11702_00' WHERE id = 224;
UPDATE department SET sbrf_code = '99_11702_01' WHERE id = 125;
UPDATE department SET sbrf_code = '99_11702_02_01' WHERE id = 146;
UPDATE department SET sbrf_code = '99_11702_02_12' WHERE id = 157;
UPDATE department SET sbrf_code = '99_11702_02_13' WHERE id = 158;
UPDATE department SET sbrf_code = '99_11702_02' WHERE id = 126;
UPDATE department SET sbrf_code = '99_11702_02_02' WHERE id = 147;
UPDATE department SET sbrf_code = '99_11702_02_04' WHERE id = 149;
UPDATE department SET sbrf_code = '99_11702_02_05' WHERE id = 150;
UPDATE department SET sbrf_code = '99_11702_02_11' WHERE id = 156;
UPDATE department SET sbrf_code = '99_11702_02_03' WHERE id = 148;
UPDATE department SET sbrf_code = '99_11702_02_06' WHERE id = 151;
UPDATE department SET sbrf_code = '99_11702_02_08' WHERE id = 153;
UPDATE department SET sbrf_code = '99_11702_02_07' WHERE id = 152;
UPDATE department SET sbrf_code = '99_11702_02_10' WHERE id = 155;
UPDATE department SET sbrf_code = '99_11702_02_09' WHERE id = 154;
UPDATE department SET sbrf_code = '99_11702_02_14' WHERE id = 159;
UPDATE department SET sbrf_code = '99_11702_03_02' WHERE id = 144;
UPDATE department SET sbrf_code = '99_11702_03_03' WHERE id = 145;
UPDATE department SET sbrf_code = '99_11702_03' WHERE id = 123;
UPDATE department SET sbrf_code = '99_11702_03_01' WHERE id = 143;
UPDATE department SET sbrf_code = '99_1170_00' WHERE id = 175;
UPDATE department SET sbrf_code = '99_1180_00' WHERE id = 179;
UPDATE department SET sbrf_code = '99_1190_00' WHERE id = 180;
UPDATE department SET sbrf_code = '99_1260_00' WHERE id = 178;
UPDATE department SET sbrf_code = '99_1260_03' WHERE id = 225;
UPDATE department SET sbrf_code = '99_2540_00' WHERE id = 142;
UPDATE department SET sbrf_code = '99_6100_00' WHERE id = 115;
UPDATE department SET sbrf_code = '99_6100_01' WHERE id = 118;
UPDATE department SET sbrf_code = '99_6100_02' WHERE id = 116;
UPDATE department SET sbrf_code = '99_6100_03' WHERE id = 119;
UPDATE department SET sbrf_code = '99_6100_04' WHERE id = 117;
UPDATE department SET sbrf_code = '99_6100_05' WHERE id = 120;
UPDATE department SET sbrf_code = '99_6200_00' WHERE id = 1;
UPDATE department SET sbrf_code = '99_9000_00' WHERE id = 130;
UPDATE department SET sbrf_code = '99_9600_00' WHERE id = 160;
UPDATE department SET sbrf_code = '99_9700_00' WHERE id = 161;
UPDATE department SET sbrf_code = '99_9800_00' WHERE id = 162;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8655: Удаление макетов при отсутствии версии

DELETE FROM declaration_source
WHERE src_department_form_type_id IN
    (SELECT id
     FROM department_form_type
     WHERE form_type_id IN (361, 378, 367, 395, 369, 374, 373, 370, 368, 396, 410, 359, 363, 327, 502, 371));


DELETE FROM form_data_source
WHERE department_form_type_id IN
    (SELECT id
     FROM department_form_type
     WHERE form_type_id IN (361, 378, 367, 395, 369, 374, 373, 370, 368, 396, 410, 359, 363, 327, 502, 371))
  OR src_department_form_type_id IN
    (SELECT id
     FROM department_form_type
     WHERE form_type_id IN (361, 378, 367, 395, 369, 374, 373, 370, 368, 396, 410, 359, 363, 327, 502, 371));

DELETE FROM department_form_type WHERE form_type_id IN (361, 378, 367, 395, 369, 374, 373, 370, 368, 396, 410, 359, 363, 327, 502, 371);

DELETE FROM form_data
WHERE form_template_id IN
    (SELECT id
     FROM form_template
     WHERE type_id IN (361, 378, 367, 395, 369, 374, 373, 370, 368, 396, 410, 359, 363, 327, 502, 371));


DELETE FROM form_template WHERE type_id IN (361, 378, 367, 395, 369, 374, 373, 370, 368, 396, 410, 359, 363, 327, 502, 371);
DELETE FROM form_type WHERE id IN (361, 378, 367, 395, 369, 374, 373, 370, 368, 396, 410, 359, 363, 327, 502, 371);
---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8536: В справочнике "Общероссийский классификатор валют" есть дубль

UPDATE ref_book_record SET status = -1 WHERE id =
(SELECT MIN(record_id) FROM ref_book_value WHERE attribute_id = 66 AND string_value = 'Бразильский реал')
AND (SELECT COUNT(record_id) FROM ref_book_value WHERE attribute_id = 66 AND string_value = 'Бразильский реал') = 2;

UPDATE ref_book_value SET string_value = '986' WHERE attribute_id = 64 AND record_id =
(SELECT r.id FROM ref_book_record r JOIN ref_book_value v ON v.record_id = r.id WHERE v.attribute_id = 66 AND v.string_value = 'Бразильский реал' AND r.status = 0);
---------------------------------------------------------------------------------------------------------
COMMIT;
EXIT;