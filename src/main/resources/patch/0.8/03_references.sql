--http://jira.aplana.com/browse/SBRFACCTAX-12832: ������� � ����������
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (501,'������� � ����������',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5011, 501, '���', 		'CODE', 2, 1, null, null, 1, 0, 	5, 1, 1, 1, 	null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5012, 501, '�������', 	'NAME', 1, 2, null, null, 1, null, 	20, 1, 2, null, null, 0, 50);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 501, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5011, 1);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5012, '���������');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 501, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5011, 2);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5012, '�� ���������');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 501, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5011, 3);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5012, '�� ����������');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 501, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5011, 4);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5012, '�������� ������������');

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12833: ���� ��������� �������
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (502,'���� ��������� �������',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5021, 502, '��� ���� ���������� ������', 		'CODE', 2, 1, null, null, 1, 0, 	5, 1, 1, 1, 	null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5022, 502, '��� ���������� ������', 			'NAME', 1, 2, null, null, 1, null, 	20, 1, 0, null, null, 0, 128);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 502, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5021, 1);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5022, '���������� �������� ��� �������, ������������ � ���');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 502, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5021, 2);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5022, '���������� �������� ��� �������, ������������ � ���');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 502, to_date('01.01.2012', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5021, 3);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5022, '����� ���������� ��������� �������');

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-1284: ��������� ������
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (503,'��������� ������',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5031, 503, '��� ���������� ������', 'CODE', 	4, 1, 502, 	5022, 1, null, 5, 	1, 1, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5032, 503, '��������', 				'VALUE', 	2, 2, null, null, 1, 2,    10, 	1, 0, null, null, 0, 4);

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12832: ������ ���������� ���/ ��� �������������� �����
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (504,'������ ���������� ���/ ��� �������������� �����',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5041,	504,	'��� �������� ��',		'REGION_ID',		4,	1, 4,		10,		1, null,	10, 1, 1, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5042, 	504, 	'��������������� ����', 'DEPARTMENT_ID', 	4, 	2, 30, 		161, 	1, null, 	10, 1, 1, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5043, 	504, 	'���������� ����', 		'NAME', 			1, 	3, null, 	null, 	1, null, 	30, 1, 1, null, null, 0, 255);

UPDATE REF_BOOK SET REGION_ATTRIBUTE_ID = 5041 WHERE ID = 504;
----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12860: ���� ����������� ���, ������ � �������� ���������� ���������������

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (505,'���� ����������� ���, ������ � �������� ���������� ���������������',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5051, 505, '��� ����', 		'CODE', 1, 1, null, null, 1, null, 	5, 1, 1, 1, 	null, 0, 15);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5052, 505, '������������', 	'NAME', 1, 2, null, null, 1, null, 20, 1, 0, null, 	null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 505, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5051, '��� ���');	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5052, '��������������� ����, ����������� ����� ����� ��������������� ');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 505, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5051, '��� ���');	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5052, '��������������� ����, ����������� ����������� ����� ���������������');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 505, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5051, '����');	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5052, '����������� ��������������� ����');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 505, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5051, '���');	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5052, '��������� ��������� ���');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 505, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5051, '��');	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5052, '����������� ����');

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12997: �����
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id, table_name, is_versioned) VALUES (1,'�����',1,0,1,null, 'COLOR', 0);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (95, 1, '������������ �����', 	'NAME', 1, 1, null, null, 1, null, 20, 1, 1, null, 	null, 0, 50);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (96, 1, 'R', 'R', 	2, 2, null, null, 1, 0, 	5, 1, 2, null, 	null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (97, 1, 'G', 'G', 	2, 3, null, null, 1, 0, 	5, 1, 2, null, 	null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (98, 1, 'B', 'B', 	2, 4, null, null, 1, 0, 	5, 1, 2, null, 	null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (99, 1, 'HEX', 'HEX', 1, 5, null, null, 1, null, 	7, 1, 3, null, 	null, 0, 7);

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12852: ��������� ������������ ���� �� ������� ���������
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (506, '��������� ������������ ���� �� ������� ���������',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5061, 506, '��� ���������', 'CODE',   1, 1, null, null, 1, null,   5, 1, 1, 1,   null, 0, 30);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5062, 506, '����',       'COLOR',   4, 2, 1, 95, 1, null,   5, 1, 0, null,   null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5063, 506, '��������',     'NAME',   1, 3, null, null, 1, null, 20, 1, 0, null, 	null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 506, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5061, '��������� 1');
	insert into ref_book_value (record_id, attribute_id, reference_value) values (seq_ref_book_record.currval, 5062, 8);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5063, '��������� �������� � ������� ����� ����������� �� ��������� ������ ����� ��������. ');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 506, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5061, '��������� 2');
	insert into ref_book_value (record_id, attribute_id, reference_value) values (seq_ref_book_record.currval, 5062, 8);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5063, '��������� �������� � ������� ����� ����������� �� ��������� ������ ����� ��������. ����������� ������ ��� � ����� ������� ���������������');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 506, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5061, '��������� 3');
	insert into ref_book_value (record_id, attribute_id, reference_value) values (seq_ref_book_record.currval, 5062, 1);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5063, '���������� ���������������� � ��������� ����, ����� �� ��������� ��������� ��������');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 506, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5061, '��������� 4');
	insert into ref_book_value (record_id, attribute_id, reference_value) values (seq_ref_book_record.currval, 5062, 12);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5063, '��������� ��������, � ������� ����� �����������, �� ����� ���������');


