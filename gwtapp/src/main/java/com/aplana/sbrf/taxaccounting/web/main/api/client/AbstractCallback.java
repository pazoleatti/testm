package com.aplana.sbrf.taxaccounting.web.main.api.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.entry.client.ClientGinjector;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.DelayedBindRegistry;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

/**
 * Вообще мне не нравятся идеи, заложенные в этом классе. В первую очередь
 * потому, что для его корректного использования, этот класс нужно хорошенько
 * задокументировать.
 * 
 * Суть в том, что когда класс создается, блокируется экран и пользователь не
 * может совершить никаких действий больше. Когда класс отрабатывает, т.е. когда
 * с сервера мы получим какой-то ответ, экран разблокируется.
 * 
 * То что блокировка происходит в конструкторе класса ведет к тому, что класс
 * можно <strong>использовать только 1 раз</strong>. Это все конечно красиво,
 * когда каждый раз создается новый анонимный класс для обработки запросов. Но
 * вот если мы хотим использовать один обработчик для обработки нескольких
 * событий, использовать этот класс нельзя.
 * 
 * Так же этот класс не стоит использовать для запросов, которые могут
 * происходить в тайне от пользователя.
 * 
 * Вообще, мне кажется, что если и делать такую жестокую блокировку, то делать
 * её надо по таймеру, а то приложение может никогда не очнуться.
 * 
 * 
 * @deprecated Используйте цепочку из <code>LockCallback.create</code>,
 *             <code>LogOnFailureCallback.create</code>.
 * @deprecated
 */
@Deprecated
public abstract class AbstractCallback<T> implements AsyncCallback<T>,
		HasHandlers {

	protected Logger logger = Logger.getLogger(getClass().getName());

	// TODO: Почему то не получается использовать @Inject для статических полей.
	// Надо разобраться.
	// Пока ворк эраунд - получение инжектора руками и установка значения.
	private static EventBus eventBus = ((ClientGinjector) DelayedBindRegistry
			.getGinjector()).getEventBus();

	/**
	 * Создает новый обработчик возврата запросак к серверу. При этом
	 * блокируется экран пользователя и он ничего не может сделать. 
	 * 
	 * @deprecated
	 * Используйте <code>CallbackUtils</code> or <code>CompositeCallbak.create</code>.
	 * @deprecated
	 */
	public AbstractCallback() {
		if (needLock()) {
			LockInteractionEvent.fire(this, true);
		}
	}

	/**
	 * Вызывается в случае успешной обработки запроса к сервером. Разблокирует
	 * экран пользователя.
	 * 
	 * @param result
	 *            результат работы сервера
	 */
	@Override
	public final void onSuccess(T result) {
		this.onReqSuccess(result);
		if (needLock()) {
			LockInteractionEvent.fire(this, false);
		}
	}

	/**
	 * Вызывается в случае неудачной обработки запроса сервером. Выводит
	 * соответствующее сообщение. Разблокирует экран пользователя.
	 * 
	 * @param throwable
	 *            ошибка, произошедшая на сервере
	 */
	@Override
	public final void onFailure(Throwable throwable) {
		this.onReqFailure(throwable);
		logger.log(Level.INFO, errorOnFailureMsg(), throwable);
		if (needErrorOnFailure()) {
			ErrorEvent.fire(this, errorOnFailureMsg(), throwable);
		}
		if (needLock()) {
			LockInteractionEvent.fire(this, false);
		}
	}

	protected void onReqSuccess(T result) {

	}

	protected void onReqFailure(Throwable throwable) {

	}

	protected boolean needLock() {
		return true;
	}

	protected boolean needErrorOnFailure() {
		return true;
	}

	protected String errorOnFailureMsg() {
		return "Ошибка при асинхронном вызове";
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		eventBus.fireEventFromSource(event, this);
	}
}