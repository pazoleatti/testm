package com.aplana.sbrf.taxaccounting.refbook;

/**
 * Фабрика для получения адаптеров справочников
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.07.13 11:49
 */

public interface RefBookFactory {

	RefBookAdapter getRefBook(Long refBookId);

}
