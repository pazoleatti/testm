--Создается аналог department_fullpath из базы данных, заполняется с помощью запросов,
--т.к. в hsqldb отсутствуют иерархические запросы (connect by prior), а рекурсивные (with recursive)
--дают недостаточно точный результат

CREATE TABLE department_fullpath (
  id NUMBER(9, 0),
  shortname VARCHAR(4000)
);

INSERT INTO department_fullpath (id, shortname) VALUES (1, 'Банк');
INSERT INTO department_fullpath (id, shortname) VALUES (2, 'Банк/ТБ1');
INSERT INTO department_fullpath (id, shortname) VALUES (3, 'Банк/ТБ2');
INSERT INTO department_fullpath (id, shortname) VALUES (4, 'Банк/ТБ2/Подр1 от ТБ2');
INSERT INTO department_fullpath (id, shortname) VALUES (5, 'Банк/ТБ2/Подр2 от ТБ2');
INSERT INTO department_fullpath (id, shortname) VALUES (6, 'Банк/ТБ1/Подр1 от ТБ1');
INSERT INTO department_fullpath (id, shortname) VALUES (7, 'Банк/ТБ3');