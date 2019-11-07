package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookFormType;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    protected DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    protected DepartmentService departmentService;
    @Autowired
    protected TAUserService taUserService;
    @Autowired
    protected DepartmentReportPeriodFormatter departmentReportPeriodFormatter;
    @Autowired
    protected PeriodService reportPeriodService;
    @Autowired
    protected CommonRefBookService commonRefBookService;

    private static final String KIND_ERROR = "операция \"%s\" не допустима для форм типа %s";
    private static final String ACTIVE_ERROR = "период формы закрыт";
    private static final String STATE_ERROR = "операция \"%s\" не допустима для форм в состоянии \"%s\"";
    private static final String ROLE_ERROR = "недостаточно прав (обратитесь к администратору)";

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

    /**
     * Право на редактирование строк формы
     */
    public static final Permission<DeclarationData> UPDATE_DOC_STATE = new UpdateDocStatePermission(1 << 14);

    public static final SendEdoPermission SEND_EDO = new SendEdoPermission(1 << 15);

    /**
     * Право на просмотр карточки ФЛ
     */
    public static final PersonViewPermission PERSON_VIEW = new PersonViewPermission(1 << 16);

    /**
     * Право на удаление строк из формы
     */
    public static final DeleteRowsPermission DELETE_ROWS = new DeleteRowsPermission(1 << 17);


    private static final String DATE_FORMAT = "dd.MM.yyyy";

    public DeclarationDataPermission(long mask) {
        super(mask);
    }

    /**
     * Добавляет в логгер ошибку о недопустимости выполнения операции
     *
     * @param departmentReportPeriod отчетный период подразделения
     * @param operationName          название операции
     * @param declarationData        налоговая форма
     * @param reason                 причина  недопустимости выполнения операции
     * @param logger                 объект для логгирования информации
     */
    protected void logError(DepartmentReportPeriod departmentReportPeriod, String operationName,
                            DeclarationData declarationData, String reason, Logger logger) {
        if (logger != null) {
            Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
            logger.error("Не выполнена операция \"%s\" для налоговой формы: № %d, Период: \"%s, %s%s\", Подразделение: \"%s\". " +
                            "Причина: %s.",
                    operationName,
                    declarationData.getId(),
                    departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    departmentReportPeriod.getReportPeriod().getName(),
                    departmentReportPeriod.getCorrectionDate() != null ? " (корр. " + new SimpleDateFormat("dd.MM.yyyy").format(departmentReportPeriod.getCorrectionDate()) + ")" : "",
                    department.getName(),
                    reason);
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
            TAUserInfo systemUserInfo = taUserService.getSystemUserInfo();
            if (systemUserInfo.getUser().equals(taUser)) {
                return true;
            }

            DeclarationType declarationType = declarationTypeDao.get(targetDomainObject.getDeclarationTemplateId());

            // Контролёр НС
            if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_NS)) {
                // Подразделение формы = (ТБ подразделения пользователя + все дочерние + иерархия подразделений от подразделения, для которого являемся исполнителем, вверх до ТБ и всех дочерних вниз)
                List<Integer> departments = departmentService.findAllAvailableIds(taUser);
                if (departments.contains(targetDomainObject.getDepartmentId())) {
                    if (declarationType.getId() != DeclarationType.NDFL_2_FL) {
                        return true;
                    }
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
                        List<Integer> departments = departmentService.findAllAvailableIds(taUser);
                        if (departments.contains(targetDomainObject.getDepartmentId())) {
                            return true;
                        }
                    }
                }
            }

            // Оператор выдачи 2-НДФЛ клиенту по запросу (НДФЛ)
            if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_OPER_2NDFL_FL)) {
                return declarationType.getId() == DeclarationType.NDFL_2_FL;
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

        private final static String OPERATION_NAME = "Проверка формы";

        public CheckPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(
                    targetDomainObject.getDepartmentReportPeriodId());

            if (VIEW.isGranted(currentUser, targetDomainObject, logger)) {
                if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
                    DeclarationFormKind declarationKind = declarationTemplateDao.get(
                            targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind();

                    return declarationKind != DeclarationFormKind.PRIMARY ||
                            !targetDomainObject.isManuallyCreated() ||
                            targetDomainObject.getLastDataModifiedDate() != null;
                } else {
                    logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ROLE_ERROR, logger);
                }
            } else {
                logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ROLE_ERROR, logger);
            }
            return false;
        }
    }

    /**
     * Право на принятие декларации
     */
    public static final class AcceptedPermission extends DeclarationDataPermission {

        private final static String OPERATION_NAME = "Принятие формы";

        public AcceptedPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(
                    targetDomainObject.getDepartmentReportPeriodId());

            if (departmentReportPeriod.isActive()) {
                if (VIEW.isGranted(currentUser, targetDomainObject, logger)) {
                    if (targetDomainObject.getState() == State.PREPARED) {
                        if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS)) {
                            return true;
                        } else {
                            logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ROLE_ERROR, logger);
                        }
                    } else {
                        logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, String.format(STATE_ERROR, OPERATION_NAME, targetDomainObject.getState().getTitle()), logger);
                    }
                } else {
                    logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ROLE_ERROR, logger);
                }
            } else {
                logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ACTIVE_ERROR, logger);
            }
            return false;
        }
    }

    /**
     * Право на удаление декларации
     */
    public static final class DeletePermission extends DeclarationDataPermission {

        private final static String OPERATION_NAME = "Удаление";

        public DeletePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(
                    targetDomainObject.getDepartmentReportPeriodId());

            if (targetDomainObject.getState() == State.CREATED) {
                if (VIEW.isGranted(currentUser, targetDomainObject, logger)) {
                    if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER, TARole.N_ROLE_OPER_2NDFL_FL)) {
                        if (departmentReportPeriod.isActive()) {
                            return true;
                        }
                    } else {
                        logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ROLE_ERROR, logger);
                        return false;
                    }
                } else {
                    logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ROLE_ERROR, logger);
                    return false;
                }
            } else {
                logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, String.format(STATE_ERROR, OPERATION_NAME, targetDomainObject.getState().getTitle()), logger);
                return false;
            }
            return false;
        }
    }

    /**
     * Право на возврат декларации в статус "Создана"
     */
    public static final class ReturnToCreatedPermission extends DeclarationDataPermission {

        private final static String OPERATION_NAME = "Возврат в Создана";

        public ReturnToCreatedPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(
                    targetDomainObject.getDepartmentReportPeriodId());

            // Период формы открыт
            if (departmentReportPeriod.isActive()) {

                // Пользователь имеет права на просмотр формы
                if (VIEW.isGranted(currentUser, targetDomainObject, logger)) {

                    // Форма."Состояние ЭД" = "Не отправлен в ФНС", если Форма."Состояние ЭД" задано
                    if (targetDomainObject.getDocStateId() == null || RefBookDocState.NOT_SENT.getId().equals(targetDomainObject.getDocStateId())) {
                        // Форма.Состояние = "Принята"
                        if (targetDomainObject.getState() == State.ACCEPTED) {

                            // Пользователю назначена роль "Контролёр УНП (НДФЛ)" либо "Контролер НС (НДФЛ)"
                            if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS)) {
                                return true;
                            } else {
                                logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ROLE_ERROR, logger);
                            }
                        }
                        // Форма.Состояние = "Подготовлена"
                        else if (targetDomainObject.getState() == State.PREPARED) {

                            // Пользователю назначена роль "Контролёр УНП (НДФЛ)" либо "Контролер НС (НДФЛ)" либо "Оператор (НДФЛ)"
                            if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
                                return true;
                            } else {
                                logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ROLE_ERROR, logger);
                            }
                        } else {
                            logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, String.format(STATE_ERROR, OPERATION_NAME, targetDomainObject.getState().getTitle()), logger);
                        }
                    } else {
                        RefBookDocState docState = null;
                        if (targetDomainObject.getDocStateId() != null) {
                            docState = commonRefBookService.fetchRecord(RefBook.Id.DOC_STATE.getId(), targetDomainObject.getDocStateId());
                        }
                        logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, String.format(STATE_ERROR, OPERATION_NAME, docState == null ? "-" : docState.getName()), logger);
                    }

                } else {
                    logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ROLE_ERROR, logger);
                }
            } else {
                logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ACTIVE_ERROR, logger);
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
            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
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
            DepartmentReportPeriod drp = departmentReportPeriodService.fetchOne(targetDomainObject.getDepartmentReportPeriodId());
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
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(
                    targetDomainObject.getDepartmentReportPeriodId());

            if (!declarationKind.equals(DeclarationFormKind.PRIMARY)) {
                logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, String.format(KIND_ERROR, OPERATION_NAME, declarationKind), logger);
                return false;
            }
            if (!departmentReportPeriod.isActive()) {
                logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ACTIVE_ERROR, logger);
                return false;
            }
            if (!(targetDomainObject.getState().equals(State.CREATED))) {
                logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, String.format(STATE_ERROR, OPERATION_NAME, targetDomainObject.getState().getTitle()), logger);
                return false;
            }
            TAUser taUser = taUserService.getUser(user.getUsername());

            boolean canView = VIEW.isGranted(user, targetDomainObject, logger);

            boolean hasRoles = taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);

            if (!canView || !hasRoles) {
                logError(departmentReportPeriod, OPERATION_NAME, targetDomainObject, ROLE_ERROR, logger);
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
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(
                    targetDomainObject.getDepartmentReportPeriodId());

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
                String errorCommonPart = String.format("Операция \"%s\" не выполнена для формы № %d, Период: \"%s, %s%s\", " +
                                "Подразделение \"%s\".",
                        OPERATION_NAME,
                        targetDomainObject.getId(),
                        departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        departmentReportPeriod.getReportPeriod().getName(),
                        departmentReportPeriod.getCorrectionDate() != null ? " (корр. " + FastDateFormat.getInstance("dd.MM.yyyy").format(departmentReportPeriod.getCorrectionDate()) + ")" : "",
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

            boolean userHasViewAccess = VIEW.isGranted(user, targetDomainObject, logger);

            int templateId = targetDomainObject.getDeclarationTemplateId();
            DeclarationTemplate template = declarationTemplateDao.get(templateId);
            DeclarationFormKind formKind = template.getDeclarationFormKind();
            boolean userHasEditAccess = taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS)
                    || (taUser.hasRole(TARole.N_ROLE_OPER) && formKind == DeclarationFormKind.PRIMARY);

            boolean formIsCreated = targetDomainObject.getState() == State.CREATED;

            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(
                    targetDomainObject.getDepartmentReportPeriodId());
            boolean formPeriodIsActive = departmentReportPeriod.isActive();

            if (userHasViewAccess && userHasEditAccess && formIsCreated && formPeriodIsActive) {
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
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(
                    targetDomainObject.getDepartmentReportPeriodId());

            boolean canView = VIEW.isGranted(user, targetDomainObject, logger);

            if (departmentReportPeriod.isActive() && canView && (targetDomainObject.getState() == State.CREATED || targetDomainObject.getState() == State.PREPARED)) {
                DeclarationFormKind declarationKind = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind();
                if ((declarationKind == DeclarationFormKind.CONSOLIDATED && taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS)
                        || declarationKind == DeclarationFormKind.PRIMARY && taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER))) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Право на изменение состояния ЭД
     */
    public static final class UpdateDocStatePermission extends DeclarationDataPermission {

        public UpdateDocStatePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject, Logger logger) {
            TAUser taUser = taUserService.getUser(user.getUsername());
            DeclarationTemplate template = declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId());
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(
                    targetDomainObject.getDepartmentReportPeriodId());
            ReportPeriodType reportPeriodType = reportPeriodService.getPeriodTypeById(departmentReportPeriod.getReportPeriod().getDictTaxPeriodId());
            Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());

            boolean canView = VIEW.isGranted(user, targetDomainObject, logger);
            boolean hasRoles = taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);

            List<String> errMsgs = new ArrayList<>();
            if (!(canView && hasRoles)) {
                errMsgs.add("недостаточно прав (обратитесь к администратору)");
            }
            if (template.getDeclarationFormKind() == DeclarationFormKind.REPORTS && errMsgs.isEmpty()) {
                return true;
            } else {
                if (logger != null) {
                    logger.error("Не выполнена операция \"%s\" для налоговой формы: " +
                                    "№ %s, Период: \"%s, %s%s\", Подразделение: \"%s\". Причина: %s",
                            AsyncTaskType.UPDATE_DOC_STATE.getDescription(),
                            targetDomainObject.getId(),
                            departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                            reportPeriodType.getName(),
                            departmentReportPeriod.getCorrectionDate() != null ? " (корр. " + FastDateFormat.getInstance("dd.MM.yyyy").format(departmentReportPeriod.getCorrectionDate()) + ")" : "",
                            department.getName(),
                            StringUtils.join(errMsgs, ", "));
                }
                return false;
            }
        }
    }

    public static final class SendEdoPermission extends DeclarationDataPermission {

        public SendEdoPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject, Logger logger) {
            return isGranted(user, targetDomainObject, logger, null);
        }

        public boolean isGranted(User user, DeclarationData declarationData, Logger logger, String fileName) {
            List<String> errMsgs = new ArrayList<>();
            if (VIEW.isGranted(user, declarationData, logger)) {
                DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationData.getDeclarationTemplateId());
                RefBookFormType refBookFormType = declarationTemplate.getFormType();
                List<Long> suitableFormTypes = Arrays.asList(RefBookFormType.NDFL_2_1.getId(), RefBookFormType.NDFL_2_2.getId(), RefBookFormType.NDFL_6.getId());
                if (suitableFormTypes.contains(refBookFormType.getId())) {
                    List<Long> suitableDocStates = Arrays.asList(RefBookDocState.NOT_SENT.getId(), RefBookDocState.ERROR.getId(), RefBookDocState.EXPORTED.getId());
                    if (suitableDocStates.contains(declarationData.getDocStateId())) {
                        return true;
                    } else {
                        RefBookDocState docState = commonRefBookService.fetchRecord(RefBook.Id.DOC_STATE.getId(), declarationData.getDocStateId());
                        errMsgs.add("Операция \"Отправка в ЭДО\" не допустима для форм в состоянии \"" + docState.getName() + "\"");
                    }
                }
            }
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(
                    declarationData.getDepartmentReportPeriodId());
            Department department = departmentService.getDepartment(declarationData.getDepartmentId());
            DeclarationTemplate template = declarationTemplateDao.get(declarationData.getDeclarationTemplateId());
            if (logger != null) {
                if (errMsgs.isEmpty()) {
                    errMsgs.add("Недостаточно прав (обратитесь к администратору)");
                }
                logger.error("Ошибка отправки в ЭДО файла \"%s\" по отчетной форме №: %s, Период: %s %s%s, Подразделение: %s, Вид: %s. Причина: %s",
                        fileName,
                        declarationData.getId(),
                        departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        departmentReportPeriod.getReportPeriod().getName(),
                        departmentReportPeriod.getCorrectionDate() != null ? " (корр. " + new SimpleDateFormat("dd.MM.yyyy").format(departmentReportPeriod.getCorrectionDate()) + ")" : "",
                        department.getName(),
                        template.getName(),
                        StringUtils.join(errMsgs, ", "));
            }
            return false;
        }
    }

    /**
     * Право на просмотр карточки ФЛ
     */
    public static final class PersonViewPermission extends DeclarationDataPermission {

        public PersonViewPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationData targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
        }
    }

    /**
     * Право на удаление строк из формы
     */
    private static final class DeleteRowsPermission extends DeclarationDataPermission {
        public DeleteRowsPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, DeclarationData targetDomainObject, Logger logger) {

            // Одновременно выполняется условия:
            // Пользователю назначены хотя бы одна из ролей: "Оператор (НДФЛ)", "Контролёр НС (НДФЛ)", "Контролёр УНП (НДФЛ)"
            if (!PermissionUtils.hasRole(user,
                    TARole.N_ROLE_OPER,
                    TARole.N_ROLE_CONTROL_UNP,
                    TARole.N_ROLE_CONTROL_NS)) {
                return false;
            }

            //Форма находится в состоянии "Создана"
            if (!State.CREATED.equals(targetDomainObject.getState())) {
                return false;
            }

            //Форма.Макет.Тип Формы = "Первичная"
            DeclarationFormKind declarationKind =
                    declarationTemplateDao.get(targetDomainObject.getDeclarationTemplateId()).getDeclarationFormKind();
            if (!declarationKind.equals(DeclarationFormKind.PRIMARY)) {
                return false;
            }

            //Период формы открыт
            DepartmentReportPeriod departmentReportPeriod =
                    departmentReportPeriodDao.fetchOne(targetDomainObject.getDepartmentReportPeriodId());
            if (!departmentReportPeriod.isActive()) {
                return false;
            }

            return true;
        }
    }
}
