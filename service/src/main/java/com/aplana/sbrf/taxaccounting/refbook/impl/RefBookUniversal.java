package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.util.BDUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private RefBookUtils refBookUtils;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private BDUtils dbUtils;

	protected Long refBookId;

    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        return refBookDao.getVersions(refBookId, startDate, endDate);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookDao.getRecordVersions(refBookId, recordId, pagingParams, filter, sortAttribute);
    }

    @Override
	public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version,
			PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		return refBookDao.getChildrenRecords(refBookId, parentRecordId, version, pagingParams, filter, sortAttribute);
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
	public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookDao.getRecordData(refBookId, recordId);
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
    public void createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        try {
            List<RefBookAttribute> attributes = refBookDao.getAttributes(refBookId);
            List<String> errors;

            //Проверка обязательности заполнения записей справочника
            errors = refBookUtils.checkFillRequiredRefBookAtributes(attributes, records);
            if (errors.size() > 0){
                throw new ServiceException("Поля " + errors.toString() + " являются обязательными для заполнения");
            }

            //Проверка корректности значений атрибутов
            errors = refBookUtils.checkRefBookAtributeValues(attributes, records);
            if (errors.size() > 0){
                for (String error : errors) {
                    logger.error(error);
                }
                throw new ServiceException("Обнаружено некорректное значение атрибута");
            }


            //Проверка корректности
            List<Pair<Long,String>> matchedRecords = refBookDao.getMatchedRecordsByUniqueAttributes(refBookId, attributes, records);
            if (matchedRecords == null || matchedRecords.size() == 0) {
                //Проверка ссылочных значений
                boolean isReferencesOk = refBookDao.isReferenceValuesCorrect(versionFrom, attributes, records);
                if (!isReferencesOk) {
                    throw new ServiceException("Период актуальности выбранного значения меньше периода актуальности версии");
                }
            } else {
                //Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
                refBookDao.checkConflictValuesVersions(matchedRecords, versionFrom, versionTo);
            }

            for (RefBookRecord record : records) {
                if (!refBookDao.isVersionExist(refBookId, record.getRecordId(), versionFrom)) {
                    //Проверка пересечения версий
                    if (record.getRecordId() != null) {
                        crossVersionsProcessing(refBookDao.checkCrossVersions(refBookId, record.getRecordId(), versionFrom, versionTo, null),
                                versionFrom, versionTo, logger);
                    }
                } else {
                    throw new ServiceException("Версия с указанной датой актуальности уже существует");
                }
            }

            //Создание настоящей и фиктивной версии
            createVersions(versionFrom, versionTo, records, logger);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                throw new ServiceLoggerException("Версия не создана, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Версия не создана, обнаружены фатальные ошибки!");
            }
        }

    }

    private void createVersions(Date versionFrom, Date versionTo, List<RefBookRecord> records, Logger logger) {
        //Генерим record_id для новых записей. Нужно для связи настоящей и фиктивной версий
        long countIds = 0;
        for (RefBookRecord record : records) {
            if (record.getRecordId() == null) {
                countIds++;
            }
        }
        List<Long> generatedIds = dbUtils.getNextIds(BDUtils.Sequence.REF_BOOK_RECORD_ROW, countIds);

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
                    logger.info("Установлена дата окончания актуальности версии "+sdf.format(addDayToDate(nextVersion.getVersionStart(), -1))+" в связи с наличием следующей версии");
                }
            } else {
                if (nextVersion == null) {
                    //Следующая версия не существует - создаем фиктивную версию
                    refBookDao.createFakeRecordVersion(refBookId, record.getRecordId(), addDayToDate(versionTo, 1));
                } else {
                    int days = daysBetween(versionTo, nextVersion.getVersionStart());
                    if (days != 1) {
                        refBookDao.createFakeRecordVersion(refBookId, record.getRecordId(), addDayToDate(versionTo, 1));
                    }
                }
            }
        }

        refBookDao.createRecordVersion(refBookId, versionFrom, VersionedObjectStatus.NORMAL, records);
    }

    /**
     * Обработка пересечений версий
     */
    private void crossVersionsProcessing(List<CheckCrossVersionsResult> results, Date versionFrom, Date versionTo, Logger logger) {
        boolean isFatalErrorExists = false;

        for (CheckCrossVersionsResult result : results) {
            if (result.getResult() == CrossResult.NEED_CHECK_USAGES) {
                boolean isReferenceToVersionExists = refBookDao.isVersionUsed(result.getRecordId(), versionFrom);
                if (isReferenceToVersionExists) {
                    throw new ServiceException("Обнаружено пересечение указанного срока актуальности с существующей версией!");
                } else {
                    if (logger != null) {
                        logger.info("Установлена дата окончания актуальности версии "+sdf.format(addDayToDate(versionFrom, -1))+" для предыдущей версии");
                    }
                }
            }
            if (result.getResult() == CrossResult.NEED_CHANGE) {
                refBookDao.updateVersionRelevancePeriod(result.getRecordId(), addDayToDate(versionTo, 1));
                updateResults(results, result);
            }
            if (result.getResult() == CrossResult.NEED_DELETE) {
                refBookDao.deleteVersion(result.getRecordId());
                updateResults(results, result);
            }
        }

        for (CheckCrossVersionsResult result : results) {
            if (result.getResult() == CrossResult.FATAL_ERROR) {
                isFatalErrorExists = true;
            }
        }

        if (isFatalErrorExists) {
            throw new ServiceException("Обнаружено пересечение указанного срока актуальности с существующей версией!");
        }
    }

    private Date addDayToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    private int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    /**
     * Обновляет значение результата для пересекаемых версий, конфликт которых решается изменением соседних
     * В частности возможно удаление либо изменение даты окончания версии, что устраняет часть конфликтов
     * @param results все результаты проверки пересечения
     * @param wantedResult результат, для которого было выполнено устранение конфликта
     */
    private void updateResults(List<CheckCrossVersionsResult> results, CheckCrossVersionsResult wantedResult) {
        for (CheckCrossVersionsResult result : results) {
            if (result.getNum() == wantedResult.getNum() - 1) {
                if (result.getResult() == CrossResult.FATAL_ERROR) {
                    result.setResult(CrossResult.OK);
                } else {
                    throw new ServiceException("Недопустимая ситуация. Начало редактируемой версии является фиктивным: "+result.getResult());
                }
            }
        }
    }

    @Override
    public List<Pair<RefBookAttribute, RefBookValue>> getUniqueAttributeValues(Long uniqueRecordId) {
        return refBookDao.getUniqueAttributeValues(refBookId, uniqueRecordId);
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        try {
            boolean isJustNeedValuesUpdate = (versionFrom == null && versionTo == null);

            List<RefBookAttribute> attributes = refBookDao.getAttributes(refBookId);
            //Получаем идентификатор записи справочника без учета версий
            Long recordId = refBookDao.getRecordId(uniqueRecordId);

            //Проверка обязательности заполнения записей справочника
            List<String> errors= refBookUtils.checkFillRequiredRefBookAtributes(attributes, records);
            if (errors.size() > 0){
                throw new ServiceException("Поля " + errors.toString() + " являются обязательными для заполнения");
            }

            RefBookRecordVersion oldVersionPeriod = refBookDao.getRecordVersionInfo(uniqueRecordId);

            boolean isRelevancePeriodChanged = false;
            if (!isJustNeedValuesUpdate) {
                isRelevancePeriodChanged = !versionFrom.equals(oldVersionPeriod.getVersionStart())
                        || (versionTo != null && !versionTo.equals(oldVersionPeriod.getVersionEnd()))
                        || (oldVersionPeriod.getVersionEnd() != null && !oldVersionPeriod.getVersionEnd().equals(versionTo));

                if (isRelevancePeriodChanged) {
                    //Проверка пересечения версий
                    crossVersionsProcessing(refBookDao.checkCrossVersions(refBookId, recordId, versionFrom, versionTo, uniqueRecordId),
                            versionFrom, versionTo, logger);
                }

                //Проверка использования
                boolean isReferenceToVersionExists = refBookDao.isVersionUsed(uniqueRecordId, versionFrom);
                if (isReferenceToVersionExists) {
                    throw new ServiceException("Обнаружено использование версии в других точках запроса");
                }
            }

            //Обновление периода актуальности
            if (isRelevancePeriodChanged) {
                List<Long> uniqueIdAsList = Arrays.asList(uniqueRecordId);
                //Обновляем дату начала актуальности
                refBookDao.updateVersionRelevancePeriod(uniqueRecordId, versionFrom);
                //Получаем запись - окончание версии. Если = null, то версия не имеет конца
                List<Long> relatedVersions = refBookDao.getRelatedVersions(uniqueIdAsList);
                if (!relatedVersions.isEmpty() && relatedVersions.size() > 1) {
                    throw new ServiceException("Обнаружено несколько фиктивных версий");
                }
                if (relatedVersions.isEmpty() && versionTo != null) {
                    //Создаем новую фиктивную версию - дату окончания
                    refBookDao.createFakeRecordVersion(refBookId, recordId, addDayToDate(versionTo, 1));
                }

                if (!relatedVersions.isEmpty() && versionTo == null) {
                    //Удаляем фиктивную запись - теперь у версии нет конца
                    refBookDao.deleteRecordVersions(relatedVersions);
                }

                if (!relatedVersions.isEmpty() && versionTo != null) {
                    //Изменяем существующую фиктивную версию
                    refBookDao.updateVersionRelevancePeriod(relatedVersions.get(0), versionTo);
                }
            }

            //Обновление значений атрибутов версии
            refBookDao.updateRecordVersion(refBookId, uniqueRecordId, records);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                throw new ServiceLoggerException("Версия не сохранена, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Версия не сохранена, обнаружены фатальные ошибки!");
            }
        }
    }

    @Override
    public void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        for (Long uniqueRecordId : uniqueRecordIds) {
            List<Long> relatedVersions = refBookDao.getRelatedVersions(uniqueRecordIds);
            if (!relatedVersions.isEmpty() && relatedVersions.size() > 1) {
                refBookDao.deleteRecordVersions(relatedVersions);
            }
            Long recordId = refBookDao.getRecordId(uniqueRecordId);
            crossVersionsProcessing(refBookDao.checkCrossVersions(refBookId, recordId, versionEnd, null, null),
                    versionEnd, null, logger);
            refBookDao.createFakeRecordVersion(refBookId, recordId, addDayToDate(versionEnd, 1));
        }
    }

    @Override
    public void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds) {
        try {
            boolean isReferenceToVersionExists = refBookDao.isVersionUsed(uniqueRecordIds);
            if (isReferenceToVersionExists) {
                throw new ServiceException("Удаление невозможно, обнаружено использование элемента справочника!");
            }
            RefBook refBook = refBookDao.get(refBookId);
            if (refBook.isHierarchic()) {
                //TODO существует ли версия, период актуальности которой пересекается с периодом актуальности удаляемой версии и для которой удаляемая версия является родительским элементом.
            }
            refBookDao.deleteAllRecordVersions(refBookId, uniqueRecordIds);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                throw new ServiceLoggerException("Элемент справочника не удален, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Элемент справочника не удален, обнаружены фатальные ошибки!");
            }
        }
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds) {
        try {
            boolean isReferenceToVersionExists = refBookDao.isVersionUsed(uniqueRecordIds);
            if (isReferenceToVersionExists) {
                throw new ServiceException("Удаление невозможно, обнаружено использование элемента справочника!");
            }
            RefBook refBook = refBookDao.get(refBookId);
            if (refBook.isHierarchic()) {
                //TODO существует ли версия, период актуальности которой пересекается с периодом актуальности удаляемой версии и для которой удаляемая версия является родительским элементом.
            }
            List<Long> fakeVersionIds = refBookDao.getRelatedVersions(uniqueRecordIds);
            uniqueRecordIds.addAll(fakeVersionIds);
            refBookDao.deleteRecordVersions(uniqueRecordIds);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
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
	public void insertRecords(Date version, List<Map<String, RefBookValue>> records) {
        refBookDao.createRecords(refBookId, version, records);
        //createRecordVersion(Logger logger, Long recordId, Date versionFrom, Date versionTo, List<Map<String, RefBookValue>> records)
	}

	@Override
	public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {
        refBookDao.updateRecords(refBookId, version, records);
        //updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, boolean isRelevancePeriodChanged, List<Map<String, RefBookValue>> records)
	}

    @Override
    public void deleteRecords(Date version, List<Long> recordIds) {
        refBookDao.deleteRecords(refBookId, version, recordIds);
        //deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds)
    }

    @Override
    public void deleteAllRecords(Date version) {
        refBookDao.deleteAllRecords(refBookId, version);
        //deleteAllRecords(Logger logger, List<Long> uniqueRecordIds)
    }
}