----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12857: ������� ���������� ��������� ������������ ����



----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12859: ������ �� ���
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (510,'������ �� ���',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5101, 510, '��� ������� �� ���', 		'CODE', 2, 1, null, null, 1, 0, 	5, 1, 1, 1, 	null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5102, 510, '��������', 					'NAME', 1, 2, null, null, 1, null, 	20, 1, 0, null, null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 510, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5101, 1);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5102, '�����������, �� ������������ �� ������������ ������������������ �� ���, ��� �����������, ������������� �� ������������ �����������������');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 510, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5101, 2);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5102, '������ �����������');


----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12858: ����������� ��������� ������

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (511,'����������� ��������� ������',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5111, 511, '���', 		'CODE', 2, 1, null, null, 1, 0, 	5, 1, 1, 1, 	null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5112, 511, '��������',  'NAME', 1, 2, null, null, 1, null, 	20, 1, 0, null, null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 511, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5111, 1);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5112, '����������� ������ ��� ���������� ��. ���, ����������� � ����� �� ����� �����������, ������� �������� ������� ������ �������');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 511, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5111, 2);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5112, '������ ���������� ����������� ����');

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12854: �������� �����������������
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (512, '�������� �����������������',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5121, 512, '���', 			'CODE', 	1, 1, null, null, 1, null, 	5, 1, 1, 1, 	null, 0, 15);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5122, 512, '��������', 		'NAME', 	1, 2, null, null, 1, null, 20, 1, 0, null, 	null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '1�');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '����������� � ���� � ������, ���� ������ ����������� ����� �/ ��� �������� ��������� � �������� �����, � ���� ������ ������� ���������� ����� 25%');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '1�');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '���� � ����������� � ������, ���� ���� ����� �/ ��� �������� ��������� � �������� ������ �����������, � ���� ������ ������� ���������� ����� 25%');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '2');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '���������� ���� � ���� � ������, ���� ����� ���������� ���� ����� �/ ��� �������� ��������� � �������� �����, � ���� ������ ������� ���������� ����� 25%');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '����������� � ���� � ������, ���� ���� � �� �� ���� ����� �/ ��� �������� ��������� � �������� ���� ����������� � �����, � ���� ������ ������� � ���� ����������� � ����� ���������� ����� 25%');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '4�');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '���� � ����, ������� ���������� �� ���������� ������������ ��������������� ������ ����� ��� �� ���������� �� ����� 50% ������� �������������� ��������������� ������ ��� ������ ���������� �����');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '4�');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '�����������, ������� ���������� �� ���������� ������������ ��������������� ������ ���� ����������� ��� �� ���������� �� ����� 50% ������� �������������� ��������������� ������ ��� ������ ���������� ���� �����������');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '5');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '�����������, ����������� �������������� ������ ������� ���� �� ����� 50% ������� �������������� ��������������� ������ ��� ������ ���������� ������� ��������� ��� ������� �� ������� ������ � ���� �� ����');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '�����������, � ������� ����� 50% ������� �������������� ��������������� ������ ��� ������ ���������� ���������� ���� � �� �� ���������� ���� ��������� � ���, �������������� ��� ����� �������� 11');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '7�');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '���� � ����, �������������� ���������� ��� ������������ ��������������� ������');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '7�');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '����������� � ����, �������������� ���������� ��� ������������ ��������������� ������');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '����������� � ����, � ������� ���������� ������������ ��������������� ������ ������������ ���� � �� �� ����');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '9');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '����������� �/��� ���������� ���� � ������, ���� ���� ������� ������� ������� ����������� ���� � ������ ����������� ����������� ���������� ����� 50%');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '10');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '���������� ���� � ������, ���� ���� ���������� ���� ����������� ������� ����������� ���� �� ������������ ���������');

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 512, to_date('01.01.2008', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5121, '11');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5122, '���������� ����, ��� ������ (�������), �������� (� ��� ����� �����������), ���� (� ��� ����� ������������), ����������� � ������������� ������ � ������, ������ (����������) � ����������');


