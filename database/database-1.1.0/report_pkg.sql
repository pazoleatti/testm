create or replace package report_pkg as 

  -- Сконвертировать BLOB в CLOB
  function blob_to_clob (p_blob in blob) return clob;
  
  -- Получить количество ФЛ в отчетной форме НДФЛ-6
  function GetNDFL6PersCnt(p_declaration number) return number;

end report_pkg;
/
show errors;

create or replace package body report_pkg as

  --------------------------------------------------------------------------------
  -- Сконвертировать BLOB в CLOB
  --------------------------------------------------------------------------------
  function blob_to_clob (p_blob in blob) return clob
  is
  v_file_clob clob;
  v_file_size integer := 1024;
  v_dest_offset integer := 1;
  v_src_offset integer := 1;
  v_blob_csid number :=  nls_charset_id('CL8MSWIN1251');--dbms_lob.default_csid;
  v_lang_context number := dbms_lob.default_lang_ctx;
  v_warning integer;
  v_size integer := 0;
  
  begin
  
  dbms_lob.createtemporary(v_file_clob, true);
  
  select dbms_lob.getlength(p_blob) into v_size from dual;
  
  --if (v_size <= v_file_size) then v_file_size := v_size; end if;
  
  v_file_size := v_size;
  
  dbms_lob.converttoclob(v_file_clob,
  p_blob,
  v_file_size,
  v_dest_offset,
  v_src_offset,
  v_blob_csid,
  v_lang_context,
  v_warning);
  
  return v_file_clob;
  
  end;


  function GetNDFL6PersCnt(p_declaration number) return number 
  as
    v_result number:=null;
  begin
    select to_number(extractValue(xmltype(report_pkg.blob_to_clob(bd.data)),'/Файл/Документ/НДФЛ6/ОбобщПоказ/@КолФЛДоход')) into v_result
      from declaration_data_file ddf left join blob_data bd on (bd.id=ddf.blob_data_id)
     where declaration_data_id=p_declaration;

    return v_result;
  
  exception when others then
  
    return null;
  
  end getndfl6perscnt;

end report_pkg;
/
show errors;
