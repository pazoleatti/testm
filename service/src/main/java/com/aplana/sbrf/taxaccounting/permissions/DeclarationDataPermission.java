package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * Реализация прав для декларации.
 */
@Configurable
public abstract class DeclarationDataPermission extends AbstractPermission<DeclarationData> {

    @Autowired
    protected DeclarationTemplateDao declarationTemplateDao;
    @Autowired
    protected DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    protected DepartmentService departmentService;
    @Autowired
    protected TAUserService taUserService;

    /**
     * Право на создание декларации вручную
     */
    public static final Permission<DeclarationData> CREATE = new CreatePermission(1 << 0);
    /**
     * Право на просмотр декларации
     */
    public static final Permission<DeclarationData> VIEW = new ViewPermission(1 << 1);
    /**
     * Право на расчет декларации
     */
    public static final Permission<DeclarationData> CALCULATE = new CalculatePermission(1 << 2);
    /**
     * Право на проверку декларации
     */
    public static final Permission<DeclarationData> CHECK = new CheckPermission(1 << 3);
    /**
     * Право на принятие декларации
     */
    public static final Permission<DeclarationData> ACCEPTED = new AcceptedPermission(1 << 4);
    /**
     * Право на удаление декларации
     */
    public static final Permission<DeclarationData> DELETE = new DeletePermission(1 << 5);
    /**
     * Право на возврат декларации в статус "Создана"
     */
    public static final Permission<DeclarationData> RETURN_TO_CREATED = new ReturnToCreatedPermission(1 << 6);
    /**
     * Право на редактирование при назначении деклараций
     */
    public static final Permission<DeclarationData> EDIT_ASSIGNMENT = new EditAssignmentPermission(1 << 7);

    /**
     * Право на выгрузку отчетных форм
     */
    public static final Permission<DeclarationData> DOWNLOAD_REPORTS = new DownloadReportsPermission(1 << 8);

    /**
     * Право на формирование печатной формы
     */
    public static final Permission<DeclarationData> SHOW = new ShowPermission(1 << 9);

    /**
     * Право на загрузку Excel-файла в форму
     */
    public static final Permission<DeclarationData> IMPORT_EXCEL = new ImportExcelPermission(1 << 10);

    public DeclarationDataPermission(long mask) {
        super(mask);
    }

    /**
     * Право на создание декларации вручную
     */
    public static final class CreatePermission extends DeclarationDataPermission {

