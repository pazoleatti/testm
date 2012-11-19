package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.server;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryManager;
import com.aplana.sbrf.taxaccounting.model.dictionary.SimpleDictionaryItem;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryPickerDataAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryPickerDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
/**
 * Сервис возвращающий список элементов справочника и общее количество записей 
 * в справочнике.
 * @author Eugene Stetsenko
 *
 */
@Service
public class DictionaryPickerDataHandler extends AbstractActionHandler<DictionaryPickerDataAction, DictionaryPickerDataResult>{
//	@Autowired
//	private StringDictionaryManager dictionaryManager;
	
	@Autowired
	private DictionaryManager<String> dictionaryManager;
	
	public DictionaryPickerDataHandler() {
		super(DictionaryPickerDataAction.class);
	}
	
	@Override
	public DictionaryPickerDataResult execute(DictionaryPickerDataAction action, ExecutionContext context) throws ActionException {
		DictionaryPickerDataResult result = new DictionaryPickerDataResult();
		List<SimpleDictionaryItem<String>> items = dictionaryManager.getDataProvider(action.getDictionaryCode()).getValues(action.getFilter());
		if ( (action.getStart() + action.getOffset()) <= items.size() ) {
			List<SimpleDictionaryItem<String>> newItems = items.subList(action.getStart(), action.getStart() + action.getOffset());
			
			result.setDictionaryItems(new LinkedList<SimpleDictionaryItem<String>>(newItems));
			result.setSize(items.size());
		} else {
			result.setDictionaryItems(items);
			result.setSize(items.size());
		}
		return result;
	}

	@Override
	public void undo(DictionaryPickerDataAction action, DictionaryPickerDataResult result, ExecutionContext context) throws ActionException {
	}
}
