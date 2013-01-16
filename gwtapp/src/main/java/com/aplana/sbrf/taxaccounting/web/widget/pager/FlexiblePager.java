package com.aplana.sbrf.taxaccounting.web.widget.pager;

import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.Range;

public class FlexiblePager extends SimplePager {

	@UiConstructor
	public FlexiblePager(TextLocation location, boolean showFastForwardButton, int fastForwardRows, boolean showLastPageButton) {
		super(location, showFastForwardButton, fastForwardRows, showLastPageButton);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	public void nextPage() {
		// Стандартное поведение пэйджера переопределено в соответствии с задачей "http://jira.aplana.com/browse/SBRFACCTAX-811"
		// Суть изменений: если при нажатии на "nextPage", количество отображаемых данных меньше, чем размер страницы,
		// то отобразить только "оставшиеся" данные на следующей странице.
		// Стандартное поведение пэйджера при нажатии на кнопку "nextPage" - всегда отображать количество данных,
		// равное размеру страницы, даже если данных для отображения меньше чем размер страницы.
		Range range = getDisplay().getVisibleRange();
		int totalNumberOfRecords = getDisplay().getRowCount();
		int nextStartRecord = range.getStart() + range.getLength();
		int numberOfRecordsToDisplay = range.getLength();
		if((totalNumberOfRecords - nextStartRecord) < numberOfRecordsToDisplay){
			lastPage();
		} else if (getDisplay() != null) {
			setPageStart(nextStartRecord);
		}
	}
}