        public CreatePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject) {
            DeclarationTemplate declarationTemplate = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId());
            DeclarationFormKind declarationFormKind = declarationTemplate.getDeclarationFormKind();
            // Если тип формы "Консолидированная" или "Отчетная", то
            // Пользователю назначена хотя бы одна из ролей: Контролёр УНП(НДФЛ), Контролер НС(НДФЛ)
            if (declarationFormKind.equals(DeclarationFormKind.CONSOLIDATED) || declarationFormKind.equals(DeclarationFormKind.REPORTS)) {
                if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS)) {
                    return true;
                }
            }
            // Если тип формы "Первичная", то
            // Пользователю назначена хотя бы одна из ролей: Оператор (НДФЛ), Контролёр УНП(НДФЛ), Контролер НС(НДФЛ)
            else if (declarationFormKind.equals(DeclarationFormKind.PRIMARY)) {
                if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Право на просмотр декларации
     */
    public static final class ViewPermission extends DeclarationDataPermission {

        public ViewPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject) {
            // Выборка для доступа к экземплярам деклараций
            // http://conf.aplana.com/pages/viewpage.action?pageId=11380670

            // Контролёр УНП может просматривать все декларации
            if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP)) {
                return true;
            }

            TAUser taUser = taUserService.getUser(currentUser.getUsername());
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(targetDomainObject.getDepartmentReportPeriodId());
            DeclarationTemplate declarationTemplate = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId());
            Long asnuId = targetDomainObject.getAsnuId();

            //Подразделение формы
            Department declarationDepartment = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());

            // Контролёр НС
            if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_NS)) {
                //ТБ формы
                int declarationTB = departmentService.getParentTB(declarationDepartment.getId()).getId();
                //Подразделение и ТБ пользователя
                int userTB = departmentService.getParentTB(taUser.getDepartmentId()).getId();

                //Подразделение формы и подразделение пользователя должны относиться к одному ТБ или
                if (userTB == declarationTB) {
                    return true;
                }

                //ТБ подразделений, для которых подразделение пользователя является исполнителем макетов
                List<Integer> tbDepartments = departmentService.getAllTBPerformers(userTB, declarationTemplate.getType());

                //Подразделение формы относится к одному из ТБ подразделений, для которых подразделение пользователя является исполнителем
                if (tbDepartments.contains(declarationTB)) {
                    return true;
                }
            }

            // Оператор
            if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_OPER)) {
                if (asnuId != null && !checkUserAsnu(taUser, asnuId)) {
                    return false;
                }

                List<Integer> executors = departmentService.getTaxDeclarationDepartments(taUser, declarationTemplate.getType());
                if (executors.contains(declarationDepartment.getId())) {
                    if (!declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.CONSOLIDATED)) {
                        return true;
                    }
                }
            }

            return false;
        }

        /**
         * Проверяет есть у пользователя права на АСНУ декларации.
         * Если табличка SEC_USER_ASNU пустая, то права есть на все записи.
         *
         * @param user   пользователь
         * @param asnuId АСНУ НФ, для ПНФ значение должно быть задано, для остальных форм null
         */
        private boolean checkUserAsnu(TAUser user, Long asnuId) {
            if (user.getAsnuIds() == null || user.getAsnuIds().isEmpty()) {
                return true;
            }

            return user.getAsnuIds().contains(asnuId);
        }
    }

    /**
     * Право на расчет декларации
     */
    public static final class CalculatePermission extends DeclarationDataPermission {

        public CalculatePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(targetDomainObject.getDepartmentReportPeriodId());
            return departmentReportPeriod.isActive() && CHECK.isGranted(currentUser, targetDomainObject);
        }
    }

    /**
     * Право на проверку декларации
     */
    public static final class CheckPermission extends DeclarationDataPermission {

        public CheckPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject) {
            if (VIEW.isGranted(currentUser, targetDomainObject)) {
                if (targetDomainObject.getState() == State.CREATED || targetDomainObject.getState() == State.PREPARED) {
                    if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
                        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(targetDomainObject.getDepartmentReportPeriodId());
                        if (departmentReportPeriod.isActive()) {
                            DeclarationFormKind kind = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind();
                            if (kind == DeclarationFormKind.PRIMARY &&
                                    targetDomainObject.getManuallyCreated() &&
                                    targetDomainObject.getLastDataModifiedDate() == null) {
                                return false;
                            }
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    /**
     * Право на принятие декларации
     */
    public static final class AcceptedPermission extends DeclarationDataPermission {

        public AcceptedPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(targetDomainObject.getDepartmentReportPeriodId());

            if (departmentReportPeriod.isActive()) {
                if (VIEW.isGranted(currentUser, targetDomainObject)) {
                    if (targetDomainObject.getState() == State.PREPARED) {
                        if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    /**
     * Право на удаление декларации
     */
    public static final class DeletePermission extends DeclarationDataPermission {

        public DeletePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject) {
            if (targetDomainObject.getState() == State.CREATED) {
                if (VIEW.isGranted(currentUser, targetDomainObject)) {
                    if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
                        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(targetDomainObject.getDepartmentReportPeriodId());
                        if (departmentReportPeriod.isActive()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    /**
     * Право на возврат декларации в статус "Создана"
     */
    public static final class ReturnToCreatedPermission extends DeclarationDataPermission {

        public ReturnToCreatedPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(targetDomainObject.getDepartmentReportPeriodId());

            if (departmentReportPeriod.isActive()) {
                if (VIEW.isGranted(currentUser, targetDomainObject)) {
                    if (targetDomainObject.getState() == State.PREPARED || targetDomainObject.getState() == State.ACCEPTED) {
                        if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP) || PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_NS)) {
                            return true;
                        }

                        //Подразделение декларации
                        Department declarationDepartment = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
                        //ТБ декларации
                        Department parent = departmentService.getParentTB(declarationDepartment.getId());
                        int declarationTB = parent != null ? parent.getId() : declarationDepartment.getId();

                        //Подразделение и ТБ пользователя
                        TAUser taUser = taUserService.getUser(currentUser.getUsername());
                        Department userDepartmentTB = departmentService.getParentTB(taUser.getDepartmentId());
                        int userTB = userDepartmentTB != null ? userDepartmentTB.getId() : taUser.getDepartmentId();
                        //ТБ подразделений, для которых подразделение пользователя является исполнителем макетов
                        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId());
                        List<Integer> tbDepartments = departmentService.getAllTBPerformers(userTB, declarationTemplate.getType());

                        if (userTB == declarationTB || tbDepartments.contains(declarationTB)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    /**
     * Право на редактирование при назначении деклараций
     */
    public static final class EditAssignmentPermission extends DeclarationDataPermission {

        public EditAssignmentPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }

    /**
     * Право на выгрузку отчетности
     */
    public static final class DownloadReportsPermission extends DeclarationDataPermission {

        public DownloadReportsPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject) {
            return targetDomainObject.getState() == State.ACCEPTED && PermissionUtils.hasRole(user,
                    TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }

    /**
     * Формирование печатной формы
     */
    public static final class ShowPermission extends DeclarationDataPermission {

        public ShowPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject) {
            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS);
        }
    }

    /**
     * Права на загрузку Excel-файла в форму
     */
    public static final class ImportExcelPermission extends DeclarationDataPermission {

        public ImportExcelPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject) {
            return DeclarationDataPermission.VIEW.isGranted(user, targetDomainObject) &&
                    DeclarationDataPermission.CREATE.isGranted(user, targetDomainObject) &&
                    declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind() == DeclarationFormKind.PRIMARY &&
                    targetDomainObject.getState() == State.CREATED &&
                    targetDomainObject.getManuallyCreated();
        }
    }
}