INSERT INTO ref_book (id, name, table_name) VALUES ('10000', 'getRecordsTest', 'get_records_test');

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, max_length) VALUES
  (20001, 10000, 'aaa', 'a', 1, 0, 20);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, max_length) VALUES
  (20002, 10000, 'bbb', 'b', 1, 1, 20);


CREATE TABLE get_records_test (
  id        NUMBER(18),
  record_id NUMBER(9)           NOT NULL,
  version   DATE                NOT NULL,
  status    NUMBER(1) DEFAULT 0 NOT NULL,
  a         VARCHAR2(100),
  b         VARCHAR2(100)
);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 1, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 1, date '2016-05-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 2, date '2016-05-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 2, date '2016-07-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 3, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 3, date '2016-05-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 3, date '2016-07-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 4, date '2016-05-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 4, date '2016-09-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 5, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 5, date '2016-05-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 5, date '2016-09-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 6, date '2016-05-01', 0, '1', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 7, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 7, date '2016-05-01', 0, '2', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 8, date '2016-07-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 8, date '2016-08-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 9, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 9, date '2016-05-01', 2, '0', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 9, date '2016-07-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES ( 9, date '2016-08-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (10, date '2016-05-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (10, date '2016-07-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (10, date '2016-08-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (11, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (11, date '2016-05-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (11, date '2016-07-01', 0, '3', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (11, date '2016-08-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (12, date '2016-08-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (12, date '2016-10-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (13, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (13, date '2016-05-01', 2, '0', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (13, date '2016-08-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (13, date '2016-10-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (14, date '2016-05-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (14, date '2016-07-01', 2, '0', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (14, date '2016-08-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (14, date '2016-10-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (15, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (15, date '2016-05-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (15, date '2016-07-01', 2, '0', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (15, date '2016-08-01', 0, '3', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (15, date '2016-10-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (16, date '2016-07-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (16, date '2016-08-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (16, date '2016-10-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (17, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (17, date '2016-05-01', 2, '0', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (17, date '2016-07-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (17, date '2016-08-01', 0, '3', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (17, date '2016-10-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (18, date '2016-05-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (18, date '2016-07-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (18, date '2016-08-01', 0, '3', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (18, date '2016-10-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (19, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (19, date '2016-05-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (19, date '2016-07-01', 0, '3', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (19, date '2016-08-01', 0, '4', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (19, date '2016-10-01', 2, '0', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (20, date '2016-08-01', 0, '1', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (21, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (21, date '2016-05-01', 2, '0', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (21, date '2016-08-01', 0, '2', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (22, date '2016-05-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (22, date '2016-07-01', 2, '0', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (22, date '2016-08-01', 0, '2', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (23, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (23, date '2016-05-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (23, date '2016-07-01', 2, '0', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (23, date '2016-08-01', 0, '3', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (24, date '2016-07-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (24, date '2016-08-01', 0, '2', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (25, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (25, date '2016-05-01', 2, '0', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (25, date '2016-07-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (25, date '2016-08-01', 0, '3', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (26, date '2016-05-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (26, date '2016-07-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (26, date '2016-08-01', 0, '3', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (27, date '2016-01-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (27, date '2016-05-01', 0, '2', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (27, date '2016-07-01', 0, '3', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (27, date '2016-08-01', 0, '4', seq_ref_book_record.nextval);

INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (28, date '2016-10-01', 0, '1', seq_ref_book_record.nextval);
INSERT INTO get_records_test (record_id, version, status, b, id) VALUES (28, date '2016-11-01', 2, '0', seq_ref_book_record.nextval);

UPDATE get_records_test SET a = record_id;