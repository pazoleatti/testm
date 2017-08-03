--ref_book
update ref_book set visible=0 where id in (940,941,939,2,938,925,937,25,26);
update ref_book set read_only = 1 where id = 922;

-- ref_book_present_place
update ref_book_ndfl_detail set present_place=null where present_place in (select id from ref_book_present_place where (for_fond=1 and for_ndfl=0 and code<>'335') or code='350');
update ref_book_fond_detail set present_place=null where present_place in (select id from ref_book_present_place where (for_fond=1 and for_ndfl=0 and code<>'335') or code='350');
delete from ref_book_present_place where (for_fond=1 and for_ndfl=0 and code<>'335') or code='350';

commit;

alter table ref_book_present_place drop (for_fond,for_ndfl);

--ref_book_form_type
update declaration_template set form_type=-1 where form_type in (select id from ref_book_form_type where tax_kind='F');
delete from ref_book_form_type where tax_kind='F';

commit;

alter table ref_book_form_type drop (tax_kind);

--report_period_type
update report_period set dict_tax_period_id=null 
 where dict_tax_period_id in (select id from report_period_type 
                               where code between '01' and '12' or code between '22' and '24' or code between '46' and '50' or
                                     code between '54' and '56' or code between '83' and '84');
delete from report_period_type 
 where code between '01' and '12' or code between '22' and '24' or code between '46' and '50' or
       code between '54' and '56' or code between '83' and '84';

delete from report_period_type where code not in ('21','31','33','34','51','52','53','90');

commit;

alter table report_period_type drop (n,f);

--sec_role
delete from sec_user_role where role_id in (select id from sec_role where alias like 'F_%');
delete from notification where role_id in (select id from sec_role where alias like 'F_%');
delete from role_event where role_id in (select id from sec_role where alias like 'F_%');

delete from sec_role where alias like 'F_%';

commit;

alter table sec_role drop (tax_type);

--declaration_type
delete from department_declaration_type where declaration_type_id in (select id from declaration_type where tax_type='F');
delete from declaration_template_file where declaration_template_id in (select id from declaration_template where declaration_type_id in (select id from declaration_type where tax_type='F'));
delete from declaration_data where declaration_template_id in (select id from declaration_template where declaration_type_id in (select id from declaration_type where tax_type='F'));
delete from declaration_template where declaration_type_id in (select id from declaration_type where tax_type='F');

delete from declaration_type where tax_type='F';

commit;

alter table declaration_type drop (tax_type) cascade constraints;

--ref_book_person
alter table ref_book_person drop (sex,pension,medical,social) cascade constraints;
alter table ref_book_id_doc drop (issued_by,issued_date) cascade constraints;

--declaration_kind
delete from declaration_template where form_kind in (select id from declaration_kind where id not in (2,3,7));
delete from declaration_kind where id not in (2,3,7);

commit;

--ref_book_attribute
delete from ref_book_attribute where id in (9243,9244,9313,27,28,843,2072);
delete from ref_book_attribute where id in (9044,9055,9056,9057,9023,9024);
update ref_book_attribute set visible=1 where id=9063;