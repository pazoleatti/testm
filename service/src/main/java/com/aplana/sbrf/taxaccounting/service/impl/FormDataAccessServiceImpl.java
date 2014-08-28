package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

@Service
public class FormDataAccessServiceImpl implements FormDataAccessService {

    private static final Log logger = LogFactory.getLog(FormDataAccessServiceImpl.class);

    public static final String LOG_EVENT_AVAILABLE_MOVES = "LOG_EVENT_AVAILABLE_MOVES";
    public static final String LOG_EVENT_READ = "READ";
    public static final String LOG_EVENT_EDIT = "EDIT";
    //public static final String LOG_EVENT_CREATE = "CREATE";

    public static final String LOG_EVENT_READ_RU = "чтение";
    public static final String LOG_EVENT_EDIT_RU = "редактирование";
    public static final String LOG_EVENT_DELETE_RU = "удаление";

    private static final String FORMDATA_KIND_STATE_ERROR_LOG = "Event type: \"%s\". Unsuppotable case for formData with \"%s\" kind and \"%s\" state!";
    private static final String REPORT_PERIOD_IS_CLOSED_LOG = "Report period (%d) is closed!";
    private static final String REPORT_PERIOD_IS_CLOSED = "Выбранный период закрыт!";
    private static final String FORM_TEMPLATE_WRONG_STATUS_LOG = "Form template (%d) does not exist in report period (%d)!";
    private static final String FORM_TEMPLATE_WRONG_STATUS = "Выбранный вид налоговой формы не существует в выбранном периоде!";
    private static final String INCORRECT_DEPARTMENT_FORM_TYPE_LOG = "Form type (%d) and form kind (%d) is not applicated for department (%d)";
    private static final String INCORRECT_DEPARTMENT_FORM_TYPE1 = "Выбранный тип налоговой формы не назначен подразделению!";
    private static final String INCORRECT_DEPARTMENT_FORM_TYPE2 = "Нет прав доступа к созданию формы с заданными параметрами!";
    private static final String INCORRECT_DEPARTMENT_FORM_TYPE3 = "Выбранный вид налоговой формы не назначен подразделению";
    private static final String CREATE_FORM_DATA_ERROR_ONLY_CONTROL_LOG = "Only ROLE_CONTOL can create form in balance period!";
    private static final String CREATE_FORM_DATA_ERROR_ONLY_CONTROL = "Выбран период ввода остатков. В периоде ввода остатков оператор не может создавать налоговые формы";
    private static final String CREATE_MANUAL_FORM_DATA_ERROR_ONLY_CONTROL_LOG = "Only ROLE_CONTOL can create manual version of form!";
    private static final String CREATE_MANUAL_FORM_DATA_ERROR_ONLY_CONTROL = "Только контролер может создавать версию ручного ввода";
    // private static final String CREATE_FORM_DATA_ERROR_ACCESS_DENIED = "Недостаточно прав для создания налоговой формы с указанными параметрами";
    private static final String FORM_DATA_ERROR_ACCESS_DENIED = "Недостаточно прав на %s формы с типом \"%s\" в статусе \"%s\"!";
    private static final String FORM_DATA_DEPARTMENT_ACCESS_DENIED_LOG = "Selected department (%d) not available in report period (%d)!";
    private static final String FORM_DATA_DEPARTMENT_ACCESS_DENIED = "Выбранное подразделение недоступно для пользователя!";
    private static final String FORM_DATA_EDIT_ERROR = "Нельзя редактировать форму \"%s\" в состоянии \"%s\"";
    private static final String ERROR_PERIOD = "Переход невозможен, т.к. у одного из приемников период не открыт.";

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
        List<Integer> avaibleDepartmentList = departmentService.getTaxFormDepartments(userInfo.getUser(),
                asList(formData.getFormType().getTaxType()), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());

