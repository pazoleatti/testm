package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookTableDataResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerUtils;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("isAuthenticated()")
public class GetRefBookDataRowHandler extends AbstractActionHandler<GetRefBookTableDataAction, GetRefBookTableDataResult> {

	@Autowired
	RefBookFactory refBookFactory;
    @Autowired
    SecurityService securityService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    LogEntryService logEntryService;

	public GetRefBookDataRowHandler() {
		super(GetRefBookTableDataAction.class);
	}

	@Override
	public GetRefBookTableDataResult execute(GetRefBookTableDataAction action, ExecutionContext executionContext) throws ActionException {

		RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());

		GetRefBookTableDataResult result = new GetRefBookTableDataResult();
		RefBook refBook = refBookFactory.get(action.getRefBookId());
		result.setTableHeaders(refBook.getAttributes());
		result.setDesc(refBook.getName());
		if (action.getPagingParams() != null) {//TODO перенести в отдельный хэндлер
            TAUser currentUser = securityService.currentUserInfo().getUser();
            String filter = null;
            if (refBook.getRegionAttribute() != null && !currentUser.hasRole("ROLE_CONTROL_UNP")) {
                List<Department> deps = departmentService.getBADepartments(securityService.currentUserInfo().getUser());
                filter = RefBookPickerUtils.buildRegionFilterForUser(deps, refBook);
                if (filter != null && filter.equals(RefBookPickerUtils.NO_REGION_MATCHES_FLAG)) {
                    //Среди подразделений пользователя нет относящихся к какому то региону и нет смысла получать записи справочника - ни одна не должна быть ему доступна
                    result.setTotalCount(0);
                    result.setDataRows(new ArrayList<RefBookDataRow>());
                    return result;
                }
            }

            String searchPattern = action.getSearchPattern();
            if (searchPattern != null && !searchPattern.isEmpty()){
                if (filter != null && filter.length() > 0){
                    filter += " and ("+refBookFactory.getSearchQueryStatement(searchPattern, refBook.getId())+")";
                } else{
                    filter = refBookFactory.getSearchQueryStatement(searchPattern, refBook.getId());
                }
            }

            if (action.getRecordId() != null) {
                Long rowNum = refBookDataProvider
                        .getRowNum(action.getRelevanceDate(), action.getRecordId(), filter, refBook.getAttributes().get(0), true);
                result.setRowNum(rowNum);

                if (rowNum != null) {
                    rowNum = rowNum - 1;
                    int countOfRecords = action.getPagingParams().getCount();
                    int startIndex = action.getPagingParams().getStartIndex();
                    int page = ((int)(rowNum/countOfRecords));
                    if (((int)startIndex/countOfRecords) != page) {
                        return result;
                    }
                }
            }
			PagingResult<Map<String, RefBookValue>> refBookPage = refBookDataProvider
					.getRecords(action.getRelevanceDate(), action.getPagingParams(), filter, refBook.getAttributes().get(0));
			List<RefBookDataRow> rows = new ArrayList<RefBookDataRow>();

			//кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
			Map<String, RefBookDataProvider> refProviders = new HashMap<String, RefBookDataProvider>();
			Map<String, String> refAliases = new HashMap<String, String>();
			for (RefBookAttribute attribute : refBook.getAttributes()) {
				if (attribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
					refProviders.put(attribute.getAlias(), refBookFactory.getDataProvider(attribute.getRefBookId()));
					RefBook refRefBook = refBookFactory.get(attribute.getRefBookId());
					RefBookAttribute refAttribute = refRefBook.getAttribute(attribute.getRefBookAttributeId());
					refAliases.put(attribute.getAlias(), refAttribute.getAlias());
				}
			}

			for (Map<String, RefBookValue> record : refBookPage) {
				Map<String, String> tableRowData = new HashMap<String, String>();
				for (RefBookAttribute attribute : refBook.getAttributes()) {
					RefBookValue value = record.get(attribute.getAlias());
					String tableCell;
					if (value == null) {
						tableCell = "";
					} else {
						switch (value.getAttributeType()) {
							case NUMBER:
								if (value.getNumberValue() == null) tableCell = "";
								else tableCell = value.getNumberValue().toString();
								break;
							case DATE:
								if (value.getDateValue() == null) tableCell = "";
								else {
                                    if (attribute.getFormat()!=null){
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(attribute.getFormat().getFormat());
                                        tableCell = simpleDateFormat.format(value.getDateValue());
                                    }
                                    else {
                                        tableCell = value.getDateValue().toString();
                                    }
                                }
								break;
							case STRING:
								if (value.getStringValue() == null) tableCell = "";
								else tableCell = value.getStringValue();
								break;
							case REFERENCE:
								if (value.getReferenceValue() == null) tableCell = "";
								else  {
									Map<String, RefBookValue> refValue = refProviders.get(attribute.getAlias()).getRecordData(value.getReferenceValue());
									tableCell = refValue.get(refAliases.get(attribute.getAlias())).toString();
								}
								break;
							default:
								tableCell = "undefined";
								break;
						}
					}
					tableRowData.put(attribute.getAlias(), tableCell);
				}
				RefBookDataRow tableRow = new RefBookDataRow();
				tableRow.setValues(tableRowData);
				tableRow.setRefBookRowId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
				rows.add(tableRow);

			}
			result.setTotalCount(refBookPage.getTotalCount());

			result.setDataRows(rows);
		}
		return result;
	}

	@Override
	public void undo(GetRefBookTableDataAction getRefBookDataRowAction, GetRefBookTableDataResult getRefBookDataRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
