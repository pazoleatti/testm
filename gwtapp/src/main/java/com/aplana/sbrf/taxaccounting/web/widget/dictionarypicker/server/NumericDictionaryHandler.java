package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.server;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryManager;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.NumericDictionaryAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.NumericDictionaryResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
@Service
public class NumericDictionaryHandler extends DictionaryHandler<NumericDictionaryAction, NumericDictionaryResult, BigDecimal> {
	@SuppressWarnings("UnusedDeclaration")
	private final Log log = LogFactory.getLog(NumericDictionaryHandler.class);

	@Autowired
	@Qualifier("numericDictionaryManager")
	private DictionaryManager<BigDecimal> dictionaryManager;

	public NumericDictionaryHandler() {
		super(NumericDictionaryAction.class);
	}

	@Override
	protected List<DictionaryItem<BigDecimal>> selectDictionaryItems(NumericDictionaryAction action) {
		DictionaryDataProvider<BigDecimal> dictionaryDataProvider = dictionaryManager.getDataProvider(action.getDictionaryCode());
		List<DictionaryItem<BigDecimal>> items;
		items = dictionaryDataProvider.getValues();
		return items;
	}

	@Override
	protected NumericDictionaryResult createResult() {
		return new NumericDictionaryResult();
	}
}