        // Создаваемые вручную формы (читают все, имеющие доступ к подразделению в любом статусе)
        if (asList(FormDataKind.ADDITIONAL, FormDataKind.PRIMARY, FormDataKind.UNP).contains(formData.getKind())
                && (userInfo.getUser().hasRole(TARole.ROLE_OPER)
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS))
                && avaibleDepartmentList.contains(formData.getDepartmentId())) {
            return;
        }

        // Создаваемые автоматически формы (читают все контролеры, имеющие доступ к подразделению в любом статусе)
        if (asList(FormDataKind.CONSOLIDATED, FormDataKind.SUMMARY).contains(formData.getKind())
                && (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS))
                && avaibleDepartmentList.contains(formData.getDepartmentId())) {
            // Передаваемые на вышестоящий уровень (читают все контролеры, имеющие доступ к подразделению в любом статусе)
            return;
        }

        // Непредусмотренное сочетание параметров состояния формы и пользователя - запрет доступа
        // Или подразделение недоступно
        logger.error(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_READ, formData.getKind().getName(),
                formData.getState().getName()));

        throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_READ_RU,
                formData.getKind().getName(), formData.getState().getName()));
    }

    @Override
    public void canCreate(TAUserInfo userInfo, int formTemplateId, FormDataKind kind, int departmentId, int reportPeriodId) {
        // http://conf.aplana.com/pages/viewpage.action?pageId=11383566

        // Если выбранный "Период" закрыт, то система выводит сообщение в панель уведомления:
        // "Выбранный период закрыт".
        if (!reportPeriodService.isActivePeriod(reportPeriodId, departmentId)) {
            logger.warn(String.format(REPORT_PERIOD_IS_CLOSED_LOG, reportPeriodId));
            throw new ServiceException(REPORT_PERIOD_IS_CLOSED);
        }

        // Проверка периода ввода остатков
        if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)
                && reportPeriodService.isBalancePeriod(reportPeriodId, departmentId)) {
            logger.warn(String.format(CREATE_FORM_DATA_ERROR_ONLY_CONTROL_LOG));
            throw new ServiceException(CREATE_FORM_DATA_ERROR_ONLY_CONTROL);
        }

        // Макет формы
        FormTemplate formTemplate = formTemplateDao.get(formTemplateId);

        // Проверка доступности подразделения
        if (!departmentService.getOpenPeriodDepartments(userInfo.getUser(),
                asList(formTemplate.getType().getTaxType()),
                reportPeriodId).contains(departmentId)) {
            logger.warn(String.format(FORM_DATA_DEPARTMENT_ACCESS_DENIED_LOG, departmentId, reportPeriodId));
            throw new ServiceException(FORM_DATA_DEPARTMENT_ACCESS_DENIED);
        }

        // Id макета
        int formTypeId = formTemplate.getType().getId();

        // Вид формы
        FormType formType = formTypeDao.get(formTypeId);
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(reportPeriodId);

        // Если выбранный "Вид формы" не назначен выбранному подразделению,
        // то система выводит сообщение в панель уведомления: "Выбранный вид налоговой формы не назначен подразделению".
        // Если у пользователя нет доступа к выбранному виду формы, то система выводит сообщение в панель уведомления:
        // "Нет прав доступа к созданию формы с заданными параметрами".
        boolean foundTypeAndKind = false;
        boolean foundKind = false;
        for (DepartmentFormType dft : sourceService.getDFTByDepartment(departmentId, formType.getTaxType(),
                reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate())) {
            if (dft.getKind() == kind) {
                foundKind = true;
                if (dft.getFormTypeId() == formTypeId) {
                    foundTypeAndKind = true;
                    break;
                }
            }
        }
        if (!foundTypeAndKind) {
            logger.warn(String.format(INCORRECT_DEPARTMENT_FORM_TYPE_LOG, formTypeId, kind.getId(), departmentId));
            throw new ServiceException(INCORRECT_DEPARTMENT_FORM_TYPE3);
        }
        if (!foundKind) {
            logger.warn(String.format(INCORRECT_DEPARTMENT_FORM_TYPE_LOG, formTypeId, kind.getId(), departmentId));
            throw new ServiceException(INCORRECT_DEPARTMENT_FORM_TYPE1);
        }

        // Доступные типы форм
        List<FormDataKind> formDataKindList = getAvailableFormDataKind(userInfo, asList(formTemplate.getType().getTaxType()));
        if (!formDataKindList.contains(kind)) {
            logger.warn(String.format(INCORRECT_DEPARTMENT_FORM_TYPE_LOG, formTypeId, kind.getId(), departmentId));
            throw new ServiceException(INCORRECT_DEPARTMENT_FORM_TYPE2);
        }

        // Если период актуальности макета, выбранного в поле "Вид формы", не пересекается с выбранным отчетным
        // периодом ИЛИ пересекается, но его STATUS не равен 0, то система выводит сообщение в панель уведомления:
        // "Выбранный вид налоговой формы не существует в выбранном периоде"
        boolean intersect = isTemplateIntesectReportPeriod(formTemplate, reportPeriodId);
        if (!intersect || formTemplate.getStatus() != VersionedObjectStatus.NORMAL) {
            logger.warn(String.format(FORM_TEMPLATE_WRONG_STATUS_LOG, formTemplate.getId(), reportPeriodId));
            throw new AccessDeniedException(FORM_TEMPLATE_WRONG_STATUS);
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

        //Период формы - открыт?
        if (!reportPeriodService.isActivePeriod(formData.getReportPeriodId(), formData.getDepartmentId())) {
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
                    "Произошли ошибки при проверке необходимых условий для перевода формы в режим ручного ввода",
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public void canEdit(TAUserInfo userInfo, long formDataId, boolean manual) {
        FormData formData = formDataDao.getWithoutRows(formDataId);
        // Проверка закрытого периода
        if (!reportPeriodService.isActivePeriod(formData.getReportPeriodId(), formData.getDepartmentId())) {
            throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
                    formData.getKind().getName(), formData.getState().getName()) + " Период закрыт!");
        }

		// Проверка периода ввода остатков
		if (reportPeriodService.isBalancePeriod(formData.getReportPeriodId(), formData.getDepartmentId())) {
			switch (formData.getState()) {
				case CREATED:
					// Созданные редактируют только контролеры, которые могут открыть форму для чтения
					if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
							&& !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
							&& !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
						throw new AccessDeniedException(
								String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
										formData.getKind().getName(), formData.getState().getName()));
					}
					return;
				case ACCEPTED:
					if (!manual) {
						// Нельзя редактировать в состоянии "Принята"
						throw new AccessDeniedException(String.format(FORM_DATA_EDIT_ERROR,
								formData.getFormType().getName(), formData.getState().getName()));
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
								formData.getKind().getName(), formData.getState().getName()));
					}
					return;
				case APPROVED:
					// Подготовленные редактируют только контролеры вышестоящего уровня, которые могут открыть форму для чтения
					// Не контролеры
					if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
							&& !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
							&& !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
						throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
								formData.getKind().getName(), formData.getState().getName()));
					}
					// Контролеры текущего уровня
					if ((userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
							|| userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS))
							&& userInfo.getUser().getDepartmentId() == formData.getDepartmentId()) {
						throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
								formData.getKind().getName(), formData.getState().getName()));
					}
					return;
				case ACCEPTED:
					if (!manual) {
						// Нельзя редактировать НФ в состоянии "Принята"
						throw new AccessDeniedException(String.format(FORM_DATA_EDIT_ERROR,
								formData.getFormType().getName(), formData.getState().getName()));
					}
			}
		}

		// Создаваемые автоматически формы
		if (asList(FormDataKind.CONSOLIDATED, FormDataKind.SUMMARY).contains(formData.getKind())) {
			switch (formData.getState()) {
				case CREATED:
					// Созданные редактируют только контролеры, которые могут открыть форму для чтения
					if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
							&& !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
							&& !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
						throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
								formData.getKind().getName(), formData.getState().getName()));
					}
					return;
				case APPROVED:
				case ACCEPTED:
					if (!manual) {
						// Нельзя редактировать НФ в состоянии "Принята"
						throw new AccessDeniedException(String.format(FORM_DATA_EDIT_ERROR,
								formData.getFormType().getName(), formData.getState().getName()));
					}
					return;
			}
		}

        // Непредвиденное состояние формы
        logger.error(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_EDIT, formData.getKind().getName(),
                formData.getState().getName()));
        throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU,
                formData.getKind().getName(), formData.getState().getName()));
    }

    @Override
    public void canDelete(TAUserInfo userInfo, long formDataId) {
        FormData formData = formDataDao.getWithoutRows(formDataId);
        if (formData.getState() != WorkflowState.CREATED) {
            throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_DELETE_RU,
                    formData.getKind().getName(), formData.getState().getName()));
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

        //Период формы - открыт?
        if (!reportPeriodService.isActivePeriod(formData.getReportPeriodId(), formData.getDepartmentId())) {
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
        ReportPeriod reportPeriod = periodService.getReportPeriod(formData.getReportPeriodId());

        if (checkForDestinations(formData, reportPeriod)) return result;

        // Проверка открытости периода
        if (!reportPeriodService.isActivePeriod(formData.getReportPeriodId(), formData.getDepartmentId())) {
            logger.warn(String.format(REPORT_PERIOD_IS_CLOSED_LOG, formData.getReportPeriodId()));
            return result;
        }

        // Проверка периода ввода остатков
        if (reportPeriodService.isBalancePeriod(formData.getReportPeriodId(), formData.getDepartmentId())) {
            switch (formData.getState()) {
                case CREATED:
                    result.add(WorkflowMove.CREATED_TO_ACCEPTED);
                    break;
                case ACCEPTED:
                    result.add(WorkflowMove.ACCEPTED_TO_CREATED);
                    break;
                default:
                    logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES,
                            formData.getKind().getName(), formData.getState()));
            }
        } else {
            try {
                checkDestinations(formDataId);
            } catch (ServiceException e) {
                return result;
            }
            // Связи НФ -> НФ
            List<DepartmentFormType> formDestinations = departmentFormTypeDao.getFormDestinations(
                    formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind(),
                    reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
            // Призрак передачи НФ на вышестоящий уровень
            boolean sendToNextLevel = false;
            for (DepartmentFormType destination : formDestinations) {
                if (formData.getDepartmentId() != destination.getDepartmentId()) {
                    sendToNextLevel = true;
                    break;
                }
            }
            if (!sendToNextLevel) {
                // Связи НФ -> Декларация
                List<DepartmentDeclarationType> declarationDestinations =
                        departmentFormTypeDao.getDeclarationDestinations(formData.getDepartmentId(),
                                formData.getFormType().getId(), formData.getKind(),
                                reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
                for (DepartmentDeclarationType destination : declarationDestinations) {
                    if (formData.getDepartmentId() != destination.getDepartmentId()) {
                        sendToNextLevel = true;
                        break;
                    }
                }
            }

            // Признак контролера вышестоящего уровня
            boolean isUpControl = (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                    || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS))
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
                        if (isUpControl || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                            result.add(WorkflowMove.APPROVED_TO_PREPARED);
                            result.add(WorkflowMove.APPROVED_TO_ACCEPTED);
                        }
                        break;
                    case ACCEPTED:
                        // Понизить статус могут контролеры вышестоящего уровня, которые имеют доступ для чтения
                        // Форма "Согласование организации" не распринимается
                        if ((isUpControl || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP))) {
                            result.add(WorkflowMove.ACCEPTED_TO_APPROVED);
                        }
                        break;
                    default:
                        logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES,
                                formData.getKind().getName(), formData.getState().getName()));
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
                    case ACCEPTED:
                        // Понизить статус могут контролеры вышестоящего уровня, которые имеют доступ для чтения
                        if (isUpControl || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                            result.add(WorkflowMove.ACCEPTED_TO_PREPARED);
                        }
                        break;
                    default:
                        logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES,
                                formData.getKind().getName(), formData.getState().getName()));
                }
            } else if (asList(FormDataKind.SUMMARY, FormDataKind.CONSOLIDATED).contains(formData.getKind())
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
                    case ACCEPTED:
                        // Понизить статус могут все контролеры, которые имеют доступ для чтения
                        if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                            result.add(WorkflowMove.ACCEPTED_TO_CREATED);
                        }
                        break;
                    default:
                        logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES,
                                formData.getKind().getName(), formData.getState().getName()));

                }
            } else if (asList(FormDataKind.SUMMARY, FormDataKind.CONSOLIDATED).contains(formData.getKind())
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
                        if (isUpControl || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                            result.add(WorkflowMove.APPROVED_TO_ACCEPTED);
                        }
                        break;
                    case ACCEPTED:
                        // Понизить статус могут контролеры вышестоящего уровня, которые имеют доступ для чтения
                        if (isUpControl || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
                            result.add(WorkflowMove.ACCEPTED_TO_APPROVED);
                        }
                        break;
                    default:
                        logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), formData.getState().getName()));
                }
            } else {
                logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES,
                        formData.getKind().getName(), formData.getState().getName()));
            }
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
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            formDataKindList.add(FormDataKind.CONSOLIDATED);
            if (!taxTypes.contains(TaxType.VAT))
                formDataKindList.add(FormDataKind.SUMMARY);
        }
        if (taxTypes.contains(TaxType.INCOME)) {
            formDataKindList.add(FormDataKind.ADDITIONAL);
            formDataKindList.add(FormDataKind.UNP);
        }
        return formDataKindList;
    }

    /**
     * Проверка пересечения периода актуальности макета и отчетного периода
     * @return true если есть пересечение
     */
    private boolean isTemplateIntesectReportPeriod(FormTemplate formTemplate, Integer reportPeriodId) {
        // TODO Таск http://jira.aplana.com/browse/SBRFACCTAX-5509
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
    public void checkDestinations(long formDataId) {
        FormData formData = formDataDao.getWithoutRows(formDataId);
        ReportPeriod reportPeriod = periodService.getReportPeriod(formData.getReportPeriodId());
        // Проверка вышестоящих налоговых форм
        List<DepartmentFormType> departmentFormTypes =
                departmentFormTypeDao.getFormDestinations(formData.getDepartmentId(),
                        formData.getFormType().getId(), formData.getKind(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        if (departmentFormTypes != null) {
            for (DepartmentFormType departmentFormType : departmentFormTypes) {
                FormData form = formDataService.findFormData(departmentFormType.getFormTypeId(), departmentFormType.getKind(),
                        departmentFormType.getDepartmentId(), formData.getReportPeriodId(), formData.getPeriodOrder());
                // Если форма существует и статус отличен от "Создана"
                if (form != null && form.getState() != WorkflowState.CREATED) {
                    throw new ServiceException("Переход невозможен, т.к. уже подготовлена/утверждена/принята вышестоящая налоговая форма.");
                }
                if (!reportPeriodService.isActivePeriod(formData.getReportPeriodId(), departmentFormType.getDepartmentId())) {
                    throw new ServiceException(ERROR_PERIOD);
                }
            }
        }

        // Проверка вышестоящих деклараций
        List<DepartmentDeclarationType> departmentDeclarationTypes = sourceService.getDeclarationDestinations(
                formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind(), formData.getReportPeriodId());
        if (departmentDeclarationTypes != null) {
            for (DepartmentDeclarationType departmentDeclarationType : departmentDeclarationTypes) {
                DeclarationData declaration = declarationDataDao.find(departmentDeclarationType.getDeclarationTypeId(),
                        departmentDeclarationType.getDepartmentId(), formData.getReportPeriodId());
                // Если декларация существует и статус "Принята"
                if (declaration != null && declaration.isAccepted()) {
                    String str = formData.getFormType().getTaxType() == TaxType.DEAL ? "принято уведомление" :
                            "принята декларация";
                    throw new ServiceException("Переход невозможен, т.к. уже " + str + ".");
                }
                if (declaration != null && !reportPeriodService.isActivePeriod(formData.getReportPeriodId(), declaration.getDepartmentId())) {
                    throw new ServiceException(ERROR_PERIOD);
                }
            }
        }
    }

    public boolean checkForDestinations(FormData formData, ReportPeriod reportPeriod) {
        // Проверка вышестоящих налоговых форм
        List<DepartmentFormType> departmentFormTypes =
                departmentFormTypeDao.getFormDestinations(formData.getDepartmentId(),
                        formData.getFormType().getId(), formData.getKind(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        if (departmentFormTypes != null) {
            for (DepartmentFormType departmentFormType : departmentFormTypes) {
                FormData form = formDataService.findFormData(departmentFormType.getFormTypeId(), departmentFormType.getKind(),
                        departmentFormType.getDepartmentId(), formData.getReportPeriodId(), formData.getPeriodOrder());
                // Если форма существует и статус отличен от "Создана"
                if (form != null && form.getState() != WorkflowState.CREATED) {
                    return true;
                }
            }
        }
        return false;
    }
}