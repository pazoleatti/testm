package com.aplana.sbrf.taxaccounting.web.widget.version.client;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.google.gwt.user.client.Window.Navigator;

/**
 * Вьюха для отображения информации о версии приложения, номера сборки, узла кластера, на котором оно выполняется и
 * информации о браузере
 */
public class ProjectVersionView extends ViewImpl implements ProjectVersionPresenter.MyView {

	interface Binder extends UiBinder<Widget, ProjectVersionView> {
	}

	@UiField
	HasText projectVersion;
	
	@Inject
	public ProjectVersionView(final Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setProjectVersion(String projectVersion) {
		this.projectVersion.setText(projectVersion + "; Браузер: " + getBrowserInfo());
	}

	private String getBrowserInfo() {
		if (Navigator.getUserAgent() != null) {
			return parseUserAgent(Navigator.getUserAgent().toLowerCase());
		}
		return "?";

	}

	/**
	 * Извлекает из строки user-agent информацию о типе и версии браузера
	 * @param agent
	 * @return
	 */
	static String parseUserAgent(String agent) {
		if (agent.contains("msie") || agent.contains("trident")) {
			if (agent.contains("msie")) {
				//ie 8, 9, 10, 11
				String found = findRegExp(agent, "msie\\s[\\d\\w\\.]+");
				if (found != null) {
					return found;
				}
			} else {
				//ie 11
				String found = findRegExp(agent, "rv:[\\d\\w\\.]+");
				if (found != null) {
					return "msie " + found.substring(3); //"rv:????"
				}
			}
		} else if (agent.contains("chrome")){
			String found = findRegExp(agent, "chrome/[\\d\\w\\.]+");
			if (found != null) {
				return found.replace("/", " ");
			}
		} else if (agent.contains("firefox")){
			String found = findRegExp(agent, "firefox/[\\d\\w\\.]+");
			if (found != null) {
				return found.replace("/", " ");
			}
		}
		return agent;
	}

	/**
	 * Ищет подстроку в строке по регулярному выражению
	 * @param source
	 * @param pattern
	 * @return найденная строка, иначе null
	 */
	static String findRegExp(String source, String pattern) {
		RegExp regexp = RegExp.compile(pattern);
		MatchResult matcher = regexp.exec(source);
		if (matcher != null && matcher.getGroupCount() > 0) {
			return matcher.getGroup(0);
		}
		return null;
	}

}