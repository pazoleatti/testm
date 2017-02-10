-- Фиас
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1010,'ФИАС Статус действия',0,0,1,null, 'fias_operstat');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1020,'ФИАС Типы адресных объектов',0,0,1,null, 'fias_socrbase');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1030,'ФИАС Реестр адресообразующих объектов',0,0,1,null, 'fias_addrobj');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1040,'ФИАС Реестр объектов адресации',0,0,1,null, 'fias_house');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1050,'ФИАС Интервалы домов',0,0,1,null, 'fias_houseint');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1060,'ФИАС Сведения по помещениям',0,0,1,null, 'fias_room');

insert into fias_operstat(NAME, ID) VALUES ('Не определено', 1);
insert into fias_operstat(NAME, ID) VALUES ('Инициация', 2);
insert into fias_operstat(NAME, ID) VALUES ('Добавление', 3);
insert into fias_operstat(NAME, ID) VALUES ('Изменение', 4);
insert into fias_operstat(NAME, ID) VALUES ('Групповое Изменение', 5);
insert into fias_operstat(NAME, ID) VALUES ('Удаление', 6);
insert into fias_operstat(NAME, ID) VALUES ('Удаление вследствие удаления вышестоящего объекта', 7);
insert into fias_operstat(NAME, ID) VALUES ('Присоединение адресного объекта (слияние)', 8);
insert into fias_operstat(NAME, ID) VALUES ('Переподчинение вследствие слияния вышестоящего объекта', 9);
insert into fias_operstat(NAME, ID) VALUES ('Прекращение существования вследствие присоединения к другому адресному объекту', 10);
insert into fias_operstat(NAME, ID) VALUES ('Создание нового адресного объекта в результате слияния адресных объектов', 11);
insert into fias_operstat(NAME, ID) VALUES ('Переподчинение', 12);
insert into fias_operstat(NAME, ID) VALUES ('Переподчинение вследствие переподчинения вышестоящего объекта', 13);
insert into fias_operstat(NAME, ID) VALUES ('Прекращение существования вследствие переподчинения вышестоящего объекта', 14);
insert into fias_operstat(NAME, ID) VALUES ('Прекращение существования вследствие дробления', 15);
insert into fias_operstat(NAME, ID) VALUES ('Создание нового адресного объекта в результате дробления', 16);
insert into fias_operstat(NAME, ID) VALUES ('Восстановление прекратившего существование объекта', 17);
insert into fias_socrbase(ID, SOCRNAME, SCNAME, KOD_T_ST) VALUES (1, '', null, '0');
insert into fias_socrbase(ID, SOCRNAME, SCNAME, KOD_T_ST) VALUES (2, 'Автономная область', 'а.обл.', '109');
insert into fias_socrbase(ID, SOCRNAME, SCNAME, KOD_T_ST) VALUES (3, 'Автономный округ', 'а.окр.', '110');
insert into fias_socrbase(ID, SOCRNAME, SCNAME, KOD_T_ST) VALUES (4, 'Автономный округ', 'АО', '101');
insert into fias_socrbase(ID, SOCRNAME, SCNAME, KOD_T_ST) VALUES (5, 'Автономная область', 'Аобл', '102');
insert into fias_socrbase(ID, SOCRNAME, SCNAME, KOD_T_ST) VALUES (6, 'Город', 'г', '103');
insert into fias_socrbase(ID, SOCRNAME, SCNAME, KOD_T_ST) VALUES (7, 'Город', 'г.', '111');
insert into fias_socrbase(ID, SOCRNAME, SCNAME, KOD_T_ST) VALUES (8, 'Город федерального значения', 'г.ф.з.', '112');
insert into fias_socrbase(ID, SOCRNAME, SCNAME, KOD_T_ST) VALUES (9, 'Край', 'край', '104');
insert into fias_socrbase(ID, SOCRNAME, SCNAME, KOD_T_ST) VALUES (10, 'Область', 'обл', '105');
insert into fias_socrbase(ID, SOCRNAME, SCNAME, KOD_T_ST) VALUES (11, 'Область', 'обл.', '113');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Адыгея', '000', 1, '000', 'Адыгея', 0, '0000', '385000', 1, null, '0000', '01', '0', '0000', 1, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 2, 'Майкоп', '000', 1, '001', 'Майкоп', 0, '0000', null, 1, 1, '0000', '01', '0', '0000', 2, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Абадзехская', '000', 1, '001', 'Абадзехская', 0, '0000', '385000', 1, 2, '0001', '01', '0', '0000', 3, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Авиационный', '000', 1, '001', 'Авиационный', 0, '0000', '385006', 1, 2, '0003', '01', '0', '0000', 4, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Абрикосовая', '000', 1, '001', 'Абрикосовая', 0, '0000', '385000', 1, 2, '0002', '01', '0', '0000', 5, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Автодорога 7', '000', 1, '001', 'Автодорога 7', 0, '0000', '385019', 1, 2, '0004', '01', '0', '0000', 6, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Армавирская', '000', 1, '001', 'Армавирская', 0, '0000', '385000', 1, 2, '0008', '01', '0', '0000', 7, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Апшеронская', '000', 1, '001', 'Апшеронская', 0, '0000', '385003', 1, 2, '0007', '01', '0', '0000', 8, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Андрухаева', '000', 1, '001', 'Андрухаева', 0, '0000', '385006', 1, 2, '0006', '01', '0', '0000', 9, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Ашхамафа', '000', 1, '001', 'Ашхамафа', 0, '0000', '385007', 1, 2, '0011', '01', '0', '0000', 10, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Адыгейская', '000', 1, '001', 'Адыгейская', 0, '0000', null, 1, 2, '0005', '01', '0', '0000', 11, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Аэродромная', '000', 1, '001', 'Аэродромная', 0, '0000', null, 1, 2, '0012', '01', '0', '0000', 12, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Ачмизова', '000', 1, '001', 'Ачмизова', 0, '0000', '385000', 1, 2, '0010', '01', '0', '0000', 13, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Армейская', '000', 1, '001', 'Армейская', 0, '0000', '385019', 1, 2, '0009', '01', '0', '0000', 14, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Ветеранов 2-я', '000', 1, '001', 'Ветеранов 2-я', 0, '0000', '385007', 1, 2, '0025', '01', '0', '0000', 15, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Батарейная', '000', 1, '001', 'Батарейная', 0, '0000', null, 1, 2, '0013', '01', '0', '0000', 16, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Вишневый', '000', 1, '001', 'Вишневый', 0, '0000', '385012', 1, 2, '0028', '01', '0', '0000', 17, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Верещагина', '000', 1, '001', 'Верещагина', 0, '0000', null, 1, 2, '0021', '01', '0', '0000', 18, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Братьев Лоскутовых', '000', 1, '001', 'Братьев Лоскутовых', 0, '0000', '385009', 1, 2, '0018', '01', '0', '0000', 19, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Весенняя', '000', 1, '001', 'Весенняя', 0, '0000', '385009', 1, 2, '0023', '01', '0', '0000', 20, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Вишневая', '000', 1, '001', 'Вишневая', 0, '0000', '385009', 1, 2, '0027', '01', '0', '0000', 21, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Ветеранов 3-я', '000', 1, '001', 'Ветеранов 3-я', 0, '0000', '385000', 1, 2, '0026', '01', '0', '0000', 22, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Береговая', '000', 1, '001', 'Береговая', 0, '0000', '385012', 1, 2, '0017', '01', '0', '0000', 23, '000', '000', '000');
insert into fias_addrobj(CURRSTATUS, CENTSTATUS, FORMALNAME, SEXTCODE, LIVESTATUS, CITYCODE, OFFNAME, DIVTYPE, PLANCODE, POSTALCODE, OPERSTATUS, PARENTGUID, STREETCODE, REGIONCODE, AUTOCODE, EXTRCODE, ID, CTARCODE, AREACODE, PLACECODE) VALUES (0, 0, 'Братьев Соловьевых', '000', 1, '001', 'Братьев Соловьевых', 0, '0000', '385000', 1, 2, '0019', '01', '0', '0000', 24, '000', '000', '000');
insert into fias_house(DIVTYPE, STATSTATUS, POSTALCODE, STRSTATUS, STRUCNUM, ESTSTATUS, BUILDNUM, ID, HOUSENUM, AOGUID) VALUES (0, 1, '111222', 0, '', 1, '12/34', 1, 'a', 18);
insert into fias_house(DIVTYPE, STATSTATUS, POSTALCODE, STRSTATUS, STRUCNUM, ESTSTATUS, BUILDNUM, ID, HOUSENUM, AOGUID) VALUES (0, 1, '222333', 0, '', 1, '123А', 2, 'a', 18);
insert into fias_house(DIVTYPE, STATSTATUS, POSTALCODE, STRSTATUS, STRUCNUM, ESTSTATUS, BUILDNUM, ID, HOUSENUM, AOGUID) VALUES (0, 1, '333111', 0, '', 1, '4', 3, 'a', 19);
insert into fias_house(DIVTYPE, STATSTATUS, POSTALCODE, STRSTATUS, STRUCNUM, ESTSTATUS, BUILDNUM, ID, HOUSENUM, AOGUID) VALUES (0, 1, '333111', 0, '', 1, '11', 4, 'a', 20);
insert into fias_house(DIVTYPE, STATSTATUS, POSTALCODE, STRSTATUS, STRUCNUM, ESTSTATUS, BUILDNUM, ID, HOUSENUM, AOGUID) VALUES (0, 1, '123132', 0, '', 1, '5', 5, 'a', 19);
insert into fias_houseint(POSTALCODE, COUNTER, INTEND, ID, INTSTATUS, INTSTART, AOGUID) VALUES ('787878', 0, 999, 1, 0, 0, 23);
insert into fias_houseint(POSTALCODE, COUNTER, INTEND, ID, INTSTATUS, INTSTART, AOGUID) VALUES ('323232', 0, 999, 2, 0, 5, 23);
insert into fias_houseint(POSTALCODE, COUNTER, INTEND, ID, INTSTATUS, INTSTART, AOGUID) VALUES ('121212', 0, 100, 3, 0, 10, 23);
insert into fias_houseint(POSTALCODE, COUNTER, INTEND, ID, INTSTATUS, INTSTART, AOGUID) VALUES ('111222', 0, 123, 4, 0, 1, 24);
insert into fias_houseint(POSTALCODE, COUNTER, INTEND, ID, INTSTATUS, INTSTART, AOGUID) VALUES ('111222', 0, 50, 5, 0, 0, 24);
insert into fias_room(POSTALCODE, REGIONCODE, ROOMTYPEID, FLATNUMBER, ID, ROOMNUMBER, HOUSEGUID, LIVESTATUS, FLATTYPE) VALUES ('111111', '77', null, 'a1', 1, 'a5', 1, 0, 0);
insert into fias_room(POSTALCODE, REGIONCODE, ROOMTYPEID, FLATNUMBER, ID, ROOMNUMBER, HOUSEGUID, LIVESTATUS, FLATTYPE) VALUES ('123456', '55', null, '5', 2, '4', 1, 0, 0);
insert into fias_room(POSTALCODE, REGIONCODE, ROOMTYPEID, FLATNUMBER, ID, ROOMNUMBER, HOUSEGUID, LIVESTATUS, FLATTYPE) VALUES ('777444', '11', null, '10', 3, 'a8', 1, 0, 0);
insert into fias_room(POSTALCODE, REGIONCODE, ROOMTYPEID, FLATNUMBER, ID, ROOMNUMBER, HOUSEGUID, LIVESTATUS, FLATTYPE) VALUES ('111222', '22', null, '1', 4, '4a', 4, 0, 0);
insert into fias_room(POSTALCODE, REGIONCODE, ROOMTYPEID, FLATNUMBER, ID, ROOMNUMBER, HOUSEGUID, LIVESTATUS, FLATTYPE) VALUES ('111222', 'aa', null, '5', 5, '8', 4, 0, 0);

-- Запрос в com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao.findAddress
-- WITH parent_to_child AS
--   (SELECT DISTINCT fa.id, fa.parentguid AS pid, fa.regioncode, fa.formalname AS fname, level AS aolevel, connect_by_isleaf AS isleaf, fa.currstatus AS status, connect_by_root fa.formalname AS ancestor, sys_connect_by_path(fa.formalname, '#') AS aopath
--   FROM fias_addrobj fa
--   WHERE REPLACE(lower(fa.formalname), ' ', '') = REPLACE(lower(:formalName), ' ', '')
--     START WITH fa.parentguid                  IS NULL
--   AND fa.regioncode                            = :regionCode
--     CONNECT BY prior fa.id                     = fa.parentguid
--   )
-- SELECT ptc.id, ptc.pid, ptc.regioncode, ptc.fname, ptc.aolevel, ptc.isleaf, ptc.aopath, ptc.ancestor
-- FROM parent_to_child ptc
-- WHERE REPLACE(lower(ptc.aopath), ' ', '') = REPLACE(lower(concat(concat('#', ptc.ancestor), :formalPath)), ' ', '')
-- AND ptc.status                            = 0
