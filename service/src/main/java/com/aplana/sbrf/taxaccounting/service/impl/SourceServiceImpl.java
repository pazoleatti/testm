package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.source.*;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class SourceServiceImpl implements SourceService {

    private static final String CHECK_EXISTENCE_MSG = "Невозможно назначить источники / приемники: %s \"%s\" не назначена подразделению \"%s\"";
    private static final String FATAL_SAVE_MSG = "Назначение источников-приёмников не выполнено";
    private static final String SOURCES_LIST_IS_EMPTY_MSG = "Все назначения были исключены в результате проверок. Продолжение операции невозможно.";
    private static final String EMPTY_END_PERIOD_INFO = "дата окончания периода не задана";
    private static final String INTERSECTION_PART = "\"%s\" в качестве %s для \"%s\" в периоде %s";
    private static final String INTERSECTION_MSG_BEGIN = "Найдены существующие назначения:";
    private static final String INTERSECTION_MSG_END = "Новое назначение создано путем слияния с найденными назначениями, новое назначение действует в периоде %s.";
    private static final String SAVE_SUCCESS_MSG = "\"%s\" назначен %s формы \"%s\" в периоде %s.";
    private static final String DELETE_SUCCESS_MSG = "Удалено назначение \"%s\" в роли %s %s \"%s\" в периоде %s.";
    private static final String UPDATE_SUCCESS_MSG = "\"%s\" назначен %s формы \"%s\" в периоде %s.";
    private static final String CIRCLE_MSG = "\"%s\" уже назначен как приёмник \"%s\"";
    private static final String RECONSOLIDATE_FORM_MSG = "Для коррекции консолидированных данных необходимо нажать на кнопку \"Консолидировать\" в формах: ";
    private static final String RECALCULATE_DECLARATION_MSG = "Для коррекции консолидированных данных необходимо нажать на кнопку \"Рассчитать\" в декларациях: ";
    private static final String DECLARATION_INSTANCE_MSG = "\"%s\", подразделение \"%s\", период \"%s%s\"%s%s";
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
    private DepartmentService departmentService;

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private SourceDao sourceDao;

    @Autowired
    private FormDataDao formDataDao;

    @Autowired
    private DeclarationDataDao declarationDataDao;

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private FormDataService formDataService;

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
                            "Декларация",
                            sourcePairs.get(0).getDestinationType(),
                            destinationDepartmentName));
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
                            "Форма",
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

        final List<Long> notExisting;
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
                        boolean isSource = notExisting.contains(sourcePair.getSource());
                        return Arrays.asList(String.format(CHECK_EXISTENCE_MSG,
                                (!isSource && isDeclaration) ? "Декларация" : "Форма",
                                isSource ? (sourcePair.getSourceKind() + ": " + sourcePair.getSourceType()) : (sourcePair.getDestinationKind() + ": " + sourcePair.getDestinationType()),
                                isSource ? sourceDepartmentName : destinationDepartmentName));
                    }
                });
    }



    /**
     * Проверяет существование экземпляров нф и заполняет списки id форм-источников и приемников
     * http://conf.aplana.com/pages/viewpage.action?pageId=12321547
     *
     * @param logger         логгер
     * @param sourcePairs  входной набор пар источник-приемник
     * @param newPeriodStart начало нового периода
     * @param newPeriodEnd   окончание нового периода
     * @param sourceIds набор id источников
     * @param destIds набор id приемников
     */
    public void checkFormInstances(Logger logger, List<SourcePair> sourcePairs, Date newPeriodStart, Date newPeriodEnd, boolean declaration, Set<Long> sourceIds, Set<Long> destIds) {
        for (SourcePair sourcePair : sourcePairs) {
            /** Получаем промежуточные периоды, которые будут объединены при создании новой версии */
            List<SourceObject> emptyPeriods = sourceDao.getEmptyPeriods(sourcePair,
                    newPeriodStart, newPeriodEnd);
            List<ConsolidatedInstance> consolidatedInstances = new ArrayList<ConsolidatedInstance>();
            if (!emptyPeriods.isEmpty()) {
                for (SourceObject empty : emptyPeriods) {
                    /** Получаем принятые экземпляры приемника в промежуточных периодах */
                    consolidatedInstances.addAll(sourceDao.findConsolidatedInstances(
                            empty.getSourcePair().getSource(), empty.getSourcePair().getDestination(),
                            empty.getPeriodStart(), empty.getPeriodEnd(), declaration));
                    if (destIds != null) {
                        destIds.addAll(sourceDao.findConsolidatedInstanceIds(empty.getSourcePair().getSource(),
                                        empty.getPeriodStart(), empty.getPeriodEnd(), declaration));
                    }
                    if (sourceIds != null) {
                        sourceIds.addAll(sourceDao.findFDConsolidatedSourceInstanceIds(empty.getSourcePair().getSource(),
                                        empty.getPeriodStart(), empty.getPeriodEnd(), declaration));
                    }
                }
            } else {
                /** Получаем принятые экземпляры приемника в новом периоде */
                consolidatedInstances.addAll(sourceDao.findConsolidatedInstances(
                        sourcePair.getSource(), sourcePair.getDestination(),
                        newPeriodStart, newPeriodEnd, declaration));
                if (destIds != null) {
                    destIds.addAll(sourceDao.findConsolidatedInstanceIds(sourcePair.getSource(),
                            newPeriodStart, newPeriodEnd, declaration));
                }
                if (sourceIds != null) {
                    sourceIds.addAll(sourceDao.findFDConsolidatedSourceInstanceIds(sourcePair.getSource(),
                            newPeriodStart, newPeriodEnd, declaration));
                }
            }

            /** Выводим информацию о найденных экземплярах-приемниках */
            printConsolidationInstancesInfo(consolidatedInstances, logger);
        }
    }

    private void printConsolidationInstancesInfo(List<ConsolidatedInstance> consolidatedInstances, Logger logger) {
        if (!consolidatedInstances.isEmpty()) {
            boolean hasForm = false;
            boolean hasDeclaration = false;

            /** Надо переконсолидировать декларации-приемники */
            for (ConsolidatedInstance consolidatedInstance : consolidatedInstances) {
                if (consolidatedInstance.isDeclaration()) {
                    if (!hasDeclaration) {
                        logger.warn(RECALCULATE_DECLARATION_MSG);
                        hasDeclaration = true;
                    }
                    logger.warn(String.format(DECLARATION_INSTANCE_MSG,
                                    consolidatedInstance.getType(),
                                    consolidatedInstance.getDepartment(),
                                    consolidatedInstance.getPeriod(),
                                    consolidatedInstance.getCorrectionDate() != null
                                            ? " с датой сдачи корректировки " + SIMPLE_DATE_FORMAT.format(consolidatedInstance.getCorrectionDate())
                                            : "",
                                    consolidatedInstance.getTaxOrganCode() != null
                                            ? ", налоговый орган " + consolidatedInstance.getTaxOrganCode()
                                            : "",
                                    consolidatedInstance.getKpp() != null
                                            ? ", КПП " + consolidatedInstance.getKpp()
                                            : "")
                    );
                }
            }

            /** Надо переконсолидировать нф-приемники */
            for (ConsolidatedInstance consolidatedInstance : consolidatedInstances) {
                if (!consolidatedInstance.isDeclaration()) {
                    if (!hasForm) {
                        logger.warn(RECONSOLIDATE_FORM_MSG);
                        hasForm = true;
                    }
                    DepartmentReportPeriod drpCompare = consolidatedInstance.getDrpComapreId() != null ?
                            departmentReportPeriodService.get(consolidatedInstance.getDrpComapreId()) : null;
                    logger.warn(MessageGenerator.getFDMsg("",
                                    consolidatedInstance.getType(),
                                    consolidatedInstance.getFormKind().getName(),
                                    consolidatedInstance.getDepartment(),
                                    consolidatedInstance.getMonth(),
                                    consolidatedInstance.isManual(),
                                    consolidatedInstance.getPeriod(),
                                    consolidatedInstance.getCorrectionDate(),
                                    drpCompare != null ?
                                            drpCompare.getReportPeriod().getName() + " " + drpCompare.getReportPeriod().getTaxPeriod().getYear() : ""
                            )
                    );
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
            String sourceDepartmentName = departmentService.getDepartment(sourceClientData.getSourceDepartmentId()).getName();
            String destinationDepartmentName = departmentService.getDepartment(sourceClientData.getDestinationDepartmentId()).getName();
            /** Проверка существования назначения подразделению */
            List<SourcePair> sourcePairs = checkExistence(logger, sourceClientData.getSourcePairs(),
                    sourceClientData.getMode(), sourceClientData.isDeclaration(),
                    sourceDepartmentName, destinationDepartmentName);

            /** Специфичные проверки */
            checkSpecifics(logger, sourceClientData.getSourcePairs(),
                    sourceClientData.getMode(), sourceClientData.isDeclaration(), sourceClientData.getTaxType());

            /** Проверка существования экземпляров нф */
            checkFormInstances(logger, sourcePairs, sourceClientData.getPeriodStart(), sourceClientData.getPeriodEnd(), sourceClientData.isDeclaration(), null, null);

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
                    if ((srcTB == null && destTB != null) ||
                            (srcTB != null && destTB == null) ||
                            (destTB != null && !destTB.equals(srcTB))) {
                        throw new ServiceLoggerException("Ограничение регионального налога: приёмник и источник должны относиться к одному и тому же территориальному банку!",
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
            HashSet<Long> sourceIds = new HashSet<Long>();
            HashSet<Long> destIds = new HashSet<Long>();

            /** Получаем принятые экземпляры приемника в удаляемых периодах */
            List<ConsolidatedInstance> consolidatedInstances = new ArrayList<ConsolidatedInstance>();
            Set<Long> processedSources = new HashSet<Long>();
            for (SourceObject sourceObject: sourceObjects) {
                final Long source = sourceObject.getSourcePair().getSource();
                if (!processedSources.contains(source)) {
                    consolidatedInstances.addAll(sourceDao.findConsolidatedInstances(
                            source, sourceObject.getSourcePair().getDestination(),
                            sourceObject.getPeriodStart(), sourceObject.getPeriodEnd(), sourceClientData.isDeclaration()));
                    destIds.addAll(sourceDao.findConsolidatedInstanceIds(
                            source,
                            sourceObject.getPeriodStart(),
                            sourceObject.getPeriodEnd(),
                            sourceClientData.isDeclaration())
                    );
                    sourceIds.addAll(
                            sourceDao.findFDConsolidatedSourceInstanceIds( source,
                                    sourceObject.getPeriodStart(),
                                    sourceObject.getPeriodEnd(),
                                    sourceClientData.isDeclaration())
                    );
                    processedSources.add(source);
                }
            }

            if (!consolidatedInstances.isEmpty()) {
                /** Выводим информацию о найденных экземплярах-приемниках */
                printConsolidationInstancesInfo(consolidatedInstances, logger);
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
            if (!sourceClientData.isDeclaration())
                sourceDao.updateFDConsolidationInfo(sourceIds, destIds);
            else
                sourceDao.updateDDConsolidationInfo(sourceIds, destIds);
        } else {
            throw new ServiceException(EMPTY_LIST_MSG);
        }
    }

    @Override
    public void updateSources(Logger logger, List<SourceClientData> sourceClientDataList) {
        ServiceLoggerException criticalError = null;

        HashSet<Long> sourceIds = new HashSet<Long>();
        HashSet<Long> destIds = new HashSet<Long>();

        for (SourceClientData sourceClientData : sourceClientDataList) {
            try {
                Date periodStart = sourceClientData.getPeriodStart();
                Date periodEnd = sourceClientData.getPeriodEnd();
                Date oldPeriodStart = sourceClientData.getOldPeriodStart();
                Date oldPeriodEnd = sourceClientData.getOldPeriodEnd();
                if (sourceClientData.getSourcePairs() != null && !sourceClientData.getSourcePairs().isEmpty()) {
                    List<SourcePair> sourcePairs = sourceClientData.getSourcePairs();
                    if (sourcePairs.size() > 1) {
                        throw new ServiceException("Нельзя редактировать более одной пары за раз!");
                    }

                    if (periodStart.before(oldPeriodStart)) {
                        /** Дата начала нового периода меньше даты начала старого периода */
                        if (periodEnd != null && ((oldPeriodEnd != null && periodEnd.before(oldPeriodEnd)) || oldPeriodEnd == null) && periodEnd.after(oldPeriodStart)) {
                            /** Дата окончания нового периода меньше даты окончания старого периода и больше даты начала старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, periodStart, SimpleDateUtils.addDayToDate(oldPeriodStart, -1), sourceClientData.isDeclaration(), sourceIds, destIds);
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(periodEnd, 1), oldPeriodEnd, sourceClientData.isDeclaration(), sourceIds, destIds);
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, oldPeriodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd,
                                    oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        } else if (periodEnd != null && (periodEnd.before(oldPeriodStart))) {
                            /** Дата окончания нового периода меньше даты начала старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, oldPeriodEnd, sourceClientData.isDeclaration(), sourceIds, destIds);
                            checkFormInstances(logger, sourcePairs, periodStart, periodEnd, sourceClientData.isDeclaration(), sourceIds, destIds);
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
                            checkFormInstances(logger, sourcePairs, periodStart, SimpleDateUtils.addDayToDate(oldPeriodStart, -1), sourceClientData.isDeclaration(), sourceIds, destIds);
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd, sourceClientData.isDeclaration(), sourceIds, destIds);
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd,
                                    oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        } else if ((periodEnd == null && oldPeriodEnd == null) || (periodEnd.equals(oldPeriodEnd))) {
                            /** Равна дате окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, periodStart, SimpleDateUtils.addDayToDate(oldPeriodStart, -1), sourceClientData.isDeclaration(), sourceIds, destIds);
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
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(periodEnd, 1), oldPeriodEnd, sourceClientData.isDeclaration(), sourceIds, destIds);
                        } else if ((periodEnd == null && oldPeriodEnd != null)
                                || (periodEnd != null && periodEnd.after(oldPeriodEnd))) {
                            /** Дата окончания нового периода больше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd, sourceClientData.isDeclaration(), sourceIds, destIds);
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
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1), sourceClientData.isDeclaration(), sourceIds, destIds);

                        } else if (periodEnd != null && (oldPeriodEnd == null || periodEnd.before(oldPeriodEnd))) {
                            /** Дата окончания нового периода меньше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1), sourceClientData.isDeclaration(), sourceIds, destIds);
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(periodEnd, 1), oldPeriodEnd, sourceClientData.isDeclaration(), sourceIds, destIds);
                        } else if ((periodEnd == null && oldPeriodEnd != null)|| (periodEnd.after(oldPeriodEnd))) {
                            /** Дата окончания нового периода больше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1), sourceClientData.isDeclaration(), sourceIds, destIds);
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd, sourceClientData.isDeclaration(), sourceIds, destIds);
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd,
                                    oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        } else if (periodEnd.after(oldPeriodEnd)) {
                            /** Дата окончания нового периода равна дате окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1), sourceClientData.isDeclaration(), sourceIds, destIds);
                        }
                    } else if (oldPeriodEnd != null && periodStart.after(oldPeriodEnd)) {
                        /** Дата начала нового периода больше даты окончания старого периода */
                        if (periodEnd == null || periodEnd.after(oldPeriodEnd)) {
                            /** Дата окончания нового периода больше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, oldPeriodEnd, sourceClientData.isDeclaration(), sourceIds, destIds);
                            checkFormInstances(logger, sourcePairs, periodStart, periodEnd, sourceClientData.isDeclaration(), sourceIds, destIds);
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd, oldPeriodStart, oldPeriodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        }
                    }
                    if (!sourcePairs.isEmpty()) {
                        // удаляем(обновляем) инф-ю о консолидации
                        if (!sourceClientData.isDeclaration())
                            sourceDao.updateFDConsolidationInfo(sourceIds, destIds);
                        else
                            sourceDao.updateDDConsolidationInfo(sourceIds, destIds);
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
    public void addDeclarationConsolidationInfo(Long tgtDeclarationId, Collection<Long> srcFormDataIds) {
        sourceDao.addDeclarationConsolidationInfo(tgtDeclarationId, srcFormDataIds);
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
        if (srcFormDataIds.isEmpty()){
            return;
        }
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
    public void updateFDDDConsolidation(long sourceFormId) {
        sourceDao.updateDDConsolidationInfo(sourceFormId);
        sourceDao.updateFDConsolidationInfo(sourceFormId);
    }

    @Override
    public boolean isFDConsolidationTopical(long fdTargetId) {
        return sourceDao.isFDConsolidationTopical(fdTargetId);
    }

    @Override
    public boolean isDDConsolidationTopical(long ddTargetId) {
        return sourceDao.isDDConsolidationTopical(ddTargetId);
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
    public List<FormToFormRelation> getRelations(FormData formData, Logger logger, TAUserInfo userInfo) {
        int departmentId = formData.getDepartmentId();
        int formTypeId = formData.getFormType().getId();
        FormDataKind kind = formData.getKind();
        int departmentReportPeriodId = formData.getDepartmentReportPeriodId();
        Integer periodOrder = formData.getPeriodOrder();

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(departmentReportPeriodId);
        ReportPeriod reportPeriod = departmentReportPeriod.getReportPeriod();

        List<FormToFormRelation> formToFormRelations = new LinkedList<FormToFormRelation>();
        // Источники
        List<DepartmentFormType> sourcesForm = formDataService.getFormSources(formData, logger, userInfo, reportPeriod, false);
        formToFormRelations.addAll(createFormToFormRelationModel(sourcesForm, departmentReportPeriod,
                periodOrder, true));
        // Приемники
        List<DepartmentFormType> destinationsForm = getFormDestinations(departmentId, formTypeId, kind,
                reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        formToFormRelations.addAll(createFormToFormRelationModel(destinationsForm, departmentReportPeriod,
                periodOrder, false));

        List<DepartmentDeclarationType> destinationsDeclaration = getDeclarationDestinations(departmentId, formTypeId, kind,
                reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        formToFormRelations.addAll(createFormToFormRelationModel(destinationsDeclaration, departmentReportPeriod));

        return formToFormRelations;
    }

    @Override
    public List<FormToFormRelation> getRelations(DeclarationData declaration) {
        List<FormToFormRelation> formToFormRelations = new LinkedList<FormToFormRelation>();
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());

        //Получаем источники-приемники
        List<DepartmentFormType> sourcesForm = declarationDataService.getFormDataSources(declaration, false, new Logger());
        formToFormRelations.addAll(createFormToFormRelationModel(sourcesForm, departmentReportPeriod,
                null, true));
        return formToFormRelations;
    }

    @Override
    public List<Pair<DepartmentFormType, Pair<Date, Date>>> findDestinationFTsForFormType(int typeId, Date dateFrom, Date dateTo) {
        if (dateFrom == null){
            dateTo = MIN_DATE;
        }
        if (dateTo == null){
            dateTo = MAX_DATE;
        }
        return departmentFormTypeDao.findDestinationsForFormType(typeId, dateFrom, dateTo);
    }

    @Override
    public List<Pair<DepartmentFormType, Pair<Date, Date>>> findSourceFTsForFormType(int typeId, Date dateFrom, Date dateTo) {
        if (dateFrom == null){
            dateTo = MIN_DATE;
        }
        if (dateTo == null){
            dateTo = MAX_DATE;
        }
        return departmentFormTypeDao.findSourcesForFormType(typeId, dateFrom, dateTo);
    }

    @Override
    public List<Pair<DepartmentFormType, Pair<Date, Date>>> findSourceFTsForDeclaration(int typeId, Date dateFrom, Date dateTo) {
        if (dateFrom == null){
            dateTo = MIN_DATE;
        }
        if (dateTo == null){
            dateTo = MAX_DATE;
        }
        return departmentDeclarationTypeDao.findSourceFTsForDeclaration(typeId, dateFrom, dateTo);
    }

    @Override
    public List<Pair<DepartmentDeclarationType, Pair<Date, Date>>> findDestinationDTsForFormType(int typeId, Date dateFrom, Date dateTo) {
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
                        periodOrder==null?departmentFormType.getPeriodOrder():null));
            } else {
                formToFormRelations.addAll(getDestinationList(departmentFormType, departmentReportPeriod,
                        periodOrder));
            }
        }
        return formToFormRelations;
    }

    /**
     * Метод для составления списка с информацией об источниках приемниках
     */
    private List<FormToFormRelation> createFormToFormRelationModel(List<DepartmentDeclarationType> departmentDeclarationTypes,
                                                                   DepartmentReportPeriod departmentReportPeriod){
        List<FormToFormRelation> formToFormRelations = new LinkedList<FormToFormRelation>();

        // По назначениям
        for (DepartmentDeclarationType departmentDeclarationType : departmentDeclarationTypes) {
            formToFormRelations.addAll(getDestinationList(departmentDeclarationType, departmentReportPeriod));
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
        formToFormRelation.setPerformer(departmentFormType.getPerformerId() != null ? departmentService.getDepartment(departmentFormType.getPerformerId()) : null);
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
     * Подготовка общей модели для сущестувющих и не существующих экземпляров
     */
    private FormToFormRelation getRelationCommon(DepartmentDeclarationType departmentDeclarationType,
                                                 DepartmentReportPeriod departmentreportPeriod) {
        FormToFormRelation formToFormRelation = new FormToFormRelation();
        formToFormRelation.setSource(false);
        formToFormRelation.setFullDepartmentName(departmentService.getParentsHierarchy(departmentDeclarationType.getDepartmentId()));
        if (departmentreportPeriod != null) {
            if (departmentreportPeriod.getCorrectionDate() != null) {
                formToFormRelation.setCorrectionDate(departmentreportPeriod.getCorrectionDate());
            }
            formToFormRelation.setYear(departmentreportPeriod.getReportPeriod().getTaxPeriod().getYear());
            formToFormRelation.setPeriodName(departmentreportPeriod.getReportPeriod().getName());
        }
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
            formToFormRelation.setCreated(false);
            formToFormRelation.setFormType(formTypeDao.get(departmentFormType.getFormTypeId()));
            formToFormRelation.setFormDataKind(departmentFormType.getKind());
            formToFormRelation.setYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());
            formToFormRelation.setPeriodName(departmentReportPeriod.getReportPeriod().getName());
        }
        return formToFormRelation;
    }

    /**
     * Заполенени модели отношения данными из экземпляра декларации или не созданного экземпляра
     */
    private FormToFormRelation performFormDataRelation(DeclarationData declarationData, FormToFormRelation formToFormRelation,
                                                       DepartmentDeclarationType departmentDeclarationType,
                                                       DepartmentReportPeriod departmentReportPeriod) {
        if (declarationData != null) {
            // Созданный экземпляр найден
            formToFormRelation.setCreated(true);
            formToFormRelation.setDeclarationType(declarationTypeDao.get(departmentDeclarationType.getDeclarationTypeId()));
            formToFormRelation.setDeclarationDataId(declarationData.getId());
            formToFormRelation.setState(declarationData.isAccepted()?WorkflowState.ACCEPTED:WorkflowState.CREATED);
            formToFormRelation.setKpp(declarationData.getKpp());
            formToFormRelation.setTaxOrganCode(declarationData.getTaxOrganCode());
        } else {
            // Экземпляр не найден
            formToFormRelation.setCreated(false);
            formToFormRelation.setDeclarationType(declarationTypeDao.get(departmentDeclarationType.getDeclarationTypeId()));
            formToFormRelation.setYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());
            formToFormRelation.setPeriodName(departmentReportPeriod.getReportPeriod().getName());
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
        /*if (!formTemplateService.existFormTemplate(departmentFormType.getFormTypeId(), departmentReportPeriod.getReportPeriod().getId()))
            return relations;*/

        FormData formData = formDataDao.getLastByDate(departmentFormType.getFormTypeId(), departmentFormType.getKind(),
                departmentFormType.getDepartmentId(), departmentReportPeriod.getReportPeriod().getId(),
                periodOrder, departmentReportPeriod.getCorrectionDate());
        DepartmentReportPeriod formDepartmentReportPeriod = null;
        if (formData != null) {
            formDepartmentReportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        }
        FormToFormRelation formToFormRelation = performFormDataRelation(formData,
                getRelationCommon(true, departmentFormType, formDepartmentReportPeriod, periodOrder), departmentFormType,
                departmentReportPeriod);
        formToFormRelation.setStatus(formTemplateService.existFormTemplate(departmentFormType.getFormTypeId(), departmentReportPeriod.getReportPeriod().getId(), true));
        //if (formToFormRelation != null) relations.add(formToFormRelation);
        relations.add(formToFormRelation);
        //}
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
        /*if (!formTemplateService.existFormTemplate(departmentFormType.getFormTypeId(), departmentReportPeriod.getReportPeriod().getId()))
            return retVal;*/

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setReportPeriodIdList(Arrays.asList(departmentReportPeriod.getReportPeriod().getId()));
        filter.setDepartmentIdList(Arrays.asList(departmentFormType.getDepartmentId()));
        // Список всех отчетных периодов
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);
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

        int formTemplateId = formTemplateService.getFormTemplateIdByFTAndReportPeriod(departmentFormType.getFormTypeId(), departmentReportPeriod.getReportPeriod().getId());
        if (!formTemplateService.get(formTemplateId).isMonthly()) {
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
            formToFormRelation.setStatus(formTemplateService.existFormTemplate(departmentFormType.getFormTypeId(), departmentReportPeriod.getReportPeriod().getId(), false));

            retVal.add(formToFormRelation);
        }
        return retVal;
    }

    /**
     * Список экземпляров-приемников
     * @param departmentDeclarationType Назначение
     * @param departmentReportPeriod Отчетный период подраделения формы, для которой ищутся формы-приемники
     */
    private List<FormToFormRelation> getDestinationList(DepartmentDeclarationType departmentDeclarationType,
                                                        DepartmentReportPeriod departmentReportPeriod) {
        List<FormToFormRelation> retVal = new LinkedList<FormToFormRelation>();
        /*if (!declarationTemplateService.existDeclarationTemplate(departmentDeclarationType.getDeclarationTypeId(), departmentReportPeriod.getReportPeriod().getId()))
            return retVal;*/

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setReportPeriodIdList(Arrays.asList(departmentReportPeriod.getReportPeriod().getId()));
        filter.setDepartmentIdList(Arrays.asList(departmentDeclarationType.getDepartmentId()));
        // Список всех отчетных периодов
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);
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

        for (DepartmentReportPeriod destinationReportPeriod : departmentReportPeriodList) {
            // Поиск экземпляра декларации в каждом существующем отчетном периоде подразделения
            List<DeclarationData> declarationDataList = declarationDataDao.find(departmentDeclarationType.getDeclarationTypeId(), destinationReportPeriod.getId().intValue());

            if (declarationDataList.isEmpty()) {
                FormToFormRelation formToFormRelation = performFormDataRelation(null,
                        getRelationCommon(departmentDeclarationType, destinationReportPeriod), departmentDeclarationType,
                        departmentReportPeriod);
                formToFormRelation.setStatus(declarationTemplateService.existDeclarationTemplate(departmentDeclarationType.getDeclarationTypeId(), departmentReportPeriod.getReportPeriod().getId()));

                retVal.add(formToFormRelation);
            } else {
                for (DeclarationData declarationData: declarationDataList) {
                    FormToFormRelation formToFormRelation = performFormDataRelation(declarationData,
                            getRelationCommon(departmentDeclarationType, destinationReportPeriod), departmentDeclarationType,
                            departmentReportPeriod);
                    formToFormRelation.setStatus(declarationTemplateService.existDeclarationTemplate(departmentDeclarationType.getDeclarationTypeId(), departmentReportPeriod.getReportPeriod().getId()));

                    retVal.add(formToFormRelation);
                }
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
}
