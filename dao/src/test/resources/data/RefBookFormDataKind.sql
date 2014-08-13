INSERT INTO ref_book (id, name) VALUES (94, 'Типы налоговых форм');

Insert into REF_BOOK_ATTRIBUTE (ID,REF_BOOK_ID,NAME,ALIAS,TYPE,ORD,REFERENCE_ID,ATTRIBUTE_ID,VISIBLE,PRECISION,WIDTH,REQUIRED,IS_UNIQUE,SORT_ORDER,FORMAT,READ_ONLY,MAX_LENGTH)
values ('837','94','Наименование','NAME','1','1',null,null,'1',null,'50','1','0','0',null,'0','2000');

Insert into FORM_KIND (ID,NAME) values ('1','Первичная');
Insert into FORM_KIND (ID,NAME) values ('2','Консолидированная');
Insert into FORM_KIND (ID,NAME) values ('3','Сводная');
Insert into FORM_KIND (ID,NAME) values ('4','Форма УНП');
Insert into FORM_KIND (ID,NAME) values ('5','Выходная');