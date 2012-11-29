package com.aplana.sbrf.taxaccounting.web.main.page.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.page.client.MainPagePresenter.MyView;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class MainPageView extends ViewImpl implements MyView {
	interface Binder extends UiBinder<Widget, MainPageView> {
	}

	public final Widget widget;

	@UiField
	FlowPanel mainMenuContentPanel;

	@UiField
	FlowPanel signInContentPanel;

	@UiField
	FlowPanel mainContentPanel;

	@UiField
	Element loadingMessage;

	@Inject
	public MainPageView(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == RevealContentTypeHolder.getMainContent()) {
			setMainContent(content);
		} else if (slot == MainPagePresenter.TYPE_SignInContent) {
			setSignInContent(content);
		} else if (slot == MainPagePresenter.TYPE_MainMenuContent) {
			setMainMenuContent(content);
		} else {
			super.setInSlot(slot, content);
		}
	}

	private void setMainContent(Widget content) {
		mainContentPanel.clear();
		if (content != null) {
			mainContentPanel.add(content);
		}
	}

	private void setSignInContent(Widget content) {
		signInContentPanel.clear();
		if (content != null) {
			signInContentPanel.add(content);
		}
	}

	private void setMainMenuContent(Widget content) {
		mainMenuContentPanel.clear();
		if (content != null) {
			mainMenuContentPanel.add(content);
		}
	}

	@Override
	public void lockAndShowLoading(boolean locked) {
		loadingMessage.getStyle().setVisibility(
				locked ? Visibility.VISIBLE : Visibility.HIDDEN);
	}
}