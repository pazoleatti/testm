--http://jira.aplana.com/browse/SBRFACCTAX-12151: Исправить некорректные архивы по xml-файлам
create or replace and compile java source named "ZipBlob" as
import oracle.sql.BLOB;
import java.io.EOFException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
public class ZipBlob {
  public static BLOB compress(BLOB blob, String fileName)
    throws Exception {
    Connection con = DriverManager.getConnection("jdbc:default:connection:");
    BLOB result = BLOB.createTemporary(con, true, BLOB.DURATION_SESSION);
    ZipOutputStream out = new ZipOutputStream(result.getBinaryOutputStream());
    InputStream in = null;
    try {
      in = blob.getBinaryStream();
      out.putNextEntry(new ZipEntry(fileName));
      byte[] b = new byte[blob.getChunkSize()];
      int iCount;
      do {
        iCount = in.read(b);
        if (iCount != -1) {
          out.write(b, 0, iCount);
        }
      } while (iCount != -1);
    } catch (EOFException e) {
    } finally {
      if (in != null) {in.close();}
    }
    out.close();
    return result;
  }
}
/

create or replace package pck_zip as
 function blob_compress(
     p_source_blob blob
   , p_source_file_name varchar2
 ) return blob as language java name
   'ZipBlob.compress(oracle.sql.BLOB, java.lang.String) return oracle.sql.BLOB';
end;
/

set serveroutput on size 30000;
declare
   err_msg VARCHAR2(200);
   b_temp_file BLOB;
   b_compressed_file BLOB;
   cursor data_to_recompress is
          select  
              b.id, 
              b.name as name,
			  regexp_substr(b.name, '^(.{1,})\.zip$', 1, 1,'m',1)||'.xml' as xml_name,
              b.creation_date as initial_create_date, 
              to_date(case when extract(year from b.creation_date) < 100 then to_char(b.creation_date + interval '2000' year(4), 'DD.MM.YYYY') else to_char(b.creation_date, 'DD.MM.YYYY') end, 'DD.MM.YYYY') fix_create_date,
              rawtohex(DBMS_LOB.SUBSTR(BLOB_TO_CLOB(b.data), 4, 1)) first4bytes_hex,
			  length(b.data)/1024/1024 as length_Mb, 
              b.data
          from declaration_report r 
              join blob_data b on b.id = r.blob_data_id
          where r.type = 1 and b.name like '%zip%' and rawtohex(DBMS_LOB.SUBSTR(BLOB_TO_CLOB(b.data), 4, 1)) <> '504B0304'; 
 begin
   for x in data_to_recompress loop
        b_temp_file := UTL_COMPRESS.LZ_UNCOMPRESS(x.data);
        b_compressed_file := pck_zip.blob_compress(b_temp_file, x.xml_name);
        update blob_data bd set bd.creation_date = to_date(x.fix_create_date, 'DD.MM.YYYY'), bd.data = b_compressed_file where bd.id = x.id;
        dbms_output.put_line('Recompressed: id='||x.id||', name='||x.name ||' (created '||to_char(x.fix_create_date, 'DD.MM.YYYY')||')');
   end loop;        
 end;
/
commit;

drop package pck_zip;
drop java source "ZipBlob";

EXIT;