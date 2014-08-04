---------------------------------------------------------------------------------------------------------
-- ����������: ���������������� ���������
INSERT INTO REF_BOOK (ID, NAME, TYPE, VISIBLE, READ_ONLY) VALUES (105, '���������������� ���������', 0, 0, 1);
INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE, ORD, VISIBLE, WIDTH) VALUES (1040, 105, '���', 'CODE', 1, 0, 0, 20);
INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE, ORD, VISIBLE, WIDTH) VALUES (1041, 105, '������������', 'NAME', 1, 1, 1, 40);

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8267: �����������. ��� �������� �� �������������� ����� ��������

--C��������� "���� ������"/"���� ������� ������"
UPDATE ref_book_attribute SET required = 1 WHERE id = 818;
UPDATE ref_book_attribute SET required = 1 WHERE id = 831;

/*
��������, ������� ����� ���������� �������������� ��� ����������:
� ��������������� ������.������
� ���� ��������.���� ��������
� ���� ������ �����.��� ���� ������ �����
� ����� �����.��� ������. ��������
� ����� ����������� ��������.���������� ���
� �����������.��� �����������
� �������������� ������������� �����.��� ������. ��������
� �����������-��������� �������������� ������.����� �����������
� ������� ������, ����������� � ���.�������������
� �������� ������������.��� �������� �����������
� ������ ������������ ������.�������������
� ������ ���������������� �� ��.������������
� ���� ������.�������������
� ������ ������.������������� ������ ������
� ������ ������.�������
� ����� ����� ���������� (�������).���� ���� ���������� (�������)
� ����� ����� �������������� (���������) ���������.���� ���� �������������� (���������) ���������
*/

UPDATE ref_book_attribute SET required=1 WHERE id=643;
UPDATE ref_book_attribute SET required=1 WHERE id=824;
UPDATE ref_book_attribute SET required=1 WHERE id=827;
UPDATE ref_book_attribute SET required=1 WHERE id=80;
UPDATE ref_book_attribute SET required=1 WHERE id=829;
UPDATE ref_book_attribute SET required=1 WHERE id=822;
UPDATE ref_book_attribute SET required=1 WHERE id=64;
UPDATE ref_book_attribute SET required=1 WHERE id=36;
UPDATE ref_book_attribute SET required=1 WHERE id=214;
UPDATE ref_book_attribute SET required=1 WHERE id=825;
UPDATE ref_book_attribute SET required=1 WHERE id=62;
UPDATE ref_book_attribute SET required=1 WHERE id=92;
UPDATE ref_book_attribute SET required=1 WHERE id=68;
UPDATE ref_book_attribute SET required=1 WHERE id=808;
UPDATE ref_book_attribute SET required=1 WHERE id=809;
UPDATE ref_book_attribute SET required=1 WHERE id=806;
UPDATE ref_book_attribute SET required=1 WHERE id=804;

/*
��������, ������� ����� ���� ������� �������������� ��� ����������:
� ���� ������������� ������.�������� ������������� ������
� ���� �����.������
� ���� ��������� ���������� ���������.��� �����
� ������ � ����� ������������ �����������.��� ������
*/

UPDATE ref_book_attribute SET required=0 WHERE id=834;
UPDATE ref_book_attribute SET required=0 WHERE id=61;
UPDATE ref_book_attribute SET required=0 WHERE id=17;
UPDATE ref_book_attribute SET required=0 WHERE id=56;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8274: ������������� �������� c ref_book
UPDATE ref_book SET name = '����', visible = 0 WHERE id = 0;
UPDATE ref_book SET name = '���� ������� ������' WHERE id = 65;
UPDATE ref_book SET name = '���� ��� �� ��������� ��������������� �������������� ��������� (���)' WHERE id = 68;
UPDATE ref_book SET type=1, read_only=1 WHERE id=96;
UPDATE ref_book SET type=0, read_only=1 WHERE id=9;
UPDATE ref_book SET type=1, read_only=1 WHERE id=3;

