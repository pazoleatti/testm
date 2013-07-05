insert into ref_book(id, name) values
  (1, 'Человек'),
  (2, 'Книга');

insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible,
  precision, width) values
  (1, 1, 1, 'Наименование', 'name', 1, null, null, 1, null, 10),
  (2, 1, 2, 'Количество страниц', 'pagecount', 2, null, null, 1, null, 10),
  (3, 1, 4, 'Автор', 'author', 4, 2, 4, 1, null, 10),
  (4, 2, 1, 'ФИО', 'name', 1, null, null, 1, null, 10);