
ALTER TABLE ref_book DISABLE CONSTRAINT ref_book_fk_region;
ALTER TABLE ref_book_attribute DISABLE CONSTRAINT ref_book_attr_fk_attribute_id;
ALTER TABLE ref_book_attribute DISABLE CONSTRAINT ref_book_attr_fk_ref_book_id;
ALTER TABLE ref_book_attribute DISABLE CONSTRAINT ref_book_attr_fk_reference_id;

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (0,'Тест',0,0,0,null);

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) VALUES (2,'Коды представления налоговой декларации по месту нахождения (учёта)',1,0,0,null,'REF_BOOK_TAX_PLACE_TYPE');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) VALUES (8,'Коды, определяющие налоговый (отчётный) период',1,0,1,null,'REPORT_PERIOD_TYPE');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) VALUES (10,'ОК 025-2001 (Общероссийский классификатор стран мира)',1,0,0,null,'REF_BOOK_COUNTRY');

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) VALUES (25,'Признак возложения обязанности по уплате налога на обособленное подразделение',1,0,0,null,'REF_BOOK_DETACH_TAX_PAY');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) VALUES (26,'Признак составления расчёта',1,0,0,null,'REF_BOOK_MAKE_CALC');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (30,'Подразделения',1,1,0,null, 'DEPARTMENT');

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) VALUES (35,'Признак лица, подписавшего документ',1,0,0,null,'REF_BOOK_SIGNATORY_MARK');

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (74,'Пользователи',0,0,1,null, 'SEC_USER');

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (95,'Системные роли',1,0,1,null, 'SEC_ROLE');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (96,'Общероссийский классификатор территорий муниципальных образований (ОКТМО)',1,0,1,null, 'REF_BOOK_OKTMO');

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (103,'Тип подразделения',1,0,1,null, 'DEPARTMENT_TYPE');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) VALUES (104,'Список полей для Журнала аудита',0,0,1,null,'LOG_SYSTEM_FIELDS');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (105,'Конфигурационные параметры',0,0,1,null);

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (204, 'Коды налоговых органов', 0, 0, 1, null);
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (205, 'КПП налоговых органов', 0, 0, 1, null);

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (207, 'Макеты форм', 1, 0, 1, null, 'DECLARATION_TYPE');

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) VALUES (360,'Коды документов',1,0,0,null,'REF_BOOK_DOC_TYPE');

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (400, 'Настройки почты', 0, 0, 0, null, 'CONFIGURATION_EMAIL');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (401, 'Настройки асинхронных задач', 0, 0, 0, null, 'ASYNC_TASK_TYPE');

-- Фиас
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1010,'ФИАС Статус действия',0,0,1,null, 'fias_operstat');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1020,'ФИАС Типы адресных объектов',0,0,1,null, 'fias_socrbase');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1030,'ФИАС Реестр адресообразующих объектов',0,0,1,null, 'fias_addrobj');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1040,'ФИАС Реестр объектов адресации',0,0,1,null, 'fias_house');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1050,'ФИАС Интервалы домов',0,0,1,null, 'fias_houseint');
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name) VALUES (1060,'ФИАС Сведения по помещениям',0,0,1,null, 'fias_room');

