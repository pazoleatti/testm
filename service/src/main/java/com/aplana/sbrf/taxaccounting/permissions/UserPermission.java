package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

/**
 * Реализация прав для пользователя.
 */
@Configurable
public abstract class UserPermission extends AbstractPermission<TAUser> {
    @Autowired
    protected TAUserService taUserService;

    /**
     * Право доступа к пункту меню "Налоги->НДФЛ->Формы"
     */
    public static final Permission<TAUser> VIEW_TAXES_NDFL = new ViewTaxesNdflPermission(1L << 0);
    /**
     * Право доступа к пунктам меню:
     * "Налоги->НДФЛ->Ведение периодов"
     * "Налоги->НДФЛ->Настройки подразделений"
     * "Налоги->НДФЛ->Назначение форм"
     */
    public static final Permission<TAUser> VIEW_TAXES_NDFL_SETTINGS = new ViewTaxesNdflSettingsPermission(1L << 1);
    /**
     * Право доступа к пункту меню "Налоги->НДФЛ->Отчетность"
     */
    public static final Permission<TAUser> VIEW_TAXES_NDFL_REPORTS = new ViewTaxesNdflReportsPermission(1L << 2);
    /**
     * Право доступа к пункту меню "Налоги->Общие параметры"
     */
    public static final Permission<TAUser> VIEW_TAXES_GENERAL = new ViewTaxesGeneralPermission(1L << 3);
    /**
     * Право доступа к пунктам меню:
     * "Администрирование->Список блокировок"
     * "Администрирование->Асинхронные задачи"
     */
    public static final Permission<TAUser> VIEW_ADMINISTRATION_BLOCK = new ViewAdministrationBlockAndAsyncPermission(1L << 4);
    /**
     * Право доступа к пунктам меню:
     * "Администрирование->Конфигурационные параметр"
     * "Администрирование->Планировщик задач"
     */
    public static final Permission<TAUser> VIEW_ADMINISTRATION_CONFIG = new ViewAdministrationConfigPermission(1L << 5);
    /**
     * Право доступа к пункту меню "Администрирование->Настройки" и всем подменю
     */
    public static final Permission<TAUser> VIEW_ADMINISTRATION_SETTINGS = new ViewAdministrationSettingsPermission(1L << 6);
    /**
     * Право доступа к пункту меню "Руководство пользователя"
     */
    public static final Permission<TAUser> VIEW_MANUAL_USER = new ViewManualUserPermission(1L << 7);
    /**
     * Право доступа к пункту меню "Руководство настройщика макетов"
     */
    public static final Permission<TAUser> VIEW_MANUAL_DESIGNER = new ViewManualDesignerPermission(1L << 8);
    /**
     * Право на просмотр журнала
     */
    public static final Permission<TAUser> VIEW_JOURNAL = new ViewJournalPermission(1L << 9);
    /**
     * Право на создание декларации вручную (журнал = отчетность)
     */
    public static final Permission<TAUser> CREATE_DECLARATION_REPORT = new CreateDeclarationReportPermission(1L << 10);
    /**
     * Право на создание первичной формы вручную (журнал = налоговые формы)
     */
    public static final Permission<TAUser> CREATE_DECLARATION_PRIMARY = new CreatePrimaryDeclarationPermission(1L << 11);

    /**
     * Право на создание консолидированный формы вручную (журнал = налоговые формы)
     */
    public static final Permission<TAUser> CREATE_DECLARATION_CONSOLIDATED = new CreateConsolidatedDeclarationPermission(1L << 12);
    /**
     * Право на создание и выгрузку отчетности
     */
    public static final Permission<TAUser> CREATE_UPLOAD_REPORT = new CreateAndUploadReportPermission(1L << 13);

    /**
     * Право на обработку ТФ из каталога загрузки
     */
    public static final Permission<TAUser> HANDLING_FILE = new HandlingFilePermission(1L << 14);
    /**
     * Право на загрузку ТФ
     */
    public static final Permission<TAUser> UPLOAD_FILE = new UploadFilePermission(1L << 15);
    /**
     * Право на редактирование общих параметров
     */
    public static final Permission<TAUser> EDIT_GENERAL_PARAMS = new EditGeneralParamsPermission(1L << 16);
    /**
     * Право на просмотр справочников
     */
    public static final Permission<TAUser> VIEW_REF_BOOK = new ViewRefBookPermission(1L << 17);
    /**
     * Право на редактирование справочников
     */
    public static final Permission<TAUser> EDIT_REF_BOOK = new EditRefBookPermission(1L << 18);
    /**
     * Право доступа к пункту меню "НСИ"
     */
    public static final Permission<TAUser> VIEW_NSI = new ViewNsiPermission(1L << 19);
    /**
     * Право доступа к пункту меню "Налоги"
     */
    public static final Permission<TAUser> VIEW_TAXES = new ViewTaxesPermission(1L << 20);
    /**
     * Право доступа к пунктам меню:
     * "Администрирование->Список пользователей"
     */
    public static final Permission<TAUser> VIEW_ADMINISTRATION_USERS = new ViewAdministrationUsersPermission(1L << 21);

