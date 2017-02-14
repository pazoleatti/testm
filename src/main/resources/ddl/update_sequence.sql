Declare 
  v_id number;
  v_seq number;
Begin 

  Select max(id) into v_id from department;
  IF (v_id IS NOT NULL) THEN 
     Select seq_department.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_department INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_department.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_department INCREMENT BY 1';
     END IF;
  END IF;

  Select max(id) into v_id from tax_period;
  IF (v_id IS NOT NULL) THEN 
     Select seq_tax_period.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_tax_period INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_tax_period.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_tax_period INCREMENT BY 1';
     END IF;
  END IF;

  Select max(id) into v_id from ref_book_record;
  IF (v_id IS NOT NULL) THEN 
     Select seq_ref_book_record.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_ref_book_record INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_ref_book_record.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_ref_book_record INCREMENT BY 100';
     END IF;
  END IF;
  
  Select max(record_id) into v_id from ref_book_record;
  IF (v_id IS NOT NULL) THEN 
     Select seq_ref_book_record_row_id.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_ref_book_record_row_id INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_ref_book_record_row_id.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_ref_book_record_row_id INCREMENT BY 1';
     END IF;
  END IF;

  
  Select max(id) into v_id from report_period;
  IF (v_id IS NOT NULL) THEN 
     Select seq_report_period.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_report_period INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_report_period.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_report_period INCREMENT BY 1';
     END IF;
  END IF;

  Select max(id) into v_id from department_declaration_type;
  IF (v_id IS NOT NULL) THEN 
     Select seq_dept_declaration_type.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_dept_declaration_type INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_dept_declaration_type.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_dept_declaration_type INCREMENT BY 1';
     END IF;
  END IF;

  Select max(id) into v_id from declaration_template;
  IF (v_id IS NOT NULL) THEN 
     Select seq_declaration_template.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_declaration_template INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_declaration_template.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_declaration_template INCREMENT BY 1';
     END IF;
  END IF;

  Select max(id) into v_id from declaration_data;
  IF (v_id IS NOT NULL) THEN 
     Select seq_declaration_data.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_declaration_data INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_declaration_data.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_declaration_data INCREMENT BY 1';
     END IF;
  END IF;
  

  Select max(id) into v_id from department_form_type;
  IF (v_id IS NOT NULL) THEN 
     Select seq_department_form_type.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_department_form_type INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_department_form_type.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_department_form_type INCREMENT BY 1';
     END IF;
  END IF;

  Select max(id) into v_id from sec_user;
  IF (v_id IS NOT NULL) THEN 
     Select seq_sec_user.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_sec_user INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_sec_user.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_sec_user INCREMENT BY 1';
     END IF;
  END IF;

  Select max(id) into v_id from log_business;
  IF (v_id IS NOT NULL) THEN 
     Select seq_log_business.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_log_business INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_log_business.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_log_business INCREMENT BY 1';
     END IF;
  END IF;

  Select max(id) into v_id from log_system;
  IF (v_id IS NOT NULL) THEN 
     Select seq_log_system.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_log_system INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_log_system.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_log_system INCREMENT BY 1';
     END IF;
  END IF;
  
  Select max(id) into v_id from task_context;
  IF (v_id IS NOT NULL) THEN 
     Select seq_task_context.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_task_context INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_task_context.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_task_context INCREMENT BY 1';
     END IF;
  END IF;
  
  Select max(id) into v_id from ref_book_oktmo;
  if (v_id is not null) then 
    Select seq_ref_book_oktmo.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_ref_book_oktmo INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_ref_book_oktmo.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_ref_book_oktmo INCREMENT BY 100';
     END IF;
  end if;

  Select max(id) into v_id from template_changes ;
  if (v_id is not null) then 
    Select seq_template_changes .NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_template_changes  INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_template_changes.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_template_changes  INCREMENT BY 1';
     END IF;
  end if;

  Select max(id) into v_id from notification;
  if (v_id is not null) then 
    Select seq_notification.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_notification  INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_notification.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_notification  INCREMENT BY 1';
     END IF;
  end if;

  Select max(id) into v_id from declaration_type;
  if (v_id is not null) then 
    Select seq_declaration_type.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_declaration_type INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_declaration_type.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_declaration_type INCREMENT BY 1';
     END IF;
  end if;
  
  Select max(id) into v_id from department_form_type;
  if (v_id is not null) then 
    Select seq_department_form_type.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_department_form_type INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_department_form_type.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_department_form_type INCREMENT BY 1';
     END IF;
  end if;

  Select max(id) into v_id from department_report_period;
  if (v_id is not null) then 
    Select seq_department_report_period.NEXTVAL into v_seq from dual;
     IF (v_id > v_seq) THEN         
        execute immediate 'alter sequence seq_department_report_period INCREMENT BY '||TO_CHAR(v_id-v_seq); 
        Select seq_department_report_period.NEXTVAL into v_seq from dual;
        execute immediate 'Alter sequence seq_department_report_period INCREMENT BY 1';
     END IF;
  end if;
End;

.
run;
commit;
exit;