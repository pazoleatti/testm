-----------------------------------------------------------------------------------------------------------------------------
-- Создание таблиц для справочников
-----------------------------------------------------------------------------------------------------------------------------
-- ОКТМО
create table ref_book_oktmo (
  id        number(18)     not null,
  code      varchar2(4000) not null,
  name      varchar2(4000) not null,
  version   date           not null,
  status    number(1)      not null,
  record_id number(9)      not null,
  razd      number(1)
);
comment on table ref_book_oktmo is 'ОКТМО';
comment on column ref_book_oktmo.id is 'Идентификатор записи';
comment on column ref_book_oktmo.code is 'Код';
comment on column ref_book_oktmo.name is 'Наименование';
comment on column ref_book_oktmo.parent_id is 'Идентификатор родительской записи';
comment on column ref_book_oktmo.version is 'Версия. Дата актуальности записи';
comment on column ref_book_oktmo.status is 'Статус записи(0-обычная запись, -1-удаленная, 1-черновик, 2-фиктивная)';
comment on column ref_book_oktmo.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_oktmo.razd is 'Раздел. Может быть 1 или 2';

create sequence seq_ref_book_oktmo start with 300000 increment by 100;
create sequence seq_ref_book_oktmo_record_id start with 1000000;

-- Коды видов дохода
create table ref_book_income_type
(
  id        number(18) not null,
  record_id number(9) not null,
  version   date not null,
  status    number(1) default 0 not null,
  code      varchar2(4 char) not null,
  name      varchar2(2000 char) not null
);

comment on table ref_book_income_type is 'Коды видов дохода';
comment on column ref_book_income_type.id is 'Уникальный идентификатор';
comment on column ref_book_income_type.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_income_type.version is 'Версия. Дата актуальности записи';
comment on column ref_book_income_type.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_income_type.code is 'Код';
comment on column ref_book_income_type.name is 'Наименование дохода';

-- Коды видов вычетов
create table ref_book_deduction_type
(
  id number(18) not null, 
	record_id number(9) not null, 
	version date not null, 
	status number(1) default 0 not null, 
	code varchar2(3 char) not null, 
	name varchar2(2000 char) not null,
    deduction_mark number(9) not null
);

comment on table ref_book_deduction_type is 'Коды видов вычетов';
comment on column ref_book_deduction_type.id is 'Уникальный идентификатор';
comment on column ref_book_deduction_type.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_deduction_type.version is 'Версия. Дата актуальности записи';
comment on column ref_book_deduction_type.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_deduction_type.code is 'Код';
comment on column ref_book_deduction_type.name is 'Наименование вычета';
comment on column ref_book_deduction_type.deduction_mark is 'Код признака вычета';

-- Коды субъектов РФ
create table ref_book_region
(
  id        number(18) not null,
  record_id number(9) not null,
  version   date not null,
  status    number(1) default 0 not null,
  code      varchar2(2 char) not null,
  name      varchar2(255 char) not null,
  okato_definition varchar2(11 char),
  oktmo     number(18),
  oktmo_definition varchar2(11 char)
);

comment on table ref_book_region is 'Коды субъектов РФ';
comment on column ref_book_region.id is 'Уникальный идентификатор';
comment on column ref_book_region.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_region.version is 'Версия. Дата актуальности записи';
comment on column ref_book_region.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_region.code is 'Код';
comment on column ref_book_region.name is 'Наименование';
comment on column ref_book_region.okato_definition is 'Определяющая часть кода ОКАТО';
comment on column ref_book_region.oktmo is 'Ссылка на код ОКТМО';
comment on column ref_book_region.oktmo_definition is 'Определяющая часть кода ОКТМО';

-- Коды места представления расчета
create table ref_book_present_place
(
  id        number(18) not null,
  record_id number(9) not null,
  version   date not null,
  status    number(1) default 0 not null,
  code      varchar2(3 char) not null,
  name      varchar2(255) not null,
  for_ndfl  number(1) default 1 not null,
  for_fond  number(1) default 1 not null
);

