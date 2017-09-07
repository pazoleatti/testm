grant create synonym to &1;
grant select on &2..department to &1;
grant references on &2..department to &1;
grant select on &2..sec_role to &1;
grant references on &2..sec_role to &1;
grant select on &2..sec_user to &1;
grant references on &2..sec_user to &1;
grant select on &2..sec_user_role to &1;
grant references on &2..sec_user_role to &1;

commit;
exit;
