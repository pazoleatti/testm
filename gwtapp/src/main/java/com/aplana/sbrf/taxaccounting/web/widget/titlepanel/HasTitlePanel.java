package com.aplana.sbrf.taxaccounting.web.widget.titlepanel;

/**
 * Обозначает, что компонент содержит TitlePanel
 * @author dloshkarev
 */
public interface HasTitlePanel {

    /**
     * Устанавливает действие, выполняемое при закрытии компонента
     * @param action
     */
    void setPanelClosingAction(PanelClosingAction action);

    /**
     * Устанавливает видимость панели с заголовком окна
     * @param visible
     */
    void setTitlePanelVisibility(boolean visible);

    /**
     * Устанавливает заголовок окна
     * @param title
     */
    void setTitleText(String title);
}
