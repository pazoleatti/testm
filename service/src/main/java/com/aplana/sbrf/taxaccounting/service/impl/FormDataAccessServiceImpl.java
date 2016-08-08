package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Arrays.asList;

@Service
public class FormDataAccessServiceImpl implements FormDataAccessService {

    private static final Log LOG = LogFactory.getLog(FormDataAccessServiceImpl.class);
    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    public static final String LOG_EVENT_AVAILABLE_MOVES = "LOG_EVENT_AVAILABLE_MOVES";
    public static final String LOG_EVENT_READ = "READ";
    public static final String LOG_EVENT_EDIT = "EDIT";

    public static final String LOG_EVENT_READ_RU = "чтение";
    public static final String LOG_EVENT_EDIT_RU = "редактирование";
    public static final String LOG_EVENT_DELETE_RU = "удаление";

    private static final String FORM_DATA_KIND_STATE_ERROR_LOG = "Event type: \"%s\". Unsuppotable case for formData with \"%s\" kind and \"%s\" state!";
    private static final String REPORT_PERIOD_IS_CLOSED_LOG = "Department report period (%d) is closed!";
    private static final String REPORT_PERIOD_IS_CLOSED = "Выбранный период закрыт";
    private static final String FORM_TEMPLATE_WRONG_STATUS_LOG = "Form template (%d) does not exist in report period (%d)!";
    private static final String FORM_TEMPLATE_WRONG_STATUS = "Выбранный тип %s не существует в выбранном периоде!";
    private static final String FORM_TEMPLATE_COMPARATIVE_LOG = "Form template (%d) is comparative!";
    private static final String FORM_TEMPLATE_COMPARATIVE = "Не указан параметр \"Период сравнения\"!";
    private static final String FORM_TEMPLATE_NOT_COMPARATIVE_LOG = "Form template (%d) is not comparative!";
    private static final String FORM_TEMPLATE_NOT_COMPARATIVE = "Указан недопустимый параметр \"Период сравнения\"!";
    private static final String FORM_TEMPLATE_PERIOD_NOT_ACCRUING_LOG = "Form template (%d) is not accruing in period (%d)!";
    private static final String FORM_TEMPLATE_PERIOD_NOT_ACCRUING = "Нельзя создать форму с признаком \"Расчет нарастающим итогом\" для данного периода!";
    private static final String FORM_TEMPLATE_NOT_ACCRUING_LOG = "Form template (%d) is not accruing!";
    private static final String FORM_TEMPLATE_NOT_ACCRUING = "Нельзя создать форму с признаком \"Расчет нарастающим итогом\"!";
    private static final String INCORRECT_DEPARTMENT_FORM_TYPE_LOG = "Form type (%d) and form kind (%d) is not applicated for department (%d)";
    private static final String INCORRECT_DEPARTMENT_FORM_TYPE1 = "Выбранный тип %s не назначен подразделению!";
    private static final String INCORRECT_DEPARTMENT_FORM_TYPE2 = "Нет прав доступа к созданию формы с заданными параметрами!";
    private static final String INCORRECT_DEPARTMENT_FORM_TYPE3 = "Выбранный тип %s не назначен подразделению";
    private static final String CREATE_FORM_DATA_ERROR_ONLY_CONTROL_LOG = "Only ROLE_CONTROL can create form in balance period!";
    private static final String CREATE_FORM_DATA_ERROR_ONLY_CONTROL = "Выбран период ввода остатков. В периоде ввода остатков оператор не может создавать %s";
    private static final String CREATE_MANUAL_FORM_DATA_ERROR_ONLY_CONTROL_LOG = "Only ROLE_CONTROL can create manual version of form!";
    private static final String CREATE_MANUAL_FORM_DATA_ERROR_ONLY_CONTROL = "Только контролер может создавать версию ручного ввода";
    private static final String FORM_DATA_ERROR_ACCESS_DENIED = "Недостаточно прав на %s формы с типом \"%s\" в статусе \"%s\"!";
    private static final String FORM_DATA_DEPARTMENT_ACCESS_DENIED_LOG = "Selected department (%d) not available in report period (%d)!";
    private static final String FORM_DATA_DEPARTMENT_ACCESS_DENIED = "Выбранное подразделение недоступно для пользователя!";
    private static final String FORM_DATA_EDIT_ERROR = "Нельзя редактировать форму \"%s\" в состоянии \"%s\"";
    private static final String ACCEPTED_DESTINATION_MSG = "Приёмник формы - \"%s\" для подразделения \"%s\" в периоде \"%s%s%s\" - находится в статусе \"Принят\"!";

