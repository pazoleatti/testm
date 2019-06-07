-- 3.6-snazin-4 https://jira.aplana.com/browse/SBRFNDFL-7096 Реализовать работу с журналом обмена с ЭДО
	     -- https://conf.aplana.com/pages/viewpage.action?pageId=47141084 "Транспортное сообщение" 

DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='create_drop_rename_tables block #1 - transport_message';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tables where table_name='TRANSPORT_MESSAGE';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'CREATE TABLE TRANSPORT_MESSAGE
		   (	ID NUMBER(18,0) NOT NULL ENABLE, 
			MESSAGE_UUID VARCHAR2(36 BYTE), 
			DATETIME DATE NOT NULL ENABLE, 
			TYPE NUMBER(1,0) NOT NULL ENABLE, 
			SENDER_SUBSYSTEM_ID NUMBER(19,0), 
			RECEIVER_SUBSYSTEM_ID NUMBER(19,0) NOT NULL ENABLE, 
			CONTENT_TYPE NUMBER(2,0) NOT NULL ENABLE, 
			STATE NUMBER(2,0) NOT NULL ENABLE, 
			BODY VARCHAR2(4000 BYTE), 
			BLOB_ID VARCHAR2(36 BYTE), 
			SOURCE_FILE_NAME VARCHAR2(255 BYTE), 
			INITIATOR_USER_ID NUMBER(9,0) NOT NULL ENABLE, 
			EXPLANATION VARCHAR2(4000 BYTE), 
			DECLARATION_ID NUMBER(18,0), 
			CONSTRAINT TRANSPORT_MESSAGE_PK PRIMARY KEY (ID) ENABLE, 
			CONSTRAINT TMESS_BLOB_ID_FK FOREIGN KEY (BLOB_ID) REFERENCES BLOB_DATA (ID) ENABLE,
			CONSTRAINT TMESS_INITIATOR_USER_ID_FK FOREIGN KEY (INITIATOR_USER_ID) REFERENCES SEC_USER (ID) ENABLE,
			CONSTRAINT TMESS_DECLARATION_ID_FK FOREIGN KEY (DECLARATION_ID) REFERENCES DECLARATION_DATA (ID) ENABLE,
			CONSTRAINT TMESS_TYPE_CK CHECK (type in (0, 1)) ENABLE, 
			CONSTRAINT TMESS_CONTENT_TYPE_CK CHECK (content_type between 0 and 13) ENABLE, 
			CONSTRAINT TMESS_STATE_CK CHECK (state between 1 and 5) ENABLE
		   )';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.ID IS ''Уникальный идентификатор сообщения''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.MESSAGE_UUID IS ''Уникальный идентификатор UUID, указанный в теле xml-сообщения; используется для связывания сообщения и технологической квитанции''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.DATETIME IS ''Дата и время сообщения''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.TYPE IS ''Направление движения сообщения (0 - исходящее, 1 - входящее)''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.SENDER_SUBSYSTEM_ID IS ''ID системы-отправителя''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.RECEIVER_SUBSYSTEM_ID IS ''ID системы-получателя''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.CONTENT_TYPE IS ''Тип данных в теле сообщения''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.STATE IS ''Статус обработки сообщения''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.BODY IS ''Тело сообщения''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.BLOB_ID IS ''Файл, который передавался через папку обмена''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.SOURCE_FILE_NAME IS ''Имя исходного файла, который отправлялся в ФНС''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.INITIATOR_USER_ID IS ''Инициатор создания сообщения (пользователь/система)''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.EXPLANATION IS ''Текст дополнительного пояснения''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN TRANSPORT_MESSAGE.DECLARATION_ID IS ''Ссылка на форму, с которой связано сообщение''';
		EXECUTE IMMEDIATE 'COMMENT ON TABLE TRANSPORT_MESSAGE  IS ''Транспортные сообщения для обмена между подсистемами АС УН''';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

COMMIT;

