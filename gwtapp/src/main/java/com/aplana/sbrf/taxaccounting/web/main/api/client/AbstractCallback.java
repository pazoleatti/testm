package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.aplana.sbrf.taxaccounting.web.main.entry.client.ClientGinjector;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.DelayedBindRegistry;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

/**
 * Вообще мне не нравятся идеи, заложенные в этом классе. В первую очередь потому, что для его корректного
 * использования, этот класс нужно хорошенько задокументировать.
 *
 * Суть в том, что когда класс создается, блокируется экран и пользователь не может совершить никаких действий больше.
 * Когда класс отрабатывает, т.е. когда с сервера мы получим какой-то ответ, экран разблокируется.
 *
 * То что блокировка происходит в конструкторе класса ведет к тому, что класс можно
 * <strong>использовать только 1 раз</strong>. Это все конечно красиво, когда каждый раз создается новый анонимный
 * класс для обработки запросов. Но вот если мы хотим использовать один обработчик для обработки нескольких событий,
 * использовать этот класс нельзя.
 *
 * Так же этот класс не стоит использовать для запросов, которые могут происходить в тайне от пользователя.
 *
 * Вообще, мне кажется, что если и делать такую жестокую блокировку, то делать её надо по таймеру,
 * а то приложение может никогда не очнуться.
 */
public abstract class AbstractCallback<T> implements AsyncCallback<T>, HasHandlers {
	
	// TODO: Почему то не получается использовать @Inject для статических полей. Надо разобраться.
	// Пока ворк эраунд - получение инжектора руками и установка значения.
	private static EventBus eventBus = ((ClientGinjector)DelayedBindRegistry.getGinjector()).getEventBus();

	/**
	 * Воздает новый обработчик возврата запросак к серверу. При этом блокируется экран пользователя
	 * и он ничего не может сделать.
	 */
	public AbstractCallback(){
		LockInteractionEvent.fire(this, true);
	}

	/**
	 * Вызывается в случае успешной обработки запроса к сервером. Разблокирует экран пользователя.
	 *
	 * @param result результат работы сервера
	 */
	@Override
	public void onSuccess(T result) {
		LockInteractionEvent.fire(this, false);
	}

	/**
	 * Вызывается в случае неудачной обработки запроса сервером. Выводит соответствующее сообщение.
	 * Разблокирует экран пользователя.
	 *
	 * @param throwable ошибка, произошедшая на сервере
	 */
	@Override
	public void onFailure(Throwable throwable) {
		throwable.printStackTrace();
		Window.alert(throwable.getMessage());
		LockInteractionEvent.fire(this, false);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		eventBus.fireEventFromSource(event, this);
	}
}