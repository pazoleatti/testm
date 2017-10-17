package com.aplana.sbrf.taxaccounting.core.impl;

import com.aplana.sbrf.taxaccounting.core.api.ServerInfo;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
		String serverName = "?";
		try {
			Class clazz = Class.forName("com.ibm.websphere.runtime.ServerName");
			Method method =	clazz.getMethod("getDisplayName", null);
			serverName = (String) method.invoke(null, null);
		} catch (Exception e) {
			//dev mode
			InetAddress addr;
			try {
				addr = InetAddress.getLocalHost();
				serverName = addr.getHostName();
			} catch (UnknownHostException e1) {}
		}
		return serverName;
	}

}
