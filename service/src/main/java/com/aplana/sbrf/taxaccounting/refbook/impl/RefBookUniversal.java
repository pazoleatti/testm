package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Универсальный провайдер данных
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 11.07.13 11:32
 */
@Service("refBookUniversal")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookUniversal implements RefBookDataProvider {

	@Autowired
	private RefBookDao refBookDao;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private BDUtils dbUtils;
    @Autowired
    private LockDataService lockService;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private RefBookHelper refBookHelper;
    @Autowired
    private RefBookFactory rbFactory;

	protected Long refBookId;

    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    private static final String CROSS_ERROR_MSG = "Обнаружено пересечение указанного срока актуальности с существующей версией!";
    private static final String UNIQ_ERROR_MSG = "Нарушено требование к уникальности, уже существуют записи %s в указанном периоде!";

	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        return refBookDao.getVersions(refBookId, startDate, endDate);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsById(Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookDao.getRecordVersionsById(refBookId, uniqueRecordId, pagingParams, filter, sortAttribute);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookDao.getRecordVersionsByRecordId(refBookId, recordId, pagingParams, filter, sortAttribute);
    }

    @Override
	public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version,
			PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		return refBookDao.getChildrenRecords(refBookId, parentRecordId, version, pagingParams, filter, sortAttribute);
	}

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        return refBookDao.getParentsHierarchy(uniqueRecordId);
    }

    @Override
    public Long getRowNum(Date version, Long recordId,
                   String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRowNum(refBookId, version, recordId, filter, sortAttribute, isSortAscending);
    }

    @Override
	public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams,
			String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
		return refBookDao.getRecords(refBookId, version, pagingParams, filter, sortAttribute, isSortAscending);
	}

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public List<Pair<Long, Long>> getRecordIdPairs(Long refBookId, Date version, Boolean needAccurateVersion, String filter) {
        return refBookDao.getRecordIdPairs(refBookId, version, needAccurateVersion, filter);
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        //TODO: возможно нужно точное совпадение версии
        List<Pair<Long, Long>> pairs = refBookDao.getRecordIdPairs(refBookId, version, false, filter);
        List<Long> uniqueRecordIds = new ArrayList<Long>(pairs.size());
        for (Pair<Long, Long> pair : pairs){
            uniqueRecordIds.add(pair.getFirst());
        }
        return uniqueRecordIds;
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return refBookDao.getRecordsCount(refBookId, version, filter);
    }

    @Override
    public List<Pair<Long, Long>> checkRecordExistence(Date version, String filter) {
        return refBookDao.getRecordIdPairs(refBookId, version, true, filter);
    }

    @Override
    public List<Long> isRecordsExist(List<Long> uniqueRecordIds) {
        return refBookDao.isRecordsExist(RefBook.REF_BOOK_RECORD_TABLE_NAME, new HashSet<Long>(uniqueRecordIds));
    }

    @Override
    @Transactional(noRollbackFor = DaoException.class)
	public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookDao.getRecordData(refBookId, recordId);
	}

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        return refBookDao.getRecordData(refBookId, recordIds);
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        return refBookDao.getValue(recordId, attributeId);
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        return refBookDao.getRecordVersionInfo(uniqueRecordId);
    }

    @Override
    public Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds) {
        return refBookDao.getRecordsVersionStart(uniqueRecordIds);
    }

    @Override
    public int getRecordVersionsCount(Long uniqueRecordId) {
        return refBookDao.getRecordVersionsCount(refBookId, uniqueRecordId);
    }

    @Override
    public List<Long> createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        if (logger == null) {
            throw new ServiceException("Logger не может быть null!");
        }
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        //Устанавливаем блокировку на тевущий справочник
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = LockData.LockObjects.REF_BOOK.name() + "_" + refBookId;
        RefBook refBook = refBookDao.get(refBookId);
        LockData lockData = lockService.lock(lockKey, userId,
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName()));
        if (lockData == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = LockData.LockObjects.REF_BOOK.name() + "_" + attribute.getRefBookId();
                        if (!lockedObjects.contains(referenceLockKey)) {
                            LockData referenceLockData = lockService.lock(referenceLockKey, userId,
                                    String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName()));
                            if (referenceLockData == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                        logEntryService.save(logger.getEntries()));
                            }
                        }
                    }
                }
                //Выполняем создание записей
                try {
                    return createRecordVersionWithoutLock(logger, versionFrom, versionTo, records);
                } catch (Exception e) {
                    throw new ServiceLoggerException(e.getLocalizedMessage(),
                            logEntryService.save(logger.getEntries()));
                }
            } finally {
                for (String lock : lockedObjects) {
                    lockService.unlock(lock, userId);
                }
            }
        } else {
            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, refBook.getName()),
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public List<Long> createRecordVersionWithoutLock(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        try {
            RefBook refBook = refBookDao.get(refBookId);
            List<RefBookAttribute> attributes = refBook.getAttributes();
            List<Long> excludedVersionEndRecords = new ArrayList<Long>();
            //Признак того, что для проверок дата окончания была изменена (была использована дата начала следующей версии)
            boolean dateToChangedForChecks = false;

            if (!refBook.isVersioned()) {
                //Устанавливаем минимальную дату
                versionFrom = new Date(0L);
            }

            long countIds = 0;
            for (RefBookRecord record : records) {
                if (record.getRecordId() == null) {
                    countIds++;
                    record.setVersionTo(versionTo);
                } else {
                    //Получение фактической даты окончания, которая может быть задана датой начала следующей версии
                    RefBookRecordVersion nextVersion = refBookDao.getNextVersion(refBookId, record.getRecordId(), versionFrom);
                    if (nextVersion != null) {
                        Date versionEnd = SimpleDateUtils.addDayToDate(nextVersion.getVersionStart(), -1);
                        if (versionEnd != null && versionFrom.after(versionEnd)) {
                            throw new ServiceException("Дата окончания получена некорректно");
                        }
                        record.setVersionTo(versionEnd);
                        dateToChangedForChecks = true;
                    } else {
                        record.setVersionTo(versionTo);
                    }
                }
            }

            //Проверка корректности
            checkCorrectness(logger, refBook, null, versionFrom, attributes, records);

            if (!refBookId.equals(RefBook.DEPARTMENT_CONFIG_TRANSPORT) &&
                    !refBookId.equals(RefBook.DEPARTMENT_CONFIG_INCOME) &&
                    !refBookId.equals(RefBook.DEPARTMENT_CONFIG_DEAL) &&
                    !refBookId.equals(RefBook.DEPARTMENT_CONFIG_VAT) &&
                    !refBookId.equals(RefBook.DEPARTMENT_CONFIG_PROPERTY) &&
                    !refBookId.equals(RefBook.WithTable.PROPERTY.getTableRefBookId()) &&
                    !refBookId.equals(RefBook.WithTable.TRANSPORT.getTableRefBookId()) &&
                    !refBookId.equals(RefBook.WithTable.INCOME.getTableRefBookId())
                    ) {

                if (refBook.isVersioned()) {
                    for (RefBookRecord record : records) {
                        //Проверка пересечения версий
                        if (record.getRecordId() != null) {
                            boolean needToCreateFakeVersion = crossVersionsProcessing(refBookDao.checkCrossVersions(refBookId, record.getRecordId(), versionFrom, record.getVersionTo(), null),
                                    refBook, versionFrom, record.getVersionTo(), logger);
                            if (!needToCreateFakeVersion) {
                                //Добавляем запись в список тех, для которых не будут созданы фиктивные версии
                                excludedVersionEndRecords.add(record.getRecordId());
                            }
                        }
                    }
                }
            }

            //Создание настоящей и фиктивной версии
            for (RefBookRecord record : records) {
                if (dateToChangedForChecks) {
                    //Возвращаем обратно пустую дату начала, т.к была установлена дата начала следующей версии для проверок
                    record.setVersionTo(null);
                    versionTo = null;
                }
            }
            return createVersions(refBook, versionFrom, versionTo, records, countIds, excludedVersionEndRecords, logger);
        } catch (DataAccessException e) {
            throw new ServiceException("Запись не сохранена. Обнаружены фатальные ошибки!", e);
        }
    }

    /**
     * Проверка корректности
     */
    // TODO использую в скрипте
    void checkCorrectness(Logger logger, RefBook refBook, Long uniqueRecordId, Date versionFrom, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        //Проверка обязательности заполнения записей справочника
        List<String> errors = RefBookUtils.checkFillRequiredRefBookAtributes(attributes, records);
        if (!errors.isEmpty()){
            throw new ServiceException("Поля " + errors.toString() + " являются обязательными для заполнения");
        }

        //Проверка корректности значений атрибутов
        errors = RefBookUtils.checkRefBookAtributeValues(attributes, records);
        if (!errors.isEmpty()){
            for (String error : errors) {
                logger.error(error);
            }
            throw new ServiceException("Обнаружено некорректное значение атрибута");
        }

        //Признак настроек подразделений
        boolean isConfig = refBookId.equals(RefBook.DEPARTMENT_CONFIG_TRANSPORT) ||
                refBookId.equals(RefBook.DEPARTMENT_CONFIG_INCOME) ||
                refBookId.equals(RefBook.DEPARTMENT_CONFIG_DEAL) ||
                refBookId.equals(RefBook.DEPARTMENT_CONFIG_VAT) ||
                refBookId.equals(RefBook.DEPARTMENT_CONFIG_PROPERTY) ||
                refBookId.equals(RefBook.WithTable.PROPERTY.getTableRefBookId()) ||
                refBookId.equals(RefBook.WithTable.TRANSPORT.getTableRefBookId()) ||
                refBookId.equals(RefBook.WithTable.INCOME.getTableRefBookId());

        if (!isConfig) {

            //Проверка отсутствия конфликта с датой актуальности родительского элемента
            if (refBook.isHierarchic() && refBook.isVersioned()) {
                checkParentConflict(logger, versionFrom, records);
            }

            for (RefBookRecord record : records) {
                //Получаем записи у которых совпали значения уникальных атрибутов
                List<Pair<Long,String>> matchedRecords = refBookDao.getMatchedRecordsByUniqueAttributes(refBookId, uniqueRecordId, attributes, Arrays.asList(record));
                if (matchedRecords != null && !matchedRecords.isEmpty()) {
                    //Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
                    List<Long> conflictedIds = refBookDao.checkConflictValuesVersions(matchedRecords, versionFrom, record.getVersionTo());

                    if (!conflictedIds.isEmpty()) {
                        throw new ServiceException(String.format(UNIQ_ERROR_MSG, makeAttrNames(matchedRecords, conflictedIds)));
                    }
                }
            }
        }

        if (refBook.isVersioned()) {
            //Проверка ссылочных значений
            checkReferences(refBook, attributes, records, versionFrom, logger);
        }
    }

    private void checkReferences(RefBook refBook, List<RefBookAttribute> attributes, List<RefBookRecord> records, Date versionFrom, Logger logger){
        if (!attributes.isEmpty()) {
            Map<String, RefBookDataProvider> providers = new HashMap<String, RefBookDataProvider>();
            Map<RefBookDataProvider, List<RefBookLinkModel>> references = new HashMap<RefBookDataProvider, List<RefBookLinkModel>>();
            List<String> uniqueAliases = new ArrayList<String>();

            for (RefBookAttribute attribute : attributes) {
                if (attribute.getUnique() != 0) {
                    uniqueAliases.add(attribute.getAlias());
                }
            }
            boolean isDepartmentConfigTable = false;
            Integer i = null;
            if (Arrays.asList(RefBook.WithTable.INCOME.getTableRefBookId(),
                    RefBook.WithTable.PROPERTY.getTableRefBookId(),
                    RefBook.WithTable.TRANSPORT.getTableRefBookId()).contains(refBook.getId())) {
                isDepartmentConfigTable = true;
                i = 1;
            }
            //Группируем ссылки по провайдерам
            for (RefBookRecord record : records) {
                Map<String, RefBookValue> values = record.getValues();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE) &&
                            values.get(attribute.getAlias()) != null && !values.get(attribute.getAlias()).isEmpty() &&
                            !attribute.getAlias().equals("DEPARTMENT_ID")) {       //Подразделения не версионируются и их нет смысла проверять
                        Long id = values.get(attribute.getAlias()).getReferenceValue();

                        RefBook attributeRefBook = rbFactory.getByAttribute(attribute.getRefBookAttributeId());
                        RefBookDataProvider provider;
                        if (!providers.containsKey(attributeRefBook.getTableName())) {
                            provider = rbFactory.getDataProvider(attributeRefBook.getId());
                            providers.put(attributeRefBook.getTableName(), provider);
                        } else {
                            provider = providers.get(attributeRefBook.getTableName());
                        }
                        if (!references.containsKey(provider)) {
                            references.put(provider, new ArrayList<RefBookLinkModel>());
                        }
                        StringBuilder specialId = null;
                        if (records.size() > 1) {
                            //Если обрабатывается несколько записей, то просто названия поля не хватит. Формируем имя записи из уникальных аттрибутов, а если их нет, то из строковых
                            specialId = new StringBuilder();
                            if (uniqueAliases.size() > 0) {
                                for (Iterator<String> it = uniqueAliases.iterator(); it.hasNext();) {
                                    String uniqueAlias = it.next();
                                    specialId.append(values.get(uniqueAlias));
                                    if (it.hasNext()) {
                                        specialId.append("/");
                                    }
                                }
                            } else {
                                for (Iterator<Map.Entry<String, RefBookValue>> it = values.entrySet().iterator(); it.hasNext();) {
                                    Map.Entry<String, RefBookValue> value = it.next();
                                    if (value.getValue().getAttributeType() == RefBookAttributeType.STRING) {
                                        specialId.append(values.get(value.getValue().getStringValue()));
                                        if (it.hasNext()) {
                                            specialId.append("/");
                                        }
                                    }
                                }
                            }
                        }
                        references.get(provider).add(new RefBookLinkModel(i, attribute.getAlias(), id, specialId != null ? specialId.toString() : null, versionFrom, record.getVersionTo()));
                    }
                }
                if (isDepartmentConfigTable) i++;
            }
            if (Arrays.asList(RefBook.WithTable.INCOME.getRefBookId(), RefBook.WithTable.INCOME.getTableRefBookId(),
                    RefBook.WithTable.PROPERTY.getRefBookId(), RefBook.WithTable.PROPERTY.getTableRefBookId(),
                    RefBook.WithTable.TRANSPORT.getRefBookId(), RefBook.WithTable.TRANSPORT.getTableRefBookId(),
                    RefBook.DEPARTMENT_CONFIG_DEAL, RefBook.DEPARTMENT_CONFIG_VAT).contains(refBook.getId())) {
                refBookHelper.checkReferenceValues(refBook, references, RefBookHelper.CHECK_REFERENCES_MODE.DEPARTMENT_CONFIG, logger);
            } else {
                refBookHelper.checkReferenceValues(refBook, references, RefBookHelper.CHECK_REFERENCES_MODE.REFBOOK, logger);
            }
        }
    }

    private String makeAttrNames(List<Pair<Long,String>> matchedRecords, List<Long> conflictedIds) {
        StringBuilder attrNames = new StringBuilder();
        Map<String, Integer> map = new HashMap<String, Integer>();
        if (conflictedIds != null) {
            //Если было ограничение по периоду то отираем нужные
            for (Long id : conflictedIds) {
                for (Pair<Long,String> pair : matchedRecords) {
                    if (pair.getFirst().equals(id)) {
                        Integer count = map.get(pair.getSecond());
                        if (count == null) count = 0;
                        count++;
                        map.put(pair.getSecond(), count);
                    }
                }
            }
        } else {
            //Иначе просто все совпадения
            for (Pair<Long,String> pair : matchedRecords) {
                Integer count = map.get(pair.getSecond());
                if (count == null) count = 0;
                count++;
                map.put(pair.getSecond(), count);
            }
        }

        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> pair = iterator.next();
            attrNames
                    .append("(")
                    .append(pair.getValue())
                    .append(" шт.) с такими значениями атрибута \"")
                    .append(pair.getKey())
                    .append("\"");
            if (iterator.hasNext()) {
                attrNames.append(", ");
            }
        }
        return attrNames.toString();
    }

    /**
     * Проверка отсутствия конфликта с датой актуальности родительского элемента
     */
    private void checkParentConflict(Logger logger, Date versionFrom, List<RefBookRecord> records) {
        List<Pair<Long, Integer>> checkResult = refBookDao.checkParentConflict(versionFrom, records);
        if (!checkResult.isEmpty()) {
            for (Pair<Long, Integer> conflict : checkResult) {
                //Дата окончания периода актуальности проверяемой версии больше даты окончания периода актуальности родительской записи для проверяемой версии
                if (conflict.getSecond() == 1) {
                    logger.error("Запись "+findNameByParent(records, conflict.getFirst())+
                            ": Дата окончания периода актуальности версии должна быть не больше даты окончания периода актуальности записи, которая является родительской в иерархии!");
                }
                //Дата начала периода актуальности проверяемой версии меньше даты начала периода актуальности родительской записи для проверяемой версии
                if (conflict.getSecond() == -1) {
                    logger.error("Запись "+findNameByParent(records, conflict.getFirst())+
                            ": Дата начала периода актуальности версии должна быть не меньше даты начала периода актуальности записи, которая является родительской в иерархии!");
                }
            }
            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException("Запись не сохранена. Обнаружены фатальные ошибки!", logEntryService.save(logger.getEntries()));
            }
        }
    }

    private String findNameByParent(List<RefBookRecord> records, Long parentId) {
        for (RefBookRecord record : records) {
            if (record.getValues().get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue().equals(parentId)) {
                return record.getValues().get("NAME").getStringValue();
            }
        }
        throw new ServiceException("Не найдена запись с заданным родительским элементом");
    }

    // TODO использую в скрипте
    List<Long> createVersions(RefBook refBook, Date versionFrom, Date versionTo, List<RefBookRecord> records, long countIds, List<Long> excludedVersionEndRecords, Logger logger) {
        //Генерим record_id для новых записей. Нужно для связи настоящей и фиктивной версий
        List<Long> generatedIds = dbUtils.getNextIds(BDUtils.Sequence.REF_BOOK_RECORD_ROW, countIds);
        if (refBook.isVersioned()) {
            int counter = 0;
            for (RefBookRecord record : records) {
                RefBookRecordVersion nextVersion = null;
                if (record.getRecordId() != null) {
                    nextVersion = refBookDao.getNextVersion(refBookId, record.getRecordId(), versionFrom);
                } else {
                    record.setRecordId(generatedIds.get(counter));
                    counter++;
                }
                if (versionTo == null) {
                    if (nextVersion != null && logger != null) {
                        logger.infoIfNotExist("Установлена дата окончания актуальности версии "+formatter.get().format(SimpleDateUtils.addDayToDate(nextVersion.getVersionStart(), -1))+" в связи с наличием следующей версии");
                    }
                } else {
                    if (!excludedVersionEndRecords.contains(record.getRecordId())) {
                        if (nextVersion == null) {
                            //Следующая версия не существует - создаем фиктивную версию
                            refBookDao.createFakeRecordVersion(refBookId, record.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                        } else {
                            int days = SimpleDateUtils.daysBetween(versionTo, nextVersion.getVersionStart());
                            if (days != 1) {
                                refBookDao.createFakeRecordVersion(refBookId, record.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                            }
                        }
                    }
                }
            }
        } else {
            //Для каждой записи своя группа, т.к версий нет
            int counter = 0;
            for (RefBookRecord record : records) {
                record.setRecordId(generatedIds.get(counter));
                counter++;
            }
        }

        return refBookDao.createRecordVersion(refBookId, versionFrom, VersionedObjectStatus.NORMAL, records);
    }

    /**
     * Обработка пересечений версий
     * @return нужна ли дальнейшая обработка даты окончания (фиктивной версии)? Она могла быть выполнена в процессе проверки пересечения
     */
    private boolean crossVersionsProcessing(List<CheckCrossVersionsResult> results, RefBook refBook, Date versionFrom, Date versionTo, Logger logger) {
        for (CheckCrossVersionsResult result : results) {
            if (result.getResult() == CrossResult.FATAL_ERROR) {
                throw new ServiceException(CROSS_ERROR_MSG);
            }
        }

        for (CheckCrossVersionsResult result : results) {
            if (result.getResult() == CrossResult.NEED_CHECK_USAGES) {
                if (refBook.isHierarchic()) {
                    //Поиск среди дочерних элементов
                    List<Pair<Date, Date>> childrenVersions = refBookDao.isVersionUsedLikeParent(refBookId, result.getRecordId(), versionFrom);
                    if (childrenVersions != null && !childrenVersions.isEmpty()) {
                        for (Pair<Date, Date> versions : childrenVersions) {
                            if (logger != null) {
                                logger.error(String.format("Существует дочерняя запись, действует с %s по %s",
                                        formatter.get().format(versions.getFirst()), versions.getSecond() != null ? formatter.get().format(versions.getSecond()) : "-"));
                            }
                        }
                        throw new ServiceException(CROSS_ERROR_MSG);
                    }
                }

                //Ищем все ссылки на запись справочника в новом периоде
                checkUsages(refBook, Arrays.asList(result.getRecordId()), versionFrom, versionTo, true, logger, CROSS_ERROR_MSG);
                if (logger != null) {
                    logger.info("Установлена дата окончания актуальности версии "+formatter.get().format(SimpleDateUtils.addDayToDate(versionFrom, -1))+" для предыдущей версии");
                }
            }
            if (result.getResult() == CrossResult.NEED_CHANGE) {
                refBookDao.updateVersionRelevancePeriod(RefBook.REF_BOOK_RECORD_TABLE_NAME, result.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                return false;
            }
            if (result.getResult() == CrossResult.NEED_DELETE) {
                refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, Arrays.asList(result.getRecordId()), false);
            }
        }
        return true;
    }

    @Override
    public Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> getUniqueAttributeValues(Long uniqueRecordId) {
        return refBookDao.getUniqueAttributeValues(refBookId, uniqueRecordId);
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        if (logger == null) {
            throw new ServiceException("Logger не может быть null!");
        }
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        //Устанавливаем блокировку на текущий справочник
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = LockData.LockObjects.REF_BOOK.name() + "_" + refBookId;
        RefBook refBook = refBookDao.get(refBookId);
        LockData lockData = lockService.lock(lockKey, userId,
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName()));
        if (lockData == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = LockData.LockObjects.REF_BOOK.name() + "_" + attribute.getRefBookId();
                        if (!lockedObjects.contains(referenceLockKey)) {
                            LockData referenceLockData = lockService.lock(referenceLockKey, userId,
                                    String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName()));
                            if (referenceLockData == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                        logEntryService.save(logger.getEntries()));
                            }
                        }
                    }
                }
                //Выполняем обновление записей
                try {
                    updateRecordVersionWithoutLock(logger, uniqueRecordId, versionFrom, versionTo, records);
                } catch (Exception e) {
                    throw new ServiceLoggerException(e.getLocalizedMessage(),
                            logEntryService.save(logger.getEntries()));
                }
            } finally {
                for (String lock : lockedObjects) {
                    lockService.unlock(lock, userId);
                }
            }
        } else {
            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, refBook.getName()),
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public void updateRecordVersionWithoutLock(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        try {
            RefBook refBook = refBookDao.get(refBookId);
            List<RefBookAttribute> attributes = refBook.getAttributes();
            boolean isJustNeedValuesUpdate = (versionFrom == null && versionTo == null);

            //Получаем идентификатор записи справочника без учета версий
            Long recordId = refBookDao.getRecordId(uniqueRecordId);
            //Получаем еще неотредактированную версию
            RefBookRecordVersion oldVersionPeriod = refBookDao.getRecordVersionInfo(uniqueRecordId);

            RefBookRecord refBookRecord = new RefBookRecord();
            refBookRecord.setUniqueRecordId(uniqueRecordId);
            refBookRecord.setValues(records);
            if (versionTo == null) {
                //Получение фактической даты окончания, которая может быть задана датой начала следующей версии
                RefBookRecordVersion nextVersion = refBookDao.getNextVersion(refBookId, recordId, versionFrom);
                if (nextVersion != null) {
                    Date versionEnd = SimpleDateUtils.addDayToDate(nextVersion.getVersionStart(), -1);
                    assert versionFrom != null;
                    if (versionEnd != null && versionFrom.after(versionEnd)) {
                        throw new ServiceException("Дата окончания получена некорректно");
                    }
                    refBookRecord.setVersionTo(versionEnd);
                } else {
                    refBookRecord.setVersionTo(versionTo);
                }
            } else {
                refBookRecord.setVersionTo(versionTo);
            }

            //Проверка корректности
            checkCorrectness(logger, refBook, uniqueRecordId, versionFrom, attributes, Arrays.asList(refBookRecord));

            if (refBook.isHierarchic()) {
                RefBookValue oldParent = refBookDao.getValue(uniqueRecordId, refBook.getAttribute(RefBook.RECORD_PARENT_ID_ALIAS).getId());
                RefBookValue newParent = records.get(RefBook.RECORD_PARENT_ID_ALIAS);
                //Проверка зацикливания
                if (!newParent.equals(oldParent) &&
                        refBookDao.hasLoops(uniqueRecordId, newParent.getReferenceValue())) {
                    //Цикл найден, формируем сообщение
                    String parentRecordName = refBookDao.buildUniqueRecordName(refBook,
                            refBookDao.getUniqueAttributeValues(refBookId, newParent.getReferenceValue()));
                    String recordName = refBookDao.buildUniqueRecordName(refBook,
                            refBookDao.getUniqueAttributeValues(refBookId, uniqueRecordId));
                    if (refBook.isVersioned()) {
                        throw new ServiceException("Версия " + parentRecordName + " не может быть указана как родительская, т.к. входит в структуру дочерних элементов версии " + recordName);
                    } else {
                        throw new ServiceException("Запись " + parentRecordName + " не может быть указана как родительская, т.к. входит в структуру дочерних элементов записей " + recordName);
                    }
                }
            }

            boolean isRelevancePeriodChanged = false;
            RefBookRecordVersion previousVersion = null;
            if (!isJustNeedValuesUpdate) {
                assert versionFrom != null;
                isRelevancePeriodChanged = !versionFrom.equals(oldVersionPeriod.getVersionStart())
                        || (versionTo != null && !versionTo.equals(oldVersionPeriod.getVersionEnd()))
                        || (oldVersionPeriod.getVersionEnd() != null && !oldVersionPeriod.getVersionEnd().equals(versionTo));

                if (isRelevancePeriodChanged) {
                    //Проверка пересечения версий
                    //Проверяем следующую версию после даты окочания
                    RefBookRecordVersion oldNextVersion = refBookDao.getNextVersion(refBookId, recordId, oldVersionPeriod.getVersionStart());
                    if (versionTo != null && oldNextVersion != null && (versionTo.equals(oldNextVersion.getVersionStart()) || versionTo.after(oldNextVersion.getVersionStart()))) {
                        //TODO: поведение отличается от поведения при создании записи, там исключения нет, просто изменяется дата окончания на дату начала след. версии
                        throw new ServiceException(CROSS_ERROR_MSG);
                    }
                    //Проверяем предыдущую версию до даты начала
                    previousVersion = refBookDao.getPreviousVersion(refBookId, recordId, oldVersionPeriod.getVersionStart());
                    if (previousVersion != null &&
                            (previousVersion.isVersionEndFake() && (versionFrom.equals(previousVersion.getVersionEnd())
                                    || versionFrom.before(previousVersion.getVersionEnd())
                                    || versionFrom.before(previousVersion.getVersionStart())))) {
                        throw new ServiceException(CROSS_ERROR_MSG);
                    }
                }
            }

            /** Проверяем изменились ли значения атрибутов */
            boolean isValuesChanged = checkValuesChanged(uniqueRecordId, records);
            
            if (isValuesChanged) {
                //Если значения атрибутов изменились, то проверяем все использования записи, без учета периода
                checkUsages(refBook, Arrays.asList(uniqueRecordId), versionFrom, versionTo, null, logger, "Изменение невозможно, обнаружено использование элемента справочника!");
            }

            //Обновление периода актуальности
            if (isRelevancePeriodChanged) {
                if (!isValuesChanged) {
                    //Если изменился только период актуальности, то ищем все ссылки не пересекающиеся с новым периодом, но которые действовали в старом
                    checkUsages(refBook, Arrays.asList(uniqueRecordId), versionFrom, versionTo, false, logger, "Изменение невозможно, обнаружено использование элемента справочника!");
                }
                if (!refBook.isVersioned()) {
                    //Если справочник не версионный, то нет смысла проверять пересечения
                    //checkUsages(refBook, Arrays.asList(uniqueRecordId), versionFrom, versionTo, null, logger, "Изменение невозможно, обнаружено использование элемента справочника!");
                } else {
                    List<Long> uniqueIdAsList = Arrays.asList(uniqueRecordId);
                    if (previousVersion != null && (previousVersion.isVersionEndFake() && SimpleDateUtils.addDayToDate(previousVersion.getVersionEnd(), 1).equals(versionFrom))) {
                        //Если установлена дата окончания, которая совпадает с существующей фиктивной версией - то она удаляется
                        Long previousVersionEnd = refBookDao.findRecord(refBookId, recordId, versionFrom);
                        refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, Arrays.asList(previousVersionEnd), false);
                    }

                    boolean delayedUpdate = false;
                    if (oldVersionPeriod.getVersionEnd() != null && versionFrom.equals(oldVersionPeriod.getVersionEnd())) {
                        //Обновляем дату начала актуальности, если не совпадает с датой окончания
                        refBookDao.updateVersionRelevancePeriod(RefBook.REF_BOOK_RECORD_TABLE_NAME, uniqueRecordId, versionFrom);
                    } else {
                        delayedUpdate = true;
                    }

                    //Получаем запись - окончание версии. Если = null, то версия не имеет конца
                    List<Long> relatedVersions = refBookDao.getRelatedVersions(uniqueIdAsList);
                    if (!relatedVersions.isEmpty() && relatedVersions.size() > 1) {
                        throw new ServiceException("Обнаружено несколько фиктивных версий");
                    }
                    if (versionTo != null) {
                        //Существует другая версия, дата начала которой = нашей новой дате окончания?
                        boolean isVersionEndAlreadyExists = refBookDao.isVersionsExist(refBookId, Arrays.asList(recordId), SimpleDateUtils.addDayToDate(versionTo, 1));
                        if (relatedVersions.isEmpty() && !isVersionEndAlreadyExists) {
                            //Создаем новую фиктивную версию - дату окончания
                            refBookDao.createFakeRecordVersion(refBookId, recordId, SimpleDateUtils.addDayToDate(versionTo, 1));
                        }

                        if (!relatedVersions.isEmpty() && !oldVersionPeriod.getVersionEnd().equals(versionTo)) {
                            if (!isVersionEndAlreadyExists) {
                                //Изменяем существующую дату окончания
                                refBookDao.updateVersionRelevancePeriod(RefBook.REF_BOOK_RECORD_TABLE_NAME, relatedVersions.get(0), SimpleDateUtils.addDayToDate(versionTo, 1));
                            } else {
                                //Удаляем дату окончания. Теперь дата окончания задается началом следующей версии
                                refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, relatedVersions, false);
                            }
                        }
                    }

                    if (!relatedVersions.isEmpty() && versionTo == null) {
                        //Удаляем фиктивную запись - теперь у версии нет конца
                        refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, relatedVersions, false);
                    }

                    if (delayedUpdate) {
                        //Обновляем дату начала актуальности, если ранее это было отложено т.к она совпадала с датой окончания (теперь она изменена)
                        refBookDao.updateVersionRelevancePeriod(RefBook.REF_BOOK_RECORD_TABLE_NAME, uniqueRecordId, versionFrom);
                    }
                }
            }

            //Обновление значений атрибутов версии
            refBookDao.updateRecordVersion(refBookId, uniqueRecordId, records);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                logger.clear(LogLevel.INFO);
                throw new ServiceLoggerException("Запись не сохранена, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Запись не сохранена, обнаружены фатальные ошибки!");
            }
        }
    }
    
    private void checkUsages(RefBook refBook, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo, Boolean restrictPeriod, Logger logger, String errorMsg) {
        //Проверка использования
        if (refBook.isHierarchic()) {
            //Поиск среди дочерних элементов
            for (Long uniqueRecordId : uniqueRecordIds) {
                List<Pair<Date, Date>> childrenVersions = refBookDao.isVersionUsedLikeParent(refBookId, uniqueRecordId, versionFrom);
                if (childrenVersions != null && !childrenVersions.isEmpty()) {
                    for (Pair<Date, Date> versions : childrenVersions) {
                        if (logger != null) {
                            String msg = "Существует дочерняя запись";
                            if (refBook.isVersioned()) {
                                msg = msg + ", действует с " + formatter.get().format(versions.getFirst()) +
                                        (versions.getSecond() != null ? " по " + formatter.get().format(versions.getSecond()) : "-");
                            }
                            logger.error(msg);
                        }
                    }
                    throw new ServiceException(errorMsg);
                }
            }
        }

        boolean used = false;

        //Проверка использования в справочниках
        List<String> refBooks = refBookDao.isVersionUsedInRefBooks(refBookId, uniqueRecordIds, versionFrom, versionTo, restrictPeriod,
                RefBookTableRef.getTablesIdByRefBook(refBookId) != null ?
                        Arrays.asList(ArrayUtils.toObject(RefBookTableRef.getTablesIdByRefBook(refBookId))) : null);
        for (String refBookMsg : refBooks) {
            logger.error(refBookMsg);
            used = true;
        }

        //Проверка использования в нф
        List<FormLink> forms = refBookDao.isVersionUsedInForms(refBookId, uniqueRecordIds, versionFrom, versionTo, restrictPeriod);
        for (FormLink form : forms) {
            //Исключаем экземпляры в статусе "Создана" использующих справочник "Участники ТЦО"
            if (refBookId == RefBook.TCO && form.getState() == WorkflowState.CREATED) {
                //Для нф в статусе "Создана" удаляем сформированные печатные представления, отменяем задачи на их формирование и рассылаем уведомления
                formDataService.deleteReport(form.getFormDataId(), false, logger.getTaUserInfo().getUser().getId(),
                        "Модифицирована запись справочника \"" + refBook.getName() + "\", которая используется в данной форме");
                /*
                reportService.delete(form.getFormDataId(), null);
                List<ReportType> interruptedReportTypes = Arrays.asList(ReportType.EXCEL, ReportType.CSV);
                for (ReportType interruptedType : interruptedReportTypes) {
                    List<String> taskKeyList = new ArrayList<String>();
                    if (ReportType.CSV.equals(interruptedType) || ReportType.EXCEL.equals(interruptedType)) {
                        taskKeyList.addAll(formDataService.generateReportKeys(interruptedType, form.getFormDataId(), null));
                    } else {
                        taskKeyList.add(formDataService.generateTaskKey(form.getFormDataId(), interruptedType));
                    }
                    for(String key: taskKeyList) {
                        LockData lockData = lockService.getLock(key);
                        if (lockData != null) {
                            lockService.interruptTask(lockData, logger.getTaUserInfo().getUser().getId(), true, cause);
                        }
                    }
                }*/
            } else {
                logger.error(form.getMsg());
                used = true;
            }
        }

        //Проверка использования в настройках подразделений
        List<String> configs = refBookDao.isVersionUsedInDepartmentConfigs(refBookId, uniqueRecordIds, versionFrom, versionTo, restrictPeriod,
                RefBookTableRef.getTablesIdByRefBook(refBookId) != null ?
                        Arrays.asList(ArrayUtils.toObject(RefBookTableRef.getTablesIdByRefBook(refBookId))) : null);
        for (String configMsg : configs) {
            logger.error(configMsg);
            used = true;
        }

        if (used) {
            throw new ServiceException(errorMsg);
        }
    }

    // TODO использую в скрипте
    boolean checkValuesChanged(Long uniqueRecordId, Map<String,RefBookValue> records) {
        Map<String,RefBookValue> oldValues = refBookDao.getRecordData(refBookId, uniqueRecordId);
        for (Map.Entry<String, RefBookValue> newValue : records.entrySet()) {
            RefBookValue oldValue = oldValues.get(newValue.getKey());
            if (!newValue.getValue().equals(oldValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        if (logger == null) {
            throw new ServiceException("Logger не может быть null!");
        }
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        //Устанавливаем блокировку на тевущий справочник
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = LockData.LockObjects.REF_BOOK.name() + "_" + refBookId;
        RefBook refBook = refBookDao.get(refBookId);
        LockData lockData = lockService.lock(lockKey, userId,
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName()));
        if (lockData == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = LockData.LockObjects.REF_BOOK.name() + "_" + attribute.getRefBookId();
                        if (!lockedObjects.contains(referenceLockKey)) {
                            LockData referenceLockData = lockService.lock(referenceLockKey, userId,
                                    String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName()));
                            if (referenceLockData == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                        logEntryService.save(logger.getEntries()));
                            }
                        }
                    }
                }
                //Устанавливаем дату окончания
                updateRecordsVersionEndWithoutLock(logger, versionEnd, uniqueRecordIds);
            } finally {
                for (String lock : lockedObjects) {
                    lockService.unlock(lock, userId);
                }
            }
        } else {
            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, refBook.getName()),
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public void updateRecordsVersionEndWithoutLock(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        try {
            for (Long uniqueRecordId : uniqueRecordIds) {
                List<Long> relatedVersions = refBookDao.getRelatedVersions(uniqueRecordIds);
                if (!relatedVersions.isEmpty() && relatedVersions.size() > 1) {
                    refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, relatedVersions, false);
                }
                Long recordId = refBookDao.getRecordId(uniqueRecordId);
                RefBook refBook = refBookDao.getByRecord(uniqueRecordId);
                //Проверяем следующую версию после даты окочания
                RefBookRecordVersion oldVersionPeriod = refBookDao.getRecordVersionInfo(uniqueRecordId);
                RefBookRecordVersion nextVersion = refBookDao.getNextVersion(refBook.getId(), recordId, oldVersionPeriod.getVersionStart());
                if (versionEnd != null && nextVersion != null && versionEnd.after(nextVersion.getVersionStart())) {
                    throw new ServiceException(CROSS_ERROR_MSG);
                }
                refBookDao.createFakeRecordVersion(refBook.getId(), recordId, SimpleDateUtils.addDayToDate(versionEnd, 1));
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                logger.clear(LogLevel.INFO);
                throw new ServiceLoggerException("Запись не сохранена. Обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Запись не сохранена. Обнаружены фатальные ошибки!");
            }
        }
    }

    private void checkChildren(List<Long> uniqueRecordIds) {
        //Если есть дочерние элементы - удалять нельзя
        List<Date> parentVersions = refBookDao.hasChildren(refBookId, uniqueRecordIds);
        if (parentVersions != null && !parentVersions.isEmpty()) {
            StringBuilder versions = new StringBuilder();
            for (int i=0; i<parentVersions.size(); i++) {
                versions.append(formatter.get().format(parentVersions.get(i)));
                if (i < parentVersions.size() - 1) {
                    versions.append(", ");
                }
            }
            throw new ServiceException("Удаление версии от "+ versions +" невозможно, существует дочерние элементы!");
        }
    }

    @Override
    public void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds) {
        if (logger == null) {
            throw new ServiceException("Logger не может быть null!");
        }
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        //Устанавливаем блокировку на тевущий справочник
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = LockData.LockObjects.REF_BOOK.name() + "_" + refBookId;
        RefBook refBook = refBookDao.get(refBookId);
        LockData lockData = lockService.lock(lockKey, userId,
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName()));
        if (lockData == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = LockData.LockObjects.REF_BOOK.name() + "_" + attribute.getRefBookId();
                        if (!lockedObjects.contains(referenceLockKey)) {
                            LockData referenceLockData = lockService.lock(referenceLockKey, userId,
                                    String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName()));
                            if (referenceLockData == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                        logEntryService.save(logger.getEntries()));
                            }
                        }
                    }
                }
                deleteAllRecordsWithoutLock(logger, uniqueRecordIds);
            } finally {
                for (String lock : lockedObjects) {
                    lockService.unlock(lock, userId);
                }
            }
        } else {
            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, refBook.getName()),
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public void deleteAllRecordsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        try {
            RefBook refBook = refBookDao.get(refBookId);
            //Проверка использования
            if (refBook.isHierarchic()) {
                checkChildren(uniqueRecordIds);
            }
            //Ищем все ссылки на запись справочника без учета периода
            checkUsages(refBook, uniqueRecordIds, null, null, null, logger, "Удаление невозможно, обнаружено использование элемента справочника!");
            refBookDao.deleteAllRecordVersions(refBookId, uniqueRecordIds);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                logger.clear(LogLevel.INFO);
                throw new ServiceLoggerException("Элемент справочника не удален, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Элемент справочника не удален, обнаружены фатальные ошибки!");
            }
        }
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds, boolean force) {
        deleteRecordVersions(logger, uniqueRecordIds);
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds) {
        if (logger == null) {
            throw new ServiceException("Logger не может быть null!");
        }
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = LockData.LockObjects.REF_BOOK.name() + "_" + refBookId;
        RefBook refBook = refBookDao.get(refBookId);
        LockData lockData = lockService.lock(lockKey, userId,
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName()));
        if (lockData == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = LockData.LockObjects.REF_BOOK.name() + "_" + attribute.getRefBookId();
                        if (!lockedObjects.contains(referenceLockKey)) {
                            LockData referenceLockData = lockService.lock(referenceLockKey, userId,
                                    String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName()));
                            if (referenceLockData == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                        logEntryService.save(logger.getEntries()));
                            }
                        }
                    }
                }
                try {
                    deleteRecordVersionsWithoutLock(logger, uniqueRecordIds);
                } catch (Exception e) {
                    throw new ServiceLoggerException(e.getLocalizedMessage(),
                            logEntryService.save(logger.getEntries()));
                }
            } finally {
                for (String lock : lockedObjects) {
                    lockService.unlock(lock, userId);
                }
            }
        } else {
            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, refBook.getName()),
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public void deleteRecordVersionsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        try {
            RefBook refBook = refBookDao.get(refBookId);
            //Проверка использования
            if (refBook.isHierarchic()) {
                checkChildren(uniqueRecordIds);
            }
            //Ищем все ссылки на запись справочника без учета периода
            checkUsages(refBook, uniqueRecordIds, null, null, null, logger, "Удаление невозможно, обнаружено использование элемента справочника!");
            if (refBook.isVersioned()) {
                List<Long> fakeVersionIds = refBookDao.getRelatedVersions(uniqueRecordIds);
                uniqueRecordIds.addAll(fakeVersionIds);
            }
            refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, uniqueRecordIds, false);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                logger.clear(LogLevel.INFO);
                throw new ServiceLoggerException("Версия элемента справочника не удалена, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Версия элемента справочника не удалена, обнаружены фатальные ошибки!");
            }
        }
    }

    @Override
    public Long getFirstRecordId(Long uniqueRecordId) {
        return refBookDao.getFirstRecordId(refBookId, uniqueRecordId);
    }

    @Override
    public Long getRecordId(Long uniqueRecordId) {
        return refBookDao.getRecordId(uniqueRecordId);
    }

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        return refBookDao.getAttributesValues(attributePairs);
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        return refBookDao.getInactiveRecordsInPeriod(RefBook.REF_BOOK_RECORD_TABLE_NAME, recordIds, periodFrom, periodTo, false);
    }

    @Override
	public void insertRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        List<String> lockedObjects = new ArrayList<String>();
        String lockKey = LockData.LockObjects.REF_BOOK.name() + "_" + refBookId;
        RefBook refBook = refBookDao.get(refBookId);
        LockData lockData = lockService.lock(lockKey, taUserInfo.getUser().getId(),
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName()));
        if (lockData == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = LockData.LockObjects.REF_BOOK.name() + "_" + attribute.getRefBookId();
                        if (!lockedObjects.contains(referenceLockKey)) {
                            LockData referenceLockData = lockService.lock(referenceLockKey, taUserInfo.getUser().getId(),
                                    String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName()));
                            if (referenceLockData == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceException(String.format(LOCK_MESSAGE, attributeRefBook.getName()));
                            }
                        }
                    }
                }
                insertRecordsWithoutLock(taUserInfo, version, records);
            } finally {
                for (String lock : lockedObjects) {
                    lockService.unlock(lock, taUserInfo.getUser().getId());
                }
            }
        } else {
            throw new ServiceException(String.format(LOCK_MESSAGE, refBook.getName()));
        }
	}

    @Override
    public void insertRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        refBookDao.createRecords(refBookId, version, records);
        //createRecordVersion(Logger logger, Long recordId, Date versionFrom, Date versionTo, List<Map<String, RefBookValue>> records)
    }

    @Override
	public void updateRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        List<String> lockedObjects = new ArrayList<String>();
        String lockKey = LockData.LockObjects.REF_BOOK.name() + "_" + refBookId;
        RefBook refBook = refBookDao.get(refBookId);
        LockData lockData = lockService.lock(lockKey, taUserInfo.getUser().getId(),
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName()));
        if (lockData == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = LockData.LockObjects.REF_BOOK.name() + "_" + attribute.getRefBookId();
                        if (!lockedObjects.contains(referenceLockKey)) {
                            LockData referenceLockData = lockService.lock(referenceLockKey, taUserInfo.getUser().getId(),
                                    String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName()));
                            if (referenceLockData == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceException(String.format(LOCK_MESSAGE, attributeRefBook.getName()));
                            }
                        }
                    }
                }
                updateRecordsWithoutLock(taUserInfo, version, records);
            } finally {
                for (String lock : lockedObjects) {
                    lockService.unlock(lock, taUserInfo.getUser().getId());
                }
            }
        } else {
            throw new ServiceException(String.format(LOCK_MESSAGE, refBook.getName()));
        }
	}

    @Override
    public void updateRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        refBookDao.updateRecords(refBookId, version, records);
        //updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, boolean isRelevancePeriodChanged, List<Map<String, RefBookValue>> records)
    }

    @Override
	public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
		return refBookDao.dereferenceValues(attributeId, recordIds);
	}

    @Override
    public List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNextVersion(Date version, String filter) {
        return refBookDao.getNextVersion(refBookId, version, filter);
    }
}
