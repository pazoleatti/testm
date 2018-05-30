begin
  for rec in (select view_name from user_views)
  loop
    execute immediate 'alter view '||rec.view_name||' compile';
  end loop;
end;
/

exit;
