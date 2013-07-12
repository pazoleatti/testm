package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.impl.RefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Реализация фабрики провайдеров данных для справочников
 * <br /><br />
 * <b><u>Примечание</u>: </b>Для предопределенных справочников предлагаю использовать отрицательные коды, чтобы
 * не возникло проблем с кодами универсальных. Например, к предопределенному справочнику можно отнести
 * справочник "Подразделение", "Отчетный период".
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 11.07.13 11:22
 */
@Service
public class RefBookFactoryImpl implements RefBookFactory {

	@Autowired
	private RefBookDao refBookDao;

	@Autowired
	private RefBookDataProvider refBookDataProvider;

	@Override
	public RefBook get(Long refBookId) {
		//здесь добавлять условия для учета нестандартных справочников
		return refBookDao.get(refBookId);
	}

	@Override
	public List<RefBook> getAll() {
		//здесь добавлять условия для учета нестандартных справочников
		return refBookDao.getAll();
	}

	@Override
	public RefBookDataProvider getDataProvider(long refBookId) {
		//здесь добавлять условия для учета нестандартных справочников
		return refBookDataProvider;
	}
}
