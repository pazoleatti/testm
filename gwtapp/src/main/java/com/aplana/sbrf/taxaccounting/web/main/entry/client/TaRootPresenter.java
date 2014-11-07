package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.RootPresenter;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

public class TaRootPresenter extends RootPresenter {
	
	private int lockCount;

    /*static {
        // Перехват ошибок, которые не были отловлены в клиентском коде
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                Dialog.errorMessage(e.toString());
                e.printStackTrace();
            }
        });
    }*/

	public static class OurView extends RootPresenter.RootView {
		private Element glass;

		@Override
		public void ensureGlass() {
			if (glass == null) {
				glass = Document.get().createDivElement();

				Style style = glass.getStyle();
				style.setPosition(Style.Position.ABSOLUTE);
				style.setLeft(0, Style.Unit.PX);
				style.setTop(0, Style.Unit.PX);
				style.setRight(0, Style.Unit.PX);
				style.setBottom(0, Style.Unit.PX);
				style.setZIndex(2147483647); // Maximum z-index
				// SPECIAL FOR IE
				style.setBackgroundColor("#FFFFFF");
				style.setOpacity(0);
			}
		}

		@Override
		public void lockScreen() {
			ensureGlass();
			Document.get().getBody().appendChild(glass);
		}

		@Override
		public void unlockScreen() {
			ensureGlass();
			Document.get().getBody().removeChild(glass);
		}

	}
	@Inject
	public TaRootPresenter(EventBus eventBus, final OurView view) {
		super(eventBus, view);
	}

	@Override
	public void onLockInteraction(LockInteractionEvent lockInteractionEvent) {
		
		if (lockInteractionEvent.shouldLock()){
			lockCount++;
		} else {
			lockCount--;
		}
		
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
