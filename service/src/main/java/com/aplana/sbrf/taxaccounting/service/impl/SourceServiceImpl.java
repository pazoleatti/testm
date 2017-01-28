package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.source.*;
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

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

	private static final Date MIN_DATE = new Date(0);

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
    private ReportPeriodDao reportPeriodDao;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private FormDataScriptingService formDataScriptingService;
    @Autowired
    private DeclarationDataScriptingService declarationDataScriptingService;

	/**
	 * Проверяет начало диапазона дат и если оно не задано, то возвращает значение по умолчанию
	 * @param date
	 * @return
	 */
	private Date checkMinDate(Date date) {
		return date != null ? date : MIN_DATE;
	}

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
                if (!emptyIsOk && sourcePairsOut.isEmpty()) {
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
     * @return обрезанный входной список связок источников-приемников, которые все еще существуют
     */
    public List<SourcePair> checkExistence(Logger logger, List<SourcePair> sourcePairs,
                                           SourceMode mode,
                                           final boolean isDeclaration) {
        List<Long> rightPart = new ArrayList<Long>();
        if (isDeclaration) {
            if (mode == SourceMode.SOURCES) {
                //Проверяем единственный приемник
                if (sourceDao.checkDDTExistence(Arrays.asList(sourcePairs.get(0).getDestination())).isEmpty()) {
                    /** Если единственное назначение было удалено, то продолжать нет смысла */
                    logger.error(String.format(CHECK_EXISTENCE_MSG,
                            "Декларация",
                            sourcePairs.get(0).getDestinationType(),
                            sourcePairs.get(0).getDestinationDepartmentName()));
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
                            sourcePairs.get(0).getSourceDepartmentName()));
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
                                isSource ? sourcePair.getSourceDepartmentName() : sourcePair.getDestinationDepartmentName()));
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
     * @param declaration   признак того, что идет обработка в режиме "Декларации"
     * @param consolidatedInstances   список идентификаторов экземпляров источник-приемник с предыдущего шага обработки
     * @return список идентификаторов экземпляров источник-приемник
     */
    public Set<ConsolidatedInstance> checkFormInstances(Logger logger, List<SourcePair> sourcePairs, Date newPeriodStart, Date newPeriodEnd, boolean declaration, Set<ConsolidatedInstance> consolidatedInstances) {
        for (SourcePair sourcePair : sourcePairs) {
            /** Получаем промежуточные периоды, которые будут объединены при создании новой версии */
            List<SourceObject> emptyPeriods = sourceDao.getEmptyPeriods(sourcePair,
                    newPeriodStart, newPeriodEnd);
            if (!emptyPeriods.isEmpty()) {
                for (SourceObject empty : emptyPeriods) {
                    /** Получаем экземпляры приемника в промежуточных периодах + идентификаторы экземпляров их источников */
                    consolidatedInstances.addAll(sourceDao.findConsolidatedInstances(
                            empty.getSourcePair().getSource(), empty.getSourcePair().getDestination(),
                            empty.getPeriodStart(), empty.getPeriodEnd(), declaration));
                }
            } else {
                /** Получаем экземпляры приемника в новом периоде + идентификаторы экземпляров их источников */
                consolidatedInstances.addAll(sourceDao.findConsolidatedInstances(
                        sourcePair.getSource(), sourcePair.getDestination(),
                        newPeriodStart, newPeriodEnd, declaration));
            }

            /** Выводим информацию о найденных экземплярах-приемниках */
            printConsolidationInstancesInfo(consolidatedInstances, logger);
        }
        return consolidatedInstances;
    }

    private void printConsolidationInstancesInfo(Set<ConsolidatedInstance> consolidatedInstances, Logger logger) {
        if (!consolidatedInstances.isEmpty()) {
            boolean hasForm = false;
            boolean hasDeclaration = false;
            // Исключаем приемники, которые уже обработаны
            Set<Long> processedDestinations = new HashSet<Long>();

            /** Надо переконсолидировать декларации-приемники */
            for (ConsolidatedInstance declaration : consolidatedInstances) {
                if (declaration.isDeclaration()) {
                    if (!hasDeclaration) {
                        logger.warn(RECALCULATE_DECLARATION_MSG);
                        hasDeclaration = true;
                    }
                    if (!processedDestinations.contains(declaration.getId())) {
                        logger.warn(String.format(DECLARATION_INSTANCE_MSG,
                                        declaration.getType(),
                                        declaration.getDepartment(),
                                        declaration.getPeriod(),
                                        declaration.getCorrectionDate() != null
                                                ? " с датой сдачи корректировки " + sdf.get().format(declaration.getCorrectionDate())
                                                : "",
                                        declaration.getTaxOrganCode() != null
                                                ? ", налоговый орган " + declaration.getTaxOrganCode()
                                                : "",
                                        declaration.getKpp() != null
                                                ? ", КПП " + declaration.getKpp()
                                                : "")
                        );
                        processedDestinations.add(declaration.getId());
                    }
                }
            }

            /** Надо переконсолидировать нф-приемники */
            for (ConsolidatedInstance form : consolidatedInstances) {
                if (!form.isDeclaration()) {
                    if (!hasForm) {
                        logger.warn(RECONSOLIDATE_FORM_MSG);
                        hasForm = true;
                    }
                    if (!processedDestinations.contains(form.getId())) {
                        DepartmentReportPeriod drpCompare = form.getDrpComapreId() != null ?
                                departmentReportPeriodService.get(form.getDrpComapreId()) : null;
                        logger.warn(MessageGenerator.getFDMsg("",
                                        form.getType(),
                                        form.getFormKind().getTitle(),
                                        form.getDepartment(),
                                        form.getMonth(),
                                        form.isManual(),
                                        form.getPeriod(),
                                        form.getCorrectionDate(),
                                        drpCompare != null ?
                                                drpCompare.getReportPeriod().getName() + " " + drpCompare.getReportPeriod().getTaxPeriod().getYear() : ""
                                )
                        );
                    }
                    processedDestinations.add(form.getId());
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
                                intersection.getSourcePair().getDestinationDepartmentName() + ", " +
                                        (isDeclaration ? intersection.getSourcePair().getDestinationType() : intersection.getSourcePair().getDestinationKind() + ": " + intersection.getSourcePair().getDestinationType()),
                                "приемника",
                                intersection.getSourcePair().getSourceDepartmentName() + ", " + intersection.getSourcePair().getSourceKind() + ": " + intersection.getSourcePair().getSourceType(),
                                sdf.get().format(intersection.getPeriodStart()) + " - " +
                                        (intersection.getPeriodEnd() != null ? sdf.get().format(intersection.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                        ));
                    } else {
                        msgsForPair.add(String.format(INTERSECTION_PART,
                                intersection.getSourcePair().getSourceDepartmentName() + ", " + intersection.getSourcePair().getSourceKind() + ": " + intersection.getSourcePair().getSourceType(),
                                "источника",
                                intersection.getSourcePair().getDestinationDepartmentName() + ", " +
                                        (isDeclaration ? intersection.getSourcePair().getDestinationType() : intersection.getSourcePair().getDestinationKind() + ": " + intersection.getSourcePair().getDestinationType()),
                                sdf.get().format(intersection.getPeriodStart()) + " - " +
                                        (intersection.getPeriodEnd() != null ? sdf.get().format(intersection.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
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
                                period = sdf.get().format(union.getPeriodStart()) + " - " +
                                        (union.getPeriodEnd() != null ? sdf.get().format(union.getPeriodEnd()) : EMPTY_END_PERIOD_INFO);
                            } else {
                                //Идет редактирование назначений
                                Pair<Date, Date> periodDates = unionPairs.get(sourcePair);
                                period = sdf.get().format(periodDates.getFirst()) + " - " +
                                        (periodDates.getSecond() != null ? sdf.get().format(periodDates.getSecond()) : EMPTY_END_PERIOD_INFO);
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
            /** Проверка существования назначения подразделению */
            List<SourcePair> sourcePairs = checkExistence(logger, sourceClientData.getSourcePairs(),
                    sourceClientData.getMode(), sourceClientData.isDeclaration());

            /** Специфичные проверки */
            checkSpecifics(logger, sourceClientData.getSourcePairs(),
                    sourceClientData.getMode(), sourceClientData.isDeclaration(), sourceClientData.getTaxType());

            /** Проверка существования экземпляров нф */
            checkFormInstances(logger, sourcePairs, sourceClientData.getPeriodStart(), sourceClientData.getPeriodEnd(),
                    sourceClientData.isDeclaration(), new HashSet<ConsolidatedInstance>());

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
                                sourceObject.getSourcePair().getDestinationDepartmentName() + ", " +
                                        (sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                        sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType()),
                                "приемником",
                                sourceObject.getSourcePair().getSourceDepartmentName() + ", " + sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                                sdf.get().format(sourceObject.getPeriodStart()) + " - " +
                                        (sourceObject.getPeriodEnd() != null ? sdf.get().format(sourceObject.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                        );
                    } else {
                        logger.info(SAVE_SUCCESS_MSG,
                                sourceObject.getSourcePair().getSourceDepartmentName() + ", " + sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                                "источником",
                                sourceObject.getSourcePair().getDestinationDepartmentName() + ", " +
                                        (sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                        sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType()),
                                sdf.get().format(sourceObject.getPeriodStart()) + " - " +
                                        (sourceObject.getPeriodEnd() != null ? sdf.get().format(sourceObject.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
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
        /*for (SourcePair pair : sourcePairs) {
            *//** Для транспортного налога и имущества источник и приемник должен относиться к одному ТБ *//*
            if (taxType == TaxType.TRANSPORT || taxType == TaxType.PROPERTY || taxType == TaxType.LAND) {
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
        }*/
    }

    @Override
    public void deleteSources(Logger logger, SourceClientData sourceClientData) {
        if (sourceClientData.getSourcePairs() != null && !sourceClientData.getSourcePairs().isEmpty()) {
            List<SourceObject> sourceObjects = sourceClientData.getSourceObjects();

            /** Получаем информацию о приемниках в удаляемом периоде + идентификаторы экземпляров их источников */
            Set<ConsolidatedInstance> consolidatedInstances = new HashSet<ConsolidatedInstance>();
            Set<Long> processedSources = new HashSet<Long>();
            for (SourceObject sourceObject: sourceObjects) {
                final Long source = sourceObject.getSourcePair().getSource();
                final Long destination = sourceObject.getSourcePair().getDestination();
                if (!processedSources.contains(source)) {
                    consolidatedInstances.addAll(sourceDao.findConsolidatedInstances(
                            source, destination,
                            sourceObject.getPeriodStart(), sourceObject.getPeriodEnd(), sourceClientData.isDeclaration()));
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
                            sourceObject.getSourcePair().getDestinationDepartmentName() + ", " + (
                                    sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                    sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType()),
                            "приемника",
                            sourceClientData.isDeclaration() ? "декларации" : "формы",
                            sourceObject.getSourcePair().getSourceDepartmentName() + ", " + sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                            sdf.get().format(sourceObject.getPeriodStart()) + " - " +
                                    (sourceObject.getPeriodEnd() != null ? sdf.get().format(sourceObject.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                    );
                }
            } else {
                for (SourceObject sourceObject : sourceObjects) {
                    logger.info(DELETE_SUCCESS_MSG,
                            sourceObject.getSourcePair().getSourceDepartmentName() + ", " + sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                            "источника",
                            sourceClientData.isDeclaration() ? "декларации" : "формы",
                            sourceObject.getSourcePair().getDestinationDepartmentName() + ", " +
                                    (sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                    sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType()),
                            sdf.get().format(sourceObject.getPeriodStart()) + " - " +
                                    (sourceObject.getPeriodEnd() != null ? sdf.get().format(sourceObject.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                    );
                }
            }
            if (!consolidatedInstances.isEmpty()) {
                //Делаем неактуальным признак консолидации для пар источник-приемник
                sourceDao.updateConsolidationInfo(consolidatedInstances, sourceClientData.isDeclaration());
            }
        } else {
            throw new ServiceException(EMPTY_LIST_MSG);
        }
    }

    @Override
    public void updateSources(Logger logger, List<SourceClientData> sourceClientDataList) {
        ServiceLoggerException criticalError = null;
        // Пары экземпляров источник-приемник, для которых надо отменить статус консолидации
        Set<ConsolidatedInstance> unconsolidatedInstances = new HashSet<ConsolidatedInstance>();

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
                            checkFormInstances(logger, sourcePairs, periodStart, SimpleDateUtils.addDayToDate(oldPeriodStart, -1), sourceClientData.isDeclaration(), unconsolidatedInstances);
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(periodEnd, 1), oldPeriodEnd, sourceClientData.isDeclaration(), unconsolidatedInstances);
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, oldPeriodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd,
                                    oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        } else if (periodEnd != null && (periodEnd.before(oldPeriodStart))) {
                            /** Дата окончания нового периода меньше даты начала старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, oldPeriodEnd, sourceClientData.isDeclaration(), unconsolidatedInstances);
                            checkFormInstances(logger, sourcePairs, periodStart, periodEnd, sourceClientData.isDeclaration(), unconsolidatedInstances);
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
                            checkFormInstances(logger, sourcePairs, periodStart, SimpleDateUtils.addDayToDate(oldPeriodStart, -1), sourceClientData.isDeclaration(), unconsolidatedInstances);
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd, sourceClientData.isDeclaration(), unconsolidatedInstances);
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd,
                                    oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        } else if ((periodEnd == null && oldPeriodEnd == null) || (periodEnd.equals(oldPeriodEnd))) {
                            /** Равна дате окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, periodStart, SimpleDateUtils.addDayToDate(oldPeriodStart, -1), sourceClientData.isDeclaration(), unconsolidatedInstances);
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
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(periodEnd, 1), oldPeriodEnd, sourceClientData.isDeclaration(), unconsolidatedInstances);
                        } else if ((periodEnd == null && oldPeriodEnd != null)
                                || (periodEnd != null && periodEnd.after(oldPeriodEnd))) {
                            /** Дата окончания нового периода больше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd, sourceClientData.isDeclaration(), unconsolidatedInstances);
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
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1), sourceClientData.isDeclaration(), unconsolidatedInstances);

                        } else if (periodEnd != null && (oldPeriodEnd == null || periodEnd.before(oldPeriodEnd))) {
                            /** Дата окончания нового периода меньше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1), sourceClientData.isDeclaration(), unconsolidatedInstances);
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(periodEnd, 1), oldPeriodEnd, sourceClientData.isDeclaration(), unconsolidatedInstances);
                        } else if ((periodEnd == null && oldPeriodEnd != null)|| (periodEnd.after(oldPeriodEnd))) {
                            /** Дата окончания нового периода больше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1), sourceClientData.isDeclaration(), unconsolidatedInstances);
                            checkFormInstances(logger, sourcePairs, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd, sourceClientData.isDeclaration(), unconsolidatedInstances);
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd,
                                    oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        } else if (periodEnd.after(oldPeriodEnd)) {
                            /** Дата окончания нового периода равна дате окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, SimpleDateUtils.addDayToDate(periodStart, -1), sourceClientData.isDeclaration(), unconsolidatedInstances);
                        }
                    } else if (oldPeriodEnd != null && periodStart.after(oldPeriodEnd)) {
                        /** Дата начала нового периода больше даты окончания старого периода */
                        if (periodEnd == null || periodEnd.after(oldPeriodEnd)) {
                            /** Дата окончания нового периода больше даты окончания старого периода */

                            /** Проверка существования экземпляров нф */
                            checkFormInstances(logger, sourcePairs, oldPeriodStart, oldPeriodEnd, sourceClientData.isDeclaration(), unconsolidatedInstances);
                            checkFormInstances(logger, sourcePairs, periodStart, periodEnd, sourceClientData.isDeclaration(), unconsolidatedInstances);
                            /** Проверка зацикливания */
                            sourcePairs = checkLoops(logger, periodStart, periodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                            /** Проверка пересечений */
                            sourcePairs = checkIntersections(logger, periodStart, periodEnd, oldPeriodStart, oldPeriodEnd,
                                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                        }
                    }
                    if (!sourcePairs.isEmpty()) {

                        if (!unconsolidatedInstances.isEmpty()) {
                            // удаляем информацию о консолидации
                            sourceDao.updateConsolidationInfo(unconsolidatedInstances, sourceClientData.isDeclaration());
                        }

                        List<SourceObject> sourceObjects = pairsToObjects(sourcePairs, oldPeriodStart, oldPeriodEnd);
                        sourceDao.updateAll(sourceObjects, periodStart, periodEnd, sourceClientData.isDeclaration());
                        if (sourceClientData.getMode() == SourceMode.DESTINATIONS) {
                            for (SourceObject sourceObject : sourceObjects) {
                                logger.info(UPDATE_SUCCESS_MSG,
                                        sourceObject.getSourcePair().getDestinationDepartmentName() + ", " +
                                                (sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                                sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType()),
                                        "приемником",
                                        sourceObject.getSourcePair().getSourceDepartmentName() + ", " + sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                                        sdf.get().format(periodStart) + " - " +
                                                (periodEnd != null ? sdf.get().format(periodEnd) : EMPTY_END_PERIOD_INFO)
                                );
                            }
                        } else {
                            for (SourceObject sourceObject : sourceObjects) {
                                logger.info(UPDATE_SUCCESS_MSG,
                                        sourceObject.getSourcePair().getSourceDepartmentName() + ", " + sourceObject.getSourcePair().getSourceKind() + ": " + sourceObject.getSourcePair().getSourceType(),
                                        "источником",
                                        sourceObject.getSourcePair().getDestinationDepartmentName() + ", " +
                                                (sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationType() :
                                                sourceObject.getSourcePair().getDestinationKind() + ": " + sourceObject.getSourcePair().getDestinationType()),
                                        sdf.get().format(periodStart) + " - " +
                                                (periodEnd != null ? sdf.get().format(periodEnd) : EMPTY_END_PERIOD_INFO)
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
    public void updateDDConsolidation(long sourceFormId) {
        sourceDao.updateDDConsolidationInfo(sourceFormId);
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
    public void saveDFT(Long departmentId, int typeId, int formId, List<Integer> performerIds) {
        long dftId = departmentFormTypeDao.save(departmentId.intValue(), typeId, formId);
        //Сохраняем исполнителей
        departmentFormTypeDao.savePerformers(dftId, performerIds);
    }

    @Override
    public void updatePerformers(int id, List<Integer> performerIds) {
        //Удаляем всех исполнителей и назначаем новых
        departmentFormTypeDao.deletePerformers(id);
        if (performerIds != null && !performerIds.isEmpty()) {
            departmentFormTypeDao.savePerformers(id, performerIds);
        }
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
    public List<Pair<DepartmentFormType, Pair<Date, Date>>> findDestinationFTsForFormType(int typeId, Date dateFrom, Date dateTo) {
        return departmentFormTypeDao.findDestinationsForFormType(typeId, checkMinDate(dateFrom), dateTo);
    }

    @Override
    public List<Pair<DepartmentFormType, Pair<Date, Date>>> findSourceFTsForFormType(int typeId, Date dateFrom, Date dateTo) {
        return departmentFormTypeDao.findSourcesForFormType(typeId, checkMinDate(dateFrom), dateTo);
    }

    @Override
    public List<Pair<DepartmentFormType, Pair<Date, Date>>> findSourceFTsForDeclaration(int typeId, Date dateFrom, Date dateTo) {
        return departmentDeclarationTypeDao.findSourceFTsForDeclaration(typeId, checkMinDate(dateFrom),dateTo);
    }

    @Override
    public List<Pair<DepartmentDeclarationType, Pair<Date, Date>>> findDestinationDTsForFormType(int typeId, Date dateFrom, Date dateTo) {
        return departmentDeclarationTypeDao.findDestinationDTsForFormType(typeId, checkMinDate(dateFrom), dateTo);
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
     * Фильтр по умолчанию
     */
    private QueryParams<SourcesSearchOrdering> getSearchOrderingDefaultFilter() {
        QueryParams<SourcesSearchOrdering> queryParams = new QueryParams<SourcesSearchOrdering>();
        queryParams.setSearchOrdering(SourcesSearchOrdering.TYPE);
        queryParams.setAscending(true);
        return queryParams;
    }

    @Override
    public List<Relation> getSourcesInfo(FormData destinationFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction, TAUserInfo userInfo, Logger logger) {
        /** Проверяем в скрипте источники-приемники для особенных форм/деклараций */
        Map<String, Object> params = new HashMap<String, Object>();
        FormSources sources = new FormSources();
        sources.setSourceList(new ArrayList<Relation>());
        sources.setSourcesProcessedByScript(false);
        params.put("sources", sources);
        params.put("light", light);
        params.put("excludeIfNotExist", excludeIfNotExist);
        params.put("stateRestriction", stateRestriction);
        params.put("needSources", true);
        params.put("form", true);

        formDataScriptingService.executeScript(userInfo, destinationFormData, FormDataEvent.GET_SOURCES, logger, params);
        if (sources.isSourcesProcessedByScript()) {
            return sources.getSourceList();
        } else {
            return sourceDao.getSourcesInfo(destinationFormData, light, excludeIfNotExist, stateRestriction);
        }
    }

    @Override
    public List<Relation> getDestinationsInfo(FormData sourceFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction, TAUserInfo userInfo, Logger logger) {
        /** Проверяем в скрипте источники-приемники для особенных форм/деклараций */
        Map<String, Object> params = new HashMap<String, Object>();
        FormSources sources = new FormSources();
        sources.setSourceList(new ArrayList<Relation>());
        sources.setSourcesProcessedByScript(false);
        params.put("sources", sources);
        params.put("light", light);
        params.put("excludeIfNotExist", excludeIfNotExist);
        params.put("stateRestriction", stateRestriction);
        params.put("needSources", false);
        params.put("form", true);

        formDataScriptingService.executeScript(userInfo, sourceFormData, FormDataEvent.GET_SOURCES, logger, params);
        if (sources.isSourcesProcessedByScript()) {
            return sources.getSourceList();
        } else {
            return sourceDao.getDestinationsInfo(sourceFormData, light, excludeIfNotExist, stateRestriction);
        }
    }

    @Override
    public List<Relation> getDeclarationDestinationsInfo(FormData sourceFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction, TAUserInfo userInfo, Logger logger) {
        /** Проверяем в скрипте источники-приемники для особенных форм/деклараций */
        Map<String, Object> params = new HashMap<String, Object>();
        FormSources sources = new FormSources();
        sources.setSourceList(new ArrayList<Relation>());
        sources.setSourcesProcessedByScript(false);
        params.put("sources", sources);
        params.put("light", light);
        params.put("excludeIfNotExist", excludeIfNotExist);
        params.put("stateRestriction", stateRestriction);
        params.put("needSources", false);
        params.put("form", false);

        formDataScriptingService.executeScript(userInfo, sourceFormData, FormDataEvent.GET_SOURCES, logger, params);
        if (sources.isSourcesProcessedByScript()) {
            return sources.getSourceList();
        } else {
            return sourceDao.getDeclarationDestinationsInfo(sourceFormData, light, excludeIfNotExist, stateRestriction);
        }
    }

    @Override
    public List<Relation> getDeclarationSourcesInfo(DeclarationData declaration, boolean light, boolean excludeIfNotExist, State stateRestriction, TAUserInfo userInfo, Logger logger) {
        /** Проверяем в скрипте источники-приемники для особенных форм/деклараций */
        Map<String, Object> params = new HashMap<String, Object>();
        FormSources sources = new FormSources();
        sources.setSourceList(new ArrayList<Relation>());
        sources.setSourcesProcessedByScript(false);
        params.put("sources", sources);
        params.put("light", light);
        params.put("excludeIfNotExist", excludeIfNotExist);
        params.put("stateRestriction", stateRestriction);
        params.put("needSources", true);
        params.put("form", true);

        Logger scriptLogger = new Logger();
        declarationDataScriptingService.executeScript(userInfo, declaration, FormDataEvent.GET_SOURCES, scriptLogger, params);
        logger.getEntries().addAll(scriptLogger.getEntries());
        if (scriptLogger.containsLevel(LogLevel.ERROR)) { // проверяем scriptLogger, т.к. в logger уже могут быть ошибки
            throw new ServiceLoggerException("Обнаружены фатальные ошибки!", logEntryService.save(logger.getEntries()));
        }
        return sources.getSourceList();
    }
}
