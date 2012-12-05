package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.server;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryManager;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.StringDictionaryAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Хандлер для получения значений строкового справочника.
 *
 * @see DictionaryHandler
 * @author Vitalii Samolovskikh
 */
@Service
public class StringDictionaryHandler extends DictionaryHandler<StringDictionaryAction, String> {
	@SuppressWarnings("UnusedDeclaration")
	private final Log log = LogFactory.getLog(StringDictionaryHandler.class);

	@Autowired
	@Qualifier("stringDictionaryManager")
	private DictionaryManager<String> dictionaryManager;

	public StringDictionaryHandler() {
		super(StringDictionaryAction.class);
	}

	@Override
	protected DictionaryDataProvider<String> getDictionaryDataProvider(String dictionaryCode) {
		return dictionaryManager.getDataProvider(dictionaryCode);
	}
}
