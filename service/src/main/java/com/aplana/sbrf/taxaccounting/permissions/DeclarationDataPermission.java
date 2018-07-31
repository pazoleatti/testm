package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация прав для декларации.
 * Описано в аналитике - https://conf.aplana.com/pages/viewpage.action?pageId=27177937
 */
@Configurable
public abstract class DeclarationDataPermission extends AbstractPermission<DeclarationData> {

    @Autowired
    protected DeclarationTemplateDao declarationTemplateDao;
    @Autowired
    protected DeclarationTypeDao declarationTypeDao;
    @Autowired
    protected DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    protected DepartmentService departmentService;
    @Autowired
    protected TAUserService taUserService;
    @Autowired
    protected DepartmentReportPeriodFormatter departmentReportPeriodFormatter;

    /**
     * Право на создание декларации вручную
     */
    public static final Permission<DeclarationData> CREATE = new CreatePermission(1 << 0);
    /**
     * Право на просмотр декларации
     */
    public static final Permission<DeclarationData> VIEW = new ViewPermission(1 << 1);
    /**
     * Право на обнолвление данных ФЛ в КНФ
     */
    public static final Permission<DeclarationData> UPDATE_PERSONS_DATA = new UpdatePersonsDataPermission(1 << 2);
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

    /**
     * Право на редактирование строк формы
     */
    public static final Permission<DeclarationData> EDIT = new EditPermission(1 << 13);

    private static final String DATE_FORMAT = "dd.MM.yyyy";

    public DeclarationDataPermission(long mask) {
        super(mask);
    }

    /**
     * Добавляет в логгер ошибку о недопустимом типе формы
     *
     * @param departmentReportPeriod отчетный период подразделения
     * @param operationName          название операции
     * @param declarationData        налоговая форма
     * @param declarationFormKind    тип налоговой формы
     * @param logger                 объект для логгирования информации
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
     *
     * @param departmentReportPeriod отчетный период подразделения
     * @param operationName          название операции
     * @param declarationData        налоговая форма
     * @param logger                 объект для логгирования информации
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
     * Добавляет в логгер ошибку о недопустимом состоянии
     *
     * @param departmentReportPeriod отчетный период подразделения
     * @param operationName          название операции
     * @param declarationData        налоговая форма
     * @param logger                 объект для логгирования информации
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
     * Добавляет в логгер ошибку о недостаточности прав для выполнения операции
     *
     * @param departmentReportPeriod отчетный период подразделения
     * @param operationName          название операции
     * @param declarationData        налоговая форма
     * @param logger                 объект для логгирования информации
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

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
            // Выборка для доступа к экземплярам деклараций
            // http://conf.aplana.com/pages/viewpage.action?pageId=11380670

            // Контролёр УНП может просматривать все декларации
            if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP)) {
                return true;
            }

            TAUser taUser = taUserService.getUser(currentUser.getUsername());
            DeclarationType declarationType = declarationTypeDao.get(targetDomainObject.getDeclarationTemplateId());

            // Контролёр НС
            if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_NS)) {
                // Подразделение формы = (ТБ подразделения пользователя + все дочерние + иерархия подразделений от подразделения, для которого являемся исполнителем, вверх до ТБ и всех дочерних вниз)
                List<Integer> departments = departmentService.getTaxFormDepartments(taUser);
                if (departments.contains(targetDomainObject.getDepartmentId())) {
                    return true;
                }
            }

            Long asnuId = targetDomainObject.getAsnuId();
            DeclarationFormKind declarationKind = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind();

            // Оператор
            if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_OPER)) {
                // Тип формы = Первичная и Вид формы = РНУ-НДФЛ
                if (declarationKind == DeclarationFormKind.PRIMARY && declarationType.getId() == DeclarationType.NDFL_PRIMARY) {
                    // Проверка АСНУ
                    if (hasUserAsnuAccess(currentUser, taUser, asnuId)) {
                        //TODO: надо проверить правильность после https://jira.aplana.com/browse/SBRFNDFL-3835
                        // Подразделение формы = подразделению пользователя (либо дочернему) или подразделению (либо дочернему) для которого исполнителем назначено подразделение пользователя
                        List<Integer> departments = departmentService.getTaxFormDepartments(taUser);
                        if (departments.contains(targetDomainObject.getDepartmentId())) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        /**
         * Проверяет есть у пользователя права на АСНУ декларации.
         * Если у формы не указана асну - есть права на нее
         * Если табличка SEC_USER_ASNU пустая, то права есть на все записи.
         * Если у пользователя есть роль N_ROLE_OPER_ALL, то права есть на все записи.
         * Если у пользователя есть только роль N_ROLE_OPER, то прав нет ни на какие АСНУ
         *
         * @param user   пользователь
         * @param asnuId АСНУ НФ, для ПНФ значение должно быть задано, для остальных форм null
         */
        private boolean hasUserAsnuAccess(User user, TAUser taUser, Long asnuId) {
            return asnuId == null || (!taUser.hasSingleRole(TARole.N_ROLE_OPER) && (taUser.getAsnuIds().contains(asnuId) ||
                    PermissionUtils.hasRole(user, TARole.N_ROLE_OPER_ALL)));

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
                            DeclarationFormKind declarationKind = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind();
                            if (declarationKind == DeclarationFormKind.PRIMARY &&
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

                        // Пользователю назначена роль "Контролёр УНП (НДФЛ)" либо "Контролер НС (НДФЛ)"
                        return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
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
            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
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
            DepartmentReportPeriod drp = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());
            return DeclarationDataPermission.VIEW.isGranted(user, targetDomainObject, logger) &&
                    DeclarationDataPermission.CREATE.isGranted(user, targetDomainObject, logger) &&
                    declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind() == DeclarationFormKind.PRIMARY &&
                    targetDomainObject.getState() == State.CREATED && drp.isActive();
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
            DeclarationFormKind declarationKind = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind();
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());
            if (!declarationKind.equals(DeclarationFormKind.PRIMARY)) {
                logFormKindError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, declarationKind, logger);
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

