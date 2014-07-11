INSERT INTO ref_book_attribute(id, ref_book_id, ord, name, alias, type, reference_id, attribute_id, visible, precision, width, max_length) VALUES
  (25, 8, 0, 'Код', 'CODE', 1, NULL, NULL, 1, NULL, 10, 100);

INSERT INTO ref_book_value(record_id, attribute_id, string_value, number_value, date_value, reference_value) VALUES
  (21, 25, '21', NULL, NULL, NULL);