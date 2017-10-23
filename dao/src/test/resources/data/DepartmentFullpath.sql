--Создается аналог department_fullpath из базы данных, заполняется с помощью запросов,
--т.к. в hsqldb отсутствуют иерархические запросы (connect by prior), а рекурсивные (with recursive)
--дают недостаточно точный результат

CREATE TABLE department_fullpath (
  id NUMBER(9, 0),
  shortname VARCHAR(4000)
);

INSERT INTO department_fullpath (id, shortname)
  VALUES (1, 'Банк');