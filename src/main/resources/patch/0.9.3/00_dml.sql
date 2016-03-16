--https://jira.aplana.com/browse/SBRFACCTAX-15006: Поменять form_type.code для 6-к

update form_type set code = '6.1' where id = 816;
update form_type set code = '6.10.1' where id = 823;
update form_type set code = '6.10.2' where id = 825;
update form_type set code = '6.11' where id = 827;
update form_type set code = '6.12' where id = 819;
update form_type set code = '6.13' where id = 826;
update form_type set code = '6.14' where id = 835;
update form_type set code = '6.15' where id = 837;
update form_type set code = '6.16' where id = 839;
update form_type set code = '6.17' where id = 811;
update form_type set code = '6.18' where id = 838;
update form_type set code = '6.19' where id = 828;
update form_type set code = '6.2' where id = 804;
update form_type set code = '6.20' where id = 831;
update form_type set code = '6.21' where id = 830;
update form_type set code = '6.22' where id = 834;
update form_type set code = '6.23' where id = 832;
update form_type set code = '6.24' where id = 833;
update form_type set code = '6.25' where id = 836;
update form_type set code = '6.3' where id = 812;
update form_type set code = '6.4' where id = 813;
update form_type set code = '6.5' where id = 814;
update form_type set code = '6.6' where id = 806;
update form_type set code = '6.7' where id = 805;
update form_type set code = '6.8' where id = 815;
update form_type set code = '6.9' where id = 817;

update form_type set name = '6.10.1. Предоставление гарантий' where id = 823;
update form_type set name = '6.10.2. Предоставление инструментов торгового финансирования и непокрытых аккредитивов' where id = 825;

COMMIT;
EXIT;