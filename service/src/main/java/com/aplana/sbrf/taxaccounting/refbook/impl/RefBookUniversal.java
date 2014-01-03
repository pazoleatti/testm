package com.aplana.sbrf.taxaccounting.refbook.impl;

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
			String filter, RefBookAttribute sortAttribute) {
		return refBookDao.getRecords(refBookId, version, pagingParams, filter, sortAttribute);
	}

	@Override
	public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookDao.getRecordData(refBookId, recordId);
	}

	@Override
	public void deleteRecords(Date version, List<Long> recordIds) {
        //refBookDao.deleteRecords(refBookId, version, recordIds);
        throw new UnsupportedOperationException();
	}

    @Override
    public void deleteAllRecords(Date version) {
        //refBookDao.deleteAllRecords(refBookId, version);
        throw new UnsupportedOperationException();
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        return refBookDao.getValue(recordId, attributeId);
    }

    @Override
    public RefBookRecordVersion getActiveRecordVersion(Long uniqueRecordId) {
        return refBookDao.getActiveRecordVersion(uniqueRecordId);
    }

    @Override
    public int getRecordVersionsCount(Long uniqueRecordId) {
        return refBookDao.getRecordVersionsCount(refBookId, uniqueRecordId);
    }

    @Override
    public void createRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, List<Map<String, RefBookValue>> records) {
        try {
            List<RefBookAttribute> attributes = refBookDao.getAttributes(refBookId);
            //Получаем идентификатор записи справочника без учета версий
            Long recordId = uniqueRecordId != null ? refBookDao.getRecordId(uniqueRecordId) : null;
            //Проверка обязательности заполнения записей справочника
            for (Map<String, RefBookValue> record : records) {
                List<String> errors= refBookUtils.checkFillRequiredRefBookAtributes(attributes, record);
                if (errors.size() > 0){
                    throw new ServiceException("Поля " + errors.toString() + "являются обязательными для заполнения");
                }
            }

            //Проверка корректности
            List<Pair<Long,String>> matchedRecords = refBookDao.getMatchedRecordsByUniqueAttributes(refBookId, attributes, records);
            if (matchedRecords.size() == 0) {
                //Проверка ссылочных значений
                boolean isReferencesOk = refBookDao.checkReferenceValuesVersions(versionFrom, attributes, records);
                if (!isReferencesOk) {
                    throw new ServiceException("Период актуальности выбранного значения меньше периода актуальности версии");
                }
            } else {
                //Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
                refBookDao.checkConflictValuesVersions(matchedRecords, versionFrom, versionTo);
            }
            //Проверка пересечения версий
            if (recordId != null) {
                crossVersionsProcessing(refBookDao.checkCrossVersions(refBookId, recordId, versionFrom, versionTo, null),
                        versionFrom, versionTo, logger);
            }
            //Создание настоящей и фиктивной версии
            createVersions(recordId, versionFrom, versionTo, records, logger);
        } catch (Exception e) {
            logger.error(e);
            throw new ServiceLoggerException("Версия не создана, обнаружены фатальные ошибки!",
                    logEntryService.save(logger.getEntries()));
        }

    }

    private void createVersions(Long recordId, Date versionFrom, Date versionTo, List<Map<String, RefBookValue>> records, Logger logger) {
        RefBookRecordVersion nextVersion = null;
        if (recordId != null) {
            nextVersion = refBookDao.getNextVersion(refBookId, recordId, versionFrom);
        }
        if (versionTo == null) {
            if (nextVersion != null) {
                logger.info("Установлена дата окончания актуальности версии "+sdf.format(addDayToDate(nextVersion.getVersionStart(), -1))+" в связи с наличием следующей версии");
            }
        } else {
            if (nextVersion == null) {
                //Следующая версия не существует - создаем фиктивную версию
                recordId = refBookDao.createRecordVersion(refBookId, recordId, addDayToDate(versionTo, 1), VersionedObjectStatus.FAKE, null);
            } else {
                int days = daysBetween(versionTo, nextVersion.getVersionStart());
                if (days != 1) {
                    refBookDao.createRecordVersion(refBookId, recordId, addDayToDate(versionTo, 1), VersionedObjectStatus.FAKE, null);
                }
            }
        }
        refBookDao.createRecordVersion(refBookId, recordId, versionFrom, VersionedObjectStatus.NORMAL, records);
    }

    /**
     * Обработка пересечений версий
     */
    private void crossVersionsProcessing(List<CheckCrossVersionsResult> results, Date versionFrom, Date versionTo, Logger logger) {
        boolean isFatalErrorExists = false;

        for (CheckCrossVersionsResult result : results) {
            if (result.getResult() == CrossResult.NEED_CHECK_USAGES) {
                boolean isReferenceToVersionExists = refBookDao.checkVersionUsages(result.getRecordId(), versionFrom);
                if (isReferenceToVersionExists) {
                    throw new ServiceException("Обнаружено пересечение указанного срока актуальности с существующей версией!");
                } else {
                    logger.info("Установлена дата окончания актуальности версии "+sdf.format(addDayToDate(versionFrom, -1))+"для предыдущей версии");
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
    public List<RefBookValue> getUniqueAttributeValues(Long uniqueRecordId) {
        return refBookDao.getUniqueAttributeValues(refBookId, uniqueRecordId);
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, boolean isRelevancePeriodChanged, List<Map<String, RefBookValue>> records) {
        try {
            List<RefBookAttribute> attributes = refBookDao.getAttributes(refBookId);
            //Получаем идентификатор записи справочника без учета версий
            Long recordId = refBookDao.getRecordId(uniqueRecordId);

            //Проверка обязательности заполнения записей справочника
            for (Map<String, RefBookValue> record : records) {
                List<String> errors= refBookUtils.checkFillRequiredRefBookAtributes(attributes, record);
                if (errors.size() > 0){
                    throw new ServiceException("Поля " + errors.toString() + "являются обязательными для заполнения");
                }
            }

            if (isRelevancePeriodChanged) {
                //Проверка пересечения версий
                crossVersionsProcessing(refBookDao.checkCrossVersions(refBookId, recordId, versionFrom, versionTo, uniqueRecordId),
                        versionFrom, versionTo, logger);
            }
            //Проверка использования
            boolean isReferenceToVersionExists = refBookDao.checkVersionUsages(uniqueRecordId, versionFrom);
            if (isReferenceToVersionExists) {
                throw new ServiceException("Обнаружено использование версии в других точках запроса");
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
                    refBookDao.createRecordVersion(refBookId, recordId, addDayToDate(versionTo, 1), VersionedObjectStatus.FAKE, null);
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
            logger.error(e);
            throw new ServiceLoggerException("Версия не сохранена, обнаружены фатальные ошибки!",
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public void deleteAllRecordVersions(Logger logger, List<Long> uniqueRecordIds) {
        try {
            boolean isReferenceToVersionExists = refBookDao.checkVersionUsages(uniqueRecordIds);
            if (isReferenceToVersionExists) {
                throw new ServiceException("Удаление невозможно, обнаружено использование элемента справочника!");
            }
            RefBook refBook = refBookDao.get(refBookId);
            if (refBook.isHierarchic()) {
                //TODO существует ли версия, период актуальности которой пересекается с периодом актуальности удаляемой версии и для которой удаляемая версия является родительским элементом.
            }
            refBookDao.deleteAllRecordVersions(refBookId, uniqueRecordIds);
        } catch (Exception e) {
            logger.error(e);
            throw new ServiceLoggerException("Элемент справочника не удален, обнаружены фатальные ошибки!",
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds) {
        try {
            boolean isReferenceToVersionExists = refBookDao.checkVersionUsages(uniqueRecordIds);
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
            logger.error(e);
            throw new ServiceLoggerException("Версия элемента справочника не удалена, обнаружены фатальные ошибки!",
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
	public void insertRecords(Date version, List<Map<String, RefBookValue>> records) {
        //refBookDao.createRecordVersion(refBookId, null, version, false ,records);
        throw new UnsupportedOperationException();
	}

	@Override
	public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {
        //refBookDao.updateRecords(refBookId, version, records);
        throw new UnsupportedOperationException();
	}
}
