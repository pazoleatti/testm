package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import java.util.Date;

import com.aplana.sbrf.taxaccounting.web.widget.titlepanel.PanelCloseAction;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Интерфейс компонента выбора значений из справочника
 * 
 * @author sgoryachkin
 * 
 */
public interface RefBookPicker extends HasValue<Long>, IsWidget {
	
	/**
	 * Устанавливает параметры и инициализирует компонент.
	 * В компоненте доступны для выбора все версии справочника.
	 * 
	 * @param refBookAttrId
	 */
	public void setAcceptableValues(long refBookAttrId);

	/**
	 * Устанавливает параметры и инициализирует компонент.
	 * В компоненте доступны версии справочника из диапазона дат.
	 * 
	 * @param refBookAttrId
	 * @param date1
	 * @param date2
	 */
	public void setAcceptableValues(long refBookAttrId, Date date1, Date date2);
	
	
	/**
	 * Устанавливает параметры и инициализирует компонент.
	 * В компоненте доступны версии справочника c учетом фильтра.
	 * В компоненте доступны версии справочника из диапазона дат.
	 * 
	 * @param refBookAttrId
	 * @param filter
	 */
	public void setAcceptableValues(long refBookAttrId, String filter);
	
	
	/**
	 * Устанавливает параметры и инициализирует компонент.
	 * В компоненте доступны версии справочника c учетом фильтра.
	 * 
	 * 
	 * @param refBookAttrId
	 * @param filter
	 * @param date1
	 * @param date2
	 */
	public void setAcceptableValues(long refBookAttrId, String filter, Date date1, Date date2);
	
	
	/**
	 * @return
	 */
	public String getDereferenceValue();

    /**
     * Устанавливает действие, выполняемое при закрытии компонента
     * @param action
     */
    void setClosedPanelAction(PanelCloseAction action);

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
