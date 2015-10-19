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
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------

COMMIT;
EXIT;