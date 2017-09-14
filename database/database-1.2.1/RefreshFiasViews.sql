create or replace procedure RefreshFiasViews
is
 v_count number;
begin
  select count(1) into v_count from user_indexes where index_name='IDX_FIAS_ADDR_CURRST_AOLEV';
  if v_count=0 then
    execute immediate 'CREATE INDEX IDX_FIAS_ADDR_CURRST_AOLEV ON FIAS_ADDROBJ (CURRSTATUS ASC, AOLEVEL ASC, REPLACE(LOWER(FORMALNAME), '' '', '''') ASC) ';
  end if;
  
  dbms_mview.REFRESH('MV_FIAS_AREA_ACT', 'C');
  dbms_mview.REFRESH('MV_FIAS_CITY_ACT', 'C');
  dbms_mview.REFRESH('MV_FIAS_LOCALITY_ACT', 'C');
  dbms_mview.REFRESH('MV_FIAS_STREET_ACT', 'C');
  
  select count(1) into v_count from user_indexes where index_name='IDX_FIAS_ADDR_CURRST_AOLEV';
  if v_count>0 then
    execute immediate 'drop index IDX_FIAS_ADDR_CURRST_AOLEV';
  end if;
end RefreshFiasViews;
/