    /**
     * Право "Назначение форм > Редактирование"
     */
    public static final Permission<TAUser> EDIT_DECLARATION_TYPES_ASSIGNMENT = new EditDeclarationTypesAssignmentPermission(1L << 22);

    /**
     * Право "Ведение периодов > Открыть период"
     */
    public static final Permission<TAUser> OPEN_DEPARTMENT_REPORT_PERIOD = new OpenDepartmentReportPeriodPermission(1L << 23);

    /**
     * Право доступа к редактированию пунктов меню:
     * "Администрирование->Конфигурационные параметр"
     * "Администрирование->Планировщик задач"
     */
    public static final Permission<TAUser> EDIT_ADMINISTRATION_CONFIG = new EditAdministrationConfigPermission(1L << 24);

    /**
     * Право "Налоги > Сервис"
     */
    public static final Permission<TAUser> VIEW_TAXES_SERVICE = new ViewTaxesServicePermission(1L << 25);

    /**
     * Право доступа к пункту меню "Налоги - Создание файла приложения 2"
     */
    public static final Permission<TAUser> VIEW_TAXES_CREATE_APPLICATION_2 = new ViewTaxesCreateApplication2Permission(1L << 26);

    /**
     * Право на создание записи настроек подразделений
     */
    public static final Permission<TAUser> CREATE_DEPARTMENT_CONFIG = new CreateDepartmentConfigPermission(1L << 27);

    /**
     * Право на выгрузку реестра ФЛ
     */
    public static final Permission<TAUser> EXPORT_PERSONS = new ExportPersonsPermission(1L << 28);

    /**
     * Право на создание записи настроек подразделений
     */
    public static final Permission<TAUser> EXPORT_DEPARTMENT_CONFIG = new ExportDepartmentConfigPermission(1L << 29);

    /**
     * Право на создание записи настроек подразделений
     */
    public static final Permission<TAUser> IMPORT_DEPARTMENT_CONFIG = new ImportDepartmentConfigPermission(1L << 30);

    /**
     * Право доступа к пункту меню "Администрирование"
     */
    public static final Permission<TAUser> VIEW_ADMINISTRATION = new ViewAdministrationPermission(1L << 31);

    /**
     * Право доступа к формированию Уведомления о неудержанном налоге
     */
    public static final Permission<TAUser> TAX_NOTIFICATION = new TaxNotificationPermission(1L << 32);
    /**
     * Право на создание первичной формы вручную (журнал = налоговые формы)
     */
    public static final Permission<TAUser> _2NDFL_FL = new _2NdflFlPermission(1L << 33);

    public UserPermission(long mask) {
        super(mask);
    }

    /**
     * Право доступа к пункту меню "Налоги->НДФЛ->Формы"
     */
    public static final class ViewTaxesNdflPermission extends UserPermission {