--����������: ������ ������������� ������
UPDATE ref_book_attribute SET name = '�������� ��' WHERE id = 414;
UPDATE ref_book_attribute SET name = '�������� ��' WHERE id = 415;

--����������: ���� ��� �� ��������� ��������������� �������������� ��������� (���)
UPDATE ref_book_attribute SET name = '��� ���' WHERE id = 637;

--����������: �������������� ������������� �����
UPDATE ref_book_attribute SET name = '������������' WHERE id = 66;

-- ����������: ������ ������������� ������: �������������� ��� �������� "��� �������� ��"
UPDATE ref_book_attribute SET required = 1 WHERE id = 417;

--������� ������� "������� ����������" ��� ����������� "����, ������������ ��������� (��������) ������"
DELETE FROM ref_book_value WHERE attribute_id = 623;
DELETE FROM ref_book_attribute WHERE id = 623;

--���������� ��� �������� "������������ ���� ������������� ��������" � ����������� "���� ����� ������������ �������"
UPDATE ref_book_attribute SET sort_order = 1 WHERE id = 423;

--�������������� ���������� ��� ���� ����� ����������� "���� ����������� ��������"
UPDATE ref_book_attribute SET required=1 WHERE id in (40, 41, 42);

--�������������� ���������� ��� �������� "�������� ������" � ����������� "���� ������"
UPDATE ref_book_attribute SET required=0 WHERE id=832;

--http://jira.aplana.com/browse/SBRFACCTAX-6996: ������� ����������� ����� "�����" � ��������������� ������� � ��������
UPDATE ref_book_attribute SET visible = 0 WHERE id in (350, 360);

---------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-7689 (SBRFACCTAX-7950): ���������� ���� "������������ ����� ������/����� ����� �����"

--������� "��� ������.���������", ���������� "�������������� ������������� �����" (���������� � ������������ ����� - 3)
UPDATE ref_book_value SET string_value = substr(trim(string_value), 1, 3) WHERE attribute_id = 65 and length(string_value)=4;

--�������������� ������������� ���������� ������������� ����������� (�����).��� (���������� � ������������ ����� - 11)
UPDATE ref_book_oktmo SET code = trim(code) WHERE length(code)>11;

--��� �������� ���� ����������� ��������.��� ��� ����� ������� ��� �� ������ �� ������
UPDATE ref_book_attribute SET type=4, reference_id=68, attribute_id=637 WHERE id = 41; 
UPDATE ref_book_value rbv1 SET rbv1.reference_value = (select rbv2.record_id from ref_book_value rbv2 WHERE rbv2.attribute_id = 637 and rbv1.string_value = rbv2.string_value) WHERE rbv1.attribute_id=41;
UPDATE ref_book_value rbv1 SET rbv1.string_value = null WHERE rbv1.attribute_id=41;

--������� ������� "�������� ��������������� ������ ��������/���������" (id=628) �� ����������� "���� ������� ��������" 
DELETE FROM ref_book_value WHERE attribute_id=628;
DELETE FROM ref_book_attribute WHERE id=628;

