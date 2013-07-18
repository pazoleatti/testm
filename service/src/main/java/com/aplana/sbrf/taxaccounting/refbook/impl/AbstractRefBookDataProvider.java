package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;

/**
 * Абстрактный класс для провадеров данных справочников
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 18.07.13 14:24
 */

public abstract class AbstractRefBookDataProvider implements RefBookDataProvider  {

	protected Long refBookId;

	@Override
	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

}