UPDATE ref_book SET is_versioned = 0 WHERE id IN (30, 93, 207, 95, 74, 103, 94, 105, 104, 108, 204, 205, 400, 401, 510, 511, 106);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (0,0,'Тестовое наименование','NAME',1,1,null,null,1,null,10,0,0,null,null,0,2000);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3,2,'Код','CODE',1,0,null,null,1,null,10,1,1,null,null,0,3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4,2,'Наименование','NAME',1,1,null,null,1,null,100,1,0,null,null,0,255);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (25,8,'Код','CODE',1,0,null,null,1,null,10,1,1,null,null,0,2);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (26,8,'Наименование','NAME',1,1,null,null,1,null,30,1,0,null,null,0,255);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (27,8,'Признак принадлежности к НДФЛ','N',2,3,null,null,1,0,6,1,0,null,null,0,1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (28,8,'Признак принадлежности к Страховым сборам, взносам','F',2,4,null,null,1,0,6,1,0,null,null,0,1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (820,8,'Дата начала периода','START_DATE',3,10,null,null,1,null,10,1,0,null,5,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (821,8,'Дата окончания периода','END_DATE',3,11,null,null,1,null,10,1,0,null,5,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (844,8,'Календарная дата начала периода','CALENDAR_START_DATE',3,12,null,null,1,null,10,1,0,null,5,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (50,10,'Код','CODE',1,0,null,null,1,null,3,1,1,null,null,0,3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (51,10,'Код (2-х букв.)','CODE_2',1,1,null,null,1,null,10,1,2,null,null,0,2);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (52,10,'Код (3-х букв.)','CODE_3',1,2,null,null,1,null,10,1,3,null,null,0,3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (53,10,'Краткое наименование','NAME',1,3,null,null,1,null,30,1,4,null,null,0,500);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (54,10,'Полное наименование','FULLNAME',1,4,null,null,1,null,50,1,5,null,null,0,500);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (110,25,'Код','CODE',2,0,null,null,1,0,10,1,1,null,null,0,1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (111,25,'Значение','NAME',1,1,null,null,1,null,50,1,0,null,null,0,50);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (120,26,'Код','CODE',2,0,null,null,1,0,10,1,1,null,null,0,1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (121,26,'Значение','NAME',1,1,null,null,1,null,50,1,0,null,null,0,255);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (160,30,'Код подразделения','CODE',2,0,null,null,1,0,5,1,1,null,null,0,15);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (161,30,'Наименование подразделения','NAME',1,1,null,null,1,null,50,1,0,null,null,0,255);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (162,30,'Сокращенное наименование подразделения','SHORTNAME',1,2,null,null,1,null,50,0,0,null,null,0,255);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (163,30,'Код родительского подразделения','PARENT_ID',4,3,30,161,1,null,50,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (164,30,'Тип подразделения','TYPE',4,4,103,1031,1,null,15,1,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (165,30,'Индекс территориального банка','TB_INDEX',1,5,null,null,1,null,10,0,0,null,null,0,2);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (166,30,'Код подразделения в нотации Сбербанка','SBRF_CODE',1,6,null,null,1,null,10,0,2,null,null,0,255);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (845,30,'Регион','REGION_ID',4,7,4,10,1,null,20,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (870,30,'Действующее подразделение','IS_ACTIVE',2,8,null,null,1,0,15,0,0,null,6,0,19);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (167,30,'Используется в АС "Гарантии"','GARANT_USE',2,9,null,null,1,0,15,0,0,null,6,1,1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (168,30,'Используется в АС СУНР','SUNR_USE',2,10,null,null,1,0,15,0,0,null,6,0,1);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (212,35,'Код лица, подписавшего документ','CODE',2,0,null,null,1,0,10,1,1,null,null,0,1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (213,35,'Лицо, подписавшее документ','NAME',1,1,null,null,1,null,50,1,0,null,null,0,50);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (651,74,'Код пользователя','ID',2,1,null,null,1,0,9,0,1,null,null,0,19);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (652,74,'ФИО','NAME',1,2,null,null,1,null,50,0,0,null,null,0,2000);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (653,74,'Код подразделения','DEPARTMENT_ID',4,3,30,162,1,null,10,0,0,null,null,0,null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (654,74,'E-mail','EMAIL',1,4,null,null,1,null,128,0,0,null,null,0,2000);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (838,95,'Наименование','NAME',1,1,null,null,1,null,30,1,0,0,null,0,2000);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (839,95,'Код роли','ALIAS',1,2,null,null,1,null,30,1,1,null,null,0,2000);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (840,96,'Код','CODE',1,1,null,null,1,null,5,1,1,0,null,0,11);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (841,96,'Наименование','NAME',1,2,null,null,1,null,20,1,0,null,null,0,500);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (842,96,'Раздел','RAZD',2,3,null,null,1,0,6,0,0,null,null,0,1);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (1031,103,'Наименование','NAME',1,0,null,null,1,null,20,0,0,null,null,0,2000);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (1050,104,'Наименование','NAME',1,1,null,null,1,null,20,0,0,null,null,0,2000);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (1040,105,'Код','CODE',1,0,null,null,0,null,20,0,0,null,null,0,2000);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (1041,105,'Наименование','NAME',1,1,null,null,1,null,40,0,0,null,null,0,2000);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2041, 204, 'Код налогового органа', 'TAX_ORGAN_CODE', 1, 1, NULL, NULL, 1, NULL, 10, 0, 0, NULL, NULL, 0, 4);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2051, 205, 'КПП', 'KPP', 1, 1, NULL, NULL, 1, NULL, 10, 0, 0, NULL, NULL, 0, 9);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2071, 207,'Наименование','NAME',1,1,null,null,1,null,50,1,0,0,null,0,2000);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2072, 207,'Вид налога','TAX_TYPE',1,2,null,null,1,null,10,1,0,null,null,0,2000);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3601, 360,'Код','CODE',1,1,null,null,1,null,6,1,1,null,null,0,2);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3602, 360,'Наименование документа','NAME',1,2,null,null,1,null,50,1,0,null,null,0,2000);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (3603, 360,'Приоритет','PRIORITY',2,3,null,null,1,0,6,1,0,null,null,0,2);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4000,400,'№','ID', 2,0,null,null,1,0,10,1,1,1,null,1,9);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4001,400,'Название','NAME',1,1,null,null,1,null,10,0,0,null,null,1,200);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4002,400,'Значение','VALUE',1,2,null,null,1,null,10,0,0,null,null,0,200);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4003,400,'Описание','DESCRIPTION',1,3,null,null,1,null,30,0,0,null,null,1,1000);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4101, 401, '№', 'ID', 2, 1, null, null, 1, 0, 10, 1, 1, 1, null, 0, 18);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4102, 401, 'Название типа задачи', 'NAME', 1, 2, null, null, 1, null, 10, 1, 0, null, null, 0, 300);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4103, 401, 'JNDI имя класса-обработчика', 'HANDLER_JNDI', 1, 3, null, null, 1, null, 10, 1, 0, null, null, 0, 500);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4104, 401, 'Ограничение на выполнение задания в очереди быстрых заданий', 'SHORT_QUEUE_LIMIT', 2, 4, null, null, 1, 0, 10, 1, 0, null, null, 0, 18);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4105, 401, 'Ограничение на выполнение задания', 'TASK_LIMIT', 2, 5, null, null, 1, 0, 10, 1, 0, null, null, 0, 18);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4106, 401, 'Вид ограничения', 'LIMIT_KIND', 1, 6, null, null, 1, null, 10, 0, 0, null, null, 0, 400);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (4107, 401, 'Признак задачи для dev-мода', 'DEV_MODE', 2, 7, null, null, 1, 0, 10, 1, 0, null, null, 0, 1);

