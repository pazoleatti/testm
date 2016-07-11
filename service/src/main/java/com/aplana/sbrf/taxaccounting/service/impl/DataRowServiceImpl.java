package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DataRowServiceImpl implements DataRowService {

	@Autowired
    private LockDataService lockDataService;

	@Autowired
	private DataRowDao dataRowDao;

	@Autowired
	private FormDataDao formDataDao;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    RefBookHelper refBookHelper;

    @Autowired
    FormTemplateService formTemplateService;

    public static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";

	@Override
	public PagingResult<DataRow<Cell>> getDataRows(long formDataId, DataRowRange range, boolean saved, boolean manual) {
		PagingResult<DataRow<Cell>> result = new PagingResult<DataRow<Cell>>();
		FormData formData = formDataDao.get(formDataId, manual);
        result.addAll(saved ? dataRowDao.getRows(formData, range) : dataRowDao.getTempRows(formData, range));
        result.setTotalCount(saved ? dataRowDao.getRowCount(formData) : dataRowDao.getTempRowCount(formData));
        return result;
	}

    @Override
    public List<DataRow<Cell>> getSavedRows(FormData formData) {
        return dataRowDao.getRows(formData, null);
    }

    @Override
	public int getRowCount(long formDataId, boolean saved, boolean manual) {
		FormData fd = formDataDao.get(formDataId, manual);
		return dataRowDao.getRowCount(fd);
	}

	@Override
	@Transactional(readOnly = false)
	public void update(TAUserInfo userInfo, long formDataId, List<DataRow<Cell>> dataRows, boolean manual) {
        checkLockedMe(lockDataService.getLock(formDataService.generateTaskKey(formDataId, ReportType.EDIT_FD)), userInfo.getUser());
		if ((dataRows != null) && (!dataRows.isEmpty())) {
			FormData fd = formDataDao.get(formDataId, manual);
			dataRowDao.updateRows(fd, dataRows);
            formDataDao.updateSorted(fd.getId(), false);
            formDataDao.updateEdited(fd.getId(), true);
		}
	}

    @Override
    @Transactional(readOnly = false)
    public void saveTempRows(FormData formData, List<DataRow<Cell>> dataRows) {
        dataRowDao.saveTempRows(formData, dataRows);
    }

    @Override
    public PagingResult<FormDataSearchResult> searchByKey(Long formDataId, DataRowRange range, String key, boolean isCaseSensitive, boolean manual) {
        PagingResult<FormDataSearchResult> results = new PagingResult<FormDataSearchResult>();
        List<FormDataSearchResult> resultsList = new ArrayList<FormDataSearchResult>();
        FormData formData = formDataDao.get(formDataId, manual);
        boolean existCommonColumn = false; // признак наличия числовых, строковых и/или автонумеруемых граф
        boolean existRefBookColumn = false; // признак наличия справочных граф
        for(Column column: formData.getFormColumns()) {
            if (ColumnType.REFBOOK.equals(column.getColumnType()) || ColumnType.REFERENCE.equals(column.getColumnType()) || ColumnType.DATE.equals(column.getColumnType()))
                existRefBookColumn = true;
            else
                existCommonColumn = true;
        }
        Long index = 0L;
        if (existRefBookColumn) {
            List<DataRow<Cell>> rows = dataRowDao.getRowsRefColumnsOnly(formData, null);
            refBookHelper.dataRowsDereference(new Logger(), rows, formData.getFormColumns());
            String searchKey = key;
            if (!isCaseSensitive) searchKey = searchKey.toUpperCase();
            for (DataRow<Cell> row : rows) {
                for (Column column : formData.getFormColumns()) {
                    if (ColumnType.REFBOOK.equals(column.getColumnType()) || ColumnType.REFERENCE.equals(column.getColumnType())) {
                        Cell valueCell = row.getCell(column.getAlias());
                        if (valueCell != null && valueCell.getRefBookDereference() != null && (isCaseSensitive && valueCell.getRefBookDereference().indexOf(searchKey) >= 0
                                || !isCaseSensitive && valueCell.getRefBookDereference().toUpperCase().indexOf(searchKey) >= 0)) {
                            if ((index++) < range.getCount()) {
                                FormDataSearchResult formDataSearchResult = new FormDataSearchResult();
                                formDataSearchResult.setColumnIndex((long) column.getOrder());
                                formDataSearchResult.setRowIndex(row.getIndex().longValue());
                                formDataSearchResult.setStringFound(valueCell.getRefBookDereference());
                                resultsList.add(formDataSearchResult);
                            }
                        }
                    } else if (ColumnType.DATE.equals(column.getColumnType())) {
                        Cell valueCell = row.getCell(column.getAlias());
                        if (valueCell != null && valueCell.getDateValue() != null) {
                            Formats formats = Formats.getById(((DateColumn) column).getFormatId());
                            SimpleDateFormat df;
                            if (formats.getId() == 0) {
                                df = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
                            } else {
                                df = new SimpleDateFormat(formats.getFormat());
                            }
                            String valueStr = df.format(valueCell.getDateValue());
                            if (isCaseSensitive && valueStr.indexOf(searchKey) >= 0
                                    || !isCaseSensitive && valueStr.toUpperCase().indexOf(searchKey) >= 0) {
                                if ((index++) < range.getCount()) {
                                    FormDataSearchResult formDataSearchResult = new FormDataSearchResult();
                                    formDataSearchResult.setColumnIndex((long) column.getOrder());
                                    formDataSearchResult.setRowIndex(row.getIndex().longValue());
                                    formDataSearchResult.setStringFound(valueStr);
                                    resultsList.add(formDataSearchResult);
                                }
                            }
                        }
                    }
                }
            }
        }
        PagingResult<FormDataSearchResult> daoResults;
        if (existCommonColumn) {
            daoResults = dataRowDao.searchByKey(formDataId, formData.getFormTemplateId(), range, key, isCaseSensitive, manual);
        } else {
            // если нет *обычных*(числовых, строковых, автонумеруемых) граф, то нет смысла проводить поиск
            daoResults = new PagingResult<FormDataSearchResult>();
        }
        results.setTotalCount(daoResults.getTotalCount() + index.intValue());
        resultsList.addAll(daoResults);
        Collections.sort(resultsList);
        index = 1L;
        for(FormDataSearchResult searchResult: resultsList) {
            searchResult.setIndex(index++);
        }
        if (range.getOffset() <= resultsList.size())
            results.addAll(resultsList.subList(range.getOffset() - 1, Math.min(range.getCount(), resultsList.size())));
        return results;
    }

    @Override
    @Transactional(readOnly = false)
    public void copyRows(long formDataSourceId, long formDataDestinationId) {
        dataRowDao.copyRows(formDataSourceId, formDataDestinationId);
        FormData formDataDestination = formDataDao.get(formDataDestinationId, false);
        formDataDao.updateSorted(formDataDestinationId, formDataDestination.isSorted() || formDataDestination.getState().equals(WorkflowState.ACCEPTED));
        formDataDao.updateEdited(formDataDestinationId, true);
    }

    @Override
    public void createCheckPoint(FormData formData) {
        dataRowDao.createCheckPoint(formData);
        formDataDao.backupSorted(formData.getId());
        formDataDao.updateEdited(formData.getId(), false);
    }

	@Override
	public void removeCheckPoint(FormData formData) {
		dataRowDao.removeCheckPoint(formData);
	}

	@Override
	public void restoreCheckPoint(FormData formData) {
		dataRowDao.restoreCheckPoint(formData);
        formDataDao.restoreSorted(formData.getId());
    }

	private void checkLockedMe(LockData lockData, TAUser user){
        if (lockData == null || lockData.getUserId() != user.getId()) {
            throw new ServiceException("Объект не заблокирован текущим пользователем");
        }
    }
}