comment on table ref_book_present_place is 'Коды места представления расчета';
comment on column ref_book_present_place.id is 'Уникальный идентификатор';
comment on column ref_book_present_place.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_present_place.version is 'Версия. Дата актуальности записи';
comment on column ref_book_present_place.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_present_place.code is 'Код';
comment on column ref_book_present_place.name is 'Наименование';
comment on column ref_book_present_place.for_ndfl is 'Используется для НДФЛ';
comment on column ref_book_present_place.for_fond is 'Используется для Страховых сборов взносов';

-- Справочник АСНУ
create table ref_book_asnu (
  id        number(9)           not null,
  code      varchar2(4)         not null,
  name      varchar2(100)       not null,
  type      varchar2(255)       not null
);

comment on table ref_book_asnu is 'Справочник АСНУ';
comment on column ref_book_asnu.id is 'Уникальный идентификатор';
comment on column ref_book_asnu.code is 'Код АСНУ';
comment on column ref_book_asnu.name is 'Наименование АСНУ';
comment on column ref_book_asnu.type is 'Тип дохода';

-- Виды налоговых форм
create table ref_book_form_type
(
  id number(18) not null,
  code varchar2(14 char) not null,
  name varchar2(255) not null,
  tax_kind varchar2(1 char) not null
);

comment on table ref_book_form_type is 'Виды налоговых форм';
comment on column ref_book_form_type.id is 'Идентификатор';
comment on column ref_book_form_type.code is 'Код';
comment on column ref_book_form_type.name is 'Наименование';
comment on column ref_book_form_type.tax_kind is 'Вид налога';

-- Типы налоговых форм
create table declaration_kind 
(
  id number(18) not null,
  name varchar2(255 char) not null
);
comment on table declaration_kind is 'Типы налоговых форм';
comment on column declaration_kind.id is 'Уникальный идентификатор';
comment on column declaration_kind.name is 'Наименование';

-- Общероссийский классификатор видов экономической деятельности
create table ref_book_okved
(
  id        number(18) not null,
  record_id number(9) not null,
  version   date not null,
  status    number(1) default 0 not null,
  code      varchar2(8 char) not null,
  name      varchar2(500 char) not null
);

comment on table ref_book_okved is 'Общероссийский классификатор видов экономической деятельности';
comment on column ref_book_okved.id is 'Уникальный идентификатор';
comment on column ref_book_okved.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_okved.version is 'Версия. Дата актуальности записи';
comment on column ref_book_okved.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_okved.code is 'Код ОКВЭД';
comment on column ref_book_okved.name is 'Наименование';

-- Признак кода вычета
create table ref_book_deduction_mark
(
  id        number(18) not null,
  record_id number(9) not null,
  version   date not null,
  status    number(1) default 0 not null,
  code      number(1) not null,
  name      varchar2(30 char) not null
);
comment on table ref_book_deduction_mark is 'Признак кода вычета';
comment on column ref_book_deduction_mark.id is 'Уникальный идентификатор';
comment on column ref_book_deduction_mark.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_deduction_mark.version is 'Версия. Дата актуальности записи';
comment on column ref_book_deduction_mark.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_deduction_mark.code is 'Код признака вычета';
comment on column ref_book_deduction_mark.name is 'Наименование признака кода вычета';

-- Коды форм реорганизации (ликвидации) организации
create table ref_book_reorganization 
(
  id        number(18)          not null,
  record_id number(9)           not null,
  version   date                not null,
  status    number(1) default 0 not null,
  code      varchar2(1 char)    not null,
  name      varchar2(255 char)  not null
);

comment on table ref_book_reorganization is 'Коды форм реорганизации (ликвидации) организации';
comment on column ref_book_reorganization.id is 'Уникальный идентификатор';
comment on column ref_book_reorganization.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_reorganization.version is 'Версия. Дата актуальности записи';
comment on column ref_book_reorganization.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_reorganization.code is 'Код';
comment on column ref_book_reorganization.name is 'Наименование';

