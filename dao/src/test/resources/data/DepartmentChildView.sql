--Создается аналог department_child_view из базы данных, заполняется с помощью запросов,
--т.к. в hsqldb отсутствуют иерархические запросы (connect by prior), а рекурсивные (with recursive)
--дают недостаточно точный результат

CREATE TABLE department_child_view (
  id NUMBER(9, 0) NOT NULL,
  parent_id NUMBER(9, 0) NOT NULL,
  view_rowid VARCHAR(81) NOT NULL
);

INSERT INTO department_child_view (id, parent_id, view_rowid)
  VALUES (2, 1, '2 | 1');
INSERT INTO department_child_view (id, parent_id, view_rowid)
  VALUES (3, 1, '3 | 1');
INSERT INTO department_child_view (id, parent_id, view_rowid)
  VALUES (4, 1, '4 | 1');
INSERT INTO department_child_view (id, parent_id, view_rowid)
  VALUES (5, 1, '5 | 1');
INSERT INTO department_child_view (id, parent_id, view_rowid)
  VALUES (6, 1, '6 | 1');
INSERT INTO department_child_view (id, parent_id, view_rowid)
  VALUES (7, 1, '7 | 1');
INSERT INTO department_child_view (id, parent_id, view_rowid)
  VALUES (6, 2, '6 | 2');
INSERT INTO department_child_view (id, parent_id, view_rowid)
  VALUES (4, 3, '4 | 3');
INSERT INTO department_child_view (id, parent_id, view_rowid)
  VALUES (5, 3, '5 | 3');