UPDATE ref_book_attribute SET max_length=1 WHERE id = 643;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 644;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 645;
UPDATE ref_book_attribute SET max_length=27 WHERE id = 801;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 802;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 833;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 834;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 835;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 836;
UPDATE ref_book_attribute SET max_length=40 WHERE id = 824;
UPDATE ref_book_attribute SET max_length=7 WHERE id = 831;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 832;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 818;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 819;
UPDATE ref_book_attribute SET max_length=2 WHERE id = 60;
UPDATE ref_book_attribute SET max_length=200 WHERE id = 61;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 827;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 828;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 249;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 250;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 140;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 141;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 142;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 143;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 144;
UPDATE ref_book_attribute SET max_length=20 WHERE id = 350;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 145;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 146;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 147;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 148;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 149;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 130;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 131;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 132;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 133;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 134;
UPDATE ref_book_attribute SET max_length=20 WHERE id = 360;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 135;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 136;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 137;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 138;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 139;
UPDATE ref_book_attribute SET max_length=7 WHERE id = 1000;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 1001;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 1002;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 150;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 151;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 152;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 153;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 154;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 637;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 638;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 631;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 632;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 100;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 101;
UPDATE ref_book_attribute SET max_length=5 WHERE id = 422;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 423;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 40;
UPDATE ref_book_attribute SET max_length=130 WHERE id = 42;
UPDATE ref_book_attribute SET max_length=5 WHERE id = 57;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 58;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 59;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 635;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 636;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 15;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 16;
UPDATE ref_book_attribute SET max_length=11 WHERE id = 7;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 8;
UPDATE ref_book_attribute SET max_length=2 WHERE id = 25;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 26;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 27;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 28;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 29;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 30;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 31;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 623;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 633;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 634;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 3;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 4;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 611;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 612;
UPDATE ref_book_attribute SET max_length=2 WHERE id = 9;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 10;
UPDATE ref_book_attribute SET max_length=11 WHERE id = 12;
UPDATE ref_book_attribute SET max_length=11 WHERE id = 5;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 629;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 630;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 625;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 626;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 627;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 13;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 14;
UPDATE ref_book_attribute SET max_length=19 WHERE id = 81;
UPDATE ref_book_attribute SET max_length=19 WHERE id = 830;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 45;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 46;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 822;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 823;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 501;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 502;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 510;
UPDATE ref_book_attribute SET max_length=22 WHERE id = 503;
UPDATE ref_book_attribute SET max_length=22 WHERE id = 504;
UPDATE ref_book_attribute SET max_length=22 WHERE id = 505;
UPDATE ref_book_attribute SET max_length=22 WHERE id = 506;
UPDATE ref_book_attribute SET max_length=22 WHERE id = 507;
UPDATE ref_book_attribute SET max_length=22 WHERE id = 508;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 64;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 65;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 66;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 67;
UPDATE ref_book_attribute SET max_length=8 WHERE id = 210;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 211;
UPDATE ref_book_attribute SET max_length=11 WHERE id = 840;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 841;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 50;
UPDATE ref_book_attribute SET max_length=2 WHERE id = 51;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 52;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 53;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 54;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 32;
UPDATE ref_book_attribute SET max_length=80 WHERE id = 34;
UPDATE ref_book_attribute SET max_length=80 WHERE id = 35;
UPDATE ref_book_attribute SET max_length=250 WHERE id = 36;
UPDATE ref_book_attribute SET max_length=12 WHERE id = 37;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 38;
UPDATE ref_book_attribute SET max_length=250 WHERE id = 656;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 520;
UPDATE ref_book_attribute SET max_length=25 WHERE id = 521;
UPDATE ref_book_attribute SET max_length=22 WHERE id = 522;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 524;
UPDATE ref_book_attribute SET max_length=4 WHERE id = 20;
UPDATE ref_book_attribute SET max_length=4 WHERE id = 21;
UPDATE ref_book_attribute SET max_length=4 WHERE id = 22;
UPDATE ref_book_attribute SET max_length=3 WHERE id = 23;
UPDATE ref_book_attribute SET max_length=15 WHERE id = 24;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 900;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 901;
UPDATE ref_book_attribute SET max_length=15 WHERE id = 160;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 161;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 162;
UPDATE ref_book_attribute SET max_length=2 WHERE id = 165;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 166;
UPDATE ref_book_attribute SET max_length=27 WHERE id = 870;
UPDATE ref_book_attribute SET max_length=27 WHERE id = 651;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 652;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 654;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 110;
UPDATE ref_book_attribute SET max_length=50 WHERE id = 111;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 212;
UPDATE ref_book_attribute SET max_length=50 WHERE id = 213;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 214;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 215;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 120;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 121;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 639;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 640;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 825;
UPDATE ref_book_attribute SET max_length=30 WHERE id = 826;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 43;
UPDATE ref_book_attribute SET max_length=130 WHERE id = 44;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 621;
UPDATE ref_book_attribute SET max_length=40 WHERE id = 622;
UPDATE ref_book_attribute SET max_length=5 WHERE id = 646;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 647;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 62;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 63;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 641;
UPDATE ref_book_attribute SET max_length=250 WHERE id = 642;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 838;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 839;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 92;
UPDATE ref_book_attribute SET max_length=17 WHERE id = 90;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 91;
UPDATE ref_book_attribute SET max_length=5 WHERE id = 411;
UPDATE ref_book_attribute SET max_length=15 WHERE id = 412;
UPDATE ref_book_attribute SET max_length=15 WHERE id = 413;
UPDATE ref_book_attribute SET max_length=15 WHERE id = 414;
UPDATE ref_book_attribute SET max_length=15 WHERE id = 415;
UPDATE ref_book_attribute SET max_length=15 WHERE id = 416;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 0;
UPDATE ref_book_attribute SET max_length=25 WHERE id = 846;
UPDATE ref_book_attribute SET max_length=256 WHERE id = 847;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 837;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 68;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 69;
UPDATE ref_book_attribute SET max_length=130 WHERE id = 70;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 648;
UPDATE ref_book_attribute SET max_length=500 WHERE id = 649;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 55;
UPDATE ref_book_attribute SET max_length=100 WHERE id = 56;
UPDATE ref_book_attribute SET max_length=11 WHERE id = 808;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 811;
UPDATE ref_book_attribute SET max_length=25 WHERE id = 812;
UPDATE ref_book_attribute SET max_length=30 WHERE id = 813;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 601;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 602;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 806;
UPDATE ref_book_attribute SET max_length=40 WHERE id = 807;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 804;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 805;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 400;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 401;
UPDATE ref_book_attribute SET max_length=11 WHERE id = 866;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 867;
UPDATE ref_book_attribute SET max_length=255 WHERE id = 868;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 1031;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 1040;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 1041;
UPDATE ref_book_attribute SET max_length=2000 WHERE id = 1050;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 235;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 234;
UPDATE ref_book_attribute SET max_length=20 WHERE id = 232;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 230;
UPDATE ref_book_attribute SET max_length=1000 WHERE id = 228;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 194;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 195;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 196;
UPDATE ref_book_attribute SET max_length=120 WHERE id = 197;
UPDATE ref_book_attribute SET max_length=1000 WHERE id = 198;
UPDATE ref_book_attribute SET max_length=4 WHERE id = 200;
UPDATE ref_book_attribute SET max_length=20 WHERE id = 201;
UPDATE ref_book_attribute SET max_length=5 WHERE id = 202;
UPDATE ref_book_attribute SET max_length=15 WHERE id = 205;
UPDATE ref_book_attribute SET max_length=15 WHERE id = 206;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 852;
UPDATE ref_book_attribute SET max_length=4 WHERE id = 853;
UPDATE ref_book_attribute SET max_length=20 WHERE id = 855;
UPDATE ref_book_attribute SET max_length=1000 WHERE id = 865;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 857;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 858;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 859;
UPDATE ref_book_attribute SET max_length=20 WHERE id = 863;
UPDATE ref_book_attribute SET max_length=5 WHERE id = 864;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 224;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 223;
UPDATE ref_book_attribute SET max_length=4 WHERE id = 222;
UPDATE ref_book_attribute SET max_length=20 WHERE id = 221;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 219;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 218;
UPDATE ref_book_attribute SET max_length=1000 WHERE id = 217;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 172;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 173;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 174;
UPDATE ref_book_attribute SET max_length=120 WHERE id = 175;
UPDATE ref_book_attribute SET max_length=1000 WHERE id = 176;
UPDATE ref_book_attribute SET max_length=20 WHERE id = 178;
UPDATE ref_book_attribute SET max_length=5 WHERE id = 179;
UPDATE ref_book_attribute SET max_length=1 WHERE id = 843;
UPDATE ref_book_attribute SET max_length=20 WHERE id = 187;
UPDATE ref_book_attribute SET max_length=1000 WHERE id = 191;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 239;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 240;
UPDATE ref_book_attribute SET max_length=60 WHERE id = 241;
UPDATE ref_book_attribute SET max_length=120 WHERE id = 242;
UPDATE ref_book_attribute SET max_length=1000 WHERE id = 243;
UPDATE ref_book_attribute SET max_length=20 WHERE id = 245;
UPDATE ref_book_attribute SET max_length=5 WHERE id = 246;
UPDATE ref_book_attribute SET max_length=4 WHERE id = 233;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 229;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 851;
UPDATE ref_book_attribute SET max_length=120 WHERE id = 860;
UPDATE ref_book_attribute SET max_length=1000 WHERE id = 861;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 183;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 184;
UPDATE ref_book_attribute SET max_length=4 WHERE id = 185;
UPDATE ref_book_attribute SET max_length=10 WHERE id = 189;
UPDATE ref_book_attribute SET max_length=9 WHERE id = 190;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8133: ������������ �������� "��� ������. ��������" � ����������� "����� �����"
UPDATE ref_book_attribute SET is_unique=1 WHERE id=80;

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7806: ������������ �������� � ����������� "������ ���������������� �� ��"
UPDATE ref_book_attribute SET is_unique=1 WHERE id=92; 

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8099: ���������� "����, ������������ ������, ������������� ����������"
INSERT INTO ref_book (id, name, visible, type, read_only) VALUES (106, '����, ������������ ������ ������������� ����������', 1, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, visible, width, required, is_unique, max_length) VALUES (1061, 106, '���', 'CODE', 1, 1, 1, 10, 1, 1, 2);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, visible, width, required, is_unique, max_length) VALUES (1062, 106, '������������', 'NAME', 1, 2, 1, 50, 1, 0, 255);

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8100 / SBRFACCTAX-8215: ���������� "������� � ������������� ��"
INSERT INTO ref_book (id, name, visible, type, read_only) VALUES (107, '������� � ������������� ��', 0, 0, 1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, visible, precision, width, required, is_unique, read_only, max_length, sort_order) VALUES (1071, 107, '���', 'YEAR', 2, 1, 1, 0, 10, 1, 0, 0, 4, -1);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, visible, precision, width, required, is_unique, read_only, max_length, reference_id, attribute_id) VALUES (1072, 107, '��� ������� ������������� ����������', 'ACCOUNT_PERIOD_ID', 4, 2, 1, null, 20, 1, 0, 0, null, 106, 1062);
INSERT INTO ref_book_attribute (id, ref_book_id, name, alias, type, ord, visible, precision, width, required, is_unique, read_only, max_length, reference_id, attribute_id) VALUES (1073, 107, '��� �������������', 'DEPARTMENT_ID', 4, 3, 1, null, 20, 1, 0, 0, null, 30, 161);

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-7839: ���������� "������ ����� ��� ������� ������"
INSERT INTO REF_BOOK (ID, NAME, TYPE, VISIBLE, READ_ONLY) VALUES (104, '������ ����� ��� ������� ������', 0, 0, 1);
INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE, ORD, WIDTH, MAX_LENGTH) VALUES (1050, 104, '������������', 'NAME', 1, 1, 20, 2000);

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8099: ���������� ����������� "����, ������������ ������, ������������� ����������"
INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (seq_ref_book_record.NEXTVAL, 1, 106, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 1061, '21');
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 1062, '������ �������');

INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (seq_ref_book_record.NEXTVAL, 2, 106, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 1061, '31');
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 1062, '���������');

INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (seq_ref_book_record.NEXTVAL, 3, 106, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 1061, '33');
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 1062, '������ �������');

INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) VALUES (seq_ref_book_record.NEXTVAL, 4, 106, to_date('01.01.2012', 'DD.MM.YYYY'), 0);
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 1061, '34');
	INSERT INTO ref_book_value (record_id, attribute_id, string_value) VALUES (seq_ref_book_record.CURRVAL, 1062, '���');

---------------------------------------------------------------------------------------------------------
-- http://jira.aplana.com/browse/SBRFACCTAX-8101: � ������� INCOME_101, INCOME_102 ���������� �������� ���� "��. ������� � ������������� ��"
CREATE TABLE tmp_account_period
	AS 
   SELECT Seq_Ref_Book_Record.NEXTVAL as ID, row_number() over (order by t.year, rbv.record_id, i.department_id) as RECORD_ID, rp.id as report_period_id, t.year, rbv.record_id as code_id, i.department_id, to_date('01.01.2012', 'DD.MM.YYYY') as VERSION 
		FROM
			(
			SELECT DISTINCT report_period_id, department_id FROM income_101
			UNION
			SELECT DISTINCT report_period_id, department_id FROM income_102
			) i
		JOIN report_period rp on i.report_period_id = rp.id
		JOIN tax_period t on rp.tax_period_id = t.id
		JOIN ref_book_value rbv on rbv.attribute_id = 1062 AND rbv.string_value = rp.name;

