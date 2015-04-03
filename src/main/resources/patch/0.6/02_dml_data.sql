--http://jira.aplana.com/browse/SBRFACCTAX-10831
insert into configuration_lock (key, timeout) values ('FILE', 3600000);
insert into configuration_lock (key, timeout) values ('CONFIGURATION_PARAMS', 86400000);

-----------------------------------------------------------------------
commit;
exit;

