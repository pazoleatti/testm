-- Транспортные сообщения
insert into transport_message
(id, message_uuid, datetime, type, receiver_subsystem_id, content_type, state, initiator_user_id, body)
values (1, 'abc123', timestamp '2010-01-01 01:01:01', 0, 11, 1, 1, 1, '<xml></xml>');

insert into transport_message
(id, message_uuid, datetime, type, receiver_subsystem_id, content_type, state, initiator_user_id)
values (2, 'BC124', timestamp '2011-01-01 01:01:01', 0, 31, 1, 1, 1);

insert into transport_message
(id, datetime, type, receiver_subsystem_id, content_type, state, initiator_user_id, blob_id)
values (3, timestamp '2011-01-01 01:01:01', 1, 31, 1, 2, 1, 'uuid_1');

-- со ссылкой на форму, у которой declaration_id = 1, department_id = 1
insert into transport_message
(id, datetime, type, receiver_subsystem_id, content_type, state, initiator_user_id, declaration_id)
values (4, timestamp '2012-01-01 01:01:01', 1, 31, 1, 3, 1, 1);
