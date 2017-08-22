package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
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
    public static final Permission<TAUser> VIEW_TAXES_NDFL = new ViewTaxesNdflPermission(1 << 0);
    /**
     * Право доступа к пунктам меню:
     * "Налоги->НДФЛ->Ведение периодов"
     * "Налоги->НДФЛ->Настройки подразделений"
     * "Налоги->НДФЛ->Назначение форм"
     */
    public static final Permission<TAUser> VIEW_TAXES_NDFL_SETTINGS = new ViewTaxesNdflSettingsPermission(1 << 1);
    /**
     * Право доступа к пункту меню "Налоги->НДФЛ->Отчетность"
     */
    public static final Permission<TAUser> VIEW_TAXES_NDFL_REPORTS = new ViewTaxesNdflReportsPermission(1 << 2);
    /**
     * Право доступа к пункту меню "Налоги->Общие параметры"
     */
    public static final Permission<TAUser> VIEW_TAXES_GENERAL = new ViewTaxesGeneralPermission(1 << 3);
    /**
     * Право доступа к пунктам меню:
     * "Администрирование->Список блокировок"
     * "Администрирование->Журнал аудита"
     */
    public static final Permission<TAUser> VIEW_ADMINISTRATION_BLOCK_AND_AUDIT = new ViewAdministrationBlockAndAuditPermission(1 << 4);
    /**
     * Право доступа к пункту меню: "Администрирование->Список пользователей"
     */
    public static final Permission<TAUser> VIEW_ADMINISTRATION_USERS = new ViewAdministrationUsersPermission(1 << 5);
    /**
     * Право доступа к пунктам меню:
     * "Администрирование->Конфигурационные параметр"
     * "Администрирование->Планировщик задач"
     */
    public static final Permission<TAUser> VIEW_ADMINISTRATION_CONFIG = new ViewAdministrationConfigPermission(1 << 6);
    /**
     * Право доступа к пункту меню "Администрирование->Настройки" и всем подменю
     */
    public static final Permission<TAUser> VIEW_ADMINISTRATION_SETTINGS = new ViewAdministrationSettingsPermission(1 << 7);
    /**
     * Право доступа к пункту меню "Руководство пользователя"
     */
    public static final Permission<TAUser> VIEW_MANUAL_USER = new ViewManualUserPermission(1 << 8);
    /**
     * Право доступа к пункту меню "Руководство настройщика макетов"
     */
    public static final Permission<TAUser> VIEW_MANUAL_DESIGNER = new ViewManualDesignerPermission(1 << 9);
    /**
     * Право на просмотр журнала
     */
    public static final Permission<TAUser> VIEW_JOURNAL = new ViewJournalPermission(1 << 10);
    /**
     * Право на создание декларации вручную (журнал = отчетность)
     */
    public static final Permission<TAUser> CREATE_DECLARATION_REPORT = new CreateDeclarationReportPermission(1 << 11);
    /**
     * Право на создание декларации вручную (журнал = налоговые формы)
     */
    public static final Permission<TAUser> CREATE_DECLARATION_TAX = new CreateDeclarationTaxPermission(1 << 12);
    /**
     * Право на создание и выгрузку отчетности
     */
    public static final Permission<TAUser> CREATE_UPLOAD_REPORT = new CreateAndUploadReportPermission(1 << 13);
    /**
     * Право на обработку ТФ из каталога загрузки
     */
    public static final Permission<TAUser> HANDLING_FILE = new HandlingFilePermission(1 << 14);
    /**
     * Право на загрузку ТФ
     */
    public static final Permission<TAUser> UPLOAD_FILE = new UploadFilePermission(1 << 15);
    /**
     * Право на редактирование общих параметров
     */
    public static final Permission<TAUser> EDIT_GENERAL_PARAMS = new EditGeneralParamsPermission(1 << 16);
    /**
     * Право на просмотр справочников
     */
    public static final Permission<TAUser> VIEW_REF_BOOK = new ViewRefBookPermission(1 << 17);
    /**
     * Право на редактирование справочников
     */
    public static final Permission<TAUser> EDIT_REF_BOOK = new EditRefBookPermission(1 << 18);


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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
        }
    }

    /**
     * Право доступа к пунктам меню:
     * "Администрирование->Список блокировок"
     * "Администрирование->Журнал аудита"
     */
    public static final class ViewAdministrationBlockAndAuditPermission extends UserPermission {

        public ViewAdministrationBlockAndAuditPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER, TARole.N_ROLE_ADMIN);
        }
    }

    /**
     * Право доступа к пункту меню: "Администрирование->Список пользователей"
     */
    public static final class ViewAdministrationUsersPermission extends UserPermission {

        public ViewAdministrationUsersPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_ADMIN);
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_ADMIN);
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER, TARole.N_ROLE_ADMIN);
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }

    /**
     * Право на создание декларации вручную (журнал = налоговые формы)
     */
    public static final class CreateDeclarationTaxPermission extends UserPermission {

        public CreateDeclarationTaxPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_ADMIN);
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_ADMIN, TARole.N_ROLE_CONF);
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
        protected boolean isGrantedInternal(User currentUser, TAUser entity) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP);
        }
    }
}