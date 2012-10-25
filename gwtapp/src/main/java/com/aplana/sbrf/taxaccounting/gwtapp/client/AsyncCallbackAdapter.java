package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This class is needed if you won't realise all methods of <code>AsyncCallback&lt;T&gt;</code> interface.
 *
 * @author Vitalii Samolovskikh
 * @see com.google.gwt.user.client.rpc.AsyncCallback
 */
public class AsyncCallbackAdapter<R> implements AsyncCallback<R> {
    @Override
    public void onFailure(Throwable throwable) {
        // Nothing!
    }

    @Override
    public void onSuccess(R result) {
        // Nothing!
    }
}
