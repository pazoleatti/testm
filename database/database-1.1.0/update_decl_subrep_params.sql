-->**************************************************************************<--
--> UPDATE declaration_subreport_params
--> Для спецотчета по ФЛ для форм 2-НДФЛ(1) и 2-НДФЛ(2) изменился набор полей
--> JIRA: https://jira.aplana.com/browse/SBRFNDFL-1455
-->**************************************************************************<--
--1
INSERT INTO declaration_subreport_params(id,declaration_subreport_id,name,alias,ord,type) 
VALUES(10207,1021,'ИНН','inn',4,'S');
INSERT INTO declaration_subreport_params(id,declaration_subreport_id,name,alias,ord,type) 
VALUES(10407,1041,'ИНН','inn',4,'S');
--2
DELETE FROM declaration_subreport_params WHERE id=10205;
DELETE FROM declaration_subreport_params WHERE id=10405;
--3
INSERT INTO declaration_subreport_params(id,declaration_subreport_id,name,alias,ord,type) 
VALUES(10208,1021,'Дата рождения с','fromBirthDay',5,'D');
INSERT INTO declaration_subreport_params(id,declaration_subreport_id,name,alias,ord,type) 
VALUES(10408,1041,'Дата рождения с','fromBirthDay',5,'D');
--4
INSERT INTO declaration_subreport_params(id,declaration_subreport_id,name,alias,ord,type) 
VALUES(10209,1021,'Дата рождения по','toBirthDay',6,'D');
INSERT INTO declaration_subreport_params(id,declaration_subreport_id,name,alias,ord,type) 
VALUES(10409,1041,'Дата рождения по','toBirthDay',6,'D');
--5
UPDATE declaration_subreport_params SET ord=7 WHERE id=10206;
UPDATE declaration_subreport_params SET ord=7 WHERE id=10406;
--6
UPDATE declaration_subreport_params SET type='S' WHERE id IN (10005,10015);
