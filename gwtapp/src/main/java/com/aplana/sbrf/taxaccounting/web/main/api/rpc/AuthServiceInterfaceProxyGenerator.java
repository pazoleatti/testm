package com.aplana.sbrf.taxaccounting.web.main.api.rpc;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.user.rebind.rpc.ProxyCreator;
import com.google.gwt.user.rebind.rpc.ServiceInterfaceProxyGenerator;

/**
 * Генератор прокси для удаленных сервисов. Подменяет стандартный креатор на наш. Нужен для корректной обработки
 * события потери сессии пользователем.
 *
 * @see com.google.gwt.user.rebind.rpc.ServiceInterfaceProxyGenerator
 * @author Vitaliy Samolovskikh
 */
public class AuthServiceInterfaceProxyGenerator extends ServiceInterfaceProxyGenerator {
	/**
	 * Переопределяет стандартный креатор прокси для удаленных сервисов.
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.main.api.rpc.AuthProxyCreator
	 */
	@Override
	protected ProxyCreator createProxyCreator(JClassType remoteService) {
		return new AuthProxyCreator(remoteService);
	}
}