-- АСНУ
insert into ref_book (id,name,visible,type,read_only,region_attribute_id,table_name,is_versioned) values (900,'АСНУ',1,0,1,null,'REF_BOOK_ASNU',0);

insert into ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) values (9002,900,'Код АСНУ','CODE',1,1,null,null,1,null,5,1,0,null,null,0,4);
insert into ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) values (9003,900,'Наименование АСНУ','NAME',1,2,null,null,1,null,25,0,0,null,null,0,100);
insert into ref_book_attribute (id,ref_book_id,name,alias,type,ord,reference_id,attribute_id,visible,precision,width,required,is_unique,sort_order,format,read_only,max_length) values (9004,900,'Тип дохода','TYPE',1,3,null,null,1,null,50,0,0,null,null,0,255);

-- Физические лица и статусы налогоплательщиков
-- с учетом изменений по задаче SBRFNDFL-132
insert into ref_book (id, name, visible, type, read_only, table_name, is_versioned) values (901, 'Адреса физических лиц', 1, 0, 0, 'REF_BOOK_ADDRESS', 0);
insert into ref_book (id, name, visible, type, read_only, table_name, is_versioned) values (902, 'Документы, удостоверяющие личность', 1, 0, 0, 'REF_BOOK_ID_DOC', 0);
insert into ref_book (id, name, visible, type, read_only, table_name, is_versioned) values (903, 'Статусы налогоплательщика', 1, 0, 0, 'REF_BOOK_TAXPAYER_STATE', 0);
insert into ref_book (id, name, visible, type, read_only, table_name, is_versioned) values (904, 'Физические лица', 1, 0, 0, 'REF_BOOK_PERSON', 1);
insert into ref_book (id, name, visible, type, read_only, table_name, is_versioned) values (905, 'Идентификаторы налогоплательщика', 1, 0, 0, 'REF_BOOK_ID_TAX_PAYER', 0);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9120, 901, 'Тип адреса', 'ADDRESS_TYPE', 2, 1, null, null, 1, 0, 15, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9111, 901, 'Код региона', 'REGION_CODE', 1, 3, null, null, 1, null, 15, 0, 0, null, null, 0, 2);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9112, 901, 'Индекс', 'POSTAL_CODE', 1, 4, null, null, 1, null, 15, 0, 0, null, null, 0, 6);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9113, 901, 'Район', 'DISTRICT', 1, 5, null, null, 1, null, 15, 0, 0, null, null, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9114, 901, 'Город', 'CITY', 1, 6, null, null, 1, null, 15, 0, 0, null, null, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9115, 901, 'Населенный пункт (село, поселок)', 'LOCALITY', 1, 7, null, null, 1, null, 15, 0, 0, null, null, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9116, 901, 'Улица (проспект, переулок)', 'STREET', 1, 8, null, null, 1, null, 15, 0, 0, null, null, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9117, 901, 'Номер дома (владения)', 'HOUSE', 1, 9, null, null, 1, null, 15, 0, 0, null, null, 0, 20);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9118, 901, 'Номер корпуса (строения)', 'BUILD', 1, 10, null, null, 1, null, 15, 0, 0, null, null, 0, 20);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9119, 901, 'Номер квартиры', 'APPARTMENT', 1, 11, null, null, 1, null, 15, 0, 0, null, null, 0, 20);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9110, 901, 'Код страны проживания', 'COUNTRY_ID', 4, 2, 10, 50, 1, null, 15, 0, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values( 9121, 901, 'Адрес', 'ADDRESS', 1, 12, null, null, 1, null, 15, 0, 0, null, null, 0, 255);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9021, 902, 'Код ДУЛ', 'DOC_ID', 4, 1, 360, 3601, 1, null, 15, 1, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9022, 902, 'Серия и номер ДУЛ', 'DOC_NUMBER', 1, 2, null, null, 1, null, 15, 1, 0, null, null, 0, 25);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9023, 902, 'Кем выдан  ДУЛ', 'ISSUED_BY', 1, 3, null, null, 1, null, 15, 0, 0, null, null, 0, 255);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9024, 902, 'Дата выдачи документа ДУЛ', 'ISSUED_DATE', 3, 4, null, null, 1, null, 15, 0, 0, null, 1, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9025, 902, 'Включается в отчетность', 'INC_REP', 2, 5, null, null, 1, 0, 6, 0, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name ,alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9026, 902, 'Физ. лицо', 'PERSON_ID', 4, 6, 904, 9041, 1, null, 15, 0, 0, null, null, 0, null);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9031, 903, 'Код', 'CODE', 1, 1, null, null, 1, null, 1, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9032, 903, 'Наименование', 'NAME', 1, 2, null, null, 1, null, 15, 1, 0, null, null, 0, 1000);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9059, 904, 'Идентификатор ФЛ', 'RECORD_ID', 2, 0, null, null, 1, 0, 18, 0, 1, null, null, 1, 18);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9041, 904, 'Фамилия', 'LAST_NAME', 1, 1, null, null, 1, null, 15, 1, 0, null, null, 0, 60);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9042, 904, 'Имя', 'FIRST_NAME', 1, 2, null, null, 1, null, 15, 1, 0, null, null, 0, 60);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9043, 904, 'Отчество', 'MIDDLE_NAME', 1, 3, null, null, 1, null, 15, 0, 0, null, null, 0, 60);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9044, 904, 'Пол', 'SEX', 2, 4, null, null, 1, 0, 15, 0, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9045, 904, 'ИНН в Российской Федерации', 'INN', 1, 5, null, null, 1, null, 12, 0, 0, null, null, 0, 12);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9046, 904, 'ИНН в стране гражданства', 'INN_FOREIGN', 1, 6, null, null, 1, null, 15, 0, 0, null, null, 0, 50);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9047, 904, 'СНИЛС', 'SNILS', 1, 7, null, null, 1, null, 14, 0, 0, null, null, 0, 14);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9048, 904, 'Статус налогоплательщика ', 'TAXPAYER_STATE', 4, 8, 903, 9031, 0, null, 15, 0, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9050, 904, 'Дата рождения', 'BIRTH_DATE', 3, 10, null, null, 1, null, 15, 1, 0, null, 1, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9051, 904, 'Место рождения', 'BIRTH_PLACE', 1, 11, null, null, 1, null, 15, 0, 0, null, null, 0, 255);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9052, 904, 'Гражданство', 'CITIZENSHIP', 4, 12, 10, 50, 1, null, 3, 1, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9054, 904, 'Адрес места жительства', 'ADDRESS', 4, 14, 901, 9114, 1, null, 15, 0, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9055, 904, 'Признак застрахованного лица в системе обязательного пенсионного страхования', 'PENSION', 2, 15, null, null, 1, 0, 15, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9056, 904, 'Признак застрахованного лица в системе обязательного медицинского страхования', 'MEDICAL', 2, 16, null, null, 1, 0, 15, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9057, 904, 'Признак застрахованного лица в системе обязательного социального страхования', 'SOCIAL', 2, 17, null, null, 1, 0, 15, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9058, 904, 'Сотрудник', 'EMPLOYEE', 2, 18, null, null, 1, 0, 15, 1, 0, null, null, 0, 1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9060, 904, 'Система-источник', 'SOURCE_ID', 4, 19, 900, 9002, 1, null, 15, 0, 0, null, null, 0, null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9063, 904, 'Старый идентификатор ФЛ', 'OLD_ID', 2, 20, null, null, 0, 0, 10, 0, 0, null, null, 1, 10);

insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9061, 905, 'ИНП', 'INP', 1, 1, null, null, 1, null, 15, 1, 1, null, null, 0, 14);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9062, 905, 'АС НУ', 'AS_NU', 4, 2, 900, 9002, 1, null, 15, 1, 0, null, null, 0, null);

-- Новые табличные справочники
insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) values (921,'Коды видов вычетов',1,0,0,null,'REF_BOOK_DEDUCTION_TYPE');
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9211, 921,'Код вычета','CODE',1,1,null,null,1,null,6,1,1,null,null,0,3);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9212, 921,'Наименование вычета','NAME',1,2,null,null,1,null,50,1,0,null,null,0,2000);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9213, 921,'Код признака','DEDUCTION_MARK',4,3,927,9271,1,null,6,1,0,null,null,0,null);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) values (922,'Коды видов доходов',1,0,0,null,'REF_BOOK_INCOME_TYPE');
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9221, 922,'Код дохода','CODE',1,1,null,null,1,null,6,1,1,null,null,0,4);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9222, 922,'Наименование дохода','NAME',1,2,null,null,1,null,50,1,0,null,null,0,2000);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) values (923,'Коды субъектов РФ',1,0,0,null,'REF_BOOK_REGION');
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9231,923,'Код','CODE',1,0,null,null,1,null,10,1,1,null,null,0,2);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9232,923,'Наименование','NAME',1,1,null,null,1,null,50,1,0,null,null,0,255);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9234,923,'Определяющая часть кода ОКАТО','OKATO_DEFINITION',1,3,null,null,1,null,11,0,0,null,null,0,11);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9235,923,'Код ОКТМО','OKTMO',4,4,96,840,1,null,11,0,0,null,null,0,null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9236,923,'Определяющая часть кода ОКТМО','OKTMO_DEFINITION',1,5,null,null,1,null,11,0,0,null,null,0,11);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) values (924,'Коды места представления расчета',1,0,0,null,'REF_BOOK_PRESENT_PLACE');
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9241, 924,'Код','CODE',1,1,null,null,1,null,6,1,1,null,null,0,3);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9242, 924,'Наименование','NAME',1,2,null,null,1,null,15,1,0,null,null,0,255);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9243, 924,'Используется для НДФЛ','FOR_NDFL',2,3,null,null,1,0,6,1,0,null,null,0,1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9244, 924,'Используется для Страховых сборов взносов','FOR_FOND',2,4,null,null,1,0,6,1,0,null,null,0,1);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) values (925,'Общероссийский классификатор видов экономической деятельности',1,0,0,null,'REF_BOOK_OKVED');
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9251, 925,'Код ОКВЭД','CODE',1,1,null,null,1,null,10,1,1,null,null,0,8);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9252, 925,'Наименование','NAME',1,2,null,null,1,null,50,1,0,null,null,0,500);

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) VALUES (927,'Признак кода вычета',1,0,1,null,'REF_BOOK_DEDUCTION_MARK');
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (9271,927,'Код','CODE',2,1,null,null,1,0,6,1,1,null,null,0,1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (9272,927,'Наименование','NAME',1,2,null,null,1,null,30,1,0,null,null,0,30);

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) VALUES (928,'Коды форм реорганизации (ликвидации) организации',1,0,0,null,'REF_BOOK_REORGANIZATION');
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (9281,928,'Код','CODE',1,0,null,null,1,null,10,1,1,null,null,0,1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (9282,928,'Наименование','NAME',1,1,null,null,1,null,50,1,0,null,null,0,255);

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) VALUES (929,'Состояние ЭД',1,0,1,null,'REF_BOOK_DOC_STATE',0);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (9291,929,'КНД','KND',1,1,null,null,1,null,10,0,0,null,null,0,7);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (9292,929,'Наименование','NAME',1,2,null,null,1,null,50,1,0,null,null,0,255);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (931,'Виды налоговых форм',1,0,1,null,'REF_BOOK_FORM_TYPE',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9311, 931,'Код','CODE',1,1,null,null,1,null,9,1,1,null,null,0,14);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9312, 931,'Наименование','NAME',1,2,null,null,1,null,15,1,0,null,null,0,255);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9313, 931,'Вид налога','TAX_KIND',1,3,null,null,1,null,9,1,0,null,null,0,1);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (932,'Типы налоговых форм',1,0,1,null,'DECLARATION_KIND',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9321, 932,'Наименование','NAME',1,1,null,null,1,null,15,1,1,null,null,0,255);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (933,'Виды дохода',1,0,1,null,'REF_BOOK_INCOME_KIND',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9331, 933,'Код вида дохода','INCOME_TYPE_ID',4,1,922,9221,1,null,9,1,0,null,null,0,null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9332, 933,'Признак вида дохода','MARK',1,2,null,null,1,null,6,1,0,null,null,0,2);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9333, 933,'Наименование','NAME',1,3,null,null,1,null,15,1,0,null,null,0,255);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (934,'Категории прикрепленных файлов',0,0,1,null,'REF_BOOK_ATTACH_FILE_TYPE',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9341, 934,'Код','CODE',2,1,null,null,1,0,6,1,0,null,null,0,1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9342, 934,'Наименование','NAME',1,2,null,null,1,null,15,1,0,null,null,0,255);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (935,'Налоговые инспекции',0,0,1,null,'REF_BOOK_TAX_INSPECTION',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9351, 935,'Код','CODE',1,1,null,null,1,null,6,1,1,null,null,0,4);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9352, 935,'Наименование','NAME',1,2,null,null,1,null,15,1,0,null,null,0,250);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (936,'Ставка НДФЛ',0,0,1,null,'REF_BOOK_NDFL_RATE',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9361, 936,'Ставка','RATE',1,1,null,null,1,null,15,1,0,null,null,0,255);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (937,'Основания заполнения сумм страховых взносов',1,0,0,null,'REF_BOOK_FILL_BASE',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9371, 937,'Код','CODE',1,1,null,null,1,null,6,1,1,null,null,0,1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9372, 937,'Наименование','NAME',1,2,null,null,1,null,15,1,0,null,null,0,2000);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (938,'Коды тарифа плательщика',1,0,0,null,'REF_BOOK_TARIFF_PAYER',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9381, 938,'Код','CODE',1,1,null,null,1,null,6,1,1,null,null,0,2);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9382, 938,'Наименование','NAME',1,2,null,null,1,null,15,1,0,null,null,0,2000);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9383, 938,'Используется в ОПС и ОМС','FOR_OPS_OMS',2,3,null,null,1,0,6,0,0,null,null,0,1);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (939,'Коды классов условий труда',1,0,0,null,'REF_BOOK_HARD_WORK',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9391, 939,'Код','CODE',1,1,null,null,1,null,6,1,1,null,null,0,1);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9392, 939,'Наименование','NAME',1,2,null,null,1,null,15,1,0,null,null,0,2000);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (940,'Классификатор доходов бюджетов Российской Федерации',1,0,0,null,'REF_BOOK_BUDGET_INCOME',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9401, 940,'Код','CODE',1,1,null,null,1,null,20,1,1,null,null,0,20);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9402, 940,'Наименование','NAME',1,2,null,null,1,null,15,1,0,null,null,0,1000);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9403, 940,'Уровень кода','LEV',1,3,null,null,1,null,6,1,0,null,null,0,1);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (941,'Коды категорий застрахованных лиц',1,0,0,null,'REF_BOOK_PERSON_CATEGORY',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9411, 941,'Код','CODE',1,1,null,null,1,null,6,1,1,null,null,0,4);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9412, 941,'Наименование','NAME',1,2,null,null,1,null,15,1,0,null,null,0,2000);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (942,'Ограничение доступа по АСНУ',1,0,0,null,'SEC_USER_ASNU',0);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9421, 942,'Пользователь','USER_ID',4,1,74,651,1,null,15,1,0,null,null,0,null);
insert into ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (9422, 942,'	АС НУ','ASNU_ID',4,2,900,9002,1,null,15,1,0,null,null,0,null);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) values (950,'Параметры подразделения по НДФЛ',0,0,0,null,'REF_BOOK_NDFL');
insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) values (951,'Параметры подразделения по НДФЛ (таблица)',0,0,0,null,'REF_BOOK_NDFL_DETAIL');

insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length) values (9501,950,1,'Подразделение','DEPARTMENT_ID',1,4,30,161,10,0,0,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length) values (9502,950,2,'ИНН','INN',1,1,null,null,10,0,0,10);

insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9511,951,1,'Ссылка на родительскую запись','REF_BOOK_NDFL_ID',0,4,950,9501,10,1,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9512,951,2,'Порядок следования','ROW_ORD',0,2,null,null,10,1,0,4,0);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9513,951,3,'Код обособленного подразделения','DEPARTMENT_ID',1,4,30,161,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9514,951,4,'Код налогового органа (кон.)','TAX_ORGAN_CODE',1,1,null,null,5,0,1,4,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9515,951,5,'КПП','KPP',1,1,null,null,5,0,1,9,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9516,951,6,'Код налогового органа (пром.)','TAX_ORGAN_CODE_MID',1,1,null,null,10,0,0,4,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9517,951,7,'Код места, по которому представляется документ','PRESENT_PLACE',1,4,924,9241,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9518,951,8,'Наименование для титульного листа','NAME',1,1,null,null,20,0,0,1000,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9520,951,10,'Код вида экономической деятельности и по классификатору ОКВЭД','OKVED',1,4,925,9251,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9521,951,11,'Субъект Российской Федерации (код)','REGION',1,4,923,9231,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9522,951,12,'ОКТМО','OKTMO',1,4,96,840,11,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9523,951,13,'Номер контактного телефона','PHONE',1,1,null,null,20,0,0,20,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9524,951,14,'Обязанность по уплате налога','OBLIGATION',1,4,25,110,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9525,951,15,'Признак расчёта','TYPE',1,4,26,120,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9526,951,16,'Код формы реорганизации и ликвидации','REORG_FORM_CODE',1,4,928,9281,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9527,951,17,'ИНН реорганизованного обособленного подразделения','REORG_INN',1,1,null,null,10,0,0,10,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9528,951,18,'КПП реорганизованного обособленного подразделения','REORG_KPP',1,1,null,null,10,0,0,9,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9529,951,19,'Признак лица подписавшего документ','SIGNATORY_ID',1,4,35,212,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9530,951,20,'Фамилия подписанта','SIGNATORY_SURNAME',1,1,null,null,20,0,0,60,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9531,951,21,'Имя подписанта','SIGNATORY_FIRSTNAME',1,1,null,null,20,0,0,60,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9532,951,22,'Отчество подписанта','SIGNATORY_LASTNAME',1,1,null,null,20,0,0,60,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9533,951,23,'Наименование документа, подтверждающего полномочия представителя','APPROVE_DOC_NAME',1,1,null,null,20,0,0,120,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9534,951,24,'Наименование организации-представителя налогоплательщика','APPROVE_ORG_NAME',1,1,null,null,20,0,0,1000,null);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) values (960,'Параметры подразделения по сборам, взносам',0,0,0,null,'REF_BOOK_FOND');
insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name) values (961,'Параметры подразделения по сборам, взносам (таблица)',0,0,0,null,'REF_BOOK_FOND_DETAIL');

insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length) values (9601,960,1,'Подразделение','DEPARTMENT_ID',1,4,30,161,10,0,0,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length) values (9602,960,2,'ИНН','INN',1,1,null,null,10,0,0,10);

insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9611,961,1,'Ссылка на родительскую запись','REF_BOOK_FOND_ID',0,4,960,9601,10,1,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9612,961,2,'Порядок следования','ROW_ORD',0,2,null,null,10,1,0,4,0);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9613,961,3,'Код обособленного подразделения','DEPARTMENT_ID',1,4,30,161,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9614,961,4,'Код налогового органа (кон.)','TAX_ORGAN_CODE',1,1,null,null,5,0,1,4,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9615,961,5,'КПП','KPP',1,1,null,null,5,0,1,9,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9616,961,6,'Код налогового органа (пром.)','TAX_ORGAN_CODE_MID',1,1,null,null,10,0,0,4,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9617,961,7,'Код места, по которому представляется документ','PRESENT_PLACE',1,4,924,9241,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9618,961,8,'Наименование для титульного листа','NAME',1,1,null,null,20,0,0,1000,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9620,961,9,'Код вида экономической деятельности и по классификатору ОКВЭД','OKVED',1,4,925,9251,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9621,961,10,'Субъект Российской Федерации (код)','REGION',1,4,923,9231,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9622,961,11,'ОКТМО','OKTMO',1,4,96,840,11,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9623,961,12,'Номер контактного телефона','PHONE',1,1,null,null,20,0,0,20,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9624,961,13,'Обязанность по уплате налога','OBLIGATION',1,4,25,110,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9625,961,14,'Признак расчёта','TYPE',1,4,26,120,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9626,961,15,'Код формы реорганизации и ликвидации','REORG_FORM_CODE',1,4,928,9281,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9627,961,16,'ИНН реорганизованного обособленного подразделения','REORG_INN',1,1,null,null,10,0,0,10,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9628,961,17,'КПП реорганизованного обособленного подразделения','REORG_KPP',1,1,null,null,10,0,0,9,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9629,961,18,'Признак лица подписавшего документ','SIGNATORY_ID',1,4,35,212,10,0,0,null,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9630,961,19,'Фамилия подписанта','SIGNATORY_SURNAME',1,1,null,null,20,0,0,60,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9631,961,20,'Имя подписанта','SIGNATORY_FIRSTNAME',1,1,null,null,20,0,0,60,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9632,961,21,'Отчество подписанта','SIGNATORY_LASTNAME',1,1,null,null,20,0,0,60,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9633,961,22,'Наименование документа, подтверждающего полномочия представителя','APPROVE_DOC_NAME',1,1,null,null,20,0,0,120,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,required,is_unique,max_length,precision) values (9634,961,23,'Наименование организации-представителя налогоплательщика','APPROVE_ORG_NAME',1,1,null,null,20,0,0,1000,null);

