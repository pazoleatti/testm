package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter;
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
    @Autowired
    DepartmentReportPeriodFormatter departmentReportPeriodFormatter;

    /**
     * Право на создание декларации вручную
     */
    public static final Permission<DeclarationData> CREATE = new CreatePermission(1 << 0);
    /**
     * Право на просмотр декларации
     */
    public static final Permission<DeclarationData> VIEW = new ViewPermission(1 << 1);
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

    /**
     * Право на идентификацию ФЛ налоговой формы
     */
    public static final Permission<DeclarationData> IDENTIFY = new IdentifyPermission(1 << 11);

    /**
     * Право на консолидацию налоговой формы
     */
    public static final Permission<DeclarationData> CONSOLIDATE = new ConsolidatePermission(1 << 12);

    private static final String DATE_FORMAT = "dd.MM.yyyy";

    public DeclarationDataPermission(long mask) {
        super(mask);
    }

    /**
     * Добавляет в логгер ошибку о недопустимом типе формы
     * @param departmentReportPeriod    отчетный период подразделения
     * @param operationName             название операции
     * @param declarationData           налоговая форма
     * @param declarationFormKind       тип налоговой формы
     * @param logger                    объект для логгирования информации
     */
    protected void logFormKindError(DepartmentReportPeriod departmentReportPeriod, String operationName,
                                    DeclarationData declarationData, DeclarationFormKind declarationFormKind, Logger logger) {
        if (logger != null) {
            Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
            logger.error("Операция \"%s\" не выполнена для формы № %d, период: \"%s\", " +
                            "подразделение \"%s\". %s не допустима для форм типа \"%s\".",
                    operationName,
                    declarationData.getId(),
                    departmentReportPeriodFormatter.formatPeriodName(departmentReportPeriod, DATE_FORMAT),
                    department.getName(),
                    operationName,
                    declarationFormKind.getTitle());
        }

    }

    /**
     * Добавляет в логгер ошибку о закрытом периоде
     * @param departmentReportPeriod    отчетный период подразделения
     * @param operationName             название операции
     * @param declarationData           налоговая форма
     * @param logger                    объект для логгирования информации
     */
    protected void logPeriodError(DepartmentReportPeriod departmentReportPeriod, String operationName,
                                  DeclarationData declarationData, Logger logger) {
        if (logger != null) {
            Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
            logger.error("Операция \"%s\" не выполнена для формы № %d, период: \"%s\"," +
                            " подразделение \"%s\". Период формы закрыт.",
                    operationName,
                    declarationData.getId(),
                    departmentReportPeriodFormatter.formatPeriodName(departmentReportPeriod, DATE_FORMAT),
                    department.getName());
        }

    }

    /**
     *  Добавляет в логгер ошибку о недопустимом состоянии
     * @param departmentReportPeriod    отчетный период подразделения
     * @param operationName             название операции
     * @param declarationData           налоговая форма
     * @param logger                    объект для логгирования информации
     */
    protected void logStateError(DepartmentReportPeriod departmentReportPeriod, String operationName,
                                 DeclarationData declarationData, Logger logger) {
        if (logger != null) {
            Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
            logger.error("Операция \"%s\" не выполнена для формы № %d,  период: \"%s\", " +
                            "подразделение: \"%s\". %s не допустима для форм в состоянии \"%s\".",
                    operationName,
                    declarationData.getId(),
                    departmentReportPeriodFormatter.formatPeriodName(departmentReportPeriod, DATE_FORMAT),
                    department.getName(),
                    operationName,
                    declarationData.getState().getTitle());
        }
    }

    /**
     *  Добавляет в логгер ошибку о недостаточности прав для выполнения операции
     * @param departmentReportPeriod    отчетный период подразделения
     * @param operationName             название операции
     * @param declarationData           налоговая форма
     * @param logger                    объект для логгирования информации
     */
    protected void logCredentialsError(DepartmentReportPeriod departmentReportPeriod, String operationName,
                                       DeclarationData declarationData, Logger logger) {
        if (logger != null) {
            Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
            logger.error("Операция \"%s\" не выполнена для формы № %d, " +
                            "период: \"%s\", подразделение \"%s\". Недостаточно прав для выполнения операции.",
                    operationName,
                    declarationData.getId(),
                    departmentReportPeriodFormatter.formatPeriodName(departmentReportPeriod, DATE_FORMAT),
                    department.getName());
        }
    }

    /**
     * Право на создание декларации вручную
     */
    public static final class CreatePermission extends DeclarationDataPermission {

        public CreatePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
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

        /**
         * Дубликат в {@link com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataAccessServiceImpl#checkRolesForReading}
         */
        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
            // Выборка для доступа к экземплярам деклараций
            // http://conf.aplana.com/pages/viewpage.action?pageId=11380670

            // Контролёр УНП может просматривать все декларации
            if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP)) {
                return true;
            }

            TAUser taUser = taUserService.getUser(currentUser.getUsername());
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());
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
     * Право на проверку декларации
     */
    public static final class CheckPermission extends DeclarationDataPermission {

        public CheckPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
            if (VIEW.isGranted(currentUser, targetDomainObject, logger)) {
                if (targetDomainObject.getState() == State.CREATED || targetDomainObject.getState() == State.PREPARED) {
                    if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
                        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());
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
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());

            if (departmentReportPeriod.isActive()) {
                if (VIEW.isGranted(currentUser, targetDomainObject, logger)) {
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
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
            if (targetDomainObject.getState() == State.CREATED) {
                if (VIEW.isGranted(currentUser, targetDomainObject, logger)) {
                    if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
                        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());
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
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());

            // Период формы открыт
            if (departmentReportPeriod.isActive()) {

                // Пользователь имеет права на просмотр формы
                if (VIEW.isGranted(currentUser, targetDomainObject, logger)) {

                    // Форма.Состояние = "Принята", "Подготовлена"
                    if (targetDomainObject.getState() == State.PREPARED || targetDomainObject.getState() == State.ACCEPTED) {

                        // Пользователю назначена роль "Контролёр УНП (НДФЛ)" либо "Контролёр УНП (Сборы)"
                        if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP)) {
                            return true;
                        } else if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_NS)) {
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
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
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
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject, Logger logger) {
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
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject, Logger logger) {
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
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject, Logger logger) {
            return DeclarationDataPermission.VIEW.isGranted(user, targetDomainObject, logger) &&
                    DeclarationDataPermission.CREATE.isGranted(user, targetDomainObject, logger) &&
                    declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind() == DeclarationFormKind.PRIMARY &&
                    targetDomainObject.getState() == State.CREATED &&
                    targetDomainObject.getManuallyCreated();
        }
    }

    /**
     * Право на идентификацию ФЛ налоговой формы
     */
    public static final class IdentifyPermission extends DeclarationDataPermission {

        private final static String OPERATION_NAME = "Идентификация ФЛ";

        public IdentifyPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject, Logger logger) {
            DeclarationTemplate declarationTemplate = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId());
            DeclarationFormKind declarationFormKind = declarationTemplate.getDeclarationFormKind();
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());
            if (!declarationFormKind.equals(DeclarationFormKind.PRIMARY)) {
                logFormKindError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, declarationFormKind, logger);
                return false;
            }
            if (!departmentReportPeriod.isActive()) {
                logPeriodError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, logger);
                return false;
            }
            if (!(targetDomainObject.getState().equals(State.CREATED) || targetDomainObject.getState().equals(State.PREPARED))) {
                logStateError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, logger);
                return false;
            }
            TAUser taUser = taUserService.getUser(user.getUsername());

            boolean canView = VIEW.isGranted(user, targetDomainObject, logger);

            boolean hasRoles = taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP,
                    TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS, TARole.N_ROLE_OPER, TARole.F_ROLE_OPER);

            if (!canView || !hasRoles) {
                logCredentialsError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, logger);
                return false;
            }

            return true;
        }
    }

    /**
     * Право на консолидацию налоговой формы
     */
    public static final class ConsolidatePermission extends DeclarationDataPermission {

        private final static String OPERATION_NAME = "Консолидация";

        public ConsolidatePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject, Logger logger) {
            DeclarationTemplate declarationTemplate = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId());
            DeclarationFormKind declarationFormKind = declarationTemplate.getDeclarationFormKind();
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());
            if (!declarationFormKind.equals(DeclarationFormKind.CONSOLIDATED)) {
                logFormKindError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, declarationFormKind, logger);
                return false;
            }
            if (!departmentReportPeriod.isActive()) {
                logPeriodError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, logger);
                return false;
            }
            if (!(targetDomainObject.getState().equals(State.CREATED) || targetDomainObject.getState().equals(State.PREPARED))) {
                logStateError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, logger);
                return false;
            }
            TAUser taUser = taUserService.getUser(user.getUsername());

            boolean canView = VIEW.isGranted(user, targetDomainObject, logger);

            boolean hasRoles = taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);

            if (!canView || !hasRoles) {
                logCredentialsError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, logger);
                return false;
            }

            return true;
        }
    }
}