--Состояния ЭД
create table ref_book_doc_state (
  id number(18) not null,
  knd varchar2(7 char),
  name varchar2(255 char) not null
);

comment on table ref_book_doc_state is 'Состояние ЭД';
comment on column ref_book_doc_state.id is 'Уникальный идентификатор';
comment on column ref_book_doc_state.knd is 'Код формы по КНД';
comment on column ref_book_doc_state.name is 'Наименование состояния';

--Виды дохода
create table ref_book_income_kind
(
  id number(18) not null,
  income_type_id number(18) not null,
  mark varchar2(2 char) not null,
  name varchar2(255)
);
comment on table ref_book_income_kind is 'Виды дохода';
comment on column ref_book_income_kind.id is 'Уникальный идентификатор';
comment on column ref_book_income_kind.income_type_id is 'Код вида дохода';
comment on column ref_book_income_kind.mark is 'Признак вида дохода';
comment on column ref_book_income_kind.name is 'Наименование';

--Категории прикрепленных файлов
create table ref_book_attach_file_type
(
  id number(18) not null,
  code number(1) not null,
  name varchar2(255 char) not null
);
comment on table ref_book_attach_file_type is 'Категории прикрепленных файлов';
comment on column ref_book_attach_file_type.id is 'Уникальный иждентификатор';
comment on column ref_book_attach_file_type.code is 'Код категории прикрепляемых файлов';
comment on column ref_book_attach_file_type.name is 'Наименование';

--Налоговые инспекции
create table ref_book_tax_inspection
(
  id        number(18)          not null,
  code      varchar2(4 char)    not null,
  name      varchar2(250 char)  not null
);

comment on table ref_book_tax_inspection is 'Налоговые инспекции';
comment on column ref_book_tax_inspection.id is 'Уникальный идентификатор';
comment on column ref_book_tax_inspection.code is 'Код налогового органа';
comment on column ref_book_tax_inspection.name is 'Наименование налогового органа';

--Ставка НДФЛ
create table ref_book_ndfl_rate
(
  id    number(18) not null,
  rate  varchar2(255 char) not null
);

comment on table ref_book_ndfl_rate is 'Ставка НДФЛ';
comment on column ref_book_ndfl_rate.id is 'Уникальный идентификатор';
comment on column ref_book_ndfl_rate.rate is 'Процентная ставка';

--Основания заполнения сумм страховых взносов
create table ref_book_fill_base
(
  id number(18) not null,
  code varchar2(1 char) not null,
  name varchar2(2000 char) not null
);

comment on table ref_book_fill_base is 'Основания заполнения сумм страховых взносов';
comment on column ref_book_fill_base.id is 'Уникальный идентификатор';
comment on column ref_book_fill_base.code is 'Код основания заполнения';
comment on column ref_book_fill_base.name is 'Название основания заполнения';

--Коды тарифа плательщика
create table ref_book_tariff_payer
(
  id number(18) not null,
  code varchar2(2 char) not null,
  name varchar2(2000 char) not null
);

comment on table ref_book_tariff_payer is 'Коды тарифа плательщика';
comment on column ref_book_tariff_payer.id is 'Уникальный идентификатор';
comment on column ref_book_tariff_payer.code is 'Код тарифа плательщика';
comment on column ref_book_tariff_payer.name is 'Название тарифа плательщика';

--Коды классов условий труда
create table ref_book_hard_work
(
  id number(18) not null,
  code varchar2(1 char) not null,
  name varchar2(2000 char) not null
);

comment on table ref_book_hard_work is 'Коды классов условий труда';
comment on column ref_book_hard_work.id is 'Уникальный идентификатор';
comment on column ref_book_hard_work.code is 'Код класса условий труда';
comment on column ref_book_hard_work.name is 'Название класса условий труда';

