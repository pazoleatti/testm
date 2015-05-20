-- http://jira.aplana.com/browse/SBRFACCTAX-11225: Реализовать в патче переход к архивированным xml файлам деклараций

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
declare
   err_msg VARCHAR2(200);
   b_temp_file BLOB;
   b_compressed_file BLOB;
   cursor data_to_compress is
          select id, xml_filename ||'.zip' as xml_filename, xml_date_create from
          (
            with t as
            (
            select  
              b.id, 
              b.name as initial_name,
              b.creation_date as initial_create_date, 
              length(b.data)/1024/1024 as length_Mb, 
              replace(translate(DBMS_LOB.SUBSTR(BLOB_TO_CLOB(b.data), 1024, 1), chr(10)||chr(11)||chr(13), '   '), '''', '"') as blob_fragment
              from declaration_report r 
                 join blob_data b on b.id = r.blob_data_id
            where r.type = 1 )
            select t.id, 
                   t.blob_fragment,
                   t.initial_name,
                   regexp_substr(t.blob_fragment, '^.*Файл ИдФайл=[''|"]([^"|'']*)[''|"].*', 1, 1,'m',1) as xml_filename,
                   coalesce(to_date(regexp_substr(t.blob_fragment, '^.*ДатаДок=[''|"]([^"|'']*)[''|"].*', 1, 1,'m',1), 'DD.MM.YYYY'), to_date(t.initial_create_date, 'DD.MM.YYYY')) as xml_date_create
                   from t 
                   ) parsed_xml
          where xml_filename is not null; 
   PRAGMA AUTONOMOUS_TRANSACTION;
begin  
   
   for i in data_to_compress loop
       -- Update blob_data with parsed info from its first 1Kb from XML
       update blob_data bd set bd.name = i.xml_filename, bd.creation_date = i.xml_date_create where bd.id = i.id;
       
       --Zip XML
       update blob_data set data = UTL_COMPRESS.LZ_COMPRESS(data) where id = i.id;
   end loop;

   COMMIT;
   
   --Handler for a really bad karma: all or nothing
  EXCEPTION
    WHEN OTHERS THEN
      err_msg := SUBSTR(SQLERRM, 1, 200);
      raise_application_error(-20001, err_msg);
      ROLLBACK;   
end;
/

COMMIT;

alter table blob_data enable row movement;
alter table blob_data shrink space cascade;
alter table blob_data disable row movement;

EXIT;