            boolean hasRoles = taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);

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
            DeclarationFormKind declarationKind = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind();

            boolean granted = true;
            List<String> causes = new ArrayList<>();

            if (!declarationKind.equals(DeclarationFormKind.CONSOLIDATED)) {
                causes.add(String.format("консолидация не допустима для форм типа \"%s\".",
                        declarationKind.getName()));
                if (logger != null) {
                    granted = false;
                } else {
                    return false;
                }
            }
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());
            if (!departmentReportPeriod.isActive()) {
                causes.add("период формы закрыт");
                if (logger != null) {
                    granted = false;
                } else {
                    return false;
                }
            }
            if (!(targetDomainObject.getState().equals(State.CREATED) || targetDomainObject.getState().equals(State.PREPARED))) {
                causes.add("форма находится в состоянии \"Принята\"");
                if (logger != null) {
                    granted = false;
                } else {
                    return false;
                }
            }

            TAUser taUser = taUserService.getUser(user.getUsername());
            boolean canView = VIEW.isGranted(user, targetDomainObject, logger);
            boolean hasRoles = taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
            if (!canView || !hasRoles) {
                causes.add("недостаточно прав (обратитесь к администратору)");
                if (logger != null) {
                    granted = false;
                } else {
                    return false;
                }
            }
            if (logger != null && !granted) {
                Department department = departmentService.getDepartment(targetDomainObject.getDepartmentId());
                String errorCommonPart = String.format("Операция \"%s\" не выполнена для формы № %d, Период: \"%s, %s\", " +
                                "Подразделение \"%s\".",
                        OPERATION_NAME,
                        targetDomainObject.getId(),
                        departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        departmentReportPeriod.getReportPeriod().getName(),
                        department.getName());
                logger.error("%s Причина: " + StringUtils.join(causes, ", "), errorCommonPart);
            }

            return granted;
        }
    }

    /**
     * Право на редактирование строк налоговой формы
     */
    public static final class EditPermission extends DeclarationDataPermission {

        private final static String OPERATION_NAME = "Редактирование";

        public EditPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject, Logger logger) {
            TAUser taUser = taUserService.getUser(user.getUsername());
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());

            boolean canView = VIEW.isGranted(user, targetDomainObject, logger);
            boolean hasRoles = taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);

            if (targetDomainObject.getState() == State.CREATED && canView && hasRoles) {
                DeclarationFormKind declarationKind = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind();

                if (declarationKind != DeclarationFormKind.CONSOLIDATED || !departmentReportPeriod.isActive()) {
                    logCredentialsError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, logger);
                    return false;
                }

                return true;
            } else {
                logCredentialsError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, logger);
                return false;
            }
        }
    }

    public static final class UpdatePersonsDataPermission extends DeclarationDataPermission {

        public UpdatePersonsDataPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject, Logger logger) {
            TAUser taUser = taUserService.getUser(user.getUsername());
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());

            boolean canView = VIEW.isGranted(user, targetDomainObject, logger);
            boolean hasRoles = taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);

            if ((targetDomainObject.getState() == State.CREATED || targetDomainObject.getState() == State.CREATED) && canView && hasRoles) {
                DeclarationFormKind declarationKind = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind();
                if (declarationKind != DeclarationFormKind.CONSOLIDATED || !departmentReportPeriod.isActive()) {
                    return false;
                }
                return true;
            }
            return false;
        }
    }
}