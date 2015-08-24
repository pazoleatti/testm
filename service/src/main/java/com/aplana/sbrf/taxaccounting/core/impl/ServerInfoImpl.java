package com.aplana.sbrf.taxaccounting.core.impl;

import com.aplana.sbrf.taxaccounting.core.api.ServerInfo;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * Сервис предоставляет информацию о сервере
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 07.08.15 21:05
 */

@Service
public class ServerInfoImpl implements ServerInfo {

	@Override
	public String getServerName() {
		// получаем информацию о сервере приложений
		try {
			Class clazz = Class.forName("com.ibm.websphere.runtime.ServerName");
			Method method =	clazz.getMethod("getDisplayName", null);
			return (String) method.invoke(null, null);
		} catch (Exception e) {
			// do nothing (dev mode)
		}
		return "?";
	}

}
