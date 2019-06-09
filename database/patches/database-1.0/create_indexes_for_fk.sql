set serveroutput on;
declare
  v_table_name varchar2(128 char):='RASCHSV_OBYAZ_PLAT_SV';
  v_error varchar2(2000 char);
begin
  for chk in (select fk.constraint_name,fk.table_name,fkc.column_list,fk.delete_rule,ic.index_name,
                     case when ic.index_name is null then ' create index '||lower(fk.constraint_name)||' on '||lower(fk.table_name)||' ('||lower(fkc.column_list)||')' 
                          else ''
                     end sql_ind
                from user_constraints pk left join user_constraints fk on (fk.r_constraint_name=pk.constraint_name)
                                         left join (select constraint_name,table_name,cast(substr(col_list,1,length(col_list)-1) as varchar2(4000 char)) column_list
                                                      from (
                                                            select constraint_name,table_name,
                                                                   trim(xmlagg(xmlelement("c", column_name,', ').extract('//text()') order by constraint_name,position).getClobVal()) col_list
                                                             from user_cons_columns
                                                             group by constraint_name,table_name )
                                                   ) fkc on (fkc.constraint_name=fk.constraint_name)
                                         left join (select index_name,table_name,cast(substr(col_list,1,length(col_list)-1) as varchar2(4000 char)) column_list
                                                      from (
                                                            select index_name,table_name,
                                                                   trim(xmlagg(xmlelement("c", column_name,', ').extract('//text()') order by index_name,column_position).getClobVal()) col_list
                                                             from user_ind_columns
                                                             group by index_name,table_name)
                                                    ) ic on (ic.table_name=fk.table_name and ic.column_list=fkc.column_list)
               --where pk.table_name=upper(v_table_name)
               --  and pk.constraint_type='P'
                 where pk.constraint_type='P'
                   and fk.delete_rule='CASCADE'
   ) loop

    if chk.sql_ind is not null then
      begin
        execute immediate chk.sql_ind;
      exception when others then
        v_error:=sqlerrm;
        dbms_output.put_line(chk.sql_ind);
        dbms_output.put_line(v_error);
      end;
    end if;

  end loop;
end;
/