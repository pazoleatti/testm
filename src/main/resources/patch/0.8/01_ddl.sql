--http://jira.aplana.com/browse/SBRFACCTAX-12614: 0.8 Добавить новые поля в FORM_DATA
alter table form_data add sorted_backup number(1) default 0 not null, edited number(1) default 0 not null;
comment on column form_data.sorted_backup is 'Статус актуальности сортировки НФ для резервного среза (0 - Сортировка неактуальна; 1 - Сортировка актуальна)';
comment on column form_data.edited is 'Признак изменения данных НФ в режиме редактирования (0 - Нет изменений; 1 - Есть изменения)';

alter table form_data add constraint form_data_chk_edited check (edited in (0, 1));
alter table form_data add constraint form_data_chk_sorted_backup check (sorted_backup in (0, 1));

--http://jira.aplana.com/browse/SBRFACCTAX-12692: 0.8 Добавить в патч изменение таблиц LOCK_DATA и CONFIGURATION_LOCK
alter table lock_data modify date_lock default sysdate;
alter table lock_data drop column date_before;
drop table configuration_lock;

--http://jira.aplana.com/browse/SBRFACCTAX-12711: Обязательность заполнения для form_data.accruing
update form_data set accruing = 0 where accruing is null;
alter table form_data modify accruing default 0 not null;
comment on column form_data.accruing is 'Признак расчета значений нарастающим итогом (0 - не нарастающим итогом, 1 - нарастающим итогом)';


--http://jira.aplana.com/browse/SBRFACCTAX-12708: 0.8 БД. "Файлы и комментарии". Добавить поле COMMENT в FORM_DATA и таблицу FORM_DATA_FILE
alter table form_data add note varchar2(512);
comment on column form_data.note is 'Комментарий к НФ, вводимый в модальном окне "Файлы и комментарии"';

create table form_data_file
(
   form_data_id number(18) not null,
   blob_data_id varchar2(36) not null,
   user_name varchar2(512) not null,
   user_department_name varchar2(4000) not null,
   note varchar2(512)    
);

comment on table form_data_file is 'Файлы налоговой формы';
comment on column form_data_file.form_data_id is 'Идентификатор экземпляра налоговой формы';
comment on column form_data_file.blob_data_id is 'Файл налоговой формы';
comment on column form_data_file.user_name is 'Полное имя пользователя, прикрепившего файл';
comment on column form_data_file.user_department_name is 'Наименование подразделения пользователя, прикрепившего файл';
comment on column form_data_file.note is 'Комментарий к файлу';

alter table form_data_file add constraint form_data_file_pk primary key (blob_data_id, form_data_id);
alter table form_data_file add constraint form_data_file_fk_form_data foreign key (form_data_id) references form_data(id);
alter table form_data_file add constraint form_data_file_fk_blob_data foreign key (blob_data_id) references blob_data(id);

--http://jira.aplana.com/browse/SBRFACCTAX-12762: Ограничение для аудита
insert into event (id, name) values (904, 'Импорт скриптов');

alter table log_system drop constraint log_system_chk_rp;
alter table log_system drop constraint log_system_chk_dcl_form;

alter table log_system add constraint log_system_chk_rp check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904) or report_period_name is not null);
alter table log_system add constraint log_system_chk_dcl_form check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904) or declaration_type_name is not null or (form_type_name is not null and form_kind_id is not null));


--http://jira.aplana.com/browse/SBRFACCTAX-12760: PK для lock_data_subscribers
alter table lock_data_subscribers add constraint lock_data_subscribers_pk primary key (lock_key, user_id);

--http://jira.aplana.com/browse/SBRFACCTAX-12997: Справочник "Цвета"
create table color
(
id number(3) not null,
name varchar2(100) not null,
r number(3) not null,
g number(3) not null,
b number(3) not null, 
hex varchar2(7) not null
);

alter table color add constraint color_pk primary key(id);
alter table color add constraint color_unq_name unique(name);
alter table color add constraint color_unq_rgb unique (r,g,b);
alter table color add constraint color_unq_hex unique (hex);
alter table color add constraint color_chk_rgb_limits check ((r between 0 and 255) and (g between 0 and 255) and (b between 0 and 255));

alter table form_style add constraint form_style_fk_font_color foreign key(font_color) references color(id);
alter table form_style add constraint form_style_fk_back_color foreign key(back_color) references color(id);

insert all
	into color values (0,  'Черный',          0,  0,  0,  '#000000')
	into color values (4,  'Белый',          255, 255, 255, '#FFFFFF')
	into color values (1,  'Светло-желтый',      255, 255, 153, '#FFFF99')
	into color values (2,  'Светло-коричневый',    255, 204, 153,  '#FFCC99')
	into color values (3,  'Светло-голубой',      204, 255, 255,  '#CCFFFF')
	into color values (5,  'Темно-серый',        149, 149, 149,	'#959595')
	into color values (6,	'Серый',					192, 192, 192,	'#C0C0C0')
	into color values (7,	'Голубой',					153, 204, 255,	'#99CCFF')
	into color values (8,	'Светло-красный',			240, 128, 128,	'#F08080')
	into color values (9,	'Светло-оранжевый',			255, 220, 130,	'#FFDC82')
	into color values (10,	'Красный',					255, 0,	0,	'#FF0000')
	into color values (11,	'Синий',					0,	0,	255,	'#0000FF')
	into color values (12,	'Светло-зеленый',			152, 251, 152,	'#98FB98')
	into color values (13,	'Темно-зеленый',			0,	108, 0,	'#006C00')
select * from dual;	

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name, is_versioned) VALUES (1,'Цвета',1,0,1,null, 'COLOR', 0);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (95, 1, 'Наименование цвета', 	'NAME', 1, 1, null, null, 1, null, 20, 1, 0, null, 	null, 0, 50);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (96, 1, 'R', 'R', 	2, 2, null, null, 1, 0, 	5, 1, 0, 1, 	null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (97, 1, 'G', 'G', 	2, 3, null, null, 1, 0, 	5, 1, 0, 1, 	null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (98, 1, 'B', 'B', 	2, 4, null, null, 1, 0, 	5, 1, 0, 1, 	null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (99, 1, 'HEX', 'HEX', 1, 5, null, null, 1, null, 	5, 1, 1, 1, 	null, 0, 7);

--http://jira.aplana.com/browse/SBRFACCTAX-12847: Новые поля в form_template
alter table form_template add accruing number(1) default 0;
alter table form_template add updating number(1) default 0;
comment on column form_template.accruing is 'Признак расчета нарастающим итогом (0 - не используется, 1 - используется)';
comment on column form_template.updating is 'Отображать кнопку "Обновить" (0 - нет, 1 - да)';
alter table form_template add constraint form_template_chk_accruing check (accruing in (0, 1));
alter table form_template add constraint form_template_chk_updating check (updating in (0, 1));

commit;
exit;