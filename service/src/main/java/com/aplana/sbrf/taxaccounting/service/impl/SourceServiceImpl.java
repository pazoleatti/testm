package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.source.SourceClientData;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.model.source.SourceObject;
import com.aplana.sbrf.taxaccounting.model.source.SourcePair;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
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
    private static final String SOURCES_LIST_IS_EMPTY_MSG = "Все назначения были исключены в результате проверок. Продолжение операции невозможно.";
    private static final String EMPTY_END_PERIOD_INFO = "дата окончания периода не задана";
    private static final String INTERSECTION_PART = "\"%s\" в качестве %s для \"%s\" в периоде %s";
    private static final String INTERSECTION_MSG_BEGIN = "Найдены существующие назначения:";
    private static final String INTERSECTION_MSG_END = "Новое назначение создано путем слияния с найденными назначениями, новое назначение действует в периоде %s.";
    private static final String SAVE_SUCCESS_MSG = "\"%s\" назначен %s формы \"%s\" в периоде %s.";
    private static final String DELETE_SUCCESS_MSG = "Удалено назначение \"%s\" в роли %s %s \"%s\" в периоде %s.";
    private static final String UPDATE_SUCCESS_MSG = "\"%s\" назначен %s формы \"%s\" в периоде %s.";
    private static final String CIRCLE_MSG = "\"%s\" уже назначен как приёмник \"%s\"";
    private static final String FORM_INSTANCES_MSG = "Для корректной передачи данных в форму-приёмник необходимо выполнить повторный перевод в статус \"Принята\" формы \"%s\" для подразделения \"%s\" в периодах: %s";
    private static final String DECLARATION_INSTANCES_MSG = "Для корректного получении данных необходимо выполнить повторное формирование при помощи кнопки \"Обновить\" во всех экземплярах \"%s\" для подразделения \"%s\" в периодах: %s";
    private static final String EDIT_FATAL_ERROR = "Найдены экземпляры \"%s\" для подразделения \"%s\" в периодах: %s в статусе \"Принята\". Для удаления их назначения в качестве источника необходимо выполнить их возврат из статуса \"Принята\"";
    private static final String DELETE_FATAL_ERROR_BEGIN = "Найдены экземпляры в статусе \"Принята\":";
    private static final String DELETE_FATAL_ERROR_MID = "\"%s\" для подразделения \"%s\" в периодах: %s";
    private static final String DELETE_FATAL_ERROR_END = "Для удаления их назначения в качестве источника необходимо выполнить их возврат из статуса \"Принята\"";
    private static final String EMPTY_LIST_MSG = "Список назначений пуст!";

    private enum LOG_LEVEL {
        INFO, WARN, ERROR
    }

    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
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
    DepartmentFormTypeDao departmentFormTypeDao;

    @Autowired
    DepartmentDeclarationTypeDao departmentDeclarationTypeDao;

    @Autowired
    FormTypeDao formTypeDao;

    @Autowired
    DeclarationTypeDao declarationTypeDao;

    @Autowired
    FormDataService formDataService;

    @Autowired
    DepartmentDao departmentDao;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    SourceDao sourceDao;

    @Autowired
    FormDataDao formDataDao;

    @Autowired
    DeclarationDataDao declarationDataDao;

    @Autowired
    ReportPeriodDao reportPeriodDao;

    @Autowired
    LogEntryService logEntryService;

    @Autowired
    FormTemplateDao formTemplateDao;

    @Autowired
    RefBookFactory rbFactory;

    @Autowired
    DepartmentReportPeriodDao departmentReportPeriodDao;

    @Override
    public List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind, Date periodStart,
                                                       Date periodEnd) {
        QueryParams filter = getSearchOrderingDefaultFilter();
        return getDFTSourcesByDFT(departmentId, formTypeId, kind, periodStart, periodEnd, filter);
    }

    @Override
    public List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind, Date periodStart,
                                                       Date periodEnd, QueryParams queryParams) {
        return departmentFormTypeDao.getFormSources(departmentId, formTypeId, kind, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind, int reportPeriodId) {
        QueryParams queryParams = getSearchOrderingDefaultFilter();
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
        QueryParams queryParams = getSearchOrderingDefaultFilter();
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
        List<String> getSourceMessage(SourcePair sourcePair);

        /**
         * Возвращает сообщение при обнаружении вхождения исключаемого назначения в списке приемников
         *
         * @param sourcePair обрабатываемая пара источник-приемник
         * @return сообщение
         */
        List<String> getDestinationMessage(SourcePair sourcePair);
    }

    /**
     * Метод исключает указанные назначения из списка пар источников-приемников
     *
     * @param logger                   логгер
     * @param sourcePairs              оригинальный список пар источников-приемников
     * @param errorDepartmentFormTypes список назначений, которые должны быть исключены
     * @param mode                     режим работы
     * @param isDeclaration            признак того, что идет обработка в режиме "Декларации"
     * @param emptyIsOk                признак того, что если в результате выполнения входной список оказывается пуст - это нормальная ситуация.
     *                                 Например в случае пересечения версий, дополнительная обработка не требуется - версии склеиваются на стадии проверки пересечений
     * @param level                    уровень логгирования
     * @param messageBuilder           билдер для построения информационных сообщений
     * @return обрезанный входной список пар источников-приемников
     */
    public List<SourcePair> truncateSources(Logger logger, List<SourcePair> sourcePairs,
                                            List<Long> errorDepartmentFormTypes,
                                            SourceMode mode, boolean isDeclaration, boolean emptyIsOk, LOG_LEVEL level,
                                            MessageBuilder messageBuilder) {
        List<SourcePair> sourcePairsOut = new LinkedList<SourcePair>(sourcePairs);
        for (Long error : errorDepartmentFormTypes) {
            for (Iterator<SourcePair> it = sourcePairsOut.iterator(); it.hasNext(); ) {
                SourcePair pair = it.next();
                /** Исключаем связку из обработки */
                if (pair.getSource().equals(error) || pair.getDestination().equals(error)) {
                    if (isDeclaration) {
                        if (pair.getDestination().equals(error)) {
                            printMsgs(logger, messageBuilder.getDestinationMessage(pair), level);
                        }
                    } else {
                        if (pair.getSource().equals(error)) {
                            printMsgs(logger, messageBuilder.getSourceMessage(pair), level);
                        } else {
                            printMsgs(logger, messageBuilder.getDestinationMessage(pair), level);
                        }
                    }
                    it.remove();
                }
                /** Если единственное назначение было удалено, то продолжать нет смысла */
                if (!emptyIsOk && pair.getSource().equals(error) && mode == SourceMode.DESTINATIONS) {
                    throw new ServiceLoggerException(FATAL_SAVE_MSG,
                            logEntryService.save(logger.getEntries()));
                }
                if (!emptyIsOk && pair.getDestination().equals(error) && mode == SourceMode.SOURCES) {
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
        return truncateSources(logger, sourcePairs, notExisting, mode, isDeclaration, false, LOG_LEVEL.ERROR,
                new MessageBuilder() {
                    @Override
                    public List<String> getSourceMessage(SourcePair sourcePair) {
                        return Arrays.asList(String.format(CHECK_EXISTENCE_MSG,
                                sourcePair.getSourceKind() + ": " + sourcePair.getSourceType(),
                                sourceDepartmentName));
                    }

                    @Override
                    public List<String> getDestinationMessage(SourcePair sourcePair) {
                        return Arrays.asList(String.format(CHECK_EXISTENCE_MSG,
                                isDeclaration ? sourcePair.getDestinationType() :
                                        sourcePair.getDestinationKind() + ": " + sourcePair.getDestinationType(),
                                destinationDepartmentName));
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
     * @return обрезанный входной список связок источников-приемников, для которых не найдены экземпляры нф
     */
    public void checkFormInstances(Logger logger, List<SourcePair> sourcePairs, Date newPeriodStart, Date newPeriodEnd,
                                    String sourceDepartmentName,
                                    String destinationDepartmentName, boolean declaration) {
        for (SourcePair sourcePair : sourcePairs) {
            /** Получаем промежуточные периоды, которые будут объединены при создании новой версии */
            List<SourceObject> emptyPeriods = sourceDao.getEmptyPeriods(sourcePair,
                    newPeriodStart, newPeriodEnd);
            List<String> acceptedSources = new ArrayList<String>();
            for (SourceObject empty : emptyPeriods) {
                /** Получаем источники, имеющие принятые экземпляры в промежуточных периодах */
                acceptedSources.addAll(sourceDao.findAcceptedInstances(empty.getSourcePair().getSource(),
                        empty.getPeriodStart(), empty.getPeriodEnd()));
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
            return truncateSources(logger, sourcePairs, unionSourcePairs(loopedSources), mode, isDeclaration, false, LOG_LEVEL.ERROR,
                    new MessageBuilder() {
                        @Override
                        public List<String> getSourceMessage(SourcePair sourcePair) {
                            SourcePair errorPair = loopsMap.get(sourcePair);
                            return Arrays.asList(String.format(CIRCLE_MSG,
                                    objectNames.get(errorPair.getSource()),
                                    objectNames.get(errorPair.getDestination())
                            ));
                        }

                        @Override
                        public List<String> getDestinationMessage(SourcePair sourcePair) {
                            return getSourceMessage(sourcePair);
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
            final Map<SourcePair, SourceObject> unionMap = new HashMap<SourcePair, SourceObject>();
            final List<String> intersectionParts = new ArrayList<String>();
            int n = 0;
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
                    if (mode == SourceMode.SOURCES) {
                        intersectionParts.add(String.format(INTERSECTION_PART,
                                        isDeclaration ? intersection.getSourcePair().getDestinationType() : intersection.getSourcePair().getDestinationKind() + ": " + intersection.getSourcePair().getDestinationType(),
                                        "приемника",
                                        intersection.getSourcePair().getSourceKind() + ": " + intersection.getSourcePair().getSourceType(),
                                        formatter.get().format(intersection.getPeriodStart()) + " - " +
                                                (intersection.getPeriodEnd() != null ? formatter.get().format(intersection.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                                )
                        );
                    } else {
                        intersectionParts.add(String.format(INTERSECTION_PART,
                                        intersection.getSourcePair().getSourceKind() + ": " +  intersection.getSourcePair().getSourceType(),
                                        "источника",
                                        isDeclaration ? intersection.getSourcePair().getDestinationType() : intersection.getSourcePair().getDestinationKind() + ": " +  intersection.getSourcePair().getDestinationType(),
                                        formatter.get().format(intersection.getPeriodStart()) + " - " +
                                                (intersection.getPeriodEnd() != null ? formatter.get().format(intersection.getPeriodEnd()) : EMPTY_END_PERIOD_INFO)
                                )
                        );
                    }
                    n++;
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

            List<Long> intersectingSources = unionSourcePairs(new ArrayList<SourcePair>(intersections.keySet()));
            if (excludedPeriodStart == null) {
                //Идет создание назначений
                /** Создаем новые назначения с объединенными периодами */
                sourceDao.createAll(new ArrayList<SourceObject>(unionSources.keySet()), isDeclaration);
            } else {
                //Идет редактирование назначений
                intersectingSources = unionSourceObjects(deleteSources);
                for (Map.Entry<SourceObject, Pair<Date, Date>> sourceEntry : unionSources.entrySet()) {
                    sourceDao.updateAll(Arrays.asList(sourceEntry.getKey()),
                            sourceEntry.getValue().getFirst(), sourceEntry.getValue().getSecond(), isDeclaration);
                }
            }

            /** Убираем назначения с пересечениями из обработки */
            return truncateSources(logger, sourcePairs, intersectingSources, mode, isDeclaration, true, LOG_LEVEL.INFO,
                    new MessageBuilder() {
                        @Override
                        public List<String> getSourceMessage(SourcePair sourcePair) {
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
                            msgs.addAll(intersectionParts);
                            msgs.add(String.format(INTERSECTION_MSG_END, period));
                            return msgs;
                        }

                        @Override
                        public List<String> getDestinationMessage(SourcePair sourcePair) {
                            return getSourceMessage(sourcePair);
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
            for (SourceObject sourceObject: sourceObjects) {
                List<String> periodsInfo = sourceDao.findAcceptedInstances(sourceObject.getSourcePair().getSource(),
                        sourceObject.getPeriodStart(), sourceObject.getPeriodEnd());
                if (!periodsInfo.isEmpty()) {
                    acceptedSources.put(sourceObject.getSourcePair(), periodsInfo);
                }
            }

            if (!acceptedSources.isEmpty()) {
                /** Если существуют принятые источники в промежуточных периодах */
                logger.error(DELETE_FATAL_ERROR_BEGIN);
                for (Map.Entry<SourcePair, List<String>> acceptedSource : acceptedSources.entrySet()) {
                    logger.error(String.format(DELETE_FATAL_ERROR_MID,
                            acceptedSource.getKey().getSourceKind() + ": " + acceptedSource.getKey().getSourceType(),
                            sourceDepartmentNames.get(acceptedSource.getKey().getSource()),
                            StringUtils.join(acceptedSource.getValue(), ", ")));
                }
                logger.error(DELETE_FATAL_ERROR_END);
                throw new ServiceLoggerException(FATAL_SAVE_MSG,
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
    public void updateSources(Logger logger, SourceClientData sourceClientData) {
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
                    acceptedSources.addAll(sourceDao.findAcceptedInstances(sourcePair.getSource(),
                            oldPeriodStart, periodStart));
                }
                if ((periodEnd != null && oldPeriodEnd == null) || (periodEnd != null && oldPeriodEnd != null && periodEnd.before(oldPeriodEnd))) {
                    acceptedSources.addAll(sourceDao.findAcceptedInstances(sourcePair.getSource(),
                            periodEnd, oldPeriodEnd));
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
        QueryParams queryParams = getSearchOrderingDefaultFilter();
        return getDFTByDepartment(departmentId, taxType, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<DepartmentFormType> getDFTByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd, QueryParams queryParams) {
        return departmentFormTypeDao.getByTaxType(departmentId, taxType, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<Long> getDFTByPerformerDep(int performerDepId, TaxType taxType, List<FormDataKind> kinds) {
        return departmentFormTypeDao.getByPerformerId(performerDepId, taxType, kinds);
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
        QueryParams queryParams = getSearchOrderingDefaultFilter();
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
    public Map<String, List> getSourcesDestinations(int departmentId, int terrBankId, List<TaxType> taxTypes) {
        HashMap<String, List> map = new HashMap<String, List>();
        List<Pair<DepartmentFormType, DepartmentFormType>> destinationFT = departmentFormTypeDao.getFormDestinationsWithDepId(departmentId, terrBankId,taxTypes);
        map.put("destinationFTs", destinationFT);
        List<Pair<DepartmentFormType, DepartmentFormType>> sourceFTs = departmentFormTypeDao.getFormSourcesWithDepId(departmentId, terrBankId,taxTypes);
        map.put("sourceFTs", sourceFTs);
        List<Pair<DepartmentFormType, DepartmentDeclarationType>> destinationDTs = departmentFormTypeDao.getDeclarationDestinationsWithDepId(departmentId, terrBankId,taxTypes);
        map.put("destinationDTs", destinationDTs);
        List<Pair<DepartmentFormType, DepartmentDeclarationType>> sourceDTs = departmentFormTypeDao.getDeclarationSourcesWithDepId(departmentId, terrBankId,taxTypes);
        map.put("sourceDTs", sourceDTs);
        return map;
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
    public List<FormToFormRelation> getRelations(int departmentId, int formTypeId, FormDataKind kind, int reportPeriodId, Integer periodOrder, boolean includeDestinations, boolean includeSources, boolean includeUncreated) {
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        List<FormToFormRelation> formToFormRelations = new ArrayList<FormToFormRelation>();
        // включения источников
        if (includeSources){
            List<DepartmentFormType> sourcesForm = getDFTSourcesByDFT(departmentId, formTypeId, kind, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
            formToFormRelations.addAll(createFormToFormRelationModel(sourcesForm, reportPeriodId, periodOrder, true, includeUncreated));
        }

        // включения приемников
        if (includeDestinations){
            List<DepartmentFormType> destinationsForm = getFormDestinations(departmentId, formTypeId, kind, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
            formToFormRelations.addAll(createFormToFormRelationModel(destinationsForm, reportPeriodId, periodOrder, false, includeUncreated));
        }

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
     * @param departmentFormTypes
     * @param reportPeriodId
     * @param periodOrder
     * @param isSource - true источник иначе приемник
     * @param includeUncreatedForms флаг включения не созданных нф в список
     * @return
     */
    private List<FormToFormRelation> createFormToFormRelationModel(List<DepartmentFormType> departmentFormTypes,
                                                                   int reportPeriodId, Integer periodOrder,
                                                                   boolean isSource, boolean includeUncreatedForms){
        List<FormToFormRelation> formToFormRelations = new ArrayList<FormToFormRelation>(departmentFormTypes.size());
        for (DepartmentFormType departmentFormType : departmentFormTypes) {
            FormToFormRelation formToFormRelation = new FormToFormRelation();
            /** источник/приемник */
            formToFormRelation.setSource(isSource);
            /** исполнитель */
            formToFormRelation.setPerformer(departmentDao.getDepartment(departmentFormType.getPerformerId()));
            /** Полное название подразделения */
            int departmentId = departmentFormType.getDepartmentId();
            formToFormRelation.setFullDepartmentName(departmentService.getParentsHierarchy(departmentId));
            ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
            int formTypeId = departmentFormType.getFormTypeId();
            FormDataKind kind = departmentFormType.getKind();
            FormData formData;

            if (isSource) {
                formData = formDataDao.getLast(formTypeId, kind, departmentId, reportPeriod.getId(), periodOrder);
            } else {
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.getLast(departmentId,
                        reportPeriod.getId());
                formData = formDataDao.find(formTypeId, kind, departmentReportPeriod.getId().intValue(), periodOrder);
            }

            if (formData != null){
                /** Форма существует */
                formToFormRelation.setCreated(true);
                /** Установить статус */
                formToFormRelation.setState(formData.getState());
                /** вид формы */
                formToFormRelation.setFormType(formData.getFormType());
                /** тип нф */
                formToFormRelation.setFormDataKind(kind);
                /** установить id */
                formToFormRelation.setFormDataId(formData.getId());

                formToFormRelations.add(formToFormRelation);

            /**
             * 0.3.9: Назначение источников-приёмников пересекается с отчетным периодом текущего экземпляра
             * Уточнения Насти: Период текущей формы пересекается с периодом действия макета,
             * для которой нет созданной нф
             */
            } else if (includeUncreatedForms && formTemplateDao.existFormTemplate(formTypeId, reportPeriodId)){
                /** Формы не существует */
                formToFormRelation.setCreated(false);
                /** вид формы */
                formToFormRelation.setFormType(formTypeDao.get(formTypeId));
                /** тип нф */
                formToFormRelation.setFormDataKind(kind);

                formToFormRelations.add(formToFormRelation);
            }
        }

        return formToFormRelations;
    }

    /**
     * Фильтр по умолчанию
     *
     * @return
     */
    private QueryParams getSearchOrderingDefaultFilter() {
        QueryParams queryParams = new QueryParams();
        queryParams.setSearchOrdering(SourcesSearchOrdering.TYPE);
        queryParams.setAscending(true);
        return queryParams;
    }
}
