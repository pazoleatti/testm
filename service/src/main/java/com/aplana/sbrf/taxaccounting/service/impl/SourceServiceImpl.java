package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.SourceDao;
import com.aplana.sbrf.taxaccounting.dao.api.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.source.*;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class SourceServiceImpl implements SourceService {

    private static final String CHECK_EXISTENCE_MSG = "Невозможно назначить источники / приемники: Форма \"%s\" не назначена подразделению %s";
    private static final String FATAL_SAVE_MSG = "Назначение источников-приёмников не выполнено";
    private static final String FATAL_DELETE_MSG = "Удаление назначения источников-приёмников не выполнено";
    private static final String SOURCES_LIST_IS_EMPTY_MSG = "Все назначения были исключены в результате проверок. Продолжение операции невозможно.";
    private static final String EMPTY_END_PERIOD_INFO = "дата окончания периода не задана";
    private static final String INTERSECTION_PART = "\"%s\" в качестве %s для \"%s\" в периоде %s";
    private static final String INTERSECTION_MSG_BEGIN = "Найдены существующие назначения:";
    private static final String INTERSECTION_MSG_END = "Новое назначение создано путем слияния с найденными назначениями, новое назначение действует в периоде %s.";
    private static final String SAVE_SUCCESS_MSG = "\"%s\" назначен %s формы \"%s\" в периоде %s.";
    private static final String DELETE_SUCCESS_MSG = "Удалено назначение \"%s\" в роли %s %s \"%s\" в периоде %s.";
    private static final String UPDATE_SUCCESS_MSG = "\"%s\" назначен %s формы \"%s\" в периоде %s.";
    private static final String CIRCLE_MSG = "\"%s\" уже назначен как приёмник \"%s\"";
    private static final String FORM_INSTANCES_MSG = "Для корректной передачи данных в форму-приёмник необходимо выполнить повторный перевод в статус \"Принята\" формы \"%s\" для подразделения \"%s\" в периодах: \"%s\"";
    private static final String DECLARATION_INSTANCES_MSG = "Для корректного получении данных необходимо выполнить повторное формирование при помощи кнопки \"Обновить\" во всех экземплярах \"%s\" для подразделения \"%s\" в периодах: \"%s\"";
    private static final String EDIT_FATAL_ERROR = "Найдены экземпляры \"%s\" для подразделения \"%s\" в периодах: \"%s\" в статусе \"Принята\". Для удаления их назначения в качестве источника необходимо выполнить их возврат из статуса \"Принята\"";
    private static final String DELETE_FATAL_ERROR_BEGIN = "Найдены экземпляры в статусе \"Принята\":";
    private static final String DELETE_FATAL_ERROR_MID = "\"%s\" для подразделения \"%s\" в периодах: \"%s\"";
    private static final String DELETE_FATAL_ERROR_END = "Для удаления их назначения в качестве источника необходимо выполнить их возврат из статуса \"Принята\"";
    private static final String EMPTY_LIST_MSG = "Список назначений пуст!";

    private enum LOG_LEVEL {
        INFO, WARN, ERROR
    }

    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return SIMPLE_DATE_FORMAT;
        }
    };

    private static final Calendar CALENDAR = Calendar.getInstance();
    private static final Date MAX_DATE;
    private static final Date MIN_DATE;
    static {
        CALENDAR.clear();
        CALENDAR.set(9999, Calendar.DECEMBER, 31);
        MAX_DATE = CALENDAR.getTime();
        CALENDAR.clear();
        CALENDAR.set(CALENDAR.getGreatestMinimum(Calendar.YEAR), Calendar.JANUARY, 31);
        MIN_DATE = CALENDAR.getTime();
    }

    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;

    @Autowired
    private DepartmentDeclarationTypeDao departmentDeclarationTypeDao;

    @Autowired
    private FormTypeDao formTypeDao;

    @Autowired
    private DeclarationTypeDao declarationTypeDao;

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SourceDao sourceDao;

    @Autowired
    private FormDataDao formDataDao;

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private FormTemplateDao formTemplateDao;

    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;

    @Autowired
    private PeriodService reportPeriodService;

    @Override
    public List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind, Date periodStart,
                                                       Date periodEnd) {
        QueryParams<SourcesSearchOrdering> filter = getSearchOrderingDefaultFilter();
        return getDFTSourcesByDFT(departmentId, formTypeId, kind, periodStart, periodEnd, filter);
    }

    @Override
    public List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind, Date periodStart,
                                                       Date periodEnd, QueryParams queryParams) {
        return departmentFormTypeDao.getFormSources(departmentId, formTypeId, kind, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind, int reportPeriodId) {
        QueryParams<SourcesSearchOrdering> queryParams = getSearchOrderingDefaultFilter();
        return getDFTSourcesByDFT(departmentId, formTypeId, kind, reportPeriodId, queryParams);
    }

    @Override
    public List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind, int reportPeriodId,
                                                       QueryParams queryParams) {
        ReportPeriod period = reportPeriodDao.get(reportPeriodId);
        return getDFTSourcesByDFT(departmentId, formTypeId, kind, period.getStartDate(), period.getEndDate(), queryParams);
    }

    @Override
    public List<DepartmentFormType> getDFTSourceByDDT(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd) {
        QueryParams<SourcesSearchOrdering> queryParams = getSearchOrderingDefaultFilter();
        return getDFTSourceByDDT(departmentId, declarationTypeId, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<DepartmentFormType> getDFTSourceByDDT(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd, QueryParams queryParams) {
        return departmentFormTypeDao.getDeclarationSources(departmentId, declarationTypeId, periodStart, periodEnd, queryParams);
    }

    /**
     * Интерфейс для создания анонимных классов, предназначенных для формирования сообщений при исключении назначений из обработки
     */
    private interface MessageBuilder {
        /**
         * Возвращает сообщение при обнаружении вхождения исключаемого назначения в списке источников
         *
         * @param sourcePair обрабатываемая пара источник-приемник
         * @return сообщение
         */
        List<String> getMessage(SourcePair sourcePair);
    }

    /**
     * Метод исключает указанные назначения из списка пар источников-приемников
     *
     * @param logger                   логгер
     * @param sourcePairs              оригинальный список пар источников-приемников
     * @param errorPairs               список назначений, которые должны быть исключены
     * @param isDeclaration            признак того, что идет обработка в режиме "Декларации"
     * @param emptyIsOk                признак того, что если в результате выполнения входной список оказывается пуст - это нормальная ситуация.
     *                                 Например в случае пересечения версий, дополнительная обработка не требуется - версии склеиваются на стадии проверки пересечений
     * @param level                    уровень логгирования
     * @param messageBuilder           билдер для построения информационных сообщений
     * @return обрезанный входной список пар источников-приемников
     */
    public List<SourcePair> truncateSources(Logger logger, List<SourcePair> sourcePairs,
                                            List<SourcePair> errorPairs,
                                            boolean isDeclaration, boolean emptyIsOk, LOG_LEVEL level,
                                            MessageBuilder messageBuilder) {
        List<SourcePair> sourcePairsOut = new LinkedList<SourcePair>(sourcePairs);
        for (SourcePair error : errorPairs) {
            for (Iterator<SourcePair> it = sourcePairsOut.iterator(); it.hasNext(); ) {
                SourcePair pair = it.next();
                /** Исключаем связку из обработки */
                if (pair.equals(error)) {
                    printMsgs(logger, messageBuilder.getMessage(pair), level);
                    it.remove();
                }
                /** Если единственное назначение было удалено, то продолжать нет смысла */
                if (!emptyIsOk && sourcePairsOut.size() == 0) {
                    throw new ServiceLoggerException(FATAL_SAVE_MSG,
                            logEntryService.save(logger.getEntries()));
                }
            }
        }
        /** Проверяем оставщиеся связки */
        if (!emptyIsOk && sourcePairsOut.isEmpty()) {
            throw new ServiceLoggerException(SOURCES_LIST_IS_EMPTY_MSG,
                    logEntryService.save(logger.getEntries()));
        }
        return sourcePairsOut;
    }

    private void printMsgs(Logger logger, List<String> msgs, LOG_LEVEL level) {
        for (String msg : msgs) {
            switch (level) {
                case ERROR: {
                    logger.error(msg);
                    break;
                }
                case WARN: {
                    logger.warn(msg);
                    break;
                }
                default: logger.info(msg);
            }
        }
    }

    /**
     * Формирует общий список идентификаторов назначений из списка пар источников-приемников
     *
     * @param sourcePairs список пар источников-приемников
     * @return список идентификаторов назначений
     */
    public List<Long> unionSourcePairs(List<SourcePair> sourcePairs) {
        if (sourcePairs != null && !sourcePairs.isEmpty()) {
            Set<Long> set = new HashSet<Long>();
            for (SourcePair pair : sourcePairs) {
                set.add(pair.getSource());
                set.add(pair.getDestination());
            }
            return new ArrayList<Long>(set);
        } else {
            throw new ServiceException("Список назначений пуст!");
        }
    }
    /**
     *
     * Формирует общий список идентификаторов назначений из списка пар источников-приемников
     *
     * @param objects список объектов-назначений
     * @return список идентификаторов назначений
     */
    public List<Long> unionSourceObjects(List<SourceObject> objects) {
        if (objects != null && !objects.isEmpty()) {
            Set<Long> set = new HashSet<Long>();
            for (SourceObject object : objects) {
                set.add(object.getSourcePair().getSource());
                set.add(object.getSourcePair().getDestination());
            }
            return new ArrayList<Long>(set);
        } else {
            throw new ServiceException("Список назначений пуст!");
        }
    }

    /**
     * Преобразует список пар источников-приемников в список объектов для обработки на стороне БД
     *
     * @param sourcePairs список пар источников-приемников
     * @param periodStart начало периода
     * @param periodEnd   окончание периода
     * @return список объектов
     */
    public List<SourceObject> pairsToObjects(List<SourcePair> sourcePairs, Date periodStart, Date periodEnd) {
        List<SourceObject> sourceObjects = new ArrayList<SourceObject>();
        for (SourcePair sourcePair : sourcePairs) {
            SourceObject sourceObject = new SourceObject(sourcePair, periodStart, periodEnd);
            sourceObject.setSourcePair(sourcePair);
            sourceObjects.add(sourceObject);
        }
        return sourceObjects;
    }

    /**
     * Проверка существования назначений в парах источников-приемников
     * http://conf.aplana.com/pages/viewpage.action?pageId=12321545
     * Существуют 2 варианта назначений: 1 источник - много приемников и 1 приемник - много источников.
     * Таким образом соотношение 1 к М. Если связка на стороне 1 исключается в результате проверки -  операция не может быть продолжена
     *
     * @param logger                  логгер
     * @param sourcePairs             входной набор пар источник-приемник
     * @param mode                    режим работы: назначение приемников или назначение источников
     * @param isDeclaration           признак того, что идет обработка в режиме "Декларации"
     * @param sourceDepartmentName      подразделение-источник. Необходимо только для формирования уведомлений
     * @param destinationDepartmentName подразделение-приемник. Необходимо только для формирования уведомлений
     * @return обрезанный входной список связок источников-приемников, которые все еще существуют
     */
    public List<SourcePair> checkExistence(Logger logger, List<SourcePair> sourcePairs,
                                           SourceMode mode,
                                           final boolean isDeclaration,
                                           final String sourceDepartmentName,
                                           final String destinationDepartmentName) {
        List<Long> rightPart = new ArrayList<Long>();
        if (isDeclaration) {
            if (mode == SourceMode.SOURCES) {
                //Проверяем единственный приемник
                if (sourceDao.checkDDTExistence(Arrays.asList(sourcePairs.get(0).getDestination())).isEmpty()) {
                    /** Если единственное назначение было удалено, то продолжать нет смысла */
                    logger.error(String.format(CHECK_EXISTENCE_MSG,
                            sourcePairs.get(0).getDestinationType(),
                            sourceDepartmentName));
                    throw new ServiceLoggerException(FATAL_SAVE_MSG,
                            logEntryService.save(logger.getEntries()));
                }
                for (SourcePair pair : sourcePairs) {
                    rightPart.add(pair.getSource());
                }
            } else {
                //Проверяем единственный источник
                if (sourceDao.checkDFTExistence(Arrays.asList(sourcePairs.get(0).getSource())).isEmpty()) {
                    /** Если единственное назначение было удалено, то продолжать нет смысла */
                    logger.error(String.format(CHECK_EXISTENCE_MSG,
                            sourcePairs.get(0).getSourceKind() + ": " + sourcePairs.get(0).getSourceType(),
                            sourceDepartmentName));
                    throw new ServiceLoggerException(FATAL_SAVE_MSG,
                            logEntryService.save(logger.getEntries()));
                }
                for (SourcePair pair : sourcePairs) {
                    rightPart.add(pair.getDestination());
                }
            }
        } else {
            rightPart = unionSourcePairs(sourcePairs);
        }

        List<Long> notExisting;
        if (isDeclaration && mode == SourceMode.DESTINATIONS) {
            notExisting = (List<Long>) CollectionUtils.subtract(rightPart, sourceDao.checkDDTExistence(rightPart));
        } else {
            notExisting = (List<Long>) CollectionUtils.subtract(rightPart, sourceDao.checkDFTExistence(rightPart));
        }

        List<SourcePair> notExistingPairs = new ArrayList<SourcePair>();
        for (SourcePair pair : sourcePairs) {
            if (notExisting.contains(pair.getSource()) || notExisting.contains(pair.getDestination())) {
                notExistingPairs.add(pair);
            }
        }
        return truncateSources(logger, sourcePairs, notExistingPairs, isDeclaration, false, LOG_LEVEL.ERROR,
                new MessageBuilder() {
                    @Override
                    public List<String> getMessage(SourcePair sourcePair) {
                        return Arrays.asList(String.format(CHECK_EXISTENCE_MSG,
                                sourcePair.getSourceKind() + ": " + sourcePair.getSourceType(),
                                sourceDepartmentName));
                    }
                });
    }



    /**
     * Проверяет существование экземпляров нф
     * http://conf.aplana.com/pages/viewpage.action?pageId=12321547
     *
     * @param logger         логгер
     * @param sourcePairs  входной набор пар источник-приемник
     * @param newPeriodStart начало нового периода
     * @param newPeriodEnd   окончание нового периода
     * @param sourceDepartmentName      подразделение-источник. Необходимо только для формирования уведомлений
     * @param destinationDepartmentName подразделение-приемник. Необходимо только для формирования уведомлений
     * @param declaration    признак того, что идет обработка в режиме "Декларации"
     */
    public void checkFormInstances(Logger logger, List<SourcePair> sourcePairs, Date newPeriodStart, Date newPeriodEnd,
                                    String sourceDepartmentName,
                                    String destinationDepartmentName, boolean declaration) {
        for (SourcePair sourcePair : sourcePairs) {
            /** Получаем промежуточные периоды, которые будут объединены при создании новой версии */
            List<SourceObject> emptyPeriods = sourceDao.getEmptyPeriods(sourcePair,
                    newPeriodStart, newPeriodEnd);
            List<String> acceptedSources = new ArrayList<String>();
            if (!emptyPeriods.isEmpty()) {
                for (SourceObject empty : emptyPeriods) {
                    /** Получаем источники, имеющие принятые экземпляры в промежуточных периодах */
                    acceptedSources.addAll(checkAcceptedFormData(sourceDao.findAcceptedInstances(empty.getSourcePair().getSource(),
                            empty.getPeriodStart(), empty.getPeriodEnd())));
                }
            } else {
                /** Получаем источники, имеющие принятые экземпляры в создаваемом новом периоде */
                acceptedSources.addAll(checkAcceptedFormData(sourceDao.findAcceptedInstances(sourcePair.getSource(),
                        newPeriodStart, newPeriodEnd)));
            }
            if (!acceptedSources.isEmpty()) {
                if (declaration) {
                    /** Надо обновить декларации-приемники в периодах внутри промежуточных, за которые есть принятые источники (входные) */
                    logger.warn(String.format(DECLARATION_INSTANCES_MSG,
                            sourcePair.getDestinationType(),
                            destinationDepartmentName,
                            StringUtils.join(acceptedSources, ", ")));
                }  else {
                    /** Надо перепринять формы-источники, отчетный период которых попадает в промежуточные периоды */
                    logger.warn(String.format(FORM_INSTANCES_MSG,
                            sourcePair.getSourceKind() + ": " + sourcePair.getSourceType(),
                            sourceDepartmentName,
                            StringUtils.join(acceptedSources, ", ")));
                }
            }
        }
    }

    /**
     * Выполняет проверку зацикливания указанных назначений в указанном периоде. Назначений формирующие циклы исключаются из обработки
     * http://conf.aplana.com/pages/viewpage.action?pageId=12321826
     *
     * @param logger        логгер
     * @param periodStart   начало периода
     * @param periodEnd     конец периода
     * @param sourcePairs   список связок источников-приемников
     * @param mode          режим работы
     * @param isDeclaration признак того, что идет обработка в режиме "Декларации"
     * @return обрезанный входной список связок источников-приемников, для которых не найдены зацикливания
     */
    public List<SourcePair> checkLoops(Logger logger, Date periodStart, Date periodEnd,
                                       List<SourcePair> sourcePairs,
                                       SourceMode mode,
                                       boolean isDeclaration) {
        /** Получаем данные о зацикливаниях */
        final Map<SourcePair, SourcePair> loopsMap = sourceDao.getLoops(sourcePairs, periodStart, periodEnd);
        if (!loopsMap.isEmpty()) {
            Set<Long> circleCauses = new HashSet<Long>();
            //Список с назначениями, в которых обнаружены циклы
            List<SourcePair> loopedSources = new ArrayList<SourcePair>();
            for (Map.Entry<SourcePair, SourcePair> pair : loopsMap.entrySet()) {
                //Берем вторую пару - причину зацикливания + ее назначение-приемника
                circleCauses.add(pair.getValue().getDestination());
                circleCauses.add(pair.getValue().getSource());
                loopedSources.add(pair.getKey());
            }
            //Получаем данные о назначениях-причинах зацикливания для вывода в сообщениях
            final Map<Long, String> objectNames = sourceDao.getSourceNames(new ArrayList<Long>(circleCauses));
            return truncateSources(logger, sourcePairs, loopedSources, isDeclaration, false, LOG_LEVEL.ERROR,
                    new MessageBuilder() {
                        @Override
                        public List<String> getMessage(SourcePair sourcePair) {
                            SourcePair errorPair = loopsMap.get(sourcePair);
                            return Arrays.asList(String.format(CIRCLE_MSG,
                                    objectNames.get(errorPair.getSource()),
                                    objectNames.get(errorPair.getDestination())
                            ));
                        }
                    });
        }
        return sourcePairs;
    }

    /**
     * Выполняет проверку пересечения.
     * Период назначений, в которых были найдены пересечения расширяется
     * Назначения, с которыми были обнаружены пересечения удаляются
     * Назначения, для которых были обнаружены пересечения исключаются из обработки
     * http://conf.aplana.com/pages/viewpage.action?pageId=12321539
     *
     * @param logger              логгер
     * @param periodStart         начало периода, в котором выполняется поиск пересечений
     * @param periodEnd           окончание периода, в котором выполняется поиск пересечений
     * @param excludedPeriodStart начало нового периода, который будет исключен из проверки
     * @param excludedPeriodEnd   конец нового периода, который будет исключен из проверки
     * @param sourcePairs         список связок источников-приемников
     * @param mode                режим работы
     * @param isDeclaration       признак того, что идет обработка в режиме "Декларации"
     * @return обрезанный входной список связок источников-приемников, для которых не найдены пересечения
     */
    public List<SourcePair> checkIntersections(Logger logger, Date periodStart, Date periodEnd,
                                               final Date excludedPeriodStart, Date excludedPeriodEnd,
                                               List<SourcePair> sourcePairs,
                                               SourceMode mode,
                                               boolean isDeclaration) {

        /** Получаем назначения с пересекающимися периодами */
        Map<SourcePair, List<SourceObject>> intersections = sourceDao.getIntersections(sourcePairs,
                SimpleDateUtils.addDayToDate(periodStart, -1), periodEnd != null ? SimpleDateUtils.addDayToDate(periodEnd, 1) : null, excludedPeriodStart, excludedPeriodEnd, isDeclaration);

        if (!intersections.isEmpty()) {
            /**
             * Формируем новый период для нового назначения = объединенному периоду обнаруженного пересечения и входного периода
             * Также формируем список на удаление состоящий из пересекающихся назначений
             */
            final Map<SourceObject, Pair<Date, Date>> unionSources = new HashMap<SourceObject, Pair<Date, Date>>();
            final Map<SourcePair, Pair<Date, Date>> unionPairs = new HashMap<SourcePair, Pair<Date, Date>>();
            List<SourceObject> deleteSources = new ArrayList<SourceObject>();
            List<SourcePair> deletePairs = new ArrayList<SourcePair>();
            final Map<SourcePair, SourceObject> unionMap = new HashMap<SourcePair, SourceObject>();
            final Map<SourcePair, List<String>> intersectionParts = new HashMap<SourcePair, List<String>>();
            for (Map.Entry<SourcePair, List<SourceObject>> intersectionGroup : intersections.entrySet()) {
                Date minDate = periodStart;
                Date maxDate = periodEnd;

                for (SourceObject intersection : intersectionGroup.getValue()) {
                    if (intersection.getPeriodStart().before(minDate)) {
                        minDate = intersection.getPeriodStart();
                    }
                    if ((intersection.getPeriodEnd() == null && maxDate != null)
                            || (intersection.getPeriodEnd() != null && maxDate != null && intersection.getPeriodEnd().after(maxDate))) {
                        maxDate = intersection.getPeriodEnd();
                    }
                    deleteSources.add(intersection);
                    deletePairs.add(intersection.getSourcePair());
                    List<String> msgsForPair = intersectionParts.get(intersection.getSourcePair());
                    if (msgsForPair == null) {
                        msgsForPair = new ArrayList<String>();
                    }
                    if (mode == SourceMode.SOURCES) {
                        msgsForPair.add(String.format(INTERSECTION_PART,
                                isDeclaration ? intersection.getSourcePair().getDestinationType() : intersection.getSourcePair().getDestinationKind() + ": " + intersection.getSourcePair().getDestinationType(),
                                "приемника",
                                intersection.getSourcePair().getSourceKind() + ": " + intersection.getSourcePair().getSourceType(),
                                formatter.get().format(intersection.getPeriodStart()) + " - " +
                                        (intersection.getPeriodEnd() != null ? formatter.get().format(intersection.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                        ));
                    } else {
                        msgsForPair.add(String.format(INTERSECTION_PART,
                                intersection.getSourcePair().getSourceKind() + ": " + intersection.getSourcePair().getSourceType(),
                                "источника",
                                isDeclaration ? intersection.getSourcePair().getDestinationType() : intersection.getSourcePair().getDestinationKind() + ": " + intersection.getSourcePair().getDestinationType(),
                                formatter.get().format(intersection.getPeriodStart()) + " - " +
                                        (intersection.getPeriodEnd() != null ? formatter.get().format(intersection.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                        ));
                    }
                    intersectionParts.put(intersection.getSourcePair(), msgsForPair);
                }
                SourceObject union;

                if (excludedPeriodStart == null) {
                    //Идет создание назначений
                    union = new SourceObject(intersectionGroup.getKey(), minDate, maxDate);
                } else {
                    //Идет редактирование назначений
                    union = new SourceObject(intersectionGroup.getKey(), excludedPeriodStart, excludedPeriodEnd);
                }
                unionSources.put(union, new Pair<Date, Date>(minDate, maxDate));
                unionPairs.put(union.getSourcePair(), new Pair<Date, Date>(minDate, maxDate));
                unionMap.put(union.getSourcePair(), union);
            }

            /** Удаляем все назначения, с периодами которых были найдены пересечения. */
            sourceDao.deleteAll(deleteSources, isDeclaration);

            List<SourcePair> intersectingPairs = new ArrayList<SourcePair>(intersections.keySet());
            if (excludedPeriodStart == null) {
                //Идет создание назначений
                /** Создаем новые назначения с объединенными периодами */
                sourceDao.createAll(new ArrayList<SourceObject>(unionSources.keySet()), isDeclaration);
            } else {
                //Идет редактирование назначений
                intersectingPairs = deletePairs;
                for (Map.Entry<SourceObject, Pair<Date, Date>> sourceEntry : unionSources.entrySet()) {
                    sourceDao.updateAll(Arrays.asList(sourceEntry.getKey()),
                            sourceEntry.getValue().getFirst(), sourceEntry.getValue().getSecond(), isDeclaration);
                }
            }

            /** Убираем назначения с пересечениями из обработки */
            return truncateSources(logger, sourcePairs, intersectingPairs, isDeclaration, true, LOG_LEVEL.INFO,
                    new MessageBuilder() {
                        @Override
                        public List<String> getMessage(SourcePair sourcePair) {
                            String period;
                            if (excludedPeriodStart == null) {
                                //Идет создание назначений
                                SourceObject union = unionMap.get(sourcePair);
                                period = formatter.get().format(union.getPeriodStart()) + " - " +
                                        (union.getPeriodEnd() != null ? formatter.get().format(union.getPeriodEnd()) : EMPTY_END_PERIOD_INFO);
                            } else {
                                //Идет редактирование назначений
                                Pair<Date, Date> periodDates = unionPairs.get(sourcePair);
                                period = formatter.get().format(periodDates.getFirst()) + " - " +
                                        (periodDates.getSecond() != null ? formatter.get().format(periodDates.getSecond()) : EMPTY_END_PERIOD_INFO);
                            }
                            List<String> msgs = new ArrayList<String>();
                            msgs.add(INTERSECTION_MSG_BEGIN);
                            msgs.addAll(intersectionParts.get(sourcePair));
                            msgs.add(String.format(INTERSECTION_MSG_END, period));
                            return msgs;
                        }
                    });
        } else {
            return sourcePairs;
        }
    }

    @Override
    public void createSources(Logger logger, SourceClientData sourceClientData) {
        if (sourceClientData.getSourcePairs() != null && !sourceClientData.getSourcePairs().isEmpty()) {
            String sourceDepartmentName = departmentDao.getDepartment(sourceClientData.getSourceDepartmentId()).getName();
            String destinationDepartmentName = departmentDao.getDepartment(sourceClientData.getDestinationDepartmentId()).getName();
            /** Проверка существования назначения подразделению */
            List<SourcePair> sourcePairs = checkExistence(logger, sourceClientData.getSourcePairs(),
                    sourceClientData.getMode(), sourceClientData.isDeclaration(),
                    sourceDepartmentName, destinationDepartmentName);

            /** Специфичные проверки */
            checkSpecifics(logger, sourceClientData.getSourcePairs(),
                    sourceClientData.getMode(), sourceClientData.isDeclaration(), sourceClientData.getTaxType());

            /** Проверка существования экземпляров нф */
            checkFormInstances(logger, sourcePairs, sourceClientData.getPeriodStart(), sourceClientData.getPeriodEnd(),
                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());

            /** Проверка зацикливания */
            sourcePairs = checkLoops(logger, sourceClientData.getPeriodStart(), sourceClientData.getPeriodEnd(),
                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());

            /** Проверка пересечений */
            sourcePairs = checkIntersections(logger, sourceClientData.getPeriodStart(), sourceClientData.getPeriodEnd(),
                    null, null, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());

            if (!sourcePairs.isEmpty()) {
                List<SourceObject> sourceObjects = pairsToObjects(sourcePairs, sourceClientData.getPeriodStart(), sourceClientData.getPeriodEnd());

                /** Создаем оставшиеся назначения */
                sourceDao.createAll(sourceObjects, sourceClientData.isDeclaration());
                for (SourceObject sourceObject : sourceObjects) {
                    if (sourceClientData.getMode() == SourceMode.DESTINATIONS) {
                        logger.info(SAVE_SUCCESS_MSG,
                                sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                        sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType(),
                                "приемником",
                                sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                                formatter.get().format(sourceObject.getPeriodStart()) + " - " +
                                        (sourceObject.getPeriodEnd() != null ? formatter.get().format(sourceObject.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                        );
                    } else {
                        logger.info(SAVE_SUCCESS_MSG,
                                sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                                "источником",
                                sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                        sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType(),
                                formatter.get().format(sourceObject.getPeriodStart()) + " - " +
                                        (sourceObject.getPeriodEnd() != null ? formatter.get().format(sourceObject.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                        );
                    }
                }
            }
        } else {
            throw new ServiceException(EMPTY_LIST_MSG);
        }
    }

    /**
     * Специфичные проверки назначений.
     * Предполагается что этот метод будет вызываться только при создании, когда участвует только одна пара назначений, так что оптимизации вызовов дао не делал
     * @param logger                  логгер
     * @param sourcePairs             входной набор пар источник-приемник
     * @param mode                    режим работы: назначение приемников или назначение источников
     * @param declaration             признак того, что идет обработка в режиме "Декларации"
     * @param taxType                 тип налога
     */
    private void checkSpecifics(Logger logger, List<SourcePair> sourcePairs, SourceMode mode, boolean declaration, TaxType taxType) {
        for (SourcePair pair : sourcePairs) {
            /** Для транспортного налога и имущества источник и приемник должен относиться к одному ТБ */
            if (taxType == TaxType.TRANSPORT || taxType == TaxType.PROPERTY) {
                Integer srcDepartmentId = sourceDao.getDepartmentIdByDepartmentFormType(pair.getSource());
                Integer destDepartmentId = declaration ?
                        sourceDao.getDepartmentIdByDepartmentDeclarationType(pair.getDestination()) : sourceDao.getDepartmentIdByDepartmentFormType(pair.getDestination());
                if (srcDepartmentId != null && destDepartmentId != null) {
                    Integer srcTB = departmentDao.getParentTBId(srcDepartmentId);
                    Integer destTB = departmentDao.getParentTBId(destDepartmentId);
                    if (srcTB != null && destTB != null && !destTB.equals(srcTB)) {
                        throw new ServiceLoggerException("Ограничение регионального налога: приёмник должен принадлежать тому же территориальному банку, к которому относится форма-источник!",
                                logEntryService.save(logger.getEntries()));
                    }
                }
            }
        }
    }

    @Override
    public void deleteSources(Logger logger, SourceClientData sourceClientData) {
        if (sourceClientData.getSourcePairs() != null && !sourceClientData.getSourcePairs().isEmpty()) {
            List<SourceObject> sourceObjects = sourceClientData.getSourceObjects();
            List<Long> sourceIds = new ArrayList<Long>();
            for (SourceObject sourceObject : sourceObjects) {
                sourceIds.add(sourceObject.getSourcePair().getSource());
            }
            Map<Long, String> sourceDepartmentNames = sourceDao.getDepartmentNamesBySource(sourceIds);

            /** Получаем источники, имеющие принятые экземпляры в удаляемых периодах */
            Map<SourcePair, List<String>> acceptedSources = new HashMap<SourcePair, List<String>>();
            Set<Long> processedSources = new HashSet<Long>();
            for (SourceObject sourceObject: sourceObjects) {
                Long source = sourceObject.getSourcePair().getSource();
                if (!processedSources.contains(source)) {
                    List<String> periodsInfo = checkAcceptedFormData(sourceDao.findAcceptedInstances(source,
                            sourceObject.getPeriodStart(), sourceObject.getPeriodEnd()));
                    if (!periodsInfo.isEmpty()) {
                        acceptedSources.put(sourceObject.getSourcePair(), periodsInfo);
                    }
                    processedSources.add(source);
                }
            }

            if (!acceptedSources.isEmpty()) {
                /** Если существуют принятые источники */
                logger.error(DELETE_FATAL_ERROR_BEGIN);
                for (Map.Entry<SourcePair, List<String>> acceptedSource : acceptedSources.entrySet()) {
                    logger.error(String.format(DELETE_FATAL_ERROR_MID,
                            acceptedSource.getKey().getSourceKind() + ": " + acceptedSource.getKey().getSourceType(),
                            sourceDepartmentNames.get(acceptedSource.getKey().getSource()),
                            StringUtils.join(acceptedSource.getValue(), ", ")));
                }
                logger.error(DELETE_FATAL_ERROR_END);
                throw new ServiceLoggerException(FATAL_DELETE_MSG,
                        logEntryService.save(logger.getEntries()));
            }

            /** Удаляем все назначения, с периодами которых были найдены пересечения. */
            sourceDao.deleteAll(sourceObjects, sourceClientData.isDeclaration());
            if (sourceClientData.getMode() == SourceMode.DESTINATIONS) {
                for (SourceObject sourceObject : sourceObjects) {
                    logger.info(DELETE_SUCCESS_MSG,
                            sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                    sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType(),
                            "приемника",
                            sourceClientData.isDeclaration() ? "декларации" : "формы",
                            sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                            formatter.get().format(sourceObject.getPeriodStart()) + " - " +
                                    (sourceObject.getPeriodEnd() != null ? formatter.get().format(sourceObject.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                    );
                }
            } else {
                for (SourceObject sourceObject : sourceObjects) {
                    logger.info(DELETE_SUCCESS_MSG,
                            sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                            "источника",
                            sourceClientData.isDeclaration() ? "декларации" : "формы",
                            sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                    sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType(),
                            formatter.get().format(sourceObject.getPeriodStart()) + " - " +
                                    (sourceObject.getPeriodEnd() != null ? formatter.get().format(sourceObject.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                    );
                }
            }
        } else {
            throw new ServiceException(EMPTY_LIST_MSG);
        }
    }

    @Override
    public void updateSources(Logger logger, List<SourceClientData> sourceClientDataList) {
        ServiceLoggerException criticalError = null;
        for (SourceClientData sourceClientData : sourceClientDataList) {
            try {
                Date periodStart = sourceClientData.getPeriodStart();
                Date periodEnd = sourceClientData.getPeriodEnd();
                Date oldPeriodStart = sourceClientData.getOldPeriodStart();
                Date oldPeriodEnd = sourceClientData.getOldPeriodEnd();
                String sourceDepartmentName = departmentDao.getDepartment(sourceClientData.getSourceDepartmentId()).getName();
                String destinationDepartmentName = departmentDao.getDepartment(sourceClientData.getDestinationDepartmentId()).getName();
                if (sourceClientData.getSourcePairs() != null && !sourceClientData.getSourcePairs().isEmpty()) {
                    List<SourcePair> sourcePairs = sourceClientData.getSourcePairs();
                    if (sourcePairs.size() > 1) {
                        throw new ServiceException("Нельзя редактировать более одной пары за раз!");
                    }

                    SourcePair sourcePair = sourcePairs.get(0);

                    if (periodStart.after(oldPeriodStart) || (periodEnd != null && oldPeriodEnd == null)
                            || (periodEnd != null && oldPeriodEnd != null && periodEnd.before(oldPeriodEnd))) {
                        /** Если новый период сузился */

                        List<String> acceptedSources = new ArrayList<String>();
                        /** Получаем источники, имеющие принятые экземпляры в промежуточных периодах */
                        if (periodStart.after(oldPeriodStart)) {
                            acceptedSources.addAll(checkAcceptedFormData(sourceDao.findAcceptedInstances(sourcePair.getSource(),
                                    oldPeriodStart , SimpleDateUtils.addDayToDate(periodStart, -1))));
                        }
                        if ((periodEnd != null && oldPeriodEnd == null) || (periodEnd != null && oldPeriodEnd != null && periodEnd.before(oldPeriodEnd))) {
                            acceptedSources.addAll(checkAcceptedFormData(sourceDao.findAcceptedInstances(sourcePair.getSource(),
                                    SimpleDateUtils.addDayToDate(periodEnd, 1), oldPeriodEnd)));
                        }

                        if (!acceptedSources.isEmpty()) {
                            /** Если существуют принятые источники в промежуточных периодах */
                            logger.error(String.format(EDIT_FATAL_ERROR,
                                    sourcePair.getSourceKind() + ": " + sourcePair.getSourceType(),
                                    sourceDepartmentName,
                                    StringUtils.join(acceptedSources, ", ")));
                            throw new ServiceLoggerException(FATAL_SAVE_MSG,
                                    logEntryService.save(logger.getEntries()));
                        }
                    }

                    if (periodStart.before(oldPeriodStart)) {
                        /** Дата начала нового периода меньше даты начала старого периода */
                        if (periodEnd != null && ((oldPeriodEnd != null && periodEnd.before(oldPeriodEnd)) || oldPeriodEnd == null) && periodEnd.after(oldPeriodStart)) {
                            /** Дата окончания нового периода меньше даты окончания старого периода и больше даты начала старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, periodStart, SimpleDateUtils.addDayToDate(oldPeriodStart, -1),
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(periodEnd, 1), oldPeriodEnd,
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, oldPeriodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd,
                                    oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        } else if (periodEnd != null && (periodEnd.before(oldPeriodStart))) {
                            /** Дата окончания нового периода меньше даты начала старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, oldPeriodEnd,
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            checkFormInstances(logger, sourcePairs, periodStart, periodEnd,
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd, oldPeriodStart, oldPeriodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        } else if ((periodEnd == null && oldPeriodEnd != null)
                                || (periodEnd != null && oldPeriodEnd != null && periodEnd.after(oldPeriodEnd))) {
                            /** Дата окончания нового периода больше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, periodStart, SimpleDateUtils.addDayToDate(oldPeriodStart, -1),
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd,
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd,
                                    oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        } else if ((periodEnd == null && oldPeriodEnd == null) || (periodEnd.equals(oldPeriodEnd))) {
                            /** Равна дате окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, periodStart, SimpleDateUtils.addDayToDate(oldPeriodStart, -1),
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd,
                                    oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        }
                    } else if (periodStart.equals(oldPeriodStart)) {
                        /** Дата начала нового периода равна дате начала старого периода */
                        if ((periodEnd != null && oldPeriodEnd == null) || (periodEnd != null && periodEnd.before(oldPeriodEnd))) {
                            /** Дата окончания нового периода меньше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(periodEnd, 1), oldPeriodEnd,
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                        } else if ((periodEnd == null && oldPeriodEnd != null)
                                || (periodEnd != null && periodEnd.after(oldPeriodEnd))) {
                            /** Дата окончания нового периода больше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd,
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd,
                                    oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        }
                    } else if (periodStart.after(oldPeriodStart) && (
                            (oldPeriodEnd != null && periodStart.before(oldPeriodEnd) || oldPeriodEnd == null)
                    )) {
                        /** Дата начала нового периода больше даты начала старого периода и меньше даты окончания старого периода */
                        if ((periodEnd == null && oldPeriodEnd == null) || (periodEnd != null && oldPeriodEnd != null && periodEnd.equals(oldPeriodEnd))) {
                            /** Равна дате окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1),
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());

                        } else if (periodEnd != null && (oldPeriodEnd == null || periodEnd.before(oldPeriodEnd))) {
                            /** Дата окончания нового периода меньше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1),
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(periodEnd, 1), oldPeriodEnd,
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                        } else if ((periodEnd == null && oldPeriodEnd != null)|| (periodEnd.after(oldPeriodEnd))) {
                            /** Дата окончания нового периода больше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1),
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd,
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd,
                                    oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        } else if (periodEnd.after(oldPeriodEnd)) {
                            /** Дата окончания нового периода равна дате окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1),
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                        }
                    } else if (oldPeriodEnd != null && periodStart.after(oldPeriodEnd)) {
                        /** Дата начала нового периода больше даты окончания старого периода */
                        if (periodEnd == null || periodEnd.after(oldPeriodEnd)) {
                            /** Дата окончания нового периода больше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, oldPeriodEnd,
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            checkFormInstances(logger, sourcePairs, periodStart, periodEnd,
                                    sourceDepartmentName, destinationDepartmentName, sourceClientData.isDeclaration());
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd, oldPeriodStart, oldPeriodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        }
                    }
                    if (!sourcePairs.isEmpty()) {
                        List<SourceObject> sourceObjects = pairsToObjects(sourcePairs, oldPeriodStart, oldPeriodEnd);
                        sourceDao.updateAll(sourceObjects, periodStart, periodEnd, sourceClientData.isDeclaration());
                        if (sourceClientData.getMode() == SourceMode.DESTINATIONS) {
                            for (SourceObject sourceObject : sourceObjects) {
                                logger.info(UPDATE_SUCCESS_MSG,
                                        sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                                sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType(),
                                        "приемником",
                                        sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                                        formatter.get().format(periodStart) + " - " +
                                                (periodEnd != null ? formatter.get().format(periodEnd) : EMPTY_END_PERIOD_INFO)
                                );
                            }
                        } else {
                            for (SourceObject sourceObject : sourceObjects) {
                                logger.info(UPDATE_SUCCESS_MSG,
                                        sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                                        "источником",
                                        sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                                sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType(),
                                        formatter.get().format(periodStart) + " - " +
                                                (periodEnd != null ? formatter.get().format(periodEnd) : EMPTY_END_PERIOD_INFO)
                                );
                            }
                        }
                    }
                } else {
                    throw new ServiceException(EMPTY_LIST_MSG);
                }
            } catch (ServiceLoggerException e) {
                //Сохраняем эксепшен чтобы выбросить его позже и продолжаем проверки остальных назначений
                criticalError = e;
            }
        }
        if (criticalError != null) {
            throw criticalError;
        }
    }

    @Override
    public int getAssignedFormsCount(List<Long> departmentsIds, char taxType) {
        return departmentFormTypeDao.getAssignedFormsCount(departmentsIds, taxType);
    }

    @Override
    public int getAssignedDeclarationsCount(List<Long> departmentsIds, char taxType) {
        return departmentDeclarationTypeDao.getAssignedDeclarationsCount(departmentsIds, taxType);
    }

    @Override
    public void deleteDeclarationConsolidateInfo(long targetDeclarationDataId) {
        sourceDao.deleteDeclarationConsolidateInfo(targetDeclarationDataId);
    }

    @Override
    public boolean isDeclarationSourceConsolidated(long declarationId, long sourceFormDataId) {
        return sourceDao.isDeclarationSourceConsolidated(declarationId, sourceFormDataId);
    }

    @Override
    public void addFormDataConsolidationInfo(Long tgtFormDataId, Collection<Long> srcFormDataIds) {
        sourceDao.addFormDataConsolidationInfo(tgtFormDataId, srcFormDataIds);
    }

    @Override
    public void deleteFDConsolidationInfo(Collection<Long> tgtFormDataIds) {
        sourceDao.deleteFormDataConsolidationInfo(tgtFormDataIds);
    }

    @Override
    public boolean isFDSourceConsolidated(long formDataId, long sourceFormDataId) {
        return sourceDao.isFDSourceConsolidated(formDataId, sourceFormDataId);
    }

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd) {
        return departmentFormTypeDao.getFormDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind, periodStart, periodEnd);
    }

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, int reportPeriodId) {
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);

        return getFormDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind, reportPeriod.getStartDate(), reportPeriod.getEndDate());
    }

    @Override
    public List<DepartmentFormType> getDFTSourcesByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd) {
        return departmentFormTypeDao.getDepartmentSources(departmentId, taxType, periodStart, periodEnd);
    }

    @Override
    public List<DepartmentFormType> getDFTByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd) {
        QueryParams<SourcesSearchOrdering> queryParams = getSearchOrderingDefaultFilter();
        return getDFTByDepartment(departmentId, taxType, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<DepartmentFormType> getDFTByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd, QueryParams queryParams) {
        return departmentFormTypeDao.getByTaxType(departmentId, taxType, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<Long> getDFTByPerformerDep(int performerDepId, TaxType taxType, List<FormDataKind> kinds) {
        return departmentFormTypeDao.getByPerformerId(performerDepId, Arrays.asList(taxType), kinds);
    }

    @Override
    public List<Long> getDFTFormTypeBySource(int performerDepId, TaxType taxType, List<FormDataKind> kinds) {
        return departmentFormTypeDao.getFormTypeBySource(performerDepId, taxType, kinds);
    }

    @Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd) {
        return departmentFormTypeDao.getDeclarationDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind, periodStart, periodEnd);
    }

    @Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, int reportPeriodId) {
        ReportPeriod period = reportPeriodDao.get(reportPeriodId);

        return getDeclarationDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind, period.getStartDate(), period.getEndDate());
    }

    @Override
    public List<FormTypeKind> getFormAssigned(Long departmentId, char taxType) {
        return departmentFormTypeDao.getFormAssigned(departmentId, taxType);
    }

    @Override
    public List<FormTypeKind> getAllFormAssigned(List<Long> departmentIds, char taxType, QueryParams<TaxNominationColumnEnum> queryParams) {
        return departmentFormTypeDao.getAllFormAssigned(departmentIds, taxType, queryParams);
    }

    @Override
    public List<FormTypeKind> getDeclarationAssigned(Long departmentId, char taxType) {
        return departmentFormTypeDao.getDeclarationAssigned(departmentId, taxType);
    }

    @Override
    public List<FormTypeKind> getAllDeclarationAssigned(List<Long> departmentIds, char taxType, QueryParams<TaxNominationColumnEnum> queryParams) {
        return departmentDeclarationTypeDao.getAllDeclarationAssigned(departmentIds, taxType, queryParams);
    }

    @Override
    public void saveDFT(Long departmentId, int typeId, int formId) {
        departmentFormTypeDao.save(departmentId.intValue(), typeId, formId);
    }

    @Override
    public void saveDFT(Long departmentId, int typeId, int formId, Integer performerId) {
        departmentFormTypeDao.save(departmentId.intValue(), typeId, formId, performerId);
    }

    @Override
    public void deleteDFT(Collection<Long> ids) {
        for (Long id : ids) {
            //TODO dloshkarev: можно переделать на in запрос
            departmentFormTypeDao.delete(id);
        }
    }

    @Override
    public void saveDDT(Long departmentId, int declarationId) {
        departmentDeclarationTypeDao.save(departmentId.intValue(), declarationId);
    }

    @Override
    public void deleteDDT(Collection<Long> ids) {
        for (Long id : ids) {
            //TODO dloshkarev: можно переделать на in запрос
            departmentDeclarationTypeDao.delete(id);
        }
    }

    @Override
    public FormType getFormType(int formTypeId) {
        return formTypeDao.get(formTypeId);
    }

    @Override
    public List<FormType> listAllByTaxType(TaxType taxType) {
        return formTypeDao.getByTaxType(taxType);
    }

    @Override
    public List<DepartmentDeclarationType> getDDTByDepartment(int departmentId, TaxType taxType, Date periodStart,
                                                              Date periodEnd) {
        QueryParams<SourcesSearchOrdering> queryParams = getSearchOrderingDefaultFilter();
        return getDDTByDepartment(departmentId, taxType, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<DepartmentDeclarationType> getDDTByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd, QueryParams queryParams) {
        return departmentDeclarationTypeDao.getByTaxType(departmentId, taxType, periodStart, periodEnd, queryParams);
    }

    @Override
    public DeclarationType getDeclarationType(int declarationTypeId) {
        return declarationTypeDao.get(declarationTypeId);
    }

    @Override
    public boolean existAssignedForm(int departmentId, int typeId, FormDataKind kind) {
        return departmentFormTypeDao.existAssignedForm(departmentId, typeId, kind);
    }


    @Override
    public List<Pair<String, String>> existAcceptedDestinations(int sourceDepartmentId, int sourceFormTypeId,
                                                                FormDataKind sourceKind, Integer reportPeriodId,
                                                                Date periodStart, Date periodEnd) {
        return departmentFormTypeDao.existAcceptedDestinations(sourceDepartmentId, sourceFormTypeId,
                sourceKind, reportPeriodId, periodStart, periodEnd);
    }

    @Override
    public List<DeclarationType> allDeclarationTypeByTaxType(TaxType taxType) {
        return declarationTypeDao.listAllByTaxType(taxType);
    }

    @Override
    public void updatePerformer(int id, Integer performerId) {
        departmentFormTypeDao.updatePerformer(id, performerId);
    }

    @Override
    public List<FormToFormRelation> getRelations(int departmentId, int formTypeId, FormDataKind kind,
                                                 int departmentReportPeriodId, Integer periodOrder) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(departmentReportPeriodId);
        ReportPeriod reportPeriod = departmentReportPeriod.getReportPeriod();

        List<FormToFormRelation> formToFormRelations = new LinkedList<FormToFormRelation>();
        // Источники
        List<DepartmentFormType> sourcesForm = getDFTSourcesByDFT(departmentId, formTypeId, kind,
                reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        formToFormRelations.addAll(createFormToFormRelationModel(sourcesForm, departmentReportPeriod,
                periodOrder, true));
        // Приемники
        List<DepartmentFormType> destinationsForm = getFormDestinations(departmentId, formTypeId, kind,
                reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        formToFormRelations.addAll(createFormToFormRelationModel(destinationsForm, departmentReportPeriod,
                periodOrder, false));

        return formToFormRelations;
    }

    @Override
    public List<Pair<DepartmentFormType, Date>> findDestinationFTsForFormType(int typeId, Date dateFrom, Date dateTo) {
        if (dateFrom == null){
            dateTo = MIN_DATE;
        }
        if (dateTo == null){
            dateTo = MAX_DATE;
        }
        return departmentFormTypeDao.findDestinationsForFormType(typeId, dateFrom, dateTo);
    }

    @Override
    public List<Pair<DepartmentFormType, Date>> findSourceFTsForFormType(int typeId, Date dateFrom, Date dateTo) {
        if (dateFrom == null){
            dateTo = MIN_DATE;
        }
        if (dateTo == null){
            dateTo = MAX_DATE;
        }
        return departmentFormTypeDao.findSourcesForFormType(typeId, dateFrom, dateTo);
    }

    @Override
    public List<Pair<DepartmentFormType, Date>> findSourceFTsForDeclaration(int typeId, Date dateFrom, Date dateTo) {
        if (dateFrom == null){
            dateTo = MIN_DATE;
        }
        if (dateTo == null){
            dateTo = MAX_DATE;
        }
        return departmentDeclarationTypeDao.findSourceFTsForDeclaration(typeId, dateFrom, dateTo);
    }

    @Override
    public List<Pair<DepartmentDeclarationType, Date>> findDestinationDTsForFormType(int typeId, Date dateFrom, Date dateTo) {
        if (dateFrom == null){
            dateTo = MIN_DATE;
        }
        if (dateTo == null){
            dateTo = MAX_DATE;
        }
        return departmentDeclarationTypeDao.findDestinationDTsForFormType(typeId, dateFrom, dateTo);
    }

    @Override
    public List<DepartmentFormType> getDFTByFormType(Integer formTypeId) {
        return departmentFormTypeDao.getDFTByFormType(formTypeId);
    }

    @Override
    public List<DepartmentDeclarationType> getDDTByDeclarationType(Integer declarationTypeId) {
        return departmentDeclarationTypeDao.getDDTByDeclarationType(declarationTypeId);
    }

    /**
     * Метод для составления списка с информацией об источниках приемниках
     */
    private List<FormToFormRelation> createFormToFormRelationModel(List<DepartmentFormType> departmentFormTypes,
                                                                   DepartmentReportPeriod departmentReportPeriod,
                                                                   Integer periodOrder,
                                                                   boolean isSource){
        List<FormToFormRelation> formToFormRelations = new LinkedList<FormToFormRelation>();

        // По назначениям
        for (DepartmentFormType departmentFormType : departmentFormTypes) {
            if (isSource) {
                formToFormRelations.addAll(getSourceList(departmentFormType, departmentReportPeriod,
                        periodOrder));
            } else {
                formToFormRelations.addAll(getDestinationList(departmentFormType, departmentReportPeriod,
                        periodOrder));
            }
        }
        return formToFormRelations;
    }

    /**
     * Подготовка общей модели для сущестувющих и не существующих экземпляров
     */
    private FormToFormRelation getRelationCommon(boolean isSource, DepartmentFormType departmentFormType,
                                                 DepartmentReportPeriod departmentreportPeriod, Integer periodOrder) {
        FormToFormRelation formToFormRelation = new FormToFormRelation();
        formToFormRelation.setSource(isSource);
        formToFormRelation.setFormDataKind(departmentFormType.getKind());
        formToFormRelation.setPerformer(departmentFormType.getPerformerId() != null ? departmentDao.getDepartment(departmentFormType.getPerformerId()) : null);
        formToFormRelation.setFullDepartmentName(departmentService.getParentsHierarchy(departmentFormType.getDepartmentId()));
        if (departmentreportPeriod != null) {
            if (departmentreportPeriod.getCorrectionDate() != null) {
                formToFormRelation.setCorrectionDate(departmentreportPeriod.getCorrectionDate());
            }
            formToFormRelation.setYear(departmentreportPeriod.getReportPeriod().getTaxPeriod().getYear());
            formToFormRelation.setPeriodName(departmentreportPeriod.getReportPeriod().getName());
        }
        formToFormRelation.setMonth(periodOrder != null ? Months.fromId(periodOrder).getTitle() : "");
        return formToFormRelation;
    }

    /**
     * Заполнение модели отношения по экземпляру НФ
     */
    private void fillFormDataRelation(FormToFormRelation formToFormRelation, FormData formData) {
        formToFormRelation.setCreated(true);
        formToFormRelation.setFormType(formData.getFormType());
        formToFormRelation.setFormDataId(formData.getId());
        formToFormRelation.setState(formData.getState());
    }

    /**
     * Заполенени модели отношения данными из экземпляра НФ или поиск не созданного экземпляра
     */
    private FormToFormRelation performFormDataRelation(FormData formData, FormToFormRelation formToFormRelation,
                                                       DepartmentFormType departmentFormType,
                                                       DepartmentReportPeriod departmentReportPeriod) {
        if (formData != null) {
            // Созданный экземпляр найден
            fillFormDataRelation(formToFormRelation, formData);
        } else {
            // Созданный экземпляр не найден, ищем не созданный в том же периоде
            if (formTemplateDao.existFormTemplate(departmentFormType.getFormTypeId(),
                    departmentReportPeriod.getReportPeriod().getId())) {
                formToFormRelation.setCreated(false);
                formToFormRelation.setFormType(formTypeDao.get(departmentFormType.getFormTypeId()));
                formToFormRelation.setFormDataKind(departmentFormType.getKind());
                formToFormRelation.setYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());
                formToFormRelation.setPeriodName(departmentReportPeriod.getReportPeriod().getName());
            } else {
                // Источников нет
                return null;
            }
        }
        return formToFormRelation;
    }

    /**
     * Список экземпляров-источников
     * @param departmentFormType Назначение
     * @param departmentReportPeriod Отчетный период подраделения формы, для которой ищутся формы-источники
     * @param periodOrder Месяц формы, для которой ищутся формы-источники
     */
    private List<FormToFormRelation> getSourceList(DepartmentFormType departmentFormType,
                                                   DepartmentReportPeriod departmentReportPeriod,
                                                   Integer periodOrder) {
        List<FormToFormRelation> relations = new ArrayList<FormToFormRelation>();
        if (!formTemplateDao.existFormTemplate(departmentFormType.getFormTypeId(), departmentReportPeriod.getReportPeriod().getId()))
            return relations;

        List<Integer> periodOrders = new ArrayList<Integer>();
        int formTemplateId = formTemplateDao.getActiveFormTemplateId(departmentFormType.getFormTypeId(), departmentReportPeriod.getReportPeriod().getId());
        if (formTemplateDao.get(formTemplateId).isMonthly() && periodOrder == null) {
            for(Months month: reportPeriodService.getAvailableMonthList(departmentReportPeriod.getReportPeriod().getId())) {
                if (month != null) periodOrders.add(month.getId());
            }
        } else {
            periodOrders.add(periodOrder);
        }

        for(Integer periodOrderForm: periodOrders) {
            FormData formData = formDataDao.getLastByDate(departmentFormType.getFormTypeId(), departmentFormType.getKind(),
                    departmentFormType.getDepartmentId(), departmentReportPeriod.getReportPeriod().getId(),
                    periodOrderForm, departmentReportPeriod.getCorrectionDate());
            DepartmentReportPeriod formDepartmentReportPeriod = null;
            if (formData != null) {
                formDepartmentReportPeriod = departmentReportPeriodDao.get(formData.getDepartmentReportPeriodId());
            }
            FormToFormRelation formToFormRelation = performFormDataRelation(formData,
                    getRelationCommon(true, departmentFormType, formDepartmentReportPeriod, periodOrderForm), departmentFormType,
                    departmentReportPeriod);
            if (formToFormRelation != null) relations.add(formToFormRelation);
        }
        return relations;
    }

    /**
     * Список экземпляров-приемников
     * @param departmentFormType Назначение
     * @param departmentReportPeriod Отчетный период подраделения формы, для которой ищутся формы-приемники
     * @param periodOrder Месяц формы, для которой ищутся формы-приемники
     */
    private List<FormToFormRelation> getDestinationList(DepartmentFormType departmentFormType,
                                                        DepartmentReportPeriod departmentReportPeriod,
                                                        Integer periodOrder) {
        List<FormToFormRelation> retVal = new LinkedList<FormToFormRelation>();
        if (!formTemplateDao.existFormTemplate(departmentFormType.getFormTypeId(), departmentReportPeriod.getReportPeriod().getId()))
            return retVal;

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setReportPeriodIdList(Arrays.asList(departmentReportPeriod.getReportPeriod().getId()));
        filter.setDepartmentIdList(Arrays.asList(departmentFormType.getDepartmentId()));
        // Список всех отчетных периодов
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(filter);
        //TODO: код похож на дублирующий из метода com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao.getLast
        // Приемник в корректирующем периоде может быть или в том же отчетном периоде подразделения или в следующем, поэтому предыдущие отчетные
        // периоды удаляем из списка
        if (departmentReportPeriod.getCorrectionDate() != null) {
            List<DepartmentReportPeriod> delList = new LinkedList<DepartmentReportPeriod>();
            for (DepartmentReportPeriod destinationReportPeriod : departmentReportPeriodList) {
                if (destinationReportPeriod.getCorrectionDate() == null ||
                        destinationReportPeriod.getCorrectionDate().before(departmentReportPeriod.getCorrectionDate())) {
                        delList.add(destinationReportPeriod);
                }
            }
            departmentReportPeriodList.removeAll(delList);
        }

        int formTemplateId = formTemplateDao.getActiveFormTemplateId(departmentFormType.getFormTypeId(), departmentReportPeriod.getReportPeriod().getId());
        if (!formTemplateDao.get(formTemplateId).isMonthly()) {
            periodOrder = null;
        }
        for (DepartmentReportPeriod destinationReportPeriod : departmentReportPeriodList) {
            // Поиск экземпляра НФ в каждом существующем отчетном периоде подразделения
            FormData formData = formDataDao.find(departmentFormType.getFormTypeId(), departmentFormType.getKind(),
                    destinationReportPeriod.getId().intValue(),
                    periodOrder);

            FormToFormRelation formToFormRelation = performFormDataRelation(formData,
                    getRelationCommon(false, departmentFormType, destinationReportPeriod, periodOrder), departmentFormType,
                    departmentReportPeriod);

             if (formToFormRelation != null) {
                 retVal.add(formToFormRelation);
             }
        }
        return retVal;
    }

    /**
     * Фильтр по умолчанию
     */
    private QueryParams<SourcesSearchOrdering> getSearchOrderingDefaultFilter() {
        QueryParams<SourcesSearchOrdering> queryParams = new QueryParams<SourcesSearchOrdering>();
        queryParams.setSearchOrdering(SourcesSearchOrdering.TYPE);
        queryParams.setAscending(true);
        return queryParams;
    }

    private List<String> checkAcceptedFormData(List<AcceptedFormData> acceptedFormDataList) {
        List<String> periodsInfo = new ArrayList<String>();
        for (AcceptedFormData acceptedFormData : acceptedFormDataList) {
            if (formTemplateDao.existFormTemplate(acceptedFormData.getFormTypeId(), acceptedFormData.getReportPeriodId()))
                periodsInfo.add(acceptedFormData.getPeriodInfo());
        }
        return periodsInfo;
    }
}
