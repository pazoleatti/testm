merge into tax_type t using (
  select 'N' id, 'НДФЛ' name from dual union all
  select 'F' id, 'Фонды и сборы' name from dual) s on
(t.id = s.id)
when not matched then
  insert (t.id, t.name) values (s.id, s.name)
when matched then
  update set t.name = s.name;
