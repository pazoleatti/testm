-- 3.11-adudenko-01, 3.11-adudenko-03, 3.11-adudenko-04, 3.11-adudenko-05
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - Create tables for app2';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tables where TABLE_NAME='NDFL_APP2';
	IF v_run_condition=1 THEN
                execute immediate 'CREATE TABLE NDFL_APP2 (
         			   ID NUMBER(18,0) NOT NULL,
			           PERSON_ID NUMBER(18,0) NOT NULL,
			           TAX_RATE NUMBER(2,0) NOT NULL,
			           TOTAL_INCOME NUMBER(17,2) NOT NULL,
			           TOTAL_DEDUCTION NUMBER(17,2),
			           TAX_BASE NUMBER(17,2) NOT NULL,
			           CALCULATED_TAX NUMBER(15,0) NOT NULL,
			           WITHHOLDING_TAX NUMBER(15,0),
			           TRANSFERED_TAX NUMBER(15,0),
			           OVERHOLDING_TAX NUMBER(15,0),
			           NOT_HOLDING_TAX NUMBER(15,0),
				   DECLARATION_ID number(18,0) not null,
			           CONSTRAINT PK_NDFL_APP2 PRIMARY KEY (ID),
			           CONSTRAINT FK_NDFL_APP2_PERSON_ID FOREIGN KEY (PERSON_ID) REFERENCES REF_BOOK_PERSON(ID),
				   CONSTRAINT FK_NDFL_APP2_DECLARATION_ID foreign key (DECLARATION_ID) references DECLARATION_DATA(ID) ON DELETE CASCADE
        			)';
		execute immediate 'comment on column NDFL_APP2.DECLARATION_ID IS ''Ссылка на НФ''';
		execute immediate 'COMMENT ON COLUMN NDFL_APP2.ID IS ''Идентификатор''';
		execute immediate 'COMMENT ON COLUMN NDFL_APP2.PERSON_ID IS ''Ссылка на ФЛ''';
		execute immediate 'COMMENT ON COLUMN NDFL_APP2.TAX_RATE IS ''Налоговая ставка''';
		execute immediate 'COMMENT ON COLUMN NDFL_APP2.TOTAL_INCOME IS ''Общая сумма дохода''';
		execute immediate 'COMMENT ON COLUMN NDFL_APP2.TOTAL_DEDUCTION IS ''Общая сумма вычетов''';
		execute immediate 'COMMENT ON COLUMN NDFL_APP2.TAX_BASE IS ''Налоговая база''';
		execute immediate 'COMMENT ON COLUMN NDFL_APP2.CALCULATED_TAX IS ''Сумма налога исчисленная''';
		execute immediate 'COMMENT ON COLUMN NDFL_APP2.WITHHOLDING_TAX IS ''Сумма налога удержанная''';
		execute immediate 'COMMENT ON COLUMN NDFL_APP2.TRANSFERED_TAX IS ''Сумма налога перечисленная''';
		execute immediate 'COMMENT ON COLUMN NDFL_APP2.OVERHOLDING_TAX IS ''Сумма налога, излишне удержанная налоговым агентом''';
       		execute immediate 'COMMENT ON COLUMN NDFL_APP2.NOT_HOLDING_TAX IS ''Сумма налога, не удержанная налоговым агентом''';
		execute immediate 'COMMENT ON TABLE NDFL_APP2 IS ''Общие сведения Приложение 2''';

                execute immediate 'CREATE TABLE NDFL_APP2_INCOME (
            			   ID NUMBER(18,0) NOT NULL,
			           NDFL_APP2_ID NUMBER(18,0) NOT NULL,
        			   INCOME_CODE_33 VARCHAR2(4),
        			   INCOME_SUMM_34 NUMBER(17,2),
			           DEDUCTION_CODE_35 VARCHAR2(3),
			           DEDUCTION_SUMM_36 NUMBER(17,2),
            		           DEDUCTION_CODE_37 VARCHAR2(3),
			           DEDUCTION_SUMM_38 NUMBER(17,2),
        		           DEDUCTION_CODE_39 VARCHAR2(3),
        		           DEDUCTION_SUMM_40 NUMBER(17,2),
			           DEDUCTION_CODE_41 VARCHAR2(3),
			           DEDUCTION_SUMM_42 NUMBER(17,2),
            		           DEDUCTION_CODE_43 VARCHAR2(3),
			           DEDUCTION_SUMM_44 NUMBER(17,2),
        		           INCOME_CODE_45 VARCHAR2(4),
        		           INCOME_SUMM_46 NUMBER(17,2),
			           DEDUCTION_CODE_47 VARCHAR2(3),
			           DEDUCTION_SUMM_48 NUMBER(17,2),
            		           DEDUCTION_CODE_49 VARCHAR2(3),
			           DEDUCTION_SUMM_50 NUMBER(17,2),
        		           DEDUCTION_CODE_51 VARCHAR2(3),
        		           DEDUCTION_SUMM_52 NUMBER(17,2),
			           DEDUCTION_CODE_53 VARCHAR2(3),
			           DEDUCTION_SUMM_54 NUMBER(17,2),
            		           DEDUCTION_CODE_55 VARCHAR2(3),
			           DEDUCTION_SUMM_56 NUMBER(17,2),
        		           INCOME_CODE_57 VARCHAR2(4),
        		           INCOME_SUMM_58 NUMBER(17,2),
			           DEDUCTION_CODE_59 VARCHAR2(3),
			           DEDUCTION_SUMM_60 NUMBER(17,2),
            		           DEDUCTION_CODE_61 VARCHAR2(3),
			           DEDUCTION_SUMM_62 NUMBER(17,2),
        		           DEDUCTION_CODE_63 VARCHAR2(3),
        		           DEDUCTION_SUMM_64 NUMBER(17,2),
			           DEDUCTION_CODE_65 VARCHAR2(3),
			           DEDUCTION_SUMM_66 NUMBER(17,2),
            		           DEDUCTION_CODE_67 VARCHAR2(3),
			           DEDUCTION_SUMM_68 NUMBER(17,2),
        		           DEDUCTION_CODE_69 VARCHAR2(3),
        		           DEDUCTION_SUMM_70 NUMBER(17,2),
			           DEDUCTION_CODE_71 VARCHAR2(3),
			           DEDUCTION_SUMM_72 NUMBER(17,2),
			           CONSTRAINT PK_NDFL_APP2_INCOME PRIMARY KEY (ID),
			           CONSTRAINT FK_NDFL_APP2_NDFL_APP2_ID FOREIGN KEY (ID) REFERENCES NDFL_APP2 (ID) ON DELETE CASCADE
			        )';

		execute immediate 'COMMENT ON COLUMN NDFL_APP2_INCOME.ID IS ''Идентификатор''';
		execute immediate 'COMMENT ON COLUMN NDFL_APP2_INCOME.NDFL_APP2_ID IS ''Ссылка на Приложение 2''';
		execute immediate 'COMMENT ON TABLE NDFL_APP2_INCOME IS ''Коды и суммы доходов в Приложении 2''';

	        execute immediate 'CREATE INDEX IDX_NDFL_APP2_PERSON_ID ON NDFL_APP2 (PERSON_ID ASC) PCTFREE 30 INITRANS 5 COMPUTE STATISTICS';
	        execute immediate 'CREATE INDEX IDX_NDFL_APP2_NDFL_APP2_ID ON NDFL_APP2_INCOME (NDFL_APP2_ID ASC) PCTFREE 30 INITRANS 5 COMPUTE STATISTICS';
        	execute immediate 'CREATE INDEX IDX_NDFL_APP2_DD_APP2_INC_ID ON NDFL_APP2_DEDUCTION (NDFL_APP2_INCOME_ID ASC) PCTFREE 30 INITRANS 5 COMPUTE STATISTICS';

		dbms_output.put_line(v_task_name||'[INFO (form_type)]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING (form_type)]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/
commit;


/



