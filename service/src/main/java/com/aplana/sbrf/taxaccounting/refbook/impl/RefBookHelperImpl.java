package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookCache;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: avanteev
 */
@Service
@Transactional
public class RefBookHelperImpl implements RefBookHelper {

	@Autowired
	private RefBookFactory refBookFactory;
    @Autowired
	private ColumnDao columnDao;
    @Autowired
    private PeriodService periodService;
    @Autowired
    LogEntryService logEntryService;
    @Autowired
    private ApplicationContext applicationContext;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public void checkReferenceValues(RefBook refBook, Map<RefBookDataProvider, List<RefBookLinkModel>> references,
                                     CHECK_REFERENCES_MODE mode, Logger logger) {
        if (!references.isEmpty()) {
            /**
             * id ссылок на справочники объединяются по провайдеру, который обрабатывает эти справочники +
             * по входной дате окончания для каждой ссылки, которая может быть разной например для случая импорта справочников
             */
            Map<RefBookDataProvider, Map<Date, List<Long>>> idsByProvider = new HashMap<RefBookDataProvider, Map<Date, List<Long>>>();

            //получаем названия колонок
            Map<String, String> aliases = new HashMap<String, String>();
            for (RefBookAttribute attribute : refBook.getAttributes()) {
                aliases.put(attribute.getAlias(), attribute.getName());
            }
            //Собираем ссылки в кучу для каждого провайдера
            for (Map.Entry<RefBookDataProvider, List<RefBookLinkModel>> entry : references.entrySet()) {
                RefBookDataProvider provider = entry.getKey();
                if (!idsByProvider.containsKey(provider)) {
                    idsByProvider.put(provider, new HashMap<Date, List<Long>>());
                }
                for (RefBookLinkModel link : entry.getValue()) {
                    Date versionTo = link.getVersionTo();
                    if (!idsByProvider.get(provider).containsKey(versionTo)) {
                        idsByProvider.get(provider).put(versionTo, new ArrayList<Long>());
                    }
                    idsByProvider.get(provider).get(versionTo).add(link.getReferenceValue());
                }
            }

            int errorCount = 0;
            boolean hasFatalError = false;

            //Проверяем ссылки отдельно по каждому провайдеру
            for (Map.Entry<RefBookDataProvider, List<RefBookLinkModel>> entry : references.entrySet()) {
                RefBookDataProvider provider = entry.getKey();
                Map<Date, List<Long>> idsByVersionEnd = idsByProvider.get(provider);
                List<RefBookLinkModel> links = entry.getValue();
                if (links != null && links.size() > 0 && idsByVersionEnd.size() > 0) {
                    //Дата начала для проверки - можно взять у любой записи, т.к она всегда одинаковая у всех
                    Date versionFrom = links.get(0).getVersionFrom();

                    //Обрабатываем группы записей с одним и тем же провайдером и датой окончания
                    for (Map.Entry<Date, List<Long>> ids : idsByVersionEnd.entrySet()) {
                        Date versionTo = ids.getKey();
                        List<ReferenceCheckResult> inactiveRecords = provider.getInactiveRecordsInPeriod(ids.getValue(), versionFrom, versionTo);
                        errorCount += inactiveRecords.size();
                        if (!inactiveRecords.isEmpty()) {
                            for (ReferenceCheckResult inactiveRecord : inactiveRecords) {
                                //ищем информацию по плохим записям и формируем сообщение по каждой
                                for (RefBookLinkModel link : links) {
                                    if (inactiveRecord.getRecordId().equals(link.getReferenceValue())) {
                                        switch (inactiveRecord.getResult()) {
                                            case NOT_EXISTS: {
                                                String msg = buildMsg("Поле \"%s\" содержит ссылку на несуществующую версию записи справочника!", link);
                                                if (mode == CHECK_REFERENCES_MODE.REFBOOK) {
                                                    hasFatalError = true;
                                                    logger.error(msg,
                                                            link.getIndex() != null ? link.getIndex() :
                                                                    link.getSpecialId() != null ? link.getSpecialId() : "",
                                                            aliases.get(link.getAttributeAlias()));
                                                } else {
                                                    logger.warn(msg,
                                                            link.getIndex() != null ? link.getIndex() :
                                                                    link.getSpecialId() != null ? link.getSpecialId() : "",
                                                            aliases.get(link.getAttributeAlias()));
                                                }
                                                break;
                                            }
                                            case NOT_CROSS: {
                                                String msg;
                                                if (mode == CHECK_REFERENCES_MODE.REFBOOK) {
                                                    msg = buildMsg("\"%s\": Период актуальности выбранного значения не пересекается с периодом актуальности версии!", link);
                                                    hasFatalError = true;
                                                    logger.error(msg,
                                                            link.getIndex() != null ? link.getIndex() :
                                                                    link.getSpecialId() != null ? link.getSpecialId() : "",
                                                            aliases.get(link.getAttributeAlias())
                                                    );
                                                } else {
                                                    msg = buildMsg("Поле \"%s\" содержит ссылку на версию записи справочника, период которой (с %s по %s) не пересекается с отчетным периодом настроек (с %s по %s)!", link);
                                                    logger.warn(msg,
                                                            link.getIndex() != null ? link.getIndex() :
                                                                    link.getSpecialId() != null ? link.getSpecialId() : "",
                                                            aliases.get(link.getAttributeAlias()),
                                                            sdf.get().format(inactiveRecord.getVersionFrom()),
                                                            inactiveRecord.getVersionTo() != null ? sdf.get().format(inactiveRecord.getVersionTo()) : "-",
                                                            sdf.get().format(link.getVersionFrom()),
                                                            link.getVersionTo() != null ? sdf.get().format(link.getVersionTo()) : "-"
                                                    );
                                                }
                                                break;
                                            }
                                            case NOT_LAST: {
                                                String msg;
                                                if (mode == CHECK_REFERENCES_MODE.REFBOOK) {
                                                    msg = buildMsg("\"%s\": Выбранная версия записи справочника не является последней действующей в периоде сохраняемой версии (с %s по %s)!", link);
                                                    logger.warn(msg,
                                                            link.getIndex() != null ? link.getIndex() :
                                                                    link.getSpecialId() != null ? link.getSpecialId() : "",
                                                            aliases.get(link.getAttributeAlias()),
                                                            sdf.get().format(versionFrom),
                                                            versionTo != null ? sdf.get().format(versionTo) : "-"
                                                    );
                                                } else {
                                                    msg = buildMsg("Поле \"%s\" содержит ссылку на версию записи справочника, которая не является последней действующей в отчетном периоде настроек (с %s по %s)!", link);
                                                    logger.warn(msg,
                                                            link.getIndex() != null ? link.getIndex() :
                                                                    link.getSpecialId() != null ? link.getSpecialId() : "",
                                                            aliases.get(link.getAttributeAlias()),
                                                            sdf.get().format(versionFrom),
                                                            versionTo != null ? sdf.get().format(versionTo) : "-"
                                                    );
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (hasFatalError) {
                //Исключение выбрасывается только для справочников и тут не важно что писать, т.к текст заменяется в вызывающем коде
                throw new ServiceException("Поля содержат некорректные справочные ссылки!");
            }

            if (errorCount > 0) {
                //Устанавливаем сообщение об ошибке для диалога в настройках подразделений
                switch (mode) {
                    case DEPARTMENT_CONFIG:
                        logger.setMainMsg(String.format("Поля настроек содержат некорректные справочные ссылки (%d). Необходимо их актуализировать", errorCount));
                }
            }
        }
    }

    private String buildMsg(String msg, RefBookLinkModel link) {
        if (link.getIndex() != null) {
            msg = "Строка %s: " + msg;
        } else if (link.getSpecialId() != null) {
            //Если проверка выполняется для нескольких записей справочника (например при импорте справочника), то формируем специальное имя для каждой записи
            msg = "Запись \"%s\", " + msg;
        } else {
            msg = "%s" + msg;
        }
        return msg;
    }

    @Override
    public void dataRowsCheck(Collection<DataRow<Cell>> dataRows, List<Column> columns) {
        Map<Long, Pair<RefBookDataProvider, RefBookAttribute>> providers = new HashMap<Long, Pair<RefBookDataProvider, RefBookAttribute>>();
        try {
            for (DataRow<Cell> dataRow : dataRows) {
                for (Map.Entry<String, Object> entry : dataRow.entrySet()) {
                    Cell cell = ((DataRow.MapEntry<Cell>) entry).getCell();
                    Object value = cell.getValue();
                    if (ColumnType.REFBOOK.equals(cell.getColumn().getColumnType()) && value != null) {
                        // Разыменование справочных ячеек
                        RefBookColumn column = (RefBookColumn) cell.getColumn();
                        Long refAttributeId = column.getRefBookAttributeId();
                        checkRefBookValue(providers, refAttributeId, value);
                    } else if (ColumnType.REFERENCE.equals(cell.getColumn().getColumnType())) {
                        // Разыменование ссылочных ячеек
                        ReferenceColumn column = (ReferenceColumn) cell.getColumn();
                        Cell parentCell = dataRow.getCellByColumnId(column.getParentId());
                        value = parentCell.getValue();
                        if (value != null) {
                            checkFormDataReferenceValues(providers, column, value);
                        }
                    }
                }
            }
        } catch (DaoException e) {
            throw new ServiceException("Данные не могут быть сохранены, так как часть выбранных справочных значений была удалена. Отредактируйте таблицу и попытайтесь сохранить заново", e);
        }
    }


    private void checkFormDataReferenceValues(Map<Long, Pair<RefBookDataProvider, RefBookAttribute>> providers,
                                              ReferenceColumn referenceColumn, Object value){
        Long refAttributeId = referenceColumn.getRefBookAttributeId();
        Pair<RefBookDataProvider, RefBookAttribute> pair = providers.get(refAttributeId);
        if (pair == null) {
            RefBook refBook = refBookFactory.getByAttribute(refAttributeId);
            RefBookAttribute attribute = refBook.getAttribute(refAttributeId);
            RefBookDataProvider provider = refBookFactory.getDataProvider(refBook.getId());
            pair = new Pair<RefBookDataProvider, RefBookAttribute>(provider, attribute);
            providers.put(refAttributeId, pair);
        }
        Map<String, RefBookValue> record = pair.getFirst().getRecordData((Long) value);
        RefBookValue refBookValue = record.get(pair.getSecond().getAlias());
        // Если найденое значение ссылка, то делаем разыменование для 2 уровня
        if ((refBookValue.getReferenceValue()!=null)&&(refBookValue.getAttributeType()==RefBookAttributeType.REFERENCE)&&(referenceColumn.getRefBookAttributeId2()!=null)){
            refAttributeId = referenceColumn.getRefBookAttributeId2();
            RefBook refBook = refBookFactory.getByAttribute(refAttributeId);
            RefBookAttribute attribute = refBook.getAttribute(refAttributeId);
            RefBookDataProvider provider = refBookFactory.getDataProvider(refBook.getId());
            pair = new Pair<RefBookDataProvider, RefBookAttribute>(provider, attribute);
            providers.put(refAttributeId, pair);
            pair.getFirst().getRecordData(refBookValue.getReferenceValue());
        }
    }


    private void checkRefBookValue(Map<Long, Pair<RefBookDataProvider, RefBookAttribute>> providers,
                                         Long refAttributeId, Object value) {
        Pair<RefBookDataProvider, RefBookAttribute> pair = providers.get(refAttributeId);
        if (pair == null) {
            RefBook refBook = refBookFactory.getByAttribute(refAttributeId);
            RefBookAttribute attribute = refBook.getAttribute(refAttributeId);
            RefBookDataProvider provider = refBookFactory.getDataProvider(refBook.getId());
            pair = new Pair<RefBookDataProvider, RefBookAttribute>(provider, attribute);
            providers.put(refAttributeId, pair);
        }
        pair.getFirst().getRecordData((Long) value);
    }

	/**
	 * Ищет в списке графу по идентификатору
	 * @param columnId идентификатор графы
	 * @param columns список граф
	 * @return
	 */
	private Column getColumnById(int columnId, List<Column> columns) {
		for (Column column : columns) {
			if (column.getId().equals(columnId)) {
				return column;
			}
		}
		throw new IllegalArgumentException("Attribute not found");
	}

	/**
	 * Групповое разыменование ссылок
	 *
	 * @param column графа для установки разыменованных значений
	 * @param parentColumn родительская графа, актуальна для зависимых граф для первого уровня разыменовывания
	 * @param attributeId атрибут справочника по которому хотим разыменовать ссылки
	 * @param dataRows куда будем записывать итоговый результат
	 * @param recordIds список ссылок для разыменования
	 * @param isLevel2 признак разименования второго уровня (справочной или зависимой ячейки)
	 */
	private void dereference(Column column, Column parentColumn, Long attributeId, Collection<DataRow<Cell>> dataRows,
                             Set<Long> recordIds, boolean isLevel2) {
		if (recordIds.isEmpty()) {
			return;
		}
		// получение данных по ссылкам
        RefBook refBook = refBookFactory.getByAttribute(attributeId);
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBook.getId());
		Map<Long, RefBookValue> values = provider.dereferenceValues(attributeId, recordIds);
		// псевдоним для получения значений ссылки
		String valueAlias = parentColumn == null ? column.getAlias() : parentColumn.getAlias();
		// установка разыменованных значений
		for (DataRow<Cell> dataRow : dataRows) {
			Cell cell = dataRow.getCell(column.getAlias());
			Cell valueCell = dataRow.getCell(valueAlias);
			RefBookValue refBookValue = null;
			if (!isLevel2) {
				BigDecimal reference = valueCell.getNumericValue();
				if (reference != null) {
					refBookValue = values.get(reference.longValue());
				}
			} else { // случай для разыменования второго уровня
				String reference = valueCell.getRefBookDereference();
				if (reference != null && !reference.isEmpty()) {
					refBookValue = values.get(Long.valueOf(reference));
				}
			}
            if (refBookValue != null && RefBookAttributeType.DATE.equals(refBookValue.getAttributeType()) && refBookValue.getDateValue() != null) {
                RefBookAttribute attribute = refBook.getAttribute(attributeId);
                cell.setRefBookDereference(attribute.getFormat() != null ?
                        (new SimpleDateFormat(attribute.getFormat().getFormat())).format(refBookValue.getDateValue()) :
                        String.valueOf(refBookValue));
                cell.setRefBookValue(refBookValue);
            } else {
                cell.setRefBookDereference(refBookValue == null ? "" : String.valueOf(refBookValue));
                cell.setRefBookValue(refBookValue);
            }
		}
	}

	@Override
    public void dataRowsDereference(Logger logger, Collection<DataRow<Cell>> dataRows, List<Column> columns) {
		if (dataRows.isEmpty() || columns.isEmpty()) {
			return;
		}
		for (Column column : columns) {
			if (ColumnType.REFBOOK.equals(column.getColumnType()) || ColumnType.REFERENCE.equals(column.getColumnType())) {
				Column parentColumn = null;
				// псевдоним для получения значений ссылки
				String valueAlias;
				if (ColumnType.REFBOOK.equals(column.getColumnType())) {
					valueAlias = column.getAlias();
				} else {
					ReferenceColumn refColumn = ((ReferenceColumn) column);
					parentColumn = getColumnById(refColumn.getParentId(), columns);
					valueAlias = parentColumn.getAlias();
				}
				// сбор всех ссылок
				Set<Long> recordIds = new HashSet<Long>();
				for (DataRow<Cell> dataRow : dataRows) {
					Object value = dataRow.get(valueAlias);
					if (value != null) {
						recordIds.add((Long) value);
					}
				}
				if (!recordIds.isEmpty()) {
					// установка значений
					Long attributeId = ColumnType.REFBOOK.equals(column.getColumnType()) ?
						((RefBookColumn) column).getRefBookAttributeId() :
						((ReferenceColumn) column).getRefBookAttributeId();
					dereference(column, parentColumn, attributeId, dataRows, recordIds, false);
				}
			}
		}

        // разыменовывание ссылок второго уровня
        for (Column column : columns) {
            if (ColumnType.REFBOOK.equals(column.getColumnType()) || ColumnType.REFERENCE.equals(column.getColumnType())) {
                Long refBookAttributeId2 = (ColumnType.REFBOOK.equals(column.getColumnType()) ?
                        ((RefBookColumn) column).getRefBookAttributeId2() :
                        ((ReferenceColumn) column).getRefBookAttributeId2());
                if (refBookAttributeId2 != null) {
                    // сбор всех ссылок на справочники по графе
                    Set<Long> recordIds = new HashSet<Long>();
                    for (DataRow<Cell> dataRow : dataRows) {
                        Cell cell = dataRow.getCell(column.getAlias());
                        String value = cell.getRefBookDereference();
                        if (value != null && !value.isEmpty()) {
                            recordIds.add(Long.valueOf(value));
                        }
                    }
                    if (!recordIds.isEmpty()) {
                        // установка значений
                        dereference(column, null, refBookAttributeId2, dataRows, recordIds, true);
                    }
                }
            }
        }
	}

    @Override
    public Map<Long, List<Long>> getAttrToListAttrId2Map(List<RefBookAttribute> attributes){
        return columnDao.getAttributeId2(attributes);
    }

    @Override
    public Map<Long, RefBookDataProvider> getHashedProviders(List<RefBookAttribute> attributes, Map<Long, List<Long>> attrId2Map){
        //кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
        Map<Long, RefBookDataProvider> refProviders = new HashMap<Long, RefBookDataProvider>();
        for (RefBookAttribute attribute : attributes) {
            if (RefBookAttributeType.REFERENCE.equals(attribute.getAttributeType())) {
                if (!refProviders.containsKey(attribute.getId())) {
                    refProviders.put(attribute.getId(), refBookFactory.getDataProvider(attribute.getRefBookId()));
                }
                // проверяем если на этот атрибут для колонок дополнительные отрибуты и кешируем продайдеров
                List<Long> id2s = attrId2Map.get(attribute.getId());
                if (id2s != null) {
                    for (Long id2 : id2s) {
                        if (!refProviders.containsKey(id2)) {
                            refProviders.put(id2, refBookFactory.getDataProvider(refBookFactory.getByAttribute(id2).getId()));
                        }
                    }
                }
            }
        }
        return refProviders;
    }

    @Override
    public RefBookRecordVersion saveOrUpdateDepartmentConfig(Long uniqueRecordId, long refBookId, long slaveRefBookId,
                                                      int reportPeriodId, String departmentAlias, long departmentId,
                                                      Map<String, RefBookValue> mainConfig,
                                                      List<Map<String, RefBookValue>> tablePart,
                                                      Logger logger) {
        RefBook slaveRefBook = refBookFactory.get(slaveRefBookId);
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        RefBookDataProvider providerSlave = refBookFactory.getDataProvider(slaveRefBookId);
        ReportPeriod rp = periodService.getReportPeriod(reportPeriodId);
        String filter = departmentAlias + " = " + departmentId;

        boolean needEdit = false;
        RefBookRecord record = new RefBookRecord();

        // Поиск версий настроек для указанного подразделения. Если они есть - создаем новую версию с существующим record_id, иначе создаем новый record_id (по сути элемент справочника)
        List<Pair<Long, Long>> recordPairsExistence = provider.checkRecordExistence(null, filter);
        if (!recordPairsExistence.isEmpty()) {
            //Проверяем, к одному ли элементу относятся версии
            Set<Long> recordIdSet = new HashSet<Long>();
            for (Pair<Long, Long> pair : recordPairsExistence) {
                recordIdSet.add(pair.getSecond());
            }

            if (recordIdSet.size() > 1) {
                throw new ServiceException("Версии настроек, отобраные по фильтру, относятся к разным подразделениям");
            }

            // Существуют версии настроек для указанного подразделения
            record.setRecordId(recordPairsExistence.get(0).getSecond());
        }

        // Проверяем, нужно ли обновление существующих настроек
        List<Pair<Long, Long>> recordPairs = provider.checkRecordExistence(rp.getCalendarStartDate(), filter);
        if (!recordPairs.isEmpty()) {
            needEdit = true;
            // Запись нашлась
            if (recordPairs.size() != 1) {
                throw new ServiceException("Найдено несколько настроек для подразделения ");
            }
        }

        mainConfig.put(departmentAlias, new RefBookValue(RefBookAttributeType.REFERENCE, departmentId));

        RefBookRecordVersion recordVersion;
        Date versionTo = provider.getEndVersion(record.getRecordId(), rp.getCalendarStartDate());
        if (!needEdit) {
            record.setValues(mainConfig);
            uniqueRecordId = provider.createRecordVersion(logger, rp.getCalendarStartDate(), versionTo, Arrays.asList(record)).get(0);
        } else {
            provider.updateRecordVersion(logger, uniqueRecordId, rp.getCalendarStartDate(), versionTo, mainConfig);
        }
        recordVersion = provider.getRecordVersionInfo(uniqueRecordId);

        /** Сохраняем табличную часть */
        String filterSlave = "LINK = " + uniqueRecordId;
        RefBookAttribute sortAttr = slaveRefBook.getAttribute("ROW_ORD");

        PagingResult<Map<String, RefBookValue>> paramsSlave = providerSlave.getRecords(rp.getCalendarStartDate(), null, filterSlave, sortAttr);

        Set<Map<String, RefBookValue>> toUpdate = new HashSet<Map<String, RefBookValue>>();
        Set<Map<String, RefBookValue>> toAdd = new HashSet<Map<String, RefBookValue>>();
        Set<Map<String, RefBookValue>> toDelete = new HashSet<Map<String, RefBookValue>>();

        int maxRowOrd = 0;
        for (Map<String, RefBookValue> rowFromClient : tablePart) {
            boolean contains = false;
            for (Map<String, RefBookValue> rowFromServer : paramsSlave) {
                if (rowFromClient.get("TAX_ORGAN_CODE").getStringValue().equals(rowFromServer.get("TAX_ORGAN_CODE").getStringValue())
                        && rowFromClient.get("KPP").getStringValue().equals(rowFromServer.get("KPP").getStringValue())) {
                    contains = true;
                    rowFromClient.put("LINK",new RefBookValue(RefBookAttributeType.REFERENCE, uniqueRecordId));
                    rowFromClient.put("ROW_ORD",rowFromServer.get("ROW_ORD"));
                    rowFromClient.put("record_id",rowFromServer.get("record_id"));
                    rowFromClient.put("DEPARTMENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, departmentId));
                    toUpdate.add(rowFromClient);
                    break;
                }
            }
            if (rowFromClient.containsKey("ROW_ORD") && rowFromClient.get("ROW_ORD") != null) {
                int rowOrd = rowFromClient.get("ROW_ORD").getNumberValue().intValue();
                if (rowOrd > maxRowOrd) {
                    maxRowOrd = rowOrd;
                }
            }
            if (!contains) {
                rowFromClient.put("LINK", new RefBookValue(RefBookAttributeType.REFERENCE, uniqueRecordId));
                rowFromClient.put("ROW_ORD", new RefBookValue(RefBookAttributeType.NUMBER, ++maxRowOrd));
                rowFromClient.put("DEPARTMENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, departmentId));
                toAdd.add(rowFromClient);
            }
        }

        List<RefBookRecord> recordsToAdd = new ArrayList<RefBookRecord>();
        for (Map<String, RefBookValue> add : toAdd) {
            RefBookRecord slaveRecord = new RefBookRecord();
            slaveRecord.setValues(add);
            slaveRecord.setRecordId(null);
            recordsToAdd.add(slaveRecord);
        }


        for (Map<String, RefBookValue> rowFromServer : paramsSlave) {
            boolean notFound = true;
            for (Map<String, RefBookValue> rowFromClient : tablePart) {
                if (rowFromClient.get("TAX_ORGAN_CODE").getStringValue().equals(rowFromServer.get("TAX_ORGAN_CODE").getStringValue())
                        && rowFromClient.get("KPP").getStringValue().equals(rowFromServer.get("KPP").getStringValue())) {
                    notFound = false;
                    break;
                }
            }
            if (notFound) {
                toDelete.add(rowFromServer);
            }
        }

        List<Long> deleteIds = new ArrayList<Long>();
        for (Map<String, RefBookValue> del : toDelete) {
            deleteIds.add(del.get("record_id").getNumberValue().longValue());
        }

        if (!logger.containsLevel(LogLevel.ERROR)) {
            if (!recordsToAdd.isEmpty()) {
                providerSlave.createRecordVersion(logger, rp.getCalendarStartDate(), recordVersion.getVersionEnd(), recordsToAdd);
            }

            for (Map<String, RefBookValue> up : toUpdate) {
                providerSlave.updateRecordVersion(logger, up.get("record_id").getNumberValue().longValue(), rp.getCalendarStartDate(), recordVersion.getVersionEnd(), up);
            }

            if (!deleteIds.isEmpty()) {
                providerSlave.deleteRecordVersions(logger, deleteIds);
            }
        }
        return recordVersion;
    }

    @Override
    public Map<Long, Map<Long, String>> dereferenceValues(RefBook refBook, List<Map<String, RefBookValue>> refBookPage, boolean includeAttrId2) {
        final RefBookCache refBookCacher = (RefBookCache) applicationContext.getBean(RefBookCache.class);
        Map<Long, Map<Long, String>> dereferenceValues = new HashMap<Long, Map<Long, String>>(); // Map<attrId, Map<referenceId, value>>
        if (refBookPage.isEmpty()) {
            return dereferenceValues;
        }
        List<RefBookAttribute> attributes = refBook.getAttributes();
        // кэшируем список дополнительных атрибутов(если есть) для каждого атрибута
        Map<Long, List<Long>> attrId2Map = getAttrToListAttrId2Map(attributes);
        Map<Long, Map<Long, Set<Long>>> attributesMap = new HashMap<Long, Map<Long, Set<Long>>>();

        // разыменовывание ссылок
        for (RefBookAttribute attribute : attributes) {
            if (RefBookAttributeType.REFERENCE.equals(attribute.getAttributeType())) {
                // сбор всех ссылок
                Long id = attribute.getId();
                String alias = attribute.getAlias();
                Set<Long> recordIds = new HashSet<Long>();
                for (Map<String, RefBookValue> record : refBookPage) {
                    RefBookValue value = record.get(alias);
                    if (value != null && !value.isEmpty()) {
                        recordIds.add(value.getReferenceValue());
                        if (includeAttrId2) {
                            //Получаем связки для атрибутов второго уровня
                            if (attrId2Map.get(id) != null) {
                                for (Long id2 : attrId2Map.get(id)) {
                                    Long refBookId2 = refBookCacher.getByAttribute(id2).getId();
                                    //attributeIds.add(id2);
                                    if (!attributesMap.containsKey(refBookId2)) {
                                        attributesMap.put(refBookId2, new HashMap<Long, Set<Long>>());
                                    }
                                    if (!attributesMap.get(refBookId2).containsKey(id2))
                                        attributesMap.get(refBookId2).put(id2, new HashSet<Long>());
                                    attributesMap.get(refBookId2).get(id2).add(value.getReferenceValue());
                                }
                            }
                        }
                    }
                }
                // групповое разыменование, если есть что разыменовывать
                if (!recordIds.isEmpty()) {
                    RefBookDataProvider provider = refBookCacher.getDataProvider(attribute.getRefBookId());
                    Map<Long, RefBookValue> values = provider.dereferenceValues(attribute.getRefBookAttributeId(), recordIds);
                    if (values != null && !values.isEmpty()) {
                        Map<Long, String> stringValues = new HashMap<Long, String>();
                        for (Map.Entry<Long, RefBookValue> entry : values.entrySet()) {
                            stringValues.put(entry.getKey(), String.valueOf(entry.getValue()));
                        }
                        dereferenceValues.put(attribute.getId(), stringValues);
                    }
                }
                if (includeAttrId2) {
                    for (Map.Entry<Long, Map<Long, Set<Long>>> entry : attributesMap.entrySet()) {
                        RefBookDataProvider provider = refBookCacher.getDataProvider(entry.getKey());
                        for (Map.Entry<Long, Set<Long>> entryAttr : entry.getValue().entrySet()) {
                            if (entryAttr.getValue() != null && !entryAttr.getValue().isEmpty()) {
                                Map<Long, RefBookValue> values = provider.dereferenceValues(entryAttr.getKey(), entryAttr.getValue());
                                if (values != null && !values.isEmpty()) {
                                    Map<Long, String> stringValues = new HashMap<Long, String>();
                                    for (Map.Entry<Long, RefBookValue> entry2 : values.entrySet()) {
                                        stringValues.put(entry2.getKey(), String.valueOf(entry2.getValue()));
                                    }
                                    dereferenceValues.put(entryAttr.getKey(), stringValues);
                                }
                            }
                        }
                    }
                }
            }
        }
        return dereferenceValues;
    }

    @Override
    public Map<Long, Pair<RefBookAttribute, Map<Long, RefBookValue>>> dereferenceValuesAttributes(RefBook refBook, List<Map<String, RefBookValue>> refBookPage) {
        Map<Long, Pair<RefBookAttribute, Map<Long, RefBookValue>>> dereferenceValues = new HashMap<Long, Pair<RefBookAttribute,Map<Long, RefBookValue>>>(); // Map<attrId, Map<referenceId, value>>
        if (refBookPage.isEmpty()) {
            return dereferenceValues;
        }
        List<RefBookAttribute> attributes = refBook.getAttributes();
        // разыменовывание ссылок
        for (RefBookAttribute attribute : attributes) {
            if (RefBookAttributeType.REFERENCE.equals(attribute.getAttributeType())) {
                // сбор всех ссылок
                String alias = attribute.getAlias();
                Set<Long> recordIds = new HashSet<Long>();
                for (Map<String, RefBookValue> record : refBookPage) {
                    RefBookValue value = record.get(alias);
                    if (value != null && !value.isEmpty()) {
                        recordIds.add(value.getReferenceValue());
                    }
                }
                // групповое разыменование, если есть что разыменовывать
                if (!recordIds.isEmpty()) {
                    RefBookDataProvider provider = refBookFactory.getDataProvider(attribute.getRefBookId());
                    Map<Long, RefBookValue> values = provider.dereferenceValues(attribute.getRefBookAttributeId(), recordIds);
                    Map<Long, RefBookValue> refBookValues = new HashMap<Long, RefBookValue>();
                    RefBookAttribute referenceAttribute = refBookFactory.get(attribute.getRefBookId()).getAttribute(attribute.getRefBookAttributeId());
                    if (values != null && !values.isEmpty()) {
                        for (Map.Entry<Long, RefBookValue> entry : values.entrySet()) {
                            refBookValues.put(entry.getKey(), entry.getValue());
                        }
                    }
                    dereferenceValues.put(attribute.getId(), new Pair<RefBookAttribute, Map<Long, RefBookValue>>(referenceAttribute, refBookValues));
                } else {
                    RefBookAttribute referenceAttribute = refBookFactory.get(attribute.getRefBookId()).getAttribute(attribute.getRefBookAttributeId());
                    dereferenceValues.put(attribute.getId(), new Pair<RefBookAttribute, Map<Long, RefBookValue>>(referenceAttribute, new HashMap<Long, RefBookValue>()));
                }
            }
        }
        return dereferenceValues;
    }

    public String dereferenceValue(long recordId, Long attributeId) {
        RefBook refBook = refBookFactory.getByAttribute(attributeId);
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBook.getId());
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        Map<String, RefBookValue> refBookValueMap = refBookDataProvider.getRecordData(recordId);
        Map<Long, Map<Long, String>> dereferenceValues = null;
        if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
            dereferenceValues = dereferenceValues(refBook, Arrays.asList(refBookValueMap), true);
        }
        return dereferenceValue(refBookValueMap, dereferenceValues, attribute);
    }

    private String dereferenceValue(Map<String, RefBookValue> record, Map<Long, Map<Long, String>> dereferenceValues, RefBookAttribute attribute) {
        RefBookValue value = record.get(attribute.getAlias());
        String dereferenceValue;
        if (value == null) {
            dereferenceValue = "";
        } else {
            switch (value.getAttributeType()) {
                case NUMBER:
                    if (value.getNumberValue() == null) dereferenceValue = "";
                    else dereferenceValue = value.getNumberValue().toString();
                    break;
                case DATE:
                    if (value.getDateValue() == null) dereferenceValue = "";
                    else {
                        if (attribute.getFormat() != null) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                                    attribute.getFormat().getFormat());
                            dereferenceValue = simpleDateFormat.format(value.getDateValue());
                        } else {
                            dereferenceValue = value.getDateValue().toString();
                        }
                    }
                    break;
                case STRING:
                    if (value.getStringValue() == null) dereferenceValue = "";
                    else dereferenceValue = value.getStringValue();
                    break;
                case REFERENCE:
                    if (value.getReferenceValue() == null) dereferenceValue = "";
                    else {
                        dereferenceValue = dereferenceValues.get(attribute.getId()).get(value.getReferenceValue());
                    }
                    break;
                default:
                    dereferenceValue = "undefined";
                    break;
            }
        }
        return dereferenceValue;
    }
}