----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12853: ��� �����������
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (513,'��� �����������',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5131, 513, '���', 		'CODE', 2, 1, null, null, 1, 0, 	5, 1, 1, 1, 	null, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5132, 513, '��������',  'NAME', 1, 2, null, null, 1, null, 	20, 1, 0, null, null, 0, 256);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 513, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5131, 1);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5132, '����������� ���� �������� ���������� ������������');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 513, to_date('01.01.2008', 'DD.MM.YYYY'), 0);	
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5131, 2);	
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5132, '����������� ���� �������� ����������� ������������');

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12856: ��������� ��������

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (514, '��������� ��������',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5141, 514, '��� ������������ ����',       'CODE',   4, 1, 505, 5051, 1, null,   30, 1, 1, null,   null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5142, 514, '��������� �������� (���.)',     'VALUE',   2, 2, null, null, 1, 0, 20, 1, 0, null, 	null, 0, 12);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 514, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5141, record_id from ref_book_value where attribute_id = 5051 and string_value = '��� ���';
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5142, 1000000000);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 514, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5141, record_id from ref_book_value where attribute_id = 5051 and string_value = '��� ���';
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5142, 60000000);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 514, to_date('02.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5141, record_id from ref_book_value where attribute_id = 5051 and string_value = '����';
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5142, 0);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 514, to_date('03.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5141, record_id from ref_book_value where attribute_id = 5051 and string_value = '���';
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5142, 60000000);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 514, to_date('04.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5141, record_id from ref_book_value where attribute_id = 5051 and string_value = '��';
	insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5142, 60000000);

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12857: ������� ���������� ��������� ������������ ����

INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (515, '������� ���������� ��������� ������������ ����',1,0,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5151, 515, '��� ������������ ����',       'CODE',   4, 1, 505, 5051, 1, null,   15, 1, 1, null,   null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5152, 515, '���������',       'CATEGORY',   4, 2, 506, 5061, 1, null,   15, 1, 0, 1,   null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5153, 515, '����������� ����� ������� � �������� (���.)',  'MIN_VALUE',   2, 3, null, null, 1, 0, 10, 1, 1, null,   null, 0, 12);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5154, 515, '������������ ����� ������� � �������� (���.)', 'MAX_VALUE',   2, 4, null, null, 1, 0, 10, 0, 0, null, 	null, 0, 12);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = '��� ���';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = '��������� 1';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 0);
  
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = '����';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = '��������� 1';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 0);
  
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = '���';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = '��������� 1';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 0);
  
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = '��';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = '��������� 1';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 0);
  
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = '��� ���';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = '��������� 2';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 700000000);
  
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = '��� ���';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = '��������� 3';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 500000000);
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5154, 699999999);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 515, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5151, record_id from ref_book_value where attribute_id = 5051 and string_value = '��� ���';
  insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5152, record_id from ref_book_value where attribute_id = 5061 and string_value = '��������� 4';
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5153, 0);
  insert into ref_book_value (record_id, attribute_id, number_value) values (seq_ref_book_record.currval, 5154, 499999999);

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12882: ����������� ������� � ��������, �� ����������� ��� ������� ������ �� �������
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (516, '����������� ������� � ��������, �� ����������� ��� ������� ������ �� �������',1,1,0,null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5161, 516, '� �/�', 'NUMBER', 1, 1, null, null, 1, null, 6, 1, 1, null, null, 0, 5);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5162, 516, '������������ �����������', 'NAME', 1, 2, null, null, 1, null, 30, 1, 1, null, null, 0, 300);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5163, 516, '������ ����� 102', 'SYMBOL_102', 1, 3, null, null, 1, null, 30, 1, 0, null, null, 0, 350);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (5164, 516, '������������ ����������', 'PARENT_ID',   4, 4, 516, 5162, 1, null,   15, 0, 0, null,   null, 0, null);

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 1, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, 'I');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������ �� ����������� �����');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(16305.02+17201.97+17202.07+17202.08 +17202.09+17202.97+17202.99+17203.02 +17203.03+17203.06+17203.09+17203.11 +17203.13+17203.14+17203.97+17306.19 +17306.20+17306.99+17307)');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 2, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, 'II');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������� �� ����������� �����');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '����� ����� �� ���� � �/� (1+2+3+4+5+6+7+8+9+10+11+12+ 13+14+15+16+17+18+19+20)');
	
insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 3, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '1');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������ �����, ������ ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26101.04+26101.12+26101.13+26101.14 +26101.99+26401.04+27203.08)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 4, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '2');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '���������� ������� (������������ ������) ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26104.01+26104.02+26104.03+26104.04 +26104.05+26104.06+26104.99+27308.15 +27308.16+27308.17+27308.18)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 5, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������� �� ��������� ������������� ������� ����������� (������������������ ����������� �����������) ���������� ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26410.04');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 6, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '���������� ������ � ������ ���');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(27308.20+27308.21)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 7, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '5');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '����������� ������������ � ������, ��������� ��������� � ���������������, ���������� � ������������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26301.02+26301.06+26301.08+26301.10 +26301.12+26301.14+26301.16+26302.02 +26302.04+26302.06+26302.08+26302.10 +26302.12+26302.14+26302.16+26302.18 +26302.20+26305.02+26305.05+26305.07 +26305.09+26305.11+26305.13+27203.20 +27203.22+27203.24+27203.26+27203.28 +27203.30+27203.32+27203.34+27203.46)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 8, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '6');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '�������� �����');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26303.02+27203.36)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 9, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '7');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������� �� ��������� ������������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26402.02');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 10, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '8');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������� �� ��������� ');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26403.04+26406.06+26406.07+26406.08 +26406.09+26406.10+26406.11+26406.13)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 11, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '9');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '����������������� �������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26405.02');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 12, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '10');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������, �����');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26411.02+26411.05+26411.06+26411.09 +26411.10+26411.11+27203.02+27203.05)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 13, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '10.1');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '���');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26411.02+26411.11+27203.02)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������, �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 14, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '10.2');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������ ������ � �����, �� ����������� ��� ����� ���������������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26411.05+26411.10+27203.05)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������, �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 15, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '10.3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '����� �� ������� (�����), ���������� �� �������, �� ����������� ��� ����� ���������������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26411.06');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������, �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 16, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '10.4');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '����� �� ���������� ����������� �� ���������� �����, ����� ������������� ����');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26411.09');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������, �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 17, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '11');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������������ �������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26412.03');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 18, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '12');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������������ � ������������ �������, �� ����������� ��� ����� ���������������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26412.09+26412.22+26412.24)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 19, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '13');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������� �� ��������� � ����������� ������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26412.12');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 20, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '14');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '����������� ������� � ������������ ������ ������� �������� �������, �� ����������� ��������������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '26412.14');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 21, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '15');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������ �������, ����������� � ��������������� � �������������� ��������, �� ����������� ��� ����� ���������������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(26412.99+27203.13+27203.48+26412.33 +26412.34+26412.35+26412.36+26412.37)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 22, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '16');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������� � ���������� ����������� �������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '27301');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 23, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '17');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '���������� � ������� ���������, �������� �� ��������������; ���������� �����������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(27203.09+27203.11+27308.22+27308.23)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 24, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '18');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, ' ������ �������, �����, ��������, �� ����������� ��� ����� ���������������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(27103+27203.15)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 25, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '19');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������ �������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '(25203.03+25302.02+26401.02+26403.02 +26410.06+26410.12+26410.98+27201.97 +27202.99+27203.38+27203.40+27203.42 +27203.44+27203.97+27203.99+27302 +27303+27304+27307+27308.97+27308.98)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 26, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '20');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '���� �������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '����� ����� �� ���� � �/� (20.1+20.2+20.3)');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '������� �� ����������� �����';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 27, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '20.1');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������� �� ������������������� � ������ ����������� ����');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '27305');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '���� �������';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 28, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '20.2');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������� �� ������������� ���������� �����������, ������, ����������� ���������-����������������� ��������� � ���� ����������� �����������');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '27306');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '���� �������';

insert into ref_book_record (id, record_id, ref_book_id, version, status) values (seq_ref_book_record.nextval, 29, 516, to_date('01.01.2014', 'DD.MM.YYYY'), 0);
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5161, '20.3');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5162, '������� � �������� ��������� ��������, ��������� �����, ������ ���������� � ����������� ���');
	insert into ref_book_value (record_id, attribute_id, string_value) values (seq_ref_book_record.currval, 5163, '27308.25');
	insert into ref_book_value (record_id, attribute_id, reference_value) select seq_ref_book_record.currval, 5164, record_id from ref_book_value where attribute_id = 5162 and string_value = '���� �������';

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-12861: ����������� ����

----------------------------------------------------------------------------------------------------------------

COMMIT;
EXIT;