    @Autowired
    private FormDataDao formDataDao;
    @Autowired
    private DeclarationDataDao declarationDataDao;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private FormTemplateDao formTemplateDao;
    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;
    @Autowired
    private PeriodService reportPeriodService;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private FormTypeDao formTypeDao;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;

    @Override
    public void canRead(TAUserInfo userInfo, long formDataId) {
        // УНП может просматривать все формы всех типов
        if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            return;
        }
        // НФ
        FormData formData = formDataDao.getWithoutRows(formDataId);
        ReportPeriod reportPeriod = periodService.getReportPeriod(formData.getReportPeriodId());

        // Подразделения, доступные пользователю
        List<Integer> availableDepartmentList = departmentService.getTaxFormDepartments(userInfo.getUser(),
                asList(formData.getFormType().getTaxType()), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());

        // Создаваемые вручную формы (читают все, имеющие доступ к подразделению в любом статусе)
        if (asList(FormDataKind.ADDITIONAL, FormDataKind.PRIMARY, FormDataKind.UNP).contains(formData.getKind())
                && (userInfo.getUser().hasRole(TARole.ROLE_OPER)
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS))
                && availableDepartmentList.contains(formData.getDepartmentId())) {
            return;
        }

        // Создаваемые автоматически формы (читают все контролеры, имеющие доступ к подразделению в любом статусе)
        if (asList(FormDataKind.CONSOLIDATED, FormDataKind.SUMMARY, FormDataKind.CALCULATED).contains(formData.getKind())
                && (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS))
                && availableDepartmentList.contains(formData.getDepartmentId())) {
            // Передаваемые на вышестоящий уровень (читают все контролеры, имеющие доступ к подразделению в любом статусе)
            return;
        }

        // Оператору доступны расчетные формы
        if (userInfo.getUser().hasRole(TARole.ROLE_OPER) && formData.getKind() == FormDataKind.CALCULATED) {
            return;
        }

        // Непредусмотренное сочетание параметров состояния формы и пользователя - запрет доступа
        // Или подразделение недоступно
        LOG.error(String.format(FORM_DATA_KIND_STATE_ERROR_LOG, LOG_EVENT_READ, formData.getKind().getTitle(),
				formData.getState().getTitle()));

        throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_READ_RU,
                formData.getKind().getTitle(), formData.getState().getTitle()));
    }

    @Override
    public void canCreate(TAUserInfo userInfo, int formTemplateId, FormDataKind kind, int departmentReportPeriodId, Integer comparativeDepPeriodId, boolean accruing) {
        // http://conf.aplana.com/pages/viewpage.action?pageId=11383566
        // Макет формы
        FormTemplate formTemplate = formTemplateDao.get(formTemplateId);

        // Если выбранный "Период" закрыт, то система выводит сообщение в панель уведомления:
        // "Выбранный период закрыт".
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(departmentReportPeriodId);
        if (!departmentReportPeriod.isActive()) {
            LOG.warn(String.format(REPORT_PERIOD_IS_CLOSED_LOG, departmentReportPeriodId));
            throw new ServiceException(REPORT_PERIOD_IS_CLOSED);
        }

        if (formTemplate.isComparative() && comparativeDepPeriodId == null) {
            LOG.warn(String.format(FORM_TEMPLATE_COMPARATIVE_LOG, formTemplateId));
            throw new ServiceException(FORM_TEMPLATE_COMPARATIVE);
        } else if (!formTemplate.isComparative() && comparativeDepPeriodId != null) {
            LOG.warn(String.format(FORM_TEMPLATE_NOT_COMPARATIVE_LOG, formTemplateId));
            throw new ServiceException(FORM_TEMPLATE_NOT_COMPARATIVE);
        }

        if (formTemplate.isAccruing()) {
            int reportPeriodId;
            if (formTemplate.isComparative()) {
                reportPeriodId = departmentReportPeriodDao.get(comparativeDepPeriodId).getReportPeriod().getId();
            } else {
                reportPeriodId = departmentReportPeriod.getReportPeriod().getId();
            }
            if (reportPeriodService.isFirstPeriod(reportPeriodId) && accruing) {
                LOG.warn(String.format(FORM_TEMPLATE_PERIOD_NOT_ACCRUING_LOG, formTemplateId, reportPeriodId));
                throw new ServiceException(FORM_TEMPLATE_PERIOD_NOT_ACCRUING);
            }
        } else {
            if (accruing) {
                LOG.warn(String.format(FORM_TEMPLATE_NOT_ACCRUING_LOG, formTemplateId));
                throw new ServiceException(FORM_TEMPLATE_NOT_ACCRUING);
            }
        }
        // Проверка периода ввода остатков
        if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)
                && departmentReportPeriod.isBalance()) {
            LOG.warn(CREATE_FORM_DATA_ERROR_ONLY_CONTROL_LOG);
            throw new ServiceException(
                    String.format(CREATE_FORM_DATA_ERROR_ONLY_CONTROL, MessageGenerator.mesSpeckPlural(formTemplate.getType().getTaxType())));
        }

        // Проверка доступности подразделения
        if (!departmentService.getOpenPeriodDepartments(userInfo.getUser(),
                asList(formTemplate.getType().getTaxType()),
                departmentReportPeriod.getReportPeriod().getId()).contains(departmentReportPeriod.getDepartmentId())) {
            LOG.warn(String.format(FORM_DATA_DEPARTMENT_ACCESS_DENIED_LOG, departmentReportPeriod.getDepartmentId(),
					departmentReportPeriod.getReportPeriod().getId()));
            throw new ServiceException(FORM_DATA_DEPARTMENT_ACCESS_DENIED);
        }

        // Id макета
        int formTypeId = formTemplate.getType().getId();

        // Вид формы
        FormType formType = formTypeDao.get(formTypeId);
        ReportPeriod reportPeriod = departmentReportPeriod.getReportPeriod();

        // Если выбранный "Вид формы" не назначен выбранному подразделению,
        // то система выводит сообщение в панель уведомления: "Выбранный вид налоговой формы не назначен подразделению".
        // Если у пользователя нет доступа к выбранному виду формы, то система выводит сообщение в панель уведомления:
        // "Нет прав доступа к созданию формы с заданными параметрами".
        boolean foundTypeAndKind = false;
        boolean foundKind = false;
        for (DepartmentFormType dft : sourceService.getDFTByDepartment(departmentReportPeriod.getDepartmentId(),
                formType.getTaxType(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate())) {
            if (dft.getKind() == kind) {
                foundKind = true;
                if (dft.getFormTypeId() == formTypeId) {
                    foundTypeAndKind = true;
                    break;
                }
            }
        }
        if (!foundTypeAndKind) {
            LOG.warn(String.format(INCORRECT_DEPARTMENT_FORM_TYPE_LOG, formTypeId, kind.getId(),
					departmentReportPeriod.getDepartmentId()));
            throw new ServiceException(String.format(INCORRECT_DEPARTMENT_FORM_TYPE3, MessageGenerator.mesSpeckPlural(formType.getTaxType())));
        }
        if (!foundKind) {
            LOG.warn(String.format(INCORRECT_DEPARTMENT_FORM_TYPE_LOG, formTypeId, kind.getId(),
					departmentReportPeriod.getDepartmentId()));
            throw new ServiceException(String.format(INCORRECT_DEPARTMENT_FORM_TYPE1, MessageGenerator.mesSpeckPlural(formType.getTaxType())));
        }

        // Доступные типы форм
        List<FormDataKind> formDataKindList = getAvailableFormDataKind(userInfo, asList(formTemplate.getType().getTaxType()));
        if (!formDataKindList.contains(kind)) {
            LOG.warn(String.format(INCORRECT_DEPARTMENT_FORM_TYPE_LOG, formTypeId, kind.getId(),
					departmentReportPeriod.getDepartmentId()));
            throw new ServiceException(INCORRECT_DEPARTMENT_FORM_TYPE2);
        }

        // Если период актуальности макета, выбранного в поле "Вид формы", не пересекается с выбранным отчетным
        // периодом ИЛИ пересекается, но его STATUS не равен 0, то система выводит сообщение в панель уведомления:
        // "Выбранный вид налоговой формы не существует в выбранном периоде"
        boolean intersect = isTemplateIntersectReportPeriod(formTemplate, departmentReportPeriod.getReportPeriod().getId());
        if (!intersect || formTemplate.getStatus() != VersionedObjectStatus.NORMAL) {
            LOG.warn(String.format(FORM_TEMPLATE_WRONG_STATUS_LOG, formTemplate.getId(), departmentReportPeriod.getReportPeriod().getId()));
            throw new AccessDeniedException(String.format(FORM_TEMPLATE_WRONG_STATUS, MessageGenerator.mesSpeckPlural(formType.getTaxType())));
        }

        // Если форма с заданными параметрами существует, то система выводит сообщение в панель уведомления:
        // "Форма с заданными параметрами уже существует".
        // Проверка реализована в скриптах, где она требуется.
    }

    @Override
    public void canCreateManual(Logger logger, TAUserInfo userInfo, long formDataId) {
        FormData formData = formDataDao.get(formDataId, false);

        //Проверка роли пользователя
        if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            logger.warn(String.format(CREATE_MANUAL_FORM_DATA_ERROR_ONLY_CONTROL_LOG));
            throw new ServiceException(CREATE_MANUAL_FORM_DATA_ERROR_ONLY_CONTROL);
        }

        //Форма принята?
        if (formData.getState() != WorkflowState.ACCEPTED) {
            logger.error("Форма не принята!");
        }

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(formData.getDepartmentReportPeriodId());

        //Период формы - открыт?
        if (!departmentReportPeriod.isActive()) {
            logger.error("Период формы закрыт!");
        }

        //Не существует приёмника формы, имеющего статус "Принят"?
        List<Pair<String, String>> destinations = sourceService.existAcceptedDestinations(formData.getDepartmentId(), formData.getFormType().getId(),
                formData.getKind(), formData.getReportPeriodId(), null, null);
        if (!destinations.isEmpty()) {
            for (Pair<String, String> destination : destinations) {
                logger.error(String.format(ACCEPTED_DESTINATION_MSG,
                        destination.getFirst(), destination.getSecond(),
                        departmentReportPeriod.getReportPeriod().getName() + " " + departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        formData.getPeriodOrder() != null ? ", Месяц: \"" + Months.fromId(formData.getPeriodOrder()).getTitle() + "\"" : "",
                        departmentReportPeriod.getCorrectionDate() != null ? ", Дата сдачи корректировки: " + sdf.get().format(departmentReportPeriod.getCorrectionDate()) : ""
                ));
            }
        }

        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Произошли ошибки при проверке необходимых условий для перевода формы в режим ручного ввода",
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public void canEdit(TAUserInfo userInfo, long formDataId, boolean manual) {
        FormData formData = formDataDao.getWithoutRows(formDataId);

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(formData.getDepartmentReportPeriodId());

        // Проверка закрытого периода
        if (!departmentReportPeriod.isActive()) {
            throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
                    formData.getKind().getTitle(), formData.getState().getTitle()) + " Период закрыт!");
        }

        // Проверка периода ввода остатков
        if (departmentReportPeriod.isBalance()) {
            switch (formData.getState()) {
                case CREATED:
                    // Созданные редактируют только контролеры, которые могут открыть форму для чтения
                    if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        throw new AccessDeniedException(
                                String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
                                        formData.getKind().getTitle(), formData.getState().getTitle()));
                    }
                    return;
                case ACCEPTED:
                    if (!manual) {
                        // Нельзя редактировать в состоянии "Принята"
                        throw new AccessDeniedException(String.format(FORM_DATA_EDIT_ERROR,
                                formData.getFormType().getName(), formData.getState().getTitle()));
                    }
            }
        }

        // Создаваемые вручную формы
        if (asList(FormDataKind.ADDITIONAL, FormDataKind.PRIMARY, FormDataKind.UNP).contains(formData.getKind())) {
            switch (formData.getState()) {
                case CREATED:
                    // Созданные редактируют все, кто может открыть
                    return;
                case PREPARED:
                    // Подготовленные редактируют только контролеры, которые могут открыть форму для чтения
                    if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
                                formData.getKind().getTitle(), formData.getState().getTitle()));
                    }
                    return;
                case APPROVED:
                    // Подготовленные редактируют только контролеры вышестоящего уровня, которые могут открыть форму для чтения
                    // Не контролеры
                    if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
                                formData.getKind().getTitle(), formData.getState().getTitle()));
                    }
                    // Контролеры текущего уровня
                    if ((userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS))
                            && userInfo.getUser().getDepartmentId() == formData.getDepartmentId()) {
                        throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
                                formData.getKind().getTitle(), formData.getState().getTitle()));
                    }
                    return;
                case ACCEPTED:
                    if (!manual) {
                        // Нельзя редактировать НФ в состоянии "Принята"
                        throw new AccessDeniedException(String.format(FORM_DATA_EDIT_ERROR,
                                formData.getFormType().getName(), formData.getState().getTitle()));
                    }
            }
        }

        // Создаваемые автоматически формы
        if (asList(FormDataKind.CONSOLIDATED, FormDataKind.SUMMARY, FormDataKind.CALCULATED).contains(formData.getKind())) {
            switch (formData.getState()) {
                case CREATED:
                    // Оператору доступны расчетные формы
                    if (userInfo.getUser().hasRole(TARole.ROLE_OPER) && formData.getKind() == FormDataKind.CALCULATED) {
                        return;
                    }
                    // Созданные редактируют только контролеры, которые могут открыть форму для чтения
                    if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
                                formData.getKind().getTitle(), formData.getState().getTitle()));
                    }
                    return;
                case APPROVED:
                case ACCEPTED:
                    if (!manual) {
                        // Нельзя редактировать НФ в состоянии "Принята"
                        throw new AccessDeniedException(String.format(FORM_DATA_EDIT_ERROR,
                                formData.getFormType().getName(), formData.getState().getTitle()));
                    }
                    return;
            }
        }

        // Непредвиденное состояние формы
        LOG.error(String.format(FORM_DATA_KIND_STATE_ERROR_LOG, LOG_EVENT_EDIT, formData.getKind().getTitle(),
				formData.getState().getTitle()));
        throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
                formData.getKind().getTitle(), formData.getState().getTitle()));
    }

    @Override
    public void canDelete(TAUserInfo userInfo, long formDataId) {
        FormData formData = formDataDao.getWithoutRows(formDataId);
        if (formData.getState() != WorkflowState.CREATED) {
            throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_DELETE_RU,
                    formData.getKind().getTitle(), formData.getState().getTitle()));
        }
        canEdit(userInfo, formDataId, false);
    }

    @Override
    public void canDeleteManual(Logger logger, TAUserInfo userInfo, long formDataId) {
        FormData formData = formDataDao.getWithoutRows(formDataId);

        //Проверка роли пользователя
        if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            logger.warn(String.format(CREATE_MANUAL_FORM_DATA_ERROR_ONLY_CONTROL_LOG));
            throw new ServiceException(CREATE_MANUAL_FORM_DATA_ERROR_ONLY_CONTROL);
        }

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(formData.getDepartmentReportPeriodId());

        //Период формы - открыт?
        if (!departmentReportPeriod.isActive()) {
            logger.error("Период формы закрыт!");
        }

        //Не существует приёмника формы, имеющего статус "Принят"?
        List<Pair<String, String>> destinations = sourceService.existAcceptedDestinations(formData.getDepartmentId(), formData.getFormType().getId(),
                formData.getKind(), formData.getReportPeriodId(), null, null);
        if (!destinations.isEmpty()) {
            ReportPeriod period = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
            for (Pair<String, String> destination : destinations) {
                logger.error("Приёмник формы - " + destination.getFirst() + " для подразделения " + destination.getSecond() +
                        " в периоде " + period.getTaxPeriod().getYear() + " " + period.getName() + " - находится в статусе \"Принят\"!");
            }
        }

        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Произошли ошибки при удалении версии ручного ввода",
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public List<WorkflowMove> getAvailableMoves(TAUserInfo userInfo, long formDataId) {
        // Есть возможность чтения
        canRead(userInfo, formDataId);
        return getAvailableMovesWithoutCanRead(userInfo, formDataId);
    }

    /**
     * Получение списка переходов без учета прохождения проверки canRead()
     */
    private List<WorkflowMove> getAvailableMovesWithoutCanRead(TAUserInfo userInfo, long formDataId) {
        List<WorkflowMove> result = new LinkedList<WorkflowMove>();
        FormData formData = formDataDao.getWithoutRows(formDataId);
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(formData.getDepartmentReportPeriodId());

        // Проверка открытости периода
        if (!departmentReportPeriod.isActive()) {
            LOG.warn(String.format(REPORT_PERIOD_IS_CLOSED_LOG, formData.getReportPeriodId()));
            return result;
        }

        // Призрак передачи НФ на вышестоящий уровень
        boolean sendToNextLevel;
        try {
            // Проверки статуса приемников
            sendToNextLevel = checkDestinations(formDataId, userInfo, new Logger());
        } catch (ServiceException e) {
            // Нет
            return result;
        }

        // Признак контролера вышестоящего уровня
        boolean isUpControl = userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && formData.getDepartmentId() != userInfo.getUser().getDepartmentId();

        if (asList(FormDataKind.PRIMARY, FormDataKind.ADDITIONAL).contains(formData.getKind()) &&
                sendToNextLevel) {
            // Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор» и передаваемых на
            // вышестоящий уровень

            switch (formData.getState()) {
                case CREATED:
                    // Повысить статус могут все, кто имеет доступ для чтения
                    if (userInfo.getUser().hasRole(TARole.ROLE_OPER)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        result.add(WorkflowMove.CREATED_TO_PREPARED);
                    }
                    break;
                case PREPARED:
                    // Повысить и понизить статус могут все контролеры, которые имеют доступ для чтения
                    if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        result.add(WorkflowMove.PREPARED_TO_CREATED);
                        result.add(WorkflowMove.PREPARED_TO_APPROVED);
                    }
                    break;
                case APPROVED:
                    // Повысить и понизить статус могут контролеры вышестоящего уровня, которые имеют доступ для чтения
                    if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)) {
                        result.add(WorkflowMove.APPROVED_TO_PREPARED);
                        result.add(WorkflowMove.APPROVED_TO_ACCEPTED);
                    }
                    break;
                case ACCEPTED:
                    // Понизить статус могут контролеры вышестоящего уровня, которые имеют доступ для чтения
                    // Форма "Согласование организации" не распринимается
                    if ((userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS))) {
                        result.add(WorkflowMove.ACCEPTED_TO_APPROVED);
                    }
                    break;
                default:
                    LOG.warn(String.format(FORM_DATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES,
                            formData.getKind().getTitle(), formData.getState().getTitle()));
            }
        } else if (asList(FormDataKind.PRIMARY, FormDataKind.ADDITIONAL, FormDataKind.UNP).contains(
                formData.getKind()) && !sendToNextLevel) {
            // Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор» и НЕ передаваемых на
            // вышестоящий уровень
            switch (formData.getState()) {
                case CREATED:
                    // Повысить статус могут все, кто имеет доступ для чтения
                    if (userInfo.getUser().hasRole(TARole.ROLE_OPER)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        result.add(WorkflowMove.CREATED_TO_PREPARED);
                    }
                    break;
                case PREPARED:
                    // Повысить и понизить статус могут все контролеры, которые имеют доступ для чтения
                    if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        result.add(WorkflowMove.PREPARED_TO_CREATED);
                        result.add(WorkflowMove.PREPARED_TO_ACCEPTED);
                    }
                    break;
                case APPROVED:
                    // !!!!!!    Такая ситуация по идее не возможна, но если некорректно манипулировать источникми-приемниками то может произойти  !!!!!
                    // в этом случае отображаем "шаг назад" без условий чтобы вывести нф из тупика
                    result.add(WorkflowMove.APPROVED_TO_PREPARED);
                    break;
                case ACCEPTED:
                    // Понизить статус могут контролеры вышестоящего уровня, которые имеют доступ для чтения
                    if (isUpControl || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)) {
                        result.add(WorkflowMove.ACCEPTED_TO_PREPARED);
                    }
                    break;
                default:
                    LOG.warn(String.format(FORM_DATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES,
                            formData.getKind().getTitle(), formData.getState().getTitle()));
            }
        } else if (asList(FormDataKind.SUMMARY, FormDataKind.CONSOLIDATED, FormDataKind.CALCULATED).contains(formData.getKind())
                && !sendToNextLevel) {
            // Жизненный цикл налоговых форм, формируемых автоматически и НЕ передаваемых на вышестоящий уровень
            switch (formData.getState()) {
                case CREATED:
                    // Повысить статус могут все контролеры, которые имеют доступ для чтения
                    if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        result.add(WorkflowMove.CREATED_TO_ACCEPTED);
                    }
                    break;
                case APPROVED:
                    // !!!!!!    Такая ситуация по идее не возможна, но если некорректно манипулировать источникми-приемниками то может произойти  !!!!!
                    // в этом случае отображаем "шаг назад" без условий чтобы вывести нф из тупика
                    result.add(WorkflowMove.APPROVED_TO_CREATED);
                    break;
                case ACCEPTED:
                    // Понизить статус могут все контролеры, которые имеют доступ для чтения
                    if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        result.add(WorkflowMove.ACCEPTED_TO_CREATED);
                    }
                    break;
                default:
                    LOG.warn(String.format(FORM_DATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES,
                            formData.getKind().getTitle(), formData.getState().getTitle()));

            }
        } else if (asList(FormDataKind.SUMMARY, FormDataKind.CONSOLIDATED, FormDataKind.CALCULATED).contains(formData.getKind())
                && sendToNextLevel) {
            // Жизненный цикл налоговых форм, формируемых автоматически и передаваемых на вышестоящий уровень
            switch (formData.getState()) {
                case CREATED:
                    // Повысить статус могут все контролеры, которые имеют доступ для чтения
                    if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        result.add(WorkflowMove.CREATED_TO_APPROVED);
                    }
                    break;
                case APPROVED:
                    // Пониизить статус могут все контролеры, которые имеют доступ для чтения
                    if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                        result.add(WorkflowMove.APPROVED_TO_CREATED);
                    }
                    // Повысить статус могут контролеры вышестоящего уровня, которые имеют доступ для чтения
                    if (isUpControl || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)) {
                        result.add(WorkflowMove.APPROVED_TO_ACCEPTED);
                    }
                    break;
                case ACCEPTED:
                    // Понизить статус могут контролеры вышестоящего уровня, которые имеют доступ для чтения
                    if (isUpControl || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)
                            || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)) {
                        result.add(WorkflowMove.ACCEPTED_TO_APPROVED);
                    }
                    break;
                default:
                    LOG.warn(String.format(FORM_DATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getTitle(), formData.getState().getTitle()));
            }
        } else {
            LOG.warn(String.format(FORM_DATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES,
                    formData.getKind().getTitle(), formData.getState().getTitle()));
        }
        return result;
    }

    @Override
    public FormDataAccessParams getFormDataAccessParams(TAUserInfo userInfo, long formDataId, boolean manual) {
        FormDataAccessParams result = new FormDataAccessParams();
        result.setCanRead(false);
        result.setCanEdit(false);
        result.setCanDelete(false);

        // Чтение
        try {
            canRead(userInfo, formDataId);
            result.setCanRead(true);
        } catch (AccessDeniedException e) {
            result.setAvailableWorkflowMoves(new ArrayList<WorkflowMove>(0));
            return result;
        }

        // Доступные переходы
        result.setAvailableWorkflowMoves(getAvailableMovesWithoutCanRead(userInfo, formDataId));

        // Редактирование
        try {
            canEdit(userInfo, formDataId, manual);
            result.setCanEdit(true);
        } catch (AccessDeniedException e) {
            return result;
        }

        // Удаление
        if (formDataDao.getWithoutRows(formDataId).getState() == WorkflowState.CREATED) {
            // Удалять можно только только доступные для чтения и редактирования и в статусе "Создана"
            result.setCanDelete(true);
        }

        return result;
    }

    @Override
    public List<FormDataKind> getAvailableFormDataKind(TAUserInfo userInfo, List<TaxType> taxTypes) {
        List<FormDataKind> formDataKindList = new ArrayList<FormDataKind>(FormDataKind.values().length);
        // http://conf.aplana.com/pages/viewpage.action?pageId=11386069
        formDataKindList.add(FormDataKind.PRIMARY);
        if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            if (!taxTypes.contains(TaxType.LAND))
                formDataKindList.add(FormDataKind.CONSOLIDATED);
            if (!taxTypes.contains(TaxType.VAT))
                formDataKindList.add(FormDataKind.SUMMARY);
        }
        if (taxTypes.contains(TaxType.INCOME)) {
            formDataKindList.add(FormDataKind.ADDITIONAL);
            formDataKindList.add(FormDataKind.UNP);
        }
        if (taxTypes.contains(TaxType.ETR)) {
            formDataKindList.add(FormDataKind.CALCULATED);
        }
        return formDataKindList;
    }

    /**
     * Проверка пересечения периода актуальности макета и отчетного периода
     * @return true если есть пересечение
     */
    private boolean isTemplateIntersectReportPeriod(FormTemplate formTemplate, Integer reportPeriodId) {
        if (formTemplate.getStatus() != VersionedObjectStatus.NORMAL)
            return false;
        Date templateEndDate = formTemplateService.getFTEndDate(formTemplate.getId());

        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(reportPeriodId);
        //Сказали что дату окончания не обязательно сравнивать, т.к. она при сохранении макета должна быть кратна отчетному периоду
        if (templateEndDate != null)
            return  formTemplate.getVersion().compareTo(reportPeriod.getStartDate()) <= 0 && templateEndDate.compareTo(reportPeriod.getEndDate()) >= 0;
        else
            return formTemplate.getVersion().compareTo(reportPeriod.getStartDate()) <= 0
                    || formTemplate.getVersion().compareTo(reportPeriod.getEndDate()) <= 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkDestinations(long formDataId, TAUserInfo userInfo, Logger logger) {
        // Признак наличия назначения-приемника в другом подразделении
        boolean retVal = false;
        // НФ
        FormData formData = formDataDao.getWithoutRows(formDataId);

        List<Relation> destinations = sourceService.getDestinationsInfo(formData, true, true, null, userInfo, logger);
        for (Relation form : destinations) {
            // Если форма существует и ее статус отличен от «Создана»
            if (form.isCreated() && form.getState() != WorkflowState.CREATED) {
                throw new ServiceException("Переход невозможен, т.к. уже подготовлена/утверждена/принята вышестоящая налоговая форма.");
            }
            if (formData.getDepartmentId() != form.getDepartmentId()) {
                retVal = true;
            }
        }

        // Назначения деклараций-приемников в периоде отчетного периода
        destinations = sourceService.getDeclarationDestinationsInfo(formData, true, true, null, userInfo, logger);
        for (Relation declaration : destinations) {
            // Если декларация существует и статус "Принята"
            if (declaration.isCreated() && declaration.getState() == WorkflowState.ACCEPTED) {
                throw new ServiceException("Переход невозможен, т.к. уже %s.",
                        formData.getFormType().getTaxType() == TaxType.DEAL ? "принято уведомление" : "принята декларация"
                );
            }
            if (!retVal) {
                if (formData.getDepartmentId() != declaration.getDepartmentId()) {
                    retVal = true;
                }
            }
        }

        return retVal;
    }
}