package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.server;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryManager;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.StringFilterDictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.StringDictionaryAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.StringDictionaryResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис возвращающий список элементов справочника и общее количество записей
 * в справочнике.
 *
 * @author Eugene Stetsenko
 */
@Service
public class StringDictionaryHandler extends DictionaryHandler<StringDictionaryAction, StringDictionaryResult, String> {
	@SuppressWarnings("UnusedDeclaration")
	private final Log log = LogFactory.getLog(StringDictionaryHandler.class);

	@Autowired
	@Qualifier("stringDictionaryManager")
	private DictionaryManager<String> dictionaryManager;

	public StringDictionaryHandler() {
		super(StringDictionaryAction.class);
	}

	@Override
	protected StringDictionaryResult createResult() {
		return new StringDictionaryResult();
	}

	@Override
	protected List<DictionaryItem<String>> selectDictionaryItems(StringDictionaryAction action) {
		DictionaryDataProvider<String> dictionaryDataProvider = dictionaryManager.getDataProvider(action.getDictionaryCode());
		List<DictionaryItem<String>> items;
		if (action.getFilter() != null && !action.getFilter().isEmpty() && dictionaryDataProvider instanceof StringFilterDictionaryDataProvider) {
			items = ((StringFilterDictionaryDataProvider) dictionaryDataProvider).getValues(action.getFilter());
		} else {
			items = dictionaryDataProvider.getValues();
		}
		return items;
	}
}
