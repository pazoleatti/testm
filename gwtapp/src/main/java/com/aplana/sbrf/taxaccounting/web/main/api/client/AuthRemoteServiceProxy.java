package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.impl.RemoteServiceProxy;
import com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter;
import com.google.gwt.user.client.rpc.impl.RpcStatsContext;
import com.google.gwt.user.client.rpc.impl.Serializer;

/**
 * Базовый класс для прокси объектов удаленных сервисов. Фактически, сервис у нас один DispatchService из GWTP. Но если
 * мы захотим использовать другие сервисы, то к ним будет применен этот же генератор.
 * <p/>
 * Этот класс унаследован от стандартного. Так что, практически ничего не меняется. Единственное различие - заменен
 * стандартный обработчик ответов. Добавлена обработка такого варианта: ответ со статусом 200 и типом HTML. В этом
 * случае, пользователь не получит ошибку, а будет перекинут на страницу авторизации.
 *
 * @author Vitaliy Samolovskikh
 * @see com.google.gwt.user.client.rpc.impl.RemoteServiceProxy
 */
public class AuthRemoteServiceProxy extends RemoteServiceProxy {
	/**
	 * Переопределен стандартный конструктор.
	 *
	 * @see com.google.gwt.user.client.rpc.impl.RemoteServiceProxy#RemoteServiceProxy(String, String, String, com.google.gwt.user.client.rpc.impl.Serializer)
	 */
	protected AuthRemoteServiceProxy(
			String moduleBaseURL, String remoteServiceRelativePath,
			String serializationPolicyName, Serializer serializer
	) {
		super(moduleBaseURL, remoteServiceRelativePath, serializationPolicyName, serializer);
	}

	/**
	 * Этот метод создает обработчик ответа, который мы и переопределяем. Чтобы долго не возиться, я просто взял
	 * обработчик, получаемый, стандартным способом и использовал его в качестве делегата для своего обработчика.
	 *
	 * @see com.google.gwt.user.client.rpc.impl.RemoteServiceProxy#doCreateRequestCallback(com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter.ResponseReader, String, com.google.gwt.user.client.rpc.impl.RpcStatsContext, com.google.gwt.user.client.rpc.AsyncCallback)
	 */
	@Override
	protected <T> RequestCallback doCreateRequestCallback(
			RequestCallbackAdapter.ResponseReader responseReader, String methodName,
			RpcStatsContext statsContext, AsyncCallback<T> callback
	) {
		// Получаем делегата. Ему мы будем передавать ответ, если не будем знать что делать.
		final RequestCallback delegate = super.doCreateRequestCallback(responseReader, methodName, statsContext, callback);

		// А вот это уже наш обработчик.
		return new RequestCallback() {
			@Override
			public void onResponseReceived(Request request, Response response) {
				if (response == null) {
					throw new IllegalArgumentException("Response can't be null.");
				}

				// Получаем стутус и тип ответа.
				int status = response.getStatusCode();
				String contentType = response.getHeader("Content-Type");

				// Можно включить, для отладки.
				// GWT.log("Get response with code "+ status+" and ContentType=\""+contentType+"\"");

				// Если ответ 200 ОК и тип HTML, релоадим страницу, так чтобы попать на страницу авторизации.
				if (status == Response.SC_OK && contentType != null && contentType.contains("html")) {
					// Illegal content type. Redirect to auth page.
					Window.alert("Ваша рабочая сессия истекла. Вы будете перенаправлены на форму авторизации.");
					Window.Location.reload();
				} else {
					// Во всех остальных случаях, пусть разбирается стандартный обраблотчик.
					delegate.onResponseReceived(request, response);
				}
			}

			/**
			 * Обработку ошибок поручаем делегату, т.е. стандартному обработчику.
			 */
			@Override
			public void onError(Request request, Throwable exception) {
				delegate.onError(request, exception);
			}
		};
	}
}
