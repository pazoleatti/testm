INSERT INTO declaration_subreport (id,declaration_template_id,name,ord,alias,blob_data_id,select_record) VALUES 
	(1001,100,'Сведения о ФЛ c параметрами',1,'rnu_ndfl_person','03f6d78d-bfd7-45bb-a16b-6c89131b76d1',1);
INSERT INTO declaration_subreport (id,declaration_template_id,name,ord,alias,blob_data_id,select_record) VALUES 
	(1011,101,'Сведения о ФЛ c параметрами',1,'rnu_ndfl_person','042c50fc-a590-46b5-bb50-d04cc3a9424a',1);
INSERT INTO declaration_subreport (id,declaration_template_id,name,ord,alias,blob_data_id,select_record) VALUES 
	(1021,102,'2-НДФЛ 1',1,'report_2ndfl','047f207c-113e-488d-8390-9afc248a3bc8',0);	
INSERT INTO declaration_subreport (id,declaration_template_id,name,ord,alias,blob_data_id,select_record) VALUES 
	(1041,104,'2-НДФЛ 2',1,'report_2ndfl','04d9b114-1782-4d09-ad88-729e5605c6ff',0);	
-- РНУ первичная	
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10001,1001,'№ ДУЛ','idDocNumber',9,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10002,1001,'Дата рождения по','toBirthDay',8,'D',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10003,1001,'Дата рождения с','fromBirthDay',7,'D',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10004,1001,'Имя','firstName',2,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10005,1001,'ИНН','inn',5,'N',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10006,1001,'ИНП','inp',6,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10007,1001,'Отчество','middleName',3,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10008,1001,'СНИЛС','snils',4,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10009,1001,'Фамилия','lastName',1,'S',null,null,0);
-- РНУ консолидированная	
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10011,1011,'№ ДУЛ','idDocNumber',9,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10012,1011,'Дата рождения по','toBirthDay',8,'D',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10013,1011,'Дата рождения с','fromBirthDay',7,'D',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10014,1011,'Имя','firstName',2,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10015,1011,'ИНН','inn',5,'N',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10016,1011,'ИНП','inp',6,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10017,1011,'Отчество','middleName',3,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10018,1011,'СНИЛС','snils',4,'S',null,null,0);
INSERT INTO declaration_subreport_params (id,declaration_subreport_id,name,alias,ord,type,filter,attribute_id,required) VALUES
	(10019,1011,'Фамилия','lastName',1,'S',null,null,0);	

	
COMMIT;
EXIT;