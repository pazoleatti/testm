insert into state(id,name) values(1, 'Создана');
insert into state(id,name) values(2, 'Подготовлена');
insert into state(id,name) values(3, 'Принята');

insert into state_change(id,from_id,to_id) values(1, null, 1);
insert into state_change(id,from_id,to_id) values(2, null, 3);
insert into state_change(id,from_id,to_id) values(3, 1, 2);
insert into state_change(id,from_id,to_id) values(4, 2, 1);
insert into state_change(id,from_id,to_id) values(5, 2, 3);
insert into state_change(id,from_id,to_id) values(6, 3, 1);