        public ViewTaxesNdflPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
        }
    }

    /**
     * Право доступа к пунктам меню:
     * "Налоги->НДФЛ->Ведение периодов"
     * "Налоги->НДФЛ->Настройки подразделений"
     * "Налоги->НДФЛ->Назначение форм"
     */
    public static final class ViewTaxesNdflSettingsPermission extends UserPermission {

        public ViewTaxesNdflSettingsPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
        }
    }

    /**
     * Право "Назначение форм > Редактирование"
     */
    public static final class EditDeclarationTypesAssignmentPermission extends UserPermission {

        public EditDeclarationTypesAssignmentPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }

    /**
     * Право доступа к пункту меню "Налоги->НДФЛ->Отчетность"
     */
    public static final class ViewTaxesNdflReportsPermission extends UserPermission {

        public ViewTaxesNdflReportsPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }

    /**
     * Право доступа к пункту меню "Налоги->Общие параметры"
     */
    public static final class ViewTaxesGeneralPermission extends UserPermission {

        public ViewTaxesGeneralPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
        }
    }

    /**
     * Право доступа к пунктам меню:
     * "Администрирование->Список блокировок"
     * "Администрирование->Журнал аудита"
     */
    public static final class ViewAdministrationBlockAndAsyncPermission extends UserPermission {

        public ViewAdministrationBlockAndAsyncPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER, TARole.ROLE_ADMIN);
        }
    }

    /**
     * Право доступа к пунктам меню:
     * "Администрирование->Список пользователей"
     */
    public static final class ViewAdministrationUsersPermission extends UserPermission {

        public ViewAdministrationUsersPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.ROLE_ADMIN);
        }
    }

    /**
     * Право доступа к пунктам меню:
     * "Администрирование->Конфигурационные параметр"
     * "Администрирование->Планировщик задач"
     */
    public static final class ViewAdministrationConfigPermission extends UserPermission {

        public ViewAdministrationConfigPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.ROLE_ADMIN);
        }
    }

    /**
     * Право доступа к пункту меню "Администрирование->Настройки" и всем подменю
     */
    public static final class ViewAdministrationSettingsPermission extends UserPermission {

        public ViewAdministrationSettingsPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONF);
        }
    }

    /**
     * Право доступа к пункту меню "Руководство пользователя"
     */
    public static final class ViewManualUserPermission extends UserPermission {

        public ViewManualUserPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER, TARole.ROLE_ADMIN);
        }
    }

    /**
     * Право доступа к пункту меню "Руководство настройщика макетов"
     */
    public static final class ViewManualDesignerPermission extends UserPermission {

        public ViewManualDesignerPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONF);
        }
    }

    /**
     * Право на просмотр журнала
     */
    public static final class ViewJournalPermission extends UserPermission {

        public ViewJournalPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
        }
    }

    /**
     * Право на создание декларации вручную (журнал = отчетность)
     */
    public static final class CreateDeclarationReportPermission extends UserPermission {

        public CreateDeclarationReportPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }

    /**
     * Право на создание консолидированной формы вручную (журнал = налоговые формы)
     */
    public static final class CreateConsolidatedDeclarationPermission extends UserPermission {

        public CreateConsolidatedDeclarationPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }

    /**
     * Право на создание первичной формы вручную (журнал = налоговые формы)
     */
    public static final class CreatePrimaryDeclarationPermission extends UserPermission {

        public CreatePrimaryDeclarationPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_OPER, TARole.N_ROLE_CONTROL_NS);
        }
    }

    /**
     * Право на создание и выгрузку отчетности
     */
    public static final class CreateAndUploadReportPermission extends UserPermission {

        public CreateAndUploadReportPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }

    /**
     * Право на обработку ТФ из каталога загрузки
     */
    public static final class HandlingFilePermission extends UserPermission {

        public HandlingFilePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.ROLE_ADMIN);
        }
    }

    /**
     * Право на загрузку ТФ
     */
    public static final class UploadFilePermission extends UserPermission {

        public UploadFilePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
        }
    }

    /**
     * Право на редактирование общих параметров
     */
    public static final class EditGeneralParamsPermission extends UserPermission {

        public EditGeneralParamsPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP);
        }
    }

    /**
     * Право на просмотр справочников
     */
    public static final class ViewRefBookPermission extends UserPermission {

        public ViewRefBookPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.ROLE_ADMIN, TARole.N_ROLE_CONF);
        }
    }

    /**
     * Право на редактирование справочников
     */
    public static final class EditRefBookPermission extends UserPermission {

        public EditRefBookPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP);
        }
    }

    /**
     * Право доступа к пункту меню "НСИ"
     */
    public static final class ViewNsiPermission extends UserPermission {

        public ViewNsiPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_OPER, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP);
        }
    }

    /**
     * Право доступа к пункту меню "Налоги"
     */
    public static final class ViewTaxesPermission extends UserPermission {

        public ViewTaxesPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_OPER, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP, TARole.ROLE_ADMIN, TARole.N_ROLE_OPER_NOTICE);
        }
    }

    /**
     * Право "Ведение периодов > Открыть период"
     */
    public static final class OpenDepartmentReportPeriodPermission extends UserPermission {

        public OpenDepartmentReportPeriodPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }

    /**
     * Право доступа к редактированию пунктов меню:
     * "Администрирование->Конфигурационные параметр"
     * "Администрирование->Планировщик задач"
     */
    public static final class EditAdministrationConfigPermission extends UserPermission{
        public EditAdministrationConfigPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, TAUser targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(user, TARole.ROLE_ADMIN);
        }
    }

    /**
     * Право "Налоги > Сервис"
     */
    public static final class ViewTaxesServicePermission extends UserPermission {

        public ViewTaxesServicePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_OPER, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP, TARole.ROLE_ADMIN);
        }
    }

    public static final class ViewTaxesCreateApplication2Permission extends UserPermission {

        public ViewTaxesCreateApplication2Permission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, TAUser targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP);
        }
    }

    public static final class CreateDepartmentConfigPermission extends UserPermission {

        public CreateDepartmentConfigPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, TAUser targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }

    public static final class ExportPersonsPermission extends UserPermission {

        public ExportPersonsPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, TAUser targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
        }
    }

    public static final class ExportDepartmentConfigPermission extends UserPermission {

        public ExportDepartmentConfigPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, TAUser targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
        }
    }

    public static final class ImportDepartmentConfigPermission extends UserPermission {

        public ImportDepartmentConfigPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, TAUser targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }

    public static final class ViewAdministrationPermission extends UserPermission {

        public ViewAdministrationPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, TAUser targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(user, TARole.ROLE_ADMIN, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER, TARole.N_ROLE_CONF);
        }
    }

    public static final class TaxNotificationPermission extends UserPermission {

        public TaxNotificationPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, TAUser targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_OPER_NOTICE);
        }
    }

    public static final class _2NdflFlPermission extends UserPermission {

        public _2NdflFlPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, TAUser targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP);
        }
    }
}