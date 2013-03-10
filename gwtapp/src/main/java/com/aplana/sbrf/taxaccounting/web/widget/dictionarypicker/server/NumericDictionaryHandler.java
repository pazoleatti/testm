package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.server;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryManager;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.NumericDictionaryAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Хандлер для получения значений числовго справочника.
 * @see DictionaryHandler
 * @author Vitalii Samolovskikh
 */
@Service
public class NumericDictionaryHandler extends DictionaryHandler<NumericDictionaryAction, BigDecimal> {

	@Autowired
	@Qualifier("numericDictionaryManager")
	private DictionaryManager<BigDecimal> dictionaryManager;

	public NumericDictionaryHandler() {
		super(NumericDictionaryAction.class);
	}

	@Override
	protected DictionaryDataProvider<BigDecimal> getDictionaryDataProvider(String dictionaryCode) {
		return dictionaryManager.getDataProvider(dictionaryCode);
	}
}
