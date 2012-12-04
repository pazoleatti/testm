package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.server;

import java.util.LinkedList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.StringFilterDictionaryDataProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryManager;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryPickerDataAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryPickerDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * Сервис возвращающий список элементов справочника и общее количество записей
 * в справочнике.
 *
 * @author Eugene Stetsenko
 */
@Service
public class DictionaryPickerDataHandler extends AbstractActionHandler<DictionaryPickerDataAction, DictionaryPickerDataResult> {
	private final Log log = LogFactory.getLog(DictionaryPickerDataHandler.class);

	@Autowired
	private DictionaryManager dictionaryManager;

	public DictionaryPickerDataHandler() {
		super(DictionaryPickerDataAction.class);
	}

	@Override
	public DictionaryPickerDataResult execute(DictionaryPickerDataAction action, ExecutionContext context) throws ActionException {
		// TODO: remove logging
		try{
			log.error("1");
			DictionaryDataProvider dictionaryDataProvider = dictionaryManager.getDataProvider(action.getDictionaryCode());
			log.error("2");
		List<DictionaryItem> items;
		if (action.getFilter() != null && !action.getFilter().isEmpty() && dictionaryDataProvider instanceof StringFilterDictionaryDataProvider) {
			items = ((StringFilterDictionaryDataProvider) dictionaryDataProvider).getValues(action.getFilter());
		} else {
			items = dictionaryDataProvider.getValues();
		}

			log.error("3");
			DictionaryPickerDataResult result = new DictionaryPickerDataResult();
		if ((action.getStart() + action.getOffset()) <= items.size()) {
			List<DictionaryItem> newItems = items.subList(action.getStart(), action.getStart() + action.getOffset());
			result.setDictionaryItems(new LinkedList<DictionaryItem>(newItems));
			result.setSize(items.size());
		} else {
			result.setDictionaryItems(items);
			result.setSize(items.size());
		}
			log.error("4");
			return result;
		}catch(Throwable e){
			log.error("Error!", e);
			throw new ActionException(e);
		}
	}

	@Override
	public void undo(DictionaryPickerDataAction action, DictionaryPickerDataResult result, ExecutionContext context) throws ActionException {
	}
}
