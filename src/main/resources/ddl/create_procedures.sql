create or replace function BLOB_TO_CLOB(v_blob_in in blob)
return clob is

v_file_clob clob;
v_file_size integer := 1024;
v_dest_offset integer := 1;
v_src_offset integer := 1;
v_blob_csid number :=  nls_charset_id('CL8MSWIN1251'); --dbms_lob.default_csid;
v_lang_context number := dbms_lob.default_lang_ctx;
v_warning integer;
v_size integer := 0;

begin

dbms_lob.createtemporary(v_file_clob, true);

select dbms_lob.getlength(v_blob_in) into v_size from dual;

if (v_size <= v_file_size) then v_file_size := v_size; end if;

dbms_lob.converttoclob(v_file_clob,
v_blob_in,
v_file_size,
v_dest_offset,
v_src_offset,
v_blob_csid,
v_lang_context,
v_warning);

return v_file_clob;

end;
/