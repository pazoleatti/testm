INSERT INTO declaration_subreport (id,declaration_template_id,name,ord,alias,blob_data_id,select_record) VALUES 
	(2001,200,'Сведения о ФЛ c параметрами',1,'person_rep_param','e88efec4-f3ab-4162-a36a-053b2004b14e',1);
INSERT INTO declaration_subreport (id,declaration_template_id,name,ord,alias,blob_data_id,select_record) VALUES 
	(2002,200,'Сводный отчет',1,'consolidated_report','3784d4da-a6f9-41c1-aec5-932d58bc2da6',0);	
	
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES 
	(20011,2001,'ИНН','inn',1,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES 
	(20012,2001,'СНИЛС','snils',2,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES 
	(20013,2001,'Фамилия','lastname',3,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES 
	(20014,2001,'Имя','name',4,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES 
	(20015,2001,'Отчество','middlename',5,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES 
	(20016,2001,'Дата рождения с','birthday_from',6,'D',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES 
	(20017,2001,'Дата рождения по','birthday_before',7,'D',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES 
	(20018,2001,'№ ДУЛ','doc',8,'S',null,null,0);	
	
COMMIT;
EXIT;