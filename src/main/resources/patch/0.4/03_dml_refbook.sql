---------------------------------------------------------------------------------------------------

-- http://jira.aplana.com/browse/SBRFACCTAX-8357: ������� ����������� �� ������ �� ���������
-- -- http://jira.aplana.com/browse/SBRFACCTAX-8359: ������������� ������������ ����������� ����� ��������� ����� � ���������� ��������� �����
UPDATE ref_book SET name = '���� ��������� ����� ������������� ������' WHERE ID = 6;
UPDATE ref_book SET name = '��������� ��������� ����� ������������� ������' WHERE ID = 7;

ALTER TABLE ref_book DISABLE CONSTRAINT ref_book_fk_region;

-- -- http://jira.aplana.com/browse/SBRFACCTAX-8318: C��������� "��������� ������������� ���������� �� ������ �� ���������"
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (200, '��������� ������������� ���������� �� ������ �� ���������', 1, 0, 0, 2001);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2001, 200, '��� �������� �� ������������� ����������', 'DECLARATION_REGION_ID', 4, 1, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2002, 200, '��� �������� ��', 'REGION_ID', 4, 2, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2003, 200, '��� ���������� ������', 'TAX_ORGAN_CODE', 1, 3, null, null, 1, null, 10, 0, 0, null, null, 0, 4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2004, 200, '���', 'KPP', 1, 4, null, null, 1, null, 10, 0, 0, null, null, 0, 9);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2005, 200, '��� �����', 'OKTMO', 4, 5, 96, 840, 1, null, 10, 0, 0, null, null, 0, null);

-- -- http://jira.aplana.com/browse/SBRFACCTAX-8358: ���������� "������ ������ �� ���������"
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (201, '������ ������ �� ���������', 1, 0, 0, 2011);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2011, 201, '��� �������� �� ������������� ����������', 'DECLARATION_REGION_ID', 4, 1, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2012, 201, '��� �������� ��', 'REGION_ID', 4, 2, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2013, 201, '������ (%)', 'RATE', 2, 3, null, null, 1, 2, 10, 0, 0, null, null, 0, 3);

-- -- http://jira.aplana.com/browse/SBRFACCTAX-8360: C��������� "���� ��������� ����� ������ �� ���������"
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (202, '���� ��������� ����� ������ �� ���������', 1, 0, 0, null);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2021,202,'��� ��������� ������','CODE',1,1,null,null,1,null,7,1,1,null,null,0,7);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2022,202,'������������ ������','NAME',1,2,null,null,1,null,50,1,0,null,null,0,2000);

INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (seq_ref_book_record.NEXTVAL, 1, 202, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2021, '2012000');
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2022, '�������������� ������ �� ������ �� ��������� �����������, ��������������� �������� ��������� ��, �� ����������� ����� � ���� �������� ������ ��� ��������� ��������� ������������������ � � ���� ���������� ����� ������, ���������� ������ � ������');

INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (seq_ref_book_record.NEXTVAL, 2, 202, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2021, '2012400');
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2022, '�������������� ������ �� ������ �� ��������� �����������, ��������������� �������� ��������� �� � ���� ��������� ��������� ������ ��� ��������� ��������� ������������������');

INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (seq_ref_book_record.NEXTVAL, 3, 202, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2021, '2012500');
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 2022, '�������������� ������ �� ������ �� ��������� �����������, ��������������� �������� ��������� �� � ���� ���������� ����� ������, ���������� ������ � ������');	

-- -- http://jira.aplana.com/browse/SBRFACCTAX-8361: C��������� "��������� ��������� ����� ������ �� ���������"	
INSERT INTO ref_book (id, name, visible, type, read_only, region_attribute_id) VALUES (203, '��������� ��������� ����� ������ �� ���������',  1, 0, 0, 2031);

INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2031, 203, '��� �������� �� ������������� ����������', 'DECLARATION_REGION_ID', 4, 1, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2032, 203, '��� �������� ��', 'REGION_ID', 4, 2, 4, 10, 1, null, 10, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2033, 203, '��� ��������� ������', 'TAX_BENEFIT_ID', 4, 3, 202, 2022, 1, null, 20, 1, 0, null, null, 0, null);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2034, 203, '��������� ���������', 'ASSETS_CATEGORY', 1, 4, null, null, 1, null, 20, 0, 0, null, null, 0, 200); 
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2035, 203, '��������� - ������','SECTION',1,5,null,null,1,null,10,1,0,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2036, 203, '��������� - �����','ITEM',1,6,null,null,1,null,10,1,0,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2037, 203, '��������� - ��������','SUBITEM',1,7,null,null,1,null,10,1,0,null,null,0,4);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2038, 203, '�������� ������, %', 'RATE', 2, 8, null, null, 1, 2, 10, 0, 0, null, null, 0, 3);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) VALUES (2039, 203, '����� ���������� �������, ���.', 'REDUCTION_SUM', 2, 9, null, null, 1, 2, 10, 0, 0, null, null, 0, 17);

ALTER TABLE ref_book ENABLE CONSTRAINT ref_book_fk_region;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8465: �������� ���� "�������� �������" � ������� REF_BOOK
UPDATE ref_book SET table_name = 'REF_BOOK_OKTMO' WHERE id = 96;
UPDATE ref_book SET table_name = 'FORM_TYPE' WHERE id = 93;
UPDATE ref_book SET table_name = 'FORM_KIND' WHERE id = 94;
UPDATE ref_book SET table_name = 'SEC_USER' WHERE id = 74;
UPDATE ref_book SET table_name = 'SEC_ROLE' WHERE id = 95;
UPDATE ref_book SET table_name = 'DEPARTMENT' WHERE id = 30;
UPDATE ref_book SET table_name = 'DEPARTMENT_TYPE' WHERE id = 103;
UPDATE ref_book SET table_name = 'INCOME_101' WHERE id = 50;
UPDATE ref_book SET table_name = 'INCOME_102' WHERE id = 52;

---------------------------------------------------------------------------------------------------


