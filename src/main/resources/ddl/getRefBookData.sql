CREATE OR REPLACE FUNCTION getRefBookData(refBookId in number, version date)
   RETURN SYS_REFCURSOR
AS
   o_cursor        SYS_REFCURSOR;
   vSQLselect      varchar2(4000);
   vSQLfrom        varchar2(4000);
   vSQLwhere       varchar2(4000);
   nCount          number:=1;
   sTYPE           varchar2(40);
BEGIN
  
vSQLselect:='with t as (select max(version) version, record_id from ref_book_record
                         where ref_book_id = '||refBookId||' and version <= to_date('||''''||to_char(version,'dd.mm.yyyy')||''''||','||''''||'dd.mm.yyyy'||''''||')group by record_id)
             select r.id as id';
vSQLfrom:=' from ref_book_record r join t on (r.version = t.version and r.record_id = t.record_id)';             
vSQLwhere:= ' where r.ref_book_id = '||refBookId||' and r.status <> -1';

for q in (select id, type
            from ref_book_attribute
            where ref_book_id=refBookId
            order by ord) 
loop  
  case q.type when 1 then sTYPE:='.string_value';
              when 2 then sTYPE:='.number_value';
              when 3 then sTYPE:='.date_value';
              when 4 then sTYPE:='.reference_value';
  end case; 
   
vSQLselect:=vSQLselect||', a'||nCOUNT||sTYPE;
vSQLfrom:=vSQLfrom||' left join ref_book_value a'||nCOUNT||' on a'||nCOUNT||'.record_id = r.id and a'||nCOUNT||'.attribute_id = '||q.id;
nCount:=nCount+1;
end loop;
   vSQLselect:=vSQLselect||vSQLfrom||vSQLwhere;
   OPEN o_cursor FOR vSQLselect;
   RETURN o_cursor;
END;
/