--3.7-dnovikov-7, 3.7-dnovikov-10 https://jira.aplana.com/browse/SBRFNDFL-5679 - рефакторинг настроек подразделений, новая таблица
--3.7-dnovikov-19 https://jira.aplana.com/browse/SBRFNDFL-7373 - Переименовать таблицу настроек подразделений и поправить вьюху
--3.7-dnovikov-25 https://jira.aplana.com/browse/SBRFNDFL-7709 Реализовать формирование xml файла  6-НДФЛ по закрывающимся КПП/ОКТМО


DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='create_drop_rename_tables block #2 - department_config';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tables where table_name='DEPARTMENT_CONFIG';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'CREATE TABLE department_config (
				ID                  NUMBER(18) NOT NULL,
				KPP                 VARCHAR2(9 CHAR) NOT NULL,
				OKTMO_ID            NUMBER(18,0) NOT NULL,
				start_date          DATE NOT NULL,
				end_date          	DATE,
				DEPARTMENT_ID       NUMBER(18) NOT NULL,
				TAX_ORGAN_CODE      VARCHAR2(4 CHAR) NOT NULL,
				TAX_ORGAN_CODE_MID  VARCHAR2(4 CHAR),
				present_place_id    NUMBER(18,0) NOT NULL,
				NAME                VARCHAR2(1000 CHAR),
				PHONE               VARCHAR2(20 CHAR),
				reorganization_id   NUMBER(18,0),
				REORG_INN           VARCHAR2(12 CHAR),
				REORG_KPP           VARCHAR2(9 CHAR),
				SIGNATORY_ID        NUMBER(18,0) NOT NULL,
				SIGNATORY_SURNAME   VARCHAR2(60 CHAR),
				SIGNATORY_FIRSTNAME VARCHAR2(60 CHAR),
				SIGNATORY_LASTNAME  VARCHAR2(60 CHAR),
				APPROVE_DOC_NAME    VARCHAR2(120 CHAR),
				APPROVE_ORG_NAME    VARCHAR2(1000 CHAR),
				reorg_successor_kpp varchar2(9 char),
				reorg_successor_name varchar2(1000 char),
				constraint dep_conf_pk primary key (id),
				constraint dep_conf_kpp_oktmo_st_date_uk unique (kpp, oktmo_id, start_date),
				constraint dep_conf_oktmo_fk FOREIGN KEY (oktmo_id) REFERENCES REF_BOOK_OKTMO (ID),
				constraint dep_conf_dep_fk FOREIGN KEY (DEPARTMENT_ID) REFERENCES DEPARTMENT (ID),
				constraint dep_conf_present_place_fk FOREIGN KEY (present_place_id) REFERENCES REF_BOOK_PRESENT_PLACE (ID),
				constraint dep_conf_reorg_fk FOREIGN KEY (reorganization_id) REFERENCES REF_BOOK_REORGANIZATION (ID),
				constraint dep_conf_sign_mark_fk FOREIGN KEY (SIGNATORY_ID) REFERENCES REF_BOOK_SIGNATORY_MARK (ID)
			)';
		EXECUTE IMMEDIATE 'create unique index dep_conf_kpp_ok_st_end_uidx on department_config(kpp, oktmo_id, start_date, end_date)';
		EXECUTE IMMEDIATE 'create index dep_conf_dep_st_end_idx on department_config(department_id, start_date, end_date)';
		EXECUTE IMMEDIATE 'create index dep_conf_dep_kpp_ok_st_end_idx on department_config(department_id, kpp, oktmo_id, start_date, end_date)';
		EXECUTE IMMEDIATE 'COMMENT ON TABLE department_config IS ''Настройки подразделений''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.ID IS ''Уникальный идентификатор''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.KPP IS ''КПП''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.OKTMO_ID IS ''ОКТМО''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.start_date IS ''Дата начала действия настройки''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.end_date IS ''Дата окончания действия настройки''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.DEPARTMENT_ID IS ''Код обособленного подразделения''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.TAX_ORGAN_CODE IS ''Код налогового органа конечного''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.TAX_ORGAN_CODE_MID IS ''Код налогового органа промежуточного''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.present_place_id IS ''Место, по которому представляется документ.''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.NAME IS ''Наименование для титульного листа''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.PHONE IS ''Номер контактного телефона''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.reorganization_id IS ''Код формы реорганизации и ликвидации''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.REORG_INN IS ''ИНН реорганизованного обособленного подразделения''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.REORG_KPP IS ''КПП реорганизованного обособленного подразделения''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.SIGNATORY_ID IS ''признак лица, подписавшего документ''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.SIGNATORY_SURNAME IS ''Фамилия подписанта''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.SIGNATORY_FIRSTNAME IS ''Имя подписанта''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.SIGNATORY_LASTNAME IS ''Отчество подписанта''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.APPROVE_DOC_NAME IS ''Наименование документа, подтверждающего полномочия''';
		EXECUTE IMMEDIATE 'COMMENT ON COLUMN department_config.APPROVE_ORG_NAME IS ''Наименование организации-представителя налогоплательщика''';
		EXECUTE IMMEDIATE 'comment on column department_config.reorg_successor_kpp is ''Код причины постановки организации по месту нахождения организации правопреемника''';
		EXECUTE IMMEDIATE 'comment on column department_config.reorg_successor_name is ''Наименование подразделения для титульного листа отчетных форм по реорганизованному подразделению''';


		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--3.7-dnovikov-14, 3.7-dnovikov-17 https://jira.aplana.com/browse/SBRFNDFL-7373 - Переименовать таблицу настроек подразделений и поправить вьюху
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='create_drop_rename_tables block #3 - REF_BOOK_NDFL';  
BEGIN
	select count(*) into v_run_condition from user_tables where table_name in ('REF_BOOK_NDFL_OLD', 'REF_BOOK_NDFL_DETAIL');
	IF v_run_condition=2 THEN
		execute immediate 'drop table REF_BOOK_NDFL_OLD';
		execute immediate 'rename REF_BOOK_NDFL_DETAIL to REF_BOOK_NDFL_DETAIL_OLD';
		dbms_output.put_line(v_task_name||'[INFO ]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--3.7-dnovikov-26 https://jira.aplana.com/browse/SBRFNDFL-7756 Добавить в систему "Справочник Расширяющие интервалы для загрузки данных

DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='create_drop_rename_tables block #3 - report_period_import';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tables where table_name='REPORT_PERIOD_IMPORT';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'create table report_period_import (
				id number(18) not null,
				record_id number(18) not null,
				report_period_type_id number(18) not null,
				period_start_date date not null,
				period_end_date date not null,
				asnu_id number(18) not null,
				version date not null,
				status number(1) default 0 not null,
				constraint report_period_import_pk primary key (id),
				constraint rep_per_ext_fk_rep_per_type foreign key (report_period_type_id) references report_period_type (id) on delete cascade,
				constraint rep_per_ext_fk_asnu foreign key (asnu_id) references ref_book_asnu (id) on delete cascade
			)';
		EXECUTE IMMEDIATE 'comment on table report_period_import is ''Справочник Дополнительные интервалы для загрузки данных''';
		EXECUTE IMMEDIATE 'comment on column report_period_import.id is ''Идентификатор версии записи''';
		EXECUTE IMMEDIATE 'comment on column report_period_import.record_id is ''Идентификатор записи''';
		EXECUTE IMMEDIATE 'comment on column report_period_import.report_period_type_id is ''Код периода''';
		EXECUTE IMMEDIATE 'comment on column report_period_import.period_start_date is ''Дата начала интервала''';
		EXECUTE IMMEDIATE 'comment on column report_period_import.period_end_date is ''Дата окончания интервала''';
		EXECUTE IMMEDIATE 'comment on column report_period_import.asnu_id is ''АСНУ''';
		EXECUTE IMMEDIATE 'comment on column report_period_import.version is ''Дата актуальности версии''';
		EXECUTE IMMEDIATE 'comment on column report_period_import.status is ''Статус записи (0 - обычная запись, -1 - удаленная, 2 - фиктивная)''';
		
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;
	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/


