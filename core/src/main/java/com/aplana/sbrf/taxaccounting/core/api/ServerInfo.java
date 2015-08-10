package com.aplana.sbrf.taxaccounting.core.api;

/**
 * Сервис предоставляет информацию о сервере
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 07.08.15 21:04
 */

public interface ServerInfo {

	/**
	 * Возвращает название сервера \ узла кластера
	 * @return
	 */
	String getServerName();

}
