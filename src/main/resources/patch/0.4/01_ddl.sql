-- http://jira.aplana.com/browse/SBRFACCTAX-8333: ���������� ����������� ref_book_attr_chk_is_unique
ALTER TABLE ref_book_attribute DROP CONSTRAINT ref_book_attr_chk_is_unique;

---------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8465: �������� ���� "�������� �������" � ������� REF_BOOK
ALTER TABLE ref_book ADD table_name VARCHAR2(100);
COMMENT ON COLUMN ref_book.table_name IS '�������� ������� ��, � ������� �������� ������';

---------------------------------------------------------------------------------------------------