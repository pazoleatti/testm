package com.aplana.sbrf.taxaccounting.web.widget.pager;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

public class FlexiblePager extends SimplePager {

	@UiConstructor
	public FlexiblePager(TextLocation location, boolean showFastForwardButton, int fastForwardRows, boolean showLastPageButton) {
		super(location, showFastForwardButton, fastForwardRows, showLastPageButton);
	}

	/**
	 * Руссификация, тк в пейджере жестко забит текст of/over of.
	 * @return the text
	 */
	@Override
	protected String createText() {
		NumberFormat formatter = NumberFormat.getFormat("#,###");
		HasRows display = getDisplay();
		Range range = display.getVisibleRange();
		int pageStart = range.getStart() + 1;
		int pageSize = range.getLength();
		int dataSize = display.getRowCount();
		int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
		endIndex = Math.max(pageStart, endIndex);
		boolean exact = display.isRowCountExact();
		return formatter.format(pageStart) + "-" + formatter.format(endIndex)
				+ (exact ? " из " : " более ") + formatter.format(dataSize);
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
