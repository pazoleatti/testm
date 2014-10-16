package com.aplana.gwt.client.testutils;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

/**
 * Использовать при тестировании асинхронных запросов презентора
 *
 * @author Fail Mukhametdinov
 */
public class DispatchAsyncStubber {
    public static <T, C extends AsyncCallback> Stubber callSuccessWith(final T data) {
        return Mockito.doAnswer(new Answer<T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                final Object[] args = invocationOnMock.getArguments();
                ((C) args[args.length - 1]).onSuccess(data);
                return null;
            }
        });
    }

    public static <C extends AsyncCallback> Stubber callFailureWith(final Throwable caught) {
        return Mockito.doAnswer(new Answer<Throwable>() {
            @Override
            @SuppressWarnings("unchecked")
            public Throwable answer(InvocationOnMock invocationOnMock) throws Throwable {
                final Object[] args = invocationOnMock.getArguments();
                ((C) args[args.length - 1]).onFailure(caught);
                return null;
            }
        });
    }
}
