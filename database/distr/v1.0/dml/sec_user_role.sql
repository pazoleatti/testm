-- Роли для системного пользователя
insert into sec_user_role(user_id, role_id) values (0, 14);
insert into sec_user_role(user_id, role_id) values (0, 19);
insert into sec_user_role(user_id, role_id) values (0, 12);
insert into sec_user_role(user_id, role_id) values (0, 17);

-- Доступные события для ролей
-- Администратор
insert into role_event(event_id, role_id) (select id, 14 from event);
-- Контролёр УНП (НДФЛ)
insert into role_event(event_id, role_id) (select id, 12 from event where id not in (501, 502, 601, 701));
-- Контролёр УНП (Сборы)
insert into role_event(event_id, role_id) (select id, 17 from event where id not in (501, 502, 601, 701));
-- Оператор (НДФЛ)
insert into role_event(event_id, role_id)
 (select id, 11 from event
  where id in (1, 2, 3, 6, 7)
   or to_char(id) like '10_'
   or to_char(id) like '40_'
   or to_char(id) like '90_');
-- Оператор (Сборы)
insert into role_event(event_id, role_id)
 (select id, 16 from event
  where id in (1, 2, 3, 6, 7)
   or to_char(id) like '10_'
   or to_char(id) like '40_'
   or to_char(id) like '90_');
-- Контролёр НС (НДФЛ)
insert into role_event(event_id, role_id)
 (select id, 15 from event
  where id in (1, 2, 3, 6, 7)
   or to_char(id) like '10_'
   or to_char(id) like '40_'
   or to_char(id) like '90_');
-- Контролёр НС (Сборы)
insert into role_event(event_id, role_id)
 (select id, 18 from event
  where id in (1, 2, 3, 6, 7)
   or to_char(id) like '10_'
   or to_char(id) like '40_'
   or to_char(id) like '90_');
