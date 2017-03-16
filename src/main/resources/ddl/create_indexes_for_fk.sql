declare
  v_table_name varchar2(128 char):='RASCHSV_OBYAZ_PLAT_SV';
begin
  for chk in (select fk.constraint_name,fk.table_name,fkc.column_name,fk.delete_rule,ic.index_name,
                     case when ic.index_name is null then ' create index '||lower(fk.constraint_name)||' on '||lower(fk.table_name)||' ('||lower(fkc.column_name)||')' 
                          else ''
                     end sql_ind
                from user_constraints pk left join user_constraints fk on (fk.r_constraint_name=pk.constraint_name)
                                         left join user_cons_columns fkc on (fkc.constraint_name=fk.constraint_name)
                                         left join user_ind_columns ic on (ic.table_name=fk.table_name and ic.column_name=fkc.column_name)
               --where pk.table_name=upper(v_table_name)
               --  and pk.constraint_type='P'
                 where pk.constraint_type='P'
                   and fk.delete_rule='CASCADE'
   ) loop

    if chk.sql_ind is not null then
      execute immediate chk.sql_ind;
    end if;

  end loop;
end;
/