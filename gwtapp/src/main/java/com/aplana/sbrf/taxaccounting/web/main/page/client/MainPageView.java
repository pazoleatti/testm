package com.aplana.sbrf.taxaccounting.web.main.page.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.page.client.MainPagePresenter.MyView;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class MainPageView extends ViewImpl implements MyView {
	interface Binder extends UiBinder<Widget, MainPageView> {
	}

	public final Widget widget;

	@UiField
	DockLayoutPanel dockPanel;

	@UiField
	Panel mainMenuContentPanel;

	@UiField
	Panel signInContentPanel;

	@UiField
	Panel mainContentPanel;

	@UiField
	AbsolutePanel footerPanel;

	@UiField
	Label projectVersion;

	@UiField
	Element loadingMessage;

	@UiField
	Widget titlePanel;

	@UiField
	HasText title;

	@Inject
	public MainPageView(Binder binder) {
		widget = binder.createAndBindUi(this);
		footerPanel.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.BOTTOM);
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
	public void showLoading(boolean locked) {
		loadingMessage.getStyle().setVisibility(
				locked ? Visibility.VISIBLE : Visibility.HIDDEN);
	}

	@Override
	public void updateTitle(String title, String desc) {
		dockPanel.setWidgetHidden(titlePanel, title == null && desc == null);
		StringBuilder text = new StringBuilder(title != null ? title : "");
		text.append(desc != null ? " / " + desc : "");
		this.title.setText(text.toString());
	}

	@Override
	public void setProjectVersion(String version){
		projectVersion.setText("Версия: " + version);
	}
}