--Классификатор доходов бюджетов Российской Федерации
create table ref_book_budget_income
(
  id number(18) not null,
  code varchar2(20 char) not null,
  name varchar2(1000 char) not null,
  lev varchar2(1 char) not null
);
comment on table ref_book_budget_income is 'Классификатор доходов бюджетов Российской Федерации';
comment on column ref_book_budget_income.id is 'Уникальный идентификатор';
comment on column ref_book_budget_income.code is 'Код бюджетной классификации';
comment on column ref_book_budget_income.name is 'Наименование дохода бюджета Российской Федерации';
comment on column ref_book_budget_income.lev is 'Уровень кода. Служит для определения уровней агрегирования кодов классификации доходов бюджетов. В рамках вида и подвида доходов код с большим значением уровня агрегируется на вышестоящий код с меньшим значением уровня';

--Коды, определяющие налоговый (отчётный) период
create table report_period_type
(
  id                  number(18) not null,
  code                varchar2(2 char) not null,
  name                varchar2(255 char) not null,
  n                   number(1),
  f                   number(1),
  start_date          date,
  end_date            date,
  calendar_start_date date
);
comment on table report_period_type is 'Коды, определяющие налоговый (отчётный) период';
comment on column report_period_type.id is 'Уникальный идентификатор';
comment on column report_period_type.code is 'Код';
comment on column report_period_type.name is 'Наименование';
comment on column report_period_type.n is 'Признак принадлежности к НДФЛ';
comment on column report_period_type.f is 'Признак принадлежности к Страховым сборам, взносам';
comment on column report_period_type.start_date is 'Дата начала периода';
comment on column report_period_type.end_date is 'Дата окончания периода';
comment on column report_period_type.calendar_start_date is 'Календарная дата начала периода';

--Общероссийский классификатор стран мира
create table ref_book_country
(
    id number(18) not null,
    record_id number(18) not null,
    status number(1) default 0 not null,
    version date not null,
    code varchar2(3 char) not null,
    code_2 varchar2(2 char) not null,
    code_3 varchar2(3 char) not null,
    name varchar2(500 char) not null,
    fullname varchar2(500 char)
);
comment on table ref_book_country is 'Общероссийский классификатор стран мира';
comment on column ref_book_country.id is 'Уникальный идентификатор';
comment on column ref_book_country.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_country.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_country.version is 'Версия. Дата актуальности записи';
comment on column ref_book_country.code is 'Код';
comment on column ref_book_country.code_2 is 'Код (2-х букв.)';
comment on column ref_book_country.code_3 is 'Код (3-х букв.)';
comment on column ref_book_country.name is 'Краткое наименование';
comment on column ref_book_country.fullname is 'Полное наименование';

--Коды документов
create table ref_book_doc_type
(
    id number(18) not null,
    record_id number(18) not null,
    status number(1) default 0 not null,
    version date not null,
    code varchar2(2 char) not null,
    name varchar2(2000 char) not null,
    priority number(2)
);
comment on table ref_book_doc_type is 'Коды документов';
comment on column ref_book_doc_type.id is 'Уникальный идентификатор';
comment on column ref_book_doc_type.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_doc_type.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_doc_type.version is 'Версия. Дата актуальности записи';
comment on column ref_book_doc_type.code is 'Код';
comment on column ref_book_doc_type.name is 'Наименование документа';
comment on column ref_book_doc_type.priority is 'Приоритет';

-- Параметры подразделения по НДФЛ
create table ref_book_ndfl
(
 id            number(18) not null,
 record_id     number(9) not null,
 version       date not null,
 status        number(1) default 0 not null,
 department_id number(18),
 inn           varchar2(12 char)
);

comment on table ref_book_ndfl is 'Параметры подразделения по НДФЛ';
comment on column ref_book_ndfl.id is 'Уникальный идентификатор';
comment on column ref_book_ndfl.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_ndfl.version is 'Версия. Дата актуальности записи';
comment on column ref_book_ndfl.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_ndfl.department_id is 'Подразделение';
comment on column ref_book_ndfl.inn is 'ИНН';