INSERT INTO ref_book_record (id, record_id, ref_book_id, version, status) SELECT id, record_id, 107, version, 0 FROM tmp_account_period;
INSERT INTO ref_book_value (record_id, attribute_id, number_value) SELECT id, 1071, year FROM tmp_account_period;
INSERT INTO ref_book_value (record_id, attribute_id, reference_value) SELECT id, 1072, code_id FROM tmp_account_period;
INSERT INTO ref_book_value (record_id, attribute_id, reference_value) SELECT id, 1073, department_id FROM tmp_account_period;

UPDATE income_101 i SET account_period_id = (SELECT id FROM tmp_account_period tap WHERE tap.report_period_id = i.report_period_id AND tap.department_id = i.department_id);
UPDATE income_102 i SET account_period_id = (SELECT id from tmp_account_period tap WHERE tap.report_period_id = i.report_period_id AND tap.department_id = i.department_id);

INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE, ORD, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, WIDTH, REQUIRED, IS_UNIQUE, READ_ONLY) values (511, 50, '������������� ������� � ������������� ��', 'ACCOUNT_PERIOD_ID', 4, 11, 107, 1072, 1, 10, 1, 0, 0);
INSERT INTO REF_BOOK_ATTRIBUTE (ID, REF_BOOK_ID, NAME, ALIAS, TYPE, ORD, REFERENCE_ID, ATTRIBUTE_ID, VISIBLE, WIDTH, REQUIRED, IS_UNIQUE, READ_ONLY) values (527, 52, '������������� ������� � ������������� ��', 'ACCOUNT_PERIOD_ID', 4, 7, 107, 1072, 1, 10, 1, 0, 0);

DELETE FROM ref_book_attribute WHERE id IN (509, 501, 525, 520);

DROP TABLE tmp_account_period;	
ALTER TABLE income_101 MODIFY account_period_id NOT NULL;
ALTER TABLE income_102 MODIFY account_period_id NOT NULL;	
ALTER TABLE income_101 DROP COLUMN report_period_id;
ALTER TABLE income_101 DROP COLUMN department_id;
ALTER TABLE income_102 DROP COLUMN report_period_id;
ALTER TABLE income_102 DROP COLUMN department_id;

---------------------------------------------------------------------------------------------------------
commit;