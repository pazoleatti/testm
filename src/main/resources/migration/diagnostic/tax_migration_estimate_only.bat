@echo off
rem %1 - пользователь, имеющий роль EXP_FULL_DATABASE (SELECT grantee FROM dba_role_privs WHERE granted_role='EXP_FULL_DATABASE';)
rem %2 - пароль пользователя
rem %3 - наименование инстанса БД
rem %4 - Oracle-директория для выгрузки (должна быть зарегистрирована на сервере БД).
if "%1"== "" (goto NotSpecified)
if "%2"== "" (goto NotSpecified)
if "%3"== "" (goto NotSpecified)

if not "%4"=="" (
	expdp %1/%2@%3 schemas=common,fact,metr,tr include=TABLE:\"IN (\'DEPARTMENT\',\'NSI_BANK_RATE\',\'NSI_CURRENCY\',\'NSI_DEPARTAX\',\'NSI_OKATO\',\'NSI_RATES\',\'NSI_SECURITY\',\'APP\',\'ROLES\',\'SEVERITIES\',\'USEREVENT\',\'USERLOG\',\'ACSLEVEL\',\'ASYSTEM\',\'DOP_TRAN\',\'ENTRANCEDIR\',\'EVENTGROUP\',\'EVENTS\',\'EXEMPLAR\',\'GENPARAM\',\'IN_FILE\',\'OBJ\',\'OBJDICT\',\'PERIODITY\',\'PERIODLIST\',\'PRELIMPROC\',\'PROVIDER\',\'SUBDICT\',\'TRANSACTION\',\'VEROBJ\',\'WAY\',\'TRD_25\',\'TRD_25M\',\'TRD_26\',\'TRD_26M\',\'TRD_27\',\'TRD_27M\',\'TRD_31\',\'TRD_31M\',\'TRD_51\',\'TRD_51M\',\'TRD_53\',\'TRD_53M\',\'TRD_54\',\'TRD_54M\',\'TRD_59\',\'TRD_59M\',\'TRD_60\',\'TRD_60M\',\'TRD_64\',\'TRD_64M\')\" directory=%4 content=data_only logfile=tax_migration_estimate_selected.txt estimate_only=y
	expdp %1/%2@%3 schemas=common,fact,metr,tr,user_check,app_rnu,commonadm,dbauser,oraform directory=%4 content=data_only logfile=tax_migration_estimate_all.txt estimate_only=y
	goto Done
) else (
expdp %1/%2@%3 schemas=common,fact,metr,tr include=TABLE:\"IN (\'DEPARTMENT\',\'NSI_BANK_RATE\',\'NSI_CURRENCY\',\'NSI_DEPARTAX\',\'NSI_OKATO\',\'NSI_RATES\',\'NSI_SECURITY\',\'APP\',\'ROLES\',\'SEVERITIES\',\'USEREVENT\',\'USERLOG\',\'ACSLEVEL\',\'ASYSTEM\',\'DOP_TRAN\',\'ENTRANCEDIR\',\'EVENTGROUP\',\'EVENTS\',\'EXEMPLAR\',\'GENPARAM\',\'IN_FILE\',\'OBJ\',\'OBJDICT\',\'PERIODITY\',\'PERIODLIST\',\'PRELIMPROC\',\'PROVIDER\',\'SUBDICT\',\'TRANSACTION\',\'VEROBJ\',\'WAY\',\'TRD_25\',\'TRD_25M\',\'TRD_26\',\'TRD_26M\',\'TRD_27\',\'TRD_27M\',\'TRD_31\',\'TRD_31M\',\'TRD_51\',\'TRD_51M\',\'TRD_53\',\'TRD_53M\',\'TRD_54\',\'TRD_54M\',\'TRD_59\',\'TRD_59M\',\'TRD_60\',\'TRD_60M\',\'TRD_64\',\'TRD_64M\')\" content=data_only logfile=tax_migration_estimate_selected.txt estimate_only=y
expdp %1/%2@%3 schemas=common,fact,metr,tr,user_check,app_rnu,commonadm,dbauser,oraform content=data_only logfile=tax_migration_estimate_all.txt estimate_only=y
goto Done
)

:NotSpecified
echo Error: Required parameters not set. Expected: "<login> <password> <DB instance> <directory>(optional)"

:Done
