insert into log (id, creation_date) values ('1-1-1', to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'));

insert into log (id, creation_date) values ('2-2-2', to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'));

insert into log (id, creation_date) values ('3-3-3', to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'));

insert into log (id, creation_date) values ('4-4-4', to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'));
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('4-4-4', 1, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 'panic!');
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('4-4-4', 2, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 'panic!');
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('4-4-4', 3, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 'panic!');

insert into log (id, creation_date) values ('5-5-5', to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'));
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('5-5-5', 0, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 'panic!');
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('5-5-5', 1, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 'panic!');
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('5-5-5', 2, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 'panic!');
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('5-5-5', 4, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 'panic!');

insert into log (id, creation_date) values ('6-6-6', to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'));
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('6-6-6', 1, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 0, 'panic!');
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('6-6-6', 2, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 1, 'panic!');
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('6-6-6', 3, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 1, 'panic!');
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('6-6-6', 4, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 2, 'panic!');
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('6-6-6', 5, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 2, 'panic!');
insert into log_entry (log_id, ord, creation_date, log_level, message) values ('6-6-6', 6, to_date('01.01.2013 12.01.01', 'DD.MM.YY HH.MI.SS'), 2, 'panic!');