insert into ref_book (id, name, visible, type, read_only, region_attribute_id,table_name,is_versioned) values (964,'Реестр справок',0,0,0,null,'NDFL_REFERENCES',0);

insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,precision,required,is_unique,max_length) values (9641,964,1,'Физическое лицо','PERSON_ID',1,4,904,9059,10,null,1,0,null);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,precision,required,is_unique,max_length) values (9642,964,2,'Номер справки','NUM',1,2,null,null,10,0,1,0,10);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,precision,required,is_unique,max_length) values (9643,964,3,'Фамилия','SURNAME',1,1,null,null,10,null,1,0,60);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,precision,required,is_unique,max_length) values (9644,964,4,'Имя','NAME',1,1,null,null,10,null,1,0,60);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,precision,required,is_unique,max_length) values (9645,964,5,'Отчество','LASTNAME',1,1,null,null,10,null,0,0,60);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,precision,required,is_unique,max_length,format) values (9646,964,6,'Дата рождения ФЛ','BIRTHDAY',1,3,null,null,10,null,1,0,null,1);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,precision,required,is_unique,max_length) values (9647,964,7,'Текст ошибки от ФНС','ERRTEXT',1,1,null,null,10,null,0,0,2000);
insert into ref_book_attribute (id,ref_book_id,ord,name,alias,visible,type,reference_id,attribute_id,width,precision,required,is_unique,max_length) values (9648,964,8,'Идентификатор налоговой формы к которой относятся данные','DECLARATION_DATA_ID',1,2,null,null,10,0,0,0,18);

---------------------------------
update ref_book_attribute set reference_id=923,attribute_id=9231 where reference_id=4;
---------------------------------

ALTER TABLE ref_book ENABLE CONSTRAINT ref_book_fk_region;
ALTER TABLE ref_book_attribute ENABLE CONSTRAINT ref_book_attr_fk_attribute_id;
ALTER TABLE ref_book_attribute ENABLE CONSTRAINT ref_book_attr_fk_ref_book_id;
ALTER TABLE ref_book_attribute ENABLE CONSTRAINT ref_book_attr_fk_reference_id;