-- Параметры подразделения по НДФЛ (таблица)
create table ref_book_ndfl_detail
(
 id                 number(18)       not null,
 record_id          number(9) not null,
 version            date not null,
 status             number(1) default 0 not null,
 ref_book_ndfl_id   number(18)       not null,
 row_ord            number(4)        not null,
 department_id      number(18),
 --Строка сведений о налогоплательщике
 tax_organ_code     varchar2(4 char),
 kpp                varchar2(9 char),
 tax_organ_code_mid varchar2(4 char),
 present_place      number(18),
 name               varchar2(1000 char),
 okved              number(18),
 region             number(18),
 oktmo              number(18),
 phone              varchar2(20 char),
 obligation         number(18),
 type               number(18),
 --Сведения о реорганизации
 reorg_form_code    number(18),
 reorg_inn          varchar2(12 char),
 reorg_kpp          varchar2(9 char),
 --Ответственный за расчет
 signatory_id       number(18),
 signatory_surname  varchar2(60 char),
 signatory_firstname varchar2(60 char),
 signatory_lastname  varchar2(60 char),
 approve_doc_name    varchar2(120 char),
 approve_org_name    varchar2(1000 char)
);

comment on table ref_book_ndfl_detail is 'Параметры подразделения по НДФЛ (таблица)';
comment on column ref_book_ndfl_detail.id is 'Уникальный идентификатор';
comment on column ref_book_ndfl_detail.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_ndfl_detail.version is 'Версия. Дата актуальности записи';
comment on column ref_book_ndfl_detail.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_ndfl_detail.ref_book_ndfl_id is 'Ссылка на родительскую запись';
comment on column ref_book_ndfl_detail.row_ord is 'Порядок следования';
comment on column ref_book_ndfl_detail.department_id is 'Код обособленного подразделения';
comment on column ref_book_ndfl_detail.tax_organ_code is 'Код налогового органа конечного';
comment on column ref_book_ndfl_detail.kpp is 'КПП';
comment on column ref_book_ndfl_detail.tax_organ_code_mid is 'Код налогового органа промежуточного';
comment on column ref_book_ndfl_detail.present_place is 'Место, по которому представляется документ.';
comment on column ref_book_ndfl_detail.name is 'Наименование для титульного листа';
comment on column ref_book_ndfl_detail.okved is 'Вид экономической деятельности и по классификатору ОКВЭД';
comment on column ref_book_ndfl_detail.region is 'Субъект Российской Федерации';
comment on column ref_book_ndfl_detail.oktmo is 'ОКТМО';
comment on column ref_book_ndfl_detail.phone is 'Номер контактного телефона';
comment on column ref_book_ndfl_detail.obligation is 'Обязанность по уплате налога';
comment on column ref_book_ndfl_detail.type is 'Признак расчета';
comment on column ref_book_ndfl_detail.reorg_form_code is 'Код формы реорганизации и ликвидации';
comment on column ref_book_ndfl_detail.reorg_inn is 'ИНН реорганизованного обособленного подразделения';
comment on column ref_book_ndfl_detail.reorg_kpp is 'КПП реорганизованного обособленного подразделения';
comment on column ref_book_ndfl_detail.signatory_id is 'признак лица, подписавшего документ';
comment on column ref_book_ndfl_detail.signatory_surname is 'Фамилия подписанта';
comment on column ref_book_ndfl_detail.signatory_firstname is 'Имя подписанта';
comment on column ref_book_ndfl_detail.signatory_lastname is 'Отчество подписанта';
comment on column ref_book_ndfl_detail.approve_doc_name is 'Наименование документа, подтверждающего полномочия';
comment on column ref_book_ndfl_detail.approve_org_name is 'Наименование организации-представителя налогоплательщика';

-- Параметры подразделения по сборам, взносам
create table ref_book_fond
(
 id            number(18) not null,
 record_id     number(9) not null,
 version       date not null,
 status        number(1) default 0 not null,
 department_id number(18),
 inn           varchar2(12 char)
);

