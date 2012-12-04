package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.RootPresenter;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

public class TaRootPresenter extends RootPresenter {
	
	private int lockCount;

	@Inject
	public TaRootPresenter(EventBus eventBus, RootView view) {
		super(eventBus, view);
	}

	@Override
	public void onLockInteraction(LockInteractionEvent lockInteractionEvent) {
		
		if (lockInteractionEvent.shouldLock()){
			lockCount++;
		} else {
			lockCount--;
		}
		
		System.out.println(lockInteractionEvent.getSource() + " : " + lockInteractionEvent.shouldLock()  + " : " + lockCount);
		if (lockCount <= 0 ){
			lockCount = 0;
			ScreenLockEvent.fire(this, false);
			getView().unlockScreen();
		} else {
			getView().lockScreen();
			ScreenLockEvent.fire(this, true);
		}

	}

}
