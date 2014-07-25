package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.*;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.source.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;

import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class SourceServiceImpl implements SourceService {

    private static final String FATAL_SAVE_MSG = "Назначение источников-приемников не сохранено. Обнаружены фатальные ошибки";
    private static final String CHECK_EXISTENCE_MSG = "Невозможно назначить источники / приемники: Форма \"%s\" не назначена подразделению %s";
    private static final String MAIN_SOURCE_NOT_EXIST_MSG = "Назначение, указанное в качестве %s не найдено. Операция не может быть продолжена";
    private static final String SOURCES_LIST_IS_EMPTY_MSG = "Все назначения были исключены в результате проверок. Продолжение операции невозможно.";
    private static final String FORM_INSTANCES_EXIST_MSG = "Найдены экземпляры, которые назначены %s формы \"%s\" и имеют статус \"Принят\":  \"%s\". Назначение не может быть выполнено";
    private static final String INTERSECTION_MSG = "\"%s\" назначен %s формы \"%s\" в периоде с %s по %s.";
    private static final String SAVE_SUCCESS_MSG = "\"%s\" назначен %s формы \"%s\" в периоде с %s по %s.";
    private static final String DELETE_SUCCESS_MSG = "Удалено назначение \"%s\" в роли %s %s %s в периоде с %s по %s.";
    private static final String UPDATE_SUCCESS_MSG = "\"%s\" назначен %s формы \"%s\" в периоде с %s по %s.";
    private static final String CIRCLE_MSG = "\"%s\" уже назначен как приёмник \"%s\"";
    private static final String SIMPLE_INSTANCES_MSG = "Для корректной передачи данных %s \"%s\" необходимо выполнить повторный перевод в статус \"Принята\" всех %s этой формы в периоде c %s по %s";
    private static final String EMPTY_LIST_MSG = "Список назначений пуст!";


    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    private static final Calendar CALENDAR = Calendar.getInstance();
    private static Date MAX_DATE;
    private static Date MIN_DATE;
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

    @Override
    public List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind, Date periodStart, Date periodEnd) {
        return departmentFormTypeDao.getFormSources(departmentId, formTypeId, kind, periodStart, periodEnd);
    }

    @Override
    public List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind, int reportPeriodId) {
        ReportPeriod period = reportPeriodDao.get(reportPeriodId);

        return getDFTSourcesByDFT(departmentId, formTypeId, kind, period.getStartDate(), period.getEndDate());
    }

    @Override
    public List<DepartmentFormType> getDFTSourceByDDT(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd) {
        return departmentFormTypeDao.getDeclarationSources(departmentId, declarationTypeId, periodStart, periodEnd);
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
        String getSourceMessage(SourcePair sourcePair);

        /**
         * Возвращает сообщение при обнаружении вхождения исключаемого назначения в списке приемников
         *
         * @param sourcePair обрабатываемая пара источник-приемник
         * @return сообщение
         */
        String getDestinationMessage(SourcePair sourcePair);
    }

    /**
     * Метод исключает указанные назначения из списка пар источников-приемников
     *
     * @param logger                   логгер
     * @param sourcePairs              оригинальный список пар источников-приемников
     * @param errorDepartmentFormTypes список назначений, которые должны быть исключены
     * @param mode                     режим работы
     * @param isDeclaration            признак того, что идет обработка в режиме "Декларации"
     * @param messageBuilder           билдер для построения информационных сообщений
     * @return обрезанный входной список пар источников-приемников
     */
    public List<SourcePair> truncateSources(Logger logger, List<SourcePair> sourcePairs,
                                            List<Long> errorDepartmentFormTypes,
                                            SourceMode mode, boolean isDeclaration,
                                            MessageBuilder messageBuilder) {
        List<SourcePair> sourcePairsOut = new LinkedList<SourcePair>(sourcePairs);
        for (Long error : errorDepartmentFormTypes) {
            for (Iterator<SourcePair> it = sourcePairsOut.iterator(); it.hasNext(); ) {
                SourcePair pair = it.next();
                /** Исключаем связку из обработки */
                if (pair.getSource().equals(error) || pair.getDestination().equals(error)) {
                    if (isDeclaration) {
                        if (pair.getDestination().equals(error)) {
                            logger.info(messageBuilder.getDestinationMessage(pair));
                        }
                    } else {
                        if (pair.getSource().equals(error)) {
                            logger.info(messageBuilder.getSourceMessage(pair));
                        } else {
                            logger.info(messageBuilder.getDestinationMessage(pair));
                        }
                    }
                    it.remove();
                }
                /** Если единственное назначение было удалено, то продолжать нет смысла */
                if (pair.getSource().equals(error) && mode == SourceMode.DESTINATIONS) {
                    throw new ServiceLoggerException(String.format(MAIN_SOURCE_NOT_EXIST_MSG, "источника"),
                            logEntryService.save(logger.getEntries()));
                }
                if (pair.getDestination().equals(error) && mode == SourceMode.SOURCES) {
                    throw new ServiceLoggerException(String.format(MAIN_SOURCE_NOT_EXIST_MSG, "приемника"),
                            logEntryService.save(logger.getEntries()));
                }
            }
        }
        /** Проверяем оставщиеся связки */
        if (sourcePairsOut.isEmpty()) {
            throw new ServiceLoggerException(SOURCES_LIST_IS_EMPTY_MSG,
                    logEntryService.save(logger.getEntries()));
        }
        return sourcePairsOut;
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
     * @param sourceDepartmentId      подразделение-источник. Необходимо только для формирования уведомлений
     * @param destinationDepartmentId подразделение-приемник. Необходимо только для формирования уведомлений
     * @return обрезанный входной список связок источников-приемников, которые все еще существуют
     */
    public List<SourcePair> checkExistence(Logger logger, List<SourcePair> sourcePairs,
                                           SourceMode mode,
                                           final boolean isDeclaration,
                                           final Integer sourceDepartmentId,
                                           final Integer destinationDepartmentId) {
        List<Long> dftIn = new ArrayList<Long>();
        if (isDeclaration) {
            if (mode == SourceMode.SOURCES) {
                //Проверяем единственный приемник
                if (sourceDao.checkDDTExistence(Arrays.asList(sourcePairs.get(0).getDestination())).isEmpty()) {
                    /** Если единственное назначение было удалено, то продолжать нет смысла */
                    throw new ServiceLoggerException(String.format(MAIN_SOURCE_NOT_EXIST_MSG, "приемника"),
                            logEntryService.save(logger.getEntries()));
                }
                for (SourcePair pair : sourcePairs) {
                    dftIn.add(pair.getSource());
                }
            } else {
                //Проверяем единственный источник
                if (sourceDao.checkDDTExistence(Arrays.asList(sourcePairs.get(0).getSource())).isEmpty()) {
                    throw new ServiceLoggerException(String.format(MAIN_SOURCE_NOT_EXIST_MSG, "источника"),
                            logEntryService.save(logger.getEntries()));
                }
                for (SourcePair pair : sourcePairs) {
                    dftIn.add(pair.getDestination());
                }
            }
        } else {
            dftIn = unionSourcePairs(sourcePairs);
        }

        @SuppressWarnings("unchecked")
        List<Long> notExistingDFT = (List<Long>) CollectionUtils.subtract(dftIn, sourceDao.checkDFTExistence(dftIn));
        return truncateSources(logger, sourcePairs, notExistingDFT, mode, isDeclaration,
                new MessageBuilder() {
                    @Override
                    public String getSourceMessage(SourcePair sourcePair) {
                        return String.format(CHECK_EXISTENCE_MSG,
                                sourcePair.getSourceKind().getName() + ": " + sourcePair.getSourceType().getName(),
                                departmentDao.getDepartment(sourceDepartmentId).getName());
                    }

                    @Override
                    public String getDestinationMessage(SourcePair sourcePair) {
                        return String.format(CHECK_EXISTENCE_MSG,
                                isDeclaration ? sourcePair.getDestinationDeclarationType().getName() :
                                        sourcePair.getDestinationKind().getName() + ": " + sourcePair.getDestinationFormType().getName(),
                                departmentDao.getDepartment(destinationDepartmentId).getName());
                    }
                });
    }

    /**
     * Проверяет существование экземпляров нф для указанных назначений.
     * http://conf.aplana.com/pages/viewpage.action?pageId=12321547
     * Если экземпляры существуют и у них есть приемники или источники в статусе "Принята", то назначения исключаются из обработки
     *
     * @param logger        логгер
     * @param periodStart   начало периода
     * @param periodEnd     окончание периода
     * @param sourcePairs   список связок источников-приемников
     * @param mode          режим работы
     * @param isDeclaration признак того, что идет обработка в режиме "Декларации"
     * @return обрезанный входной список связок источников-приемников, для которых не найдены экземпляры нф
     */
    public List<SourcePair> checkFormInstances(Logger logger, Date periodStart, Date periodEnd,
                                               List<SourcePair> sourcePairs,
                                               SourceMode mode,
                                               boolean isDeclaration) {
        List<SourcePair> sourcePairsOut = new LinkedList<SourcePair>(sourcePairs);

        /**
         * Получаем данные об экземплярах нф, созданных по этим назначениям
         * Если существуют такие экземпляры и у них есть принятые источники или приемники, то исключаем их из обработки
         */
        List<FormDataInfo> forms = sourceDao.findForms(periodStart, periodEnd, unionSourcePairs(sourcePairs));
        if (!forms.isEmpty()) {
            /** Формируем список назначений, которые должны быть исключены из обработки */
            final Map<Long, List<FormDataInfo>> formsMap = new HashMap<Long, List<FormDataInfo>>();
            for (FormDataInfo form : forms) {
                if (formsMap.containsKey(form.getDepartmentFormTypeId())) {
                    formsMap.get(form.getDepartmentFormTypeId()).add(form);
                } else {
                    formsMap.put(form.getDepartmentFormTypeId(), Arrays.asList(form));
                }
            }
            /** Убираем назначения из обработки */
            sourcePairsOut = truncateSources(logger, sourcePairs, new ArrayList<Long>(formsMap.keySet()), mode, isDeclaration,
                    new MessageBuilder() {
                        private String getMsg(Long departmentFormTypeId, String mode) {
                            FormDataInfo formDataInfo = formsMap.get(departmentFormTypeId).get(0);
                            StringBuilder formsInfo = new StringBuilder();
                            for (Iterator<FormDataInfo> it = formsMap.get(departmentFormTypeId).iterator(); it.hasNext(); ) {
                                FormDataInfo form = it.next();
                                formsInfo.append("\"")
                                        .append(form.getFormTypeName()).append("; ")
                                        .append(form.getFormKindName()).append("; ")
                                        .append(form.getPeriod()).append("; ")
                                        .append(form.getDepartment())
                                        .append("\"");
                                if (it.hasNext()) {
                                    formsInfo.append(", ");
                                }
                            }
                            return String.format(FORM_INSTANCES_EXIST_MSG,
                                    mode,
                                    formDataInfo.getDepartment() + "-" + formDataInfo.getFormTypeName() + "-" + formDataInfo.getFormKindName(),
                                    formsInfo.toString()
                            );
                        }

                        @Override
                        public String getSourceMessage(SourcePair sourcePair) {
                            return getMsg(sourcePair.getSource(), "источниками");
                        }

                        @Override
                        public String getDestinationMessage(SourcePair sourcePair) {
                            return getMsg(sourcePair.getDestination(), "приемниками");
                        }
                    });
        }

        /**
         * Получаем данные об экземплярах деклараций, созданных по этим назначениям
         * Если существуют такие экземпляры и у них есть принятые источники или приемники, то исключаем их из обработки
         */
        //Формируем список только из приемников, т.к декларация не может быть источником
        List<Long> destinationIds = new ArrayList<Long>();
        for (SourcePair sourcePair : sourcePairsOut) {
            destinationIds.add(sourcePair.getDestination());
        }
        List<DeclarationDataInfo> declarations = sourceDao.findDeclarations(periodStart, periodEnd, destinationIds);
        if (!declarations.isEmpty()) {
            /** Формируем список назначений, которые должны быть исключены из обработки */
            final Map<Long, List<DeclarationDataInfo>> declarationsMap = new HashMap<Long, List<DeclarationDataInfo>>();
            for (DeclarationDataInfo declaration : declarations) {
                if (declarationsMap.containsKey(declaration.getDepartmentDeclarationTypeId())) {
                    declarationsMap.get(declaration.getDepartmentDeclarationTypeId()).add(declaration);
                } else {
                    declarationsMap.put(declaration.getDepartmentDeclarationTypeId(), Arrays.asList(declaration));
                }
            }
            /** Убираем назначения из обработки */
            sourcePairsOut = truncateSources(logger, sourcePairs, new ArrayList<Long>(declarationsMap.keySet()), mode, isDeclaration,
                    new MessageBuilder() {

                        @Override
                        public String getSourceMessage(SourcePair sourcePair) {
                            throw new ServiceException("При поиске экземпляров деклараций, обнаружено совпадение в нф!");
                        }

                        @Override
                        public String getDestinationMessage(SourcePair sourcePair) {
                            DeclarationDataInfo declarationDataInfo = declarationsMap.get(sourcePair.getDestination()).get(0);
                            StringBuilder declarationInfo = new StringBuilder();
                            for (Iterator<DeclarationDataInfo> it = declarationsMap.get(sourcePair.getDestination()).iterator(); it.hasNext(); ) {
                                DeclarationDataInfo declaration = it.next();
                                declarationInfo.append("\"")
                                        .append(declaration.getDeclarationTypeName()).append("; ")
                                        .append(declaration.getPeriod()).append("; ")
                                        .append(declaration.getDepartment())
                                        .append("\"");
                                if (it.hasNext()) {
                                    declarationInfo.append(", ");
                                }
                            }
                            return String.format(FORM_INSTANCES_EXIST_MSG,
                                    "приемниками",
                                    declarationDataInfo.getDepartment() + "-" + declarationDataInfo.getDeclarationTypeName(),
                                    declarationInfo.toString()
                            );
                        }
                    });
        }

        return sourcePairsOut;
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
            return truncateSources(logger, sourcePairs, unionSourcePairs(loopedSources), mode, isDeclaration,
                    new MessageBuilder() {
                        @Override
                        public String getSourceMessage(SourcePair sourcePair) {
                            SourcePair errorPair = loopsMap.get(sourcePair);
                            return String.format(CIRCLE_MSG,
                                    objectNames.get(errorPair.getSource()),
                                    objectNames.get(errorPair.getDestination())
                            );
                        }

                        @Override
                        public String getDestinationMessage(SourcePair sourcePair) {
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
     * @param periodStart         начало периода
     * @param periodEnd           окончание периода
     * @param excludedPeriodStart начало нового периода, который будет исключен из проверки
     * @param excludedPeriodEnd   конец нового периода, который будет исключен из проверки
     * @param sourcePairs         список связок источников-приемников
     * @param mode                режим работы
     * @param isDeclaration       признак того, что идет обработка в режиме "Декларации"
     * @return обрезанный входной список связок источников-приемников, для которых не найдены пересечения
     */
    public List<SourcePair> checkIntersections(Logger logger, Date periodStart, Date periodEnd,
                                               Date excludedPeriodStart, Date excludedPeriodEnd,
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
            List<SourceObject> unionSources = new ArrayList<SourceObject>();
            List<SourceObject> deleteSources = new ArrayList<SourceObject>();
            final Map<SourcePair, SourceObject> unionMap = new HashMap<SourcePair, SourceObject>();
            for (Map.Entry<SourcePair, List<SourceObject>> intersectionGroup : intersections.entrySet()) {
                Date minDate = intersectionGroup.getValue().get(0).getPeriodStart();
                Date maxDate = intersectionGroup.getValue().get(0).getPeriodEnd();

                for (SourceObject intersection : intersectionGroup.getValue()) {
                    if (intersection.getPeriodStart().before(minDate)) {
                        minDate = intersection.getPeriodStart();
                    }
                    if ((intersection.getPeriodEnd() == null && maxDate != null) || intersection.getPeriodEnd().after(maxDate)) {
                        maxDate = intersection.getPeriodEnd();
                    }
                    deleteSources.add(intersection);
                }
                SourceObject union = new SourceObject(intersectionGroup.getKey(), minDate, maxDate);
                unionSources.add(union);
                unionMap.put(union.getSourcePair(), union);
            }

            /** Удаляем все назначения, с периодами которых были найдены пересечения. */
            sourceDao.deleteAll(deleteSources, isDeclaration);

            /** Создаем новые назначения с объединенными периодами */
            sourceDao.createAll(unionSources, isDeclaration);

            /** Убираем назначения с пересечениями из обработки */
            List<Long> intersectingSources = unionSourcePairs(new ArrayList<SourcePair>(intersections.keySet()));
            return truncateSources(logger, sourcePairs, intersectingSources, mode, isDeclaration,
                    new MessageBuilder() {
                        @Override
                        public String getSourceMessage(SourcePair sourcePair) {
                            SourceObject union = unionMap.get(sourcePair);
                            return String.format(INTERSECTION_MSG,
                                    sourcePair.getSourceKind().getName() + ": " + sourcePair.getSourceType().getName(),
                                    "источником",
                                    sourcePair.getDestinationFormType() == null ? sourcePair.getDestinationDeclarationType().getName() :
                                            sourcePair.getDestinationKind().getName() + ": " + sourcePair.getDestinationFormType().getName(),
                                    formatter.get().format(union.getPeriodStart()),
                                    union.getPeriodEnd() != null ? formatter.get().format(union.getPeriodEnd()) : ""
                            );
                        }

                        @Override
                        public String getDestinationMessage(SourcePair sourcePair) {
                            SourceObject union = unionMap.get(sourcePair);
                            return String.format(INTERSECTION_MSG,
                                    sourcePair.getDestinationFormType() == null ? sourcePair.getDestinationDeclarationType().getName() :
                                            sourcePair.getDestinationKind().getName() + ": " + sourcePair.getDestinationFormType().getName(),
                                    "приемником",
                                    sourcePair.getSourceKind().getName() + ": " + sourcePair.getSourceType().getName(),
                                    formatter.get().format(union.getPeriodStart()),
                                    union.getPeriodEnd() != null ? formatter.get().format(union.getPeriodEnd()) : ""
                            );
                        }
                    });
        } else {
            return sourcePairs;
        }
    }

    /**
     * Замена сложной проверки существования экземпляров нф и деклараций для назначений
     *
     * @param logger          логгер
     * @param sourcePairs     список пар назначений
     * @param periodStartName начало периода в текстовом представлении
     * @param periodEndName   окончание периода в текстовом представлении
     * @param isDeclaration   признак того, что идет обработка в режиме "Декларации"
     * @param mode            режим
     */
    public void instancesNotification(Logger logger, List<SourcePair> sourcePairs, String periodStartName, String periodEndName, SourceMode mode, boolean isDeclaration) {
        if (sourcePairs != null && !sourcePairs.isEmpty()) {
            if (mode == SourceMode.DESTINATIONS) {
                SourcePair source = sourcePairs.get(0);
                logger.warn(SIMPLE_INSTANCES_MSG,
                        "из",
                        source.getSourceKind().getName() + ": " + source.getSourceType().getName(),
                        "экземпляров",
                        periodStartName,
                        periodEndName != null ? periodEndName : "-"
                );
                for (SourcePair pair : sourcePairs) {
                    logger.warn(SIMPLE_INSTANCES_MSG,
                            "в",
                            isDeclaration ? pair.getDestinationDeclarationType().getName() :
                                    pair.getDestinationKind().getName() + ": " + pair.getDestinationFormType().getName(),
                            "источников",
                            periodStartName,
                            periodEndName != null ? periodEndName : "-"
                    );
                }
            } else {
                SourcePair destination = sourcePairs.get(0);
                logger.warn(SIMPLE_INSTANCES_MSG,
                        "в",
                        isDeclaration ? destination.getDestinationDeclarationType().getName() :
                                destination.getDestinationKind().getName() + ": " + destination.getDestinationFormType().getName(),
                        "источников",
                        periodStartName,
                        periodEndName != null ? periodEndName : "-"
                );
                for (SourcePair pair : sourcePairs) {
                    logger.warn(SIMPLE_INSTANCES_MSG,
                            "в",
                            pair.getSourceKind().getName() + ": " + pair.getSourceType().getName(),
                            "источников",
                            periodStartName,
                            periodEndName != null ? periodEndName : "-"
                    );
                }
            }
        } else {
            throw new ServiceException("Список назначений пуст!");
        }
    }

    @Override
    public void createSources(Logger logger, SourceClientData sourceClientData) {
        if (sourceClientData.getSourcePairs() != null && !sourceClientData.getSourcePairs().isEmpty()) {
            /** Проверка существования назначения подразделению */
            List<SourcePair> sourcePairs = checkExistence(logger, sourceClientData.getSourcePairs(),
                    sourceClientData.getMode(), sourceClientData.isDeclaration(),
                    sourceClientData.getSourceDepartmentId(), sourceClientData.getDestinationDepartmentId());

            /** Проверка существования экземпляров нф */
            //sourcePairs = checkFormInstances(logger, sourceClientData.getPeriodStart(), sourceClientData.getPeriodEnd(),
            //        sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
            instancesNotification(logger, sourcePairs, sourceClientData.getPeriodStartName(), sourceClientData.getPeriodEndName(),
                    sourceClientData.getMode(), sourceClientData.isDeclaration());

            /** Проверка зацикливания */
            sourcePairs = checkLoops(logger, sourceClientData.getPeriodStart(), sourceClientData.getPeriodEnd(),
                    sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());

            /** Проверка пересечений */
            sourcePairs = checkIntersections(logger, sourceClientData.getPeriodStart(), sourceClientData.getPeriodEnd(),
                    null, null, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());

            List<SourceObject> sourceObjects = pairsToObjects(sourcePairs, sourceClientData.getPeriodStart(), sourceClientData.getPeriodEnd());

            /** Создаем оставшиеся назначения */
            sourceDao.createAll(sourceObjects, sourceClientData.isDeclaration());
            for (SourceObject sourceObject : sourceObjects) {
                if (sourceClientData.getMode() == SourceMode.DESTINATIONS) {
                    logger.info(SAVE_SUCCESS_MSG,
                            sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationDeclarationType().getName() :
                                    sourceObject.getSourcePair().getDestinationKind().getName() + ": " + sourceObject.getSourcePair().getDestinationFormType().getName(),
                            "приемником",
                            sourceObject.getSourcePair().getSourceKind().getName() + ": " + sourceObject.getSourcePair().getSourceType().getName(),
                            formatter.get().format(sourceObject.getPeriodStart()),
                            sourceObject.getPeriodEnd() != null ? formatter.get().format(sourceObject.getPeriodEnd()) : ""
                    );
                } else {
                    logger.info(SAVE_SUCCESS_MSG,
                            sourceObject.getSourcePair().getSourceKind().getName() + ": " + sourceObject.getSourcePair().getSourceType().getName(),
                            "источником",
                            sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationDeclarationType().getName() :
                                    sourceObject.getSourcePair().getDestinationKind().getName() + ": " + sourceObject.getSourcePair().getDestinationFormType().getName(),
                            formatter.get().format(sourceObject.getPeriodStart()),
                            sourceObject.getPeriodEnd() != null ? formatter.get().format(sourceObject.getPeriodEnd()) : ""
                    );
                }
            }
        } else {
            throw new ServiceException(EMPTY_LIST_MSG);
        }
    }

    @Override
    public void deleteSources(Logger logger, SourceClientData sourceClientData) {
        if (sourceClientData.getSourcePairs() != null && !sourceClientData.getSourcePairs().isEmpty()) {
            List<SourcePair> sourcePairs = sourceClientData.getSourcePairs();
            /** Проверка существования экземпляров нф */
            //List<SourcePair> sourcePairs = checkFormInstances(logger, sourceClientData.getPeriodStart(), sourceClientData.getPeriodEnd(),
            //        sourceClientData.getSourcePairs(), sourceClientData.getMode(), sourceClientData.isDeclaration());
            instancesNotification(logger, sourcePairs, sourceClientData.getPeriodStartName(), sourceClientData.getPeriodEndName(),
                    sourceClientData.getMode(), sourceClientData.isDeclaration());

            List<SourceObject> sourceObjects = sourceClientData.getSourceObjects();

            /** Удаляем все назначения, с периодами которых были найдены пересечения. */
            sourceDao.deleteAll(sourceObjects, sourceClientData.isDeclaration());
            if (sourceClientData.getMode() == SourceMode.DESTINATIONS) {
                for (SourceObject sourceObject : sourceObjects) {
                    logger.info(DELETE_SUCCESS_MSG,
                            sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationDeclarationType().getName() :
                                    sourceObject.getSourcePair().getDestinationKind().getName() + ": " + sourceObject.getSourcePair().getDestinationFormType().getName(),
                            "приемника",
                            sourceClientData.isDeclaration() ? "декларации" : "формы",
                            sourceObject.getSourcePair().getSourceKind().getName() + ": " + sourceObject.getSourcePair().getSourceType().getName(),
                            sourceClientData.getPeriodStartName(),
                            sourceClientData.getPeriodEndName() != null ? sourceClientData.getPeriodEndName() : "-"
                    );
                }
            } else {
                for (SourceObject sourceObject : sourceObjects) {
                    logger.info(DELETE_SUCCESS_MSG,
                            sourceObject.getSourcePair().getSourceKind().getName() + ": " + sourceObject.getSourcePair().getSourceType().getName(),
                            "источника",
                            sourceClientData.isDeclaration() ? "декларации" : "формы",
                            sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationDeclarationType().getName() :
                                    sourceObject.getSourcePair().getDestinationKind().getName() + ": " + sourceObject.getSourcePair().getDestinationFormType().getName(),
                            sourceClientData.getPeriodStartName(),
                            sourceClientData.getPeriodEndName() != null ? sourceClientData.getPeriodEndName() : "-"
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
        if (sourceClientData.getSourcePairs() != null && !sourceClientData.getSourcePairs().isEmpty()) {
            List<SourcePair> sourcePairs = sourceClientData.getSourcePairs();
            if (periodStart.before(oldPeriodStart)) {
                /** Дата начала нового периода меньше даты начала старого периода */
                if (periodEnd != null && oldPeriodEnd != null && periodEnd.before(oldPeriodEnd) && periodEnd.after(oldPeriodStart)) {
                    /** Дата окончания нового периода меньше даты окончания старого периода и больше даты начала старого периода */

                    /** Проверка существования экземпляров нф */
                    instancesNotification(logger, sourcePairs, sourceClientData.getPeriodStartName(), sourceClientData.getOldPeriodEndName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
                    /** Проверка зацикливания */
                    sourcePairs = checkLoops(logger, periodStart, SimpleDateUtils.addDayToDate(oldPeriodEnd, -1),
                            sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                    /** Проверка пересечений */
                    sourcePairs = checkIntersections(logger, periodStart, SimpleDateUtils.addDayToDate(oldPeriodEnd, -1),
                            oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                } else if (periodEnd != null && (periodEnd.before(oldPeriodStart) || periodEnd.equals(oldPeriodStart))) {
                    /** Дата окончания нового периода меньше либо равна дате начала старого периода */

                    /** Проверка существования экземпляров нф */
                    instancesNotification(logger, sourcePairs, sourceClientData.getOldPeriodStartName(), sourceClientData.getOldPeriodEndName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
                    instancesNotification(logger, sourcePairs, sourceClientData.getPeriodStartName(), sourceClientData.getPeriodEndName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
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
                    instancesNotification(logger, sourcePairs, sourceClientData.getPeriodStartName(), sourceClientData.getPeriodEndName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
                    /** Проверка зацикливания */
                    sourcePairs = checkLoops(logger, periodStart, SimpleDateUtils.addDayToDate(oldPeriodStart, -1),
                            sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                    sourcePairs = checkLoops(logger, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd,
                            sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                    /** Проверка пересечений */
                    sourcePairs = checkIntersections(logger, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd,
                            oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                }
            } else if (periodStart.equals(oldPeriodStart)) {
                /** Дата начала нового периода равна дате начала старого периода */
                if ((periodEnd != null && oldPeriodEnd == null) || (periodEnd != null && periodEnd.before(oldPeriodEnd))) {
                    /** Дата окончания нового периода меньше даты окончания старого периода */

                    /** Проверка существования экземпляров нф */
                    instancesNotification(logger, sourcePairs, sourceClientData.getPeriodEndName(), sourceClientData.getOldPeriodEndName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
                } else if ((periodEnd == null && oldPeriodEnd != null)
                        || (periodEnd != null && periodEnd.after(oldPeriodEnd))) {
                    /** Дата окончания нового периода больше даты окончания старого периода */

                    /** Проверка существования экземпляров нф */
                    instancesNotification(logger, sourcePairs, sourceClientData.getOldPeriodEndName(), sourceClientData.getPeriodEndName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
                    /** Проверка зацикливания */
                    sourcePairs = checkLoops(logger, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd,
                            sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                    /** Проверка пересечений */
                    sourcePairs = checkIntersections(logger, SimpleDateUtils.addDayToDate(oldPeriodEnd, 1), periodEnd,
                            oldPeriodStart, oldPeriodEnd, sourcePairs, sourceClientData.getMode(), sourceClientData.isDeclaration());
                }
            } else if (periodStart.after(oldPeriodStart)
                    && oldPeriodEnd != null && periodStart.before(oldPeriodEnd)) {
                /** Дата начала нового периода больше даты начала старого периода и меньше даты окончания старого периода */
                if (periodEnd.before(oldPeriodEnd)) {
                    /** Дата окончания нового периода меньше даты окончания старого периода */

                    /** Проверка существования экземпляров нф */
                    instancesNotification(logger, sourcePairs, sourceClientData.getOldPeriodStartName(), sourceClientData.getPeriodStartName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
                    instancesNotification(logger, sourcePairs, sourceClientData.getOldPeriodEndName(), sourceClientData.getPeriodEndName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
                } else if (periodEnd.equals(oldPeriodStart)) {
                    /** Дата окончания нового периода равна дате начала старого периода */

                    /** Проверка существования экземпляров нф */
                    instancesNotification(logger, sourcePairs, sourceClientData.getOldPeriodStartName(), sourceClientData.getPeriodStartName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
                } else if (periodEnd.after(oldPeriodEnd)) {
                    /** Дата окончания нового периода больше даты окончания старого периода */

                    /** Проверка существования экземпляров нф */
                    instancesNotification(logger, sourcePairs, sourceClientData.getOldPeriodStartName(), sourceClientData.getPeriodStartName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
                    instancesNotification(logger, sourcePairs, sourceClientData.getOldPeriodEndName(), sourceClientData.getPeriodEndName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
                }
            } else if (oldPeriodEnd != null && periodStart.after(oldPeriodEnd)) {
                /** Дата начала нового периода больше даты окончания старого периода */
                if (periodEnd == null || periodEnd.after(oldPeriodEnd)) {
                    /** Дата окончания нового периода больше даты окончания старого периода */

                    /** Проверка существования экземпляров нф */
                    instancesNotification(logger, sourcePairs, sourceClientData.getOldPeriodStartName(), sourceClientData.getOldPeriodEndName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
                    instancesNotification(logger, sourcePairs, sourceClientData.getPeriodStartName(), sourceClientData.getPeriodEndName(),
                            sourceClientData.getMode(), sourceClientData.isDeclaration());
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
                                sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationDeclarationType().getName() :
                                        sourceObject.getSourcePair().getDestinationKind().getName() + ": " + sourceObject.getSourcePair().getDestinationFormType().getName(),
                                "приемником",
                                sourceObject.getSourcePair().getSourceKind().getName() + ": " + sourceObject.getSourcePair().getSourceType().getName(),
                                sourceClientData.getPeriodStartName(),
                                sourceClientData.getPeriodEndName() != null ? sourceClientData.getPeriodEndName() : "-"
                        );
                    }
                } else {
                    for (SourceObject sourceObject : sourceObjects) {
                        logger.info(UPDATE_SUCCESS_MSG,
                                sourceObject.getSourcePair().getSourceKind().getName() + ": " + sourceObject.getSourcePair().getSourceType().getName(),
                                "источником",
                                sourceClientData.isDeclaration() ? sourceObject.getSourcePair().getDestinationDeclarationType().getName() :
                                        sourceObject.getSourcePair().getDestinationKind().getName() + ": " + sourceObject.getSourcePair().getDestinationFormType().getName(),
                                sourceClientData.getPeriodStartName(),
                                sourceClientData.getPeriodEndName() != null ? sourceClientData.getPeriodEndName() : "-"
                        );
                    }
                }
            }
        } else {
            throw new ServiceException(EMPTY_LIST_MSG);
        }
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
        return departmentFormTypeDao.getByTaxType(departmentId, taxType, periodStart, periodEnd);
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

    public List<FormTypeKind> getFormAssigned(Long departmentId, char taxType) {
        return departmentFormTypeDao.getFormAssigned(departmentId, taxType);
    }

    @Override
    public List<FormTypeKind> getDeclarationAssigned(Long departmentId, char taxType) {
        return departmentFormTypeDao.getDeclarationAssigned(departmentId, taxType);
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
    public List<DepartmentDeclarationType> getDDTByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd) {
        return departmentDeclarationTypeDao.getByTaxType(departmentId, taxType, periodStart, periodEnd);
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
    public List<DepartmentFormType> getDFTByFormType(@NotNull Integer formTypeId) {
        return departmentFormTypeDao.getDFTByFormType(formTypeId);
    }

    @Override
    public List<DepartmentDeclarationType> getDDTByDeclarationType(@NotNull Integer declarationTypeId) {
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
    private List<FormToFormRelation> createFormToFormRelationModel(List<DepartmentFormType> departmentFormTypes, int reportPeriodId, Integer periodOrder, boolean isSource, boolean includeUncreatedForms){
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
            FormData formData = (periodOrder == null) ?
                    formDataDao.find(formTypeId, kind, departmentId, reportPeriod.getId()) :
                    formDataDao.findMonth(formTypeId, kind, departmentId, reportPeriod.getTaxPeriod().getId(), periodOrder);
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
}