comment on table ref_book_fond is 'Параметры подразделения по сборам, взносам';
comment on column ref_book_fond.id is 'Уникальный идентификатор';
comment on column ref_book_fond.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_fond.version is 'Версия. Дата актуальности записи';
comment on column ref_book_fond.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_fond.department_id is 'Подразделение';
comment on column ref_book_fond.inn is 'ИНН';

-- Параметры подразделения по сборам, взносам (таблица)
create table ref_book_fond_detail
(
 id                 number(18)       not null,
 record_id          number(9) not null,
 version            date not null,
 status             number(1) default 0 not null,
 ref_book_fond_id   number(18)       not null,
 row_ord            number(4)        not null,
 department_id      number(18),
 --Строка сведений о налогоплательщике
 tax_organ_code     varchar2(4 char),
 kpp                varchar2(9 char),
 tax_organ_code_mid varchar2(4 char),
 present_place      number(18),
 name               varchar2(1000 char),
 okved              number(18),
 region             number(18),
 oktmo              number(18),
 phone              varchar2(20 char),
 obligation         number(18),
 type               number(18),
 --Сведения о реорганизации
 reorg_form_code    number(18),
 reorg_inn          varchar2(12 char),
 reorg_kpp          varchar2(9 char),
 --Ответственный за расчет
 signatory_id       number(18),
 signatory_surname  varchar2(60 char),
 signatory_firstname varchar2(60 char),
 signatory_lastname  varchar2(60 char),
 approve_doc_name    varchar2(120 char),
 approve_org_name    varchar2(1000 char)
);

comment on table ref_book_fond_detail is 'Параметры подразделения по сборам, взносам (таблица)';
comment on column ref_book_fond_detail.id is 'Уникальный идентификатор';
comment on column ref_book_fond_detail.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_fond_detail.version is 'Версия. Дата актуальности записи';
comment on column ref_book_fond_detail.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_fond_detail.ref_book_fond_id is 'Ссылка на родительскую запись';
comment on column ref_book_fond_detail.row_ord is 'Порядок следования';
comment on column ref_book_fond_detail.department_id is 'Код обособленного подразделения';
comment on column ref_book_fond_detail.tax_organ_code is 'Код налогового органа конечного';
comment on column ref_book_fond_detail.kpp is 'КПП';
comment on column ref_book_fond_detail.tax_organ_code_mid is 'Код налогового органа промежуточного';
comment on column ref_book_fond_detail.present_place is 'Место, по которому представляется документ.';
comment on column ref_book_fond_detail.name is 'Наименование для титульного листа';
comment on column ref_book_fond_detail.okved is 'Вид экономической деятельности и по классификатору ОКВЭД';
comment on column ref_book_fond_detail.region is 'Субъект Российской Федерации';
comment on column ref_book_fond_detail.oktmo is 'ОКТМО';
comment on column ref_book_fond_detail.phone is 'Номер контактного телефона';
comment on column ref_book_fond_detail.obligation is 'Обязанность по уплате налога';
comment on column ref_book_fond_detail.type is 'Признак расчета';
comment on column ref_book_fond_detail.reorg_form_code is 'Код формы реорганизации и ликвидации';
comment on column ref_book_fond_detail.reorg_inn is 'ИНН реорганизованного обособленного подразделения';
comment on column ref_book_fond_detail.reorg_kpp is 'КПП реорганизованного обособленного подразделения';
comment on column ref_book_fond_detail.signatory_id is 'признак лица, подписавшего документ';
comment on column ref_book_fond_detail.signatory_surname is 'Фамилия подписанта';
comment on column ref_book_fond_detail.signatory_firstname is 'Имя подписанта';
comment on column ref_book_fond_detail.signatory_lastname is 'Отчество подписанта';
comment on column ref_book_fond_detail.approve_doc_name is 'Наименование документа, подтверждающего полномочия';
comment on column ref_book_fond_detail.approve_org_name is 'Наименование организации-представителя налогоплательщика';

-----------------------------------------------------------------------------------------------------------------------------
