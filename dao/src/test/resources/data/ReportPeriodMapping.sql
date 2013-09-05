insert into ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width) values
  (25, 8, 0, 'Код', 'CODE', 1, null, null, 1, null, 10);

insert into ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) values
  (21, 25, '21', null, null, null);