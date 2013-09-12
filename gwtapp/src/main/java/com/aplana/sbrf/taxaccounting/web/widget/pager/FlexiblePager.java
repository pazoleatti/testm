package com.aplana.sbrf.taxaccounting.web.widget.pager;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

/**
 * A pager for controlling a {@link HasRows} that only supports simple page
 * navigation.
 *
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.cellview.SimplePagerExample}
 * </p>
 */
public class FlexiblePager extends AbstractPager {
	/**
	 * Constant for labeling the simple pager navigational {@link ImageButton}s
	 */
	@DefaultLocale("en_US")
	public interface ImageButtonsConstants extends Constants {
		@DefaultStringValue("Fast forward")
		String fastForward();
		@DefaultStringValue("First page")
		String firstPage();
		@DefaultStringValue("Last page")
		String lastPage();
		@DefaultStringValue("Next page")
		String nextPage();
		@DefaultStringValue("Previous page")
		String prevPage();
	}

	interface LocalHtmlTemplates extends SafeHtmlTemplates {
		@Template("Показано: <b>{0}</b> из <b>{1}</b>")
		SafeHtml leftLabel(String range, int maxCount);
	}

	/**
	 * A ClientBundle that provides images for this widget.
	 */
	public static interface Resources extends ClientBundle {

		/**
		 * The image used to skip ahead multiple pages.
		 */
		@ImageOptions(flipRtl = true)
		ImageResource flexiblePagerFastForward();

		/**
		 * The disabled "fast forward" image.
		 */
		@ImageOptions(flipRtl = true)
		ImageResource flexiblePagerFastForwardDisabled();

		/**
		 * The image used to go to the first page.
		 */
		@ImageOptions(flipRtl = true)
		ImageResource flexiblePagerFirstPage();

		/**
		 * The disabled first page image.
		 */
		@ImageOptions(flipRtl = true)
		ImageResource flexiblePagerFirstPageDisabled();

		/**
		 * The image used to go to the last page.
		 */
		@ImageOptions(flipRtl = true)
		ImageResource flexiblePagerLastPage();

		/**
		 * The disabled last page image.
		 */
		@ImageOptions(flipRtl = true)
		ImageResource flexiblePagerLastPageDisabled();

		/**
		 * The image used to go to the next page.
		 */
		@ImageOptions(flipRtl = true)
		ImageResource flexiblePagerNextPage();

		/**
		 * The disabled next page image.
		 */
		@ImageOptions(flipRtl = true)
		ImageResource flexiblePagerNextPageDisabled();

		/**
		 * The image used to go to the previous page.
		 */
		@ImageOptions(flipRtl = true)
		ImageResource flexiblePagerPreviousPage();

		/**
		 * The disabled previous page image.
		 */
		@ImageOptions(flipRtl = true)
		ImageResource flexiblePagerPreviousPageDisabled();

		/**
		 * The styles used in this widget.
		 */
		@Source("FlexiblePager.css")
		Style flexiblePagerStyle();
	}

	/**
	 * Styles used by this widget.
	 */
	public static interface Style extends CssResource {

		/**
		 * Applied to buttons.
		 */
		String button();

		/**
		 * Applied to disabled buttons.
		 */
		String disabledButton();

		/**
		 * Applied to the details text.
		 */
		String pageDetails();
	}

	/**
	 * The location of the text relative to the paging buttons.
	 */
	public static enum TextLocation {
		CENTER, LEFT, RIGHT;
	}

	/**
	 * An {@link Image} that acts as a button.
	 */
	private static class ImageButton extends Image {
		private boolean disabled;
		private final ImageResource resDisabled;
		private final ImageResource resEnabled;
		private final String styleDisabled;

		public ImageButton(ImageResource resEnabled, ImageResource resDiabled,
						   String disabledStyle, String label) {
			super(resEnabled);
			this.resEnabled = resEnabled;
			this.resDisabled = resDiabled;
			this.styleDisabled = disabledStyle;
			Roles.getButtonRole().set(getElement());
			Roles.getButtonRole().setAriaLabelProperty(getElement(), label);
		}

		public boolean isDisabled() {
			return disabled;
		}

		@Override
		public void onBrowserEvent(Event event) {
			// Ignore events if disabled.
			if (disabled) {
				return;
			}

			super.onBrowserEvent(event);
		}

		public void setDisabled(boolean isDisabled) {
			if (this.disabled == isDisabled) {
				return;
			}

			this.disabled = isDisabled;
			if (disabled) {
				setResource(resDisabled);
				getElement().getParentElement().addClassName(styleDisabled);
			} else {
				setResource(resEnabled);
				getElement().getParentElement().removeClassName(styleDisabled);
			}
			Roles.getButtonRole().setAriaDisabledState(getElement(), disabled);
		}
	}

	private static final int DEFAULT_FAST_FORWARD_ROWS = 1000;
	private static Resources defaultResources;

	private static Resources getDefaultResources() {
		if (defaultResources == null) {
			defaultResources = GWT.create(Resources.class);
		}
		return defaultResources;
	}

	private final ImageButton fastForward;

	private final int fastForwardRows;

	private final ImageButton firstPage;

	/**
	 * We use an {@link HTML} so we can embed the loading image.
	 */
	private final HTML leftLabel = new HTML();
	private final HTML middleLeftLabel = new HTML();
	private final HTML middleRightLabel = new HTML();
	private final IntegerBox pageNumber = new IntegerBox();

	private final ImageButton lastPage;
	private final ImageButton nextPage;
	private final ImageButton prevPage;

	/**
	 * The {@link Resources} used by this widget.
	 */
	private final Resources resources;

	private static final LocalHtmlTemplates templates = GWT.create(LocalHtmlTemplates.class);

	/**
	 * The {@link Style} used by this widget.
	 */
	private final Style style;

	/**
	 * Construct a {@link FlexiblePager} with the default text location.
	 */
	public FlexiblePager() {
		this(TextLocation.CENTER);
	}

	/**
	 * Construct a {@link FlexiblePager} with the specified text location.
	 *
	 * @param location the location of the text relative to the buttons
	 */
	public FlexiblePager(TextLocation location) {
		this(location, getDefaultResources(), true, DEFAULT_FAST_FORWARD_ROWS,
				false);
	}

	/**
	 * Construct a {@link FlexiblePager} with the default resources, fast forward rows and
	 * default image button names.
	 *
	 * @param location the location of the text relative to the buttons
	 * @param showFastForwardButton if true, show a fast-forward button that
	 *          advances by a larger increment than a single page
	 * @param showLastPageButton if true, show a button to go the the last page
	 */
	public FlexiblePager(TextLocation location, boolean showFastForwardButton,
						 boolean showLastPageButton) {
		this(location, showFastForwardButton, DEFAULT_FAST_FORWARD_ROWS,
				showLastPageButton);
	}

	/**
	 * Construct a {@link FlexiblePager} with the default resources and default image button names.
	 *
	 * @param location the location of the text relative to the buttons
	 * @param showFastForwardButton if true, show a fast-forward button that
	 *          advances by a larger increment than a single page
	 * @param fastForwardRows the number of rows to jump when fast forwarding
	 * @param showLastPageButton if true, show a button to go the the last page
	 */
	@UiConstructor
	public FlexiblePager(TextLocation location, boolean showFastForwardButton,
						 final int fastForwardRows, boolean showLastPageButton) {
		this(location, getDefaultResources(), showFastForwardButton, fastForwardRows,
				showLastPageButton);
	}

	/**
	 * Construct a {@link FlexiblePager} with the specified resources.
	 *
	 * @param location the location of the text relative to the buttons
	 * @param resources the {@link Resources} to use
	 * @param showFastForwardButton if true, show a fast-forward button that
	 *          advances by a larger increment than a single page
	 * @param fastForwardRows the number of rows to jump when fast forwarding
	 * @param showLastPageButton if true, show a button to go the the last page
	 * @param imageButtonConstants Constants that contain the image button names
	 */
	public FlexiblePager(TextLocation location, Resources resources,
						 boolean showFastForwardButton, final int fastForwardRows,
						 boolean showLastPageButton, ImageButtonsConstants imageButtonConstants) {
		super();
		
		this.resources = resources;
		this.fastForwardRows = fastForwardRows;
		this.style = resources.flexiblePagerStyle();
		this.style.ensureInjected();
		this.setRangeLimited(false);

		// Create the buttons.
		String disabledStyle = style.disabledButton();
		firstPage = new ImageButton(resources.flexiblePagerFirstPage(),
				resources.flexiblePagerFirstPageDisabled(), disabledStyle, imageButtonConstants.firstPage());
		firstPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				firstPage();
			}
		});
		nextPage = new ImageButton(resources.flexiblePagerNextPage(),
				resources.flexiblePagerNextPageDisabled(), disabledStyle, imageButtonConstants.nextPage());
		nextPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				nextPage();
			}
		});
		prevPage = new ImageButton(resources.flexiblePagerPreviousPage(),
				resources.flexiblePagerPreviousPageDisabled(), disabledStyle,
				imageButtonConstants.prevPage());
		prevPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				previousPage();
			}
		});
		if (showLastPageButton) {
			lastPage = new ImageButton(resources.flexiblePagerLastPage(),
					resources.flexiblePagerLastPageDisabled(), disabledStyle, imageButtonConstants.lastPage());
			lastPage.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					lastPage();
				}
			});
		} else {
			lastPage = null;
		}
		if (showFastForwardButton) {
			fastForward = new ImageButton(resources.flexiblePagerFastForward(),
					resources.flexiblePagerFastForwardDisabled(), disabledStyle,
					imageButtonConstants.fastForward());
			fastForward.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					setPage(getPage() + getFastForwardPages());
				}
			});
		} else {
			fastForward = null;
		}

		// Construct the widget.
		HorizontalPanel layout = new HorizontalPanel();
		layout.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		initWidget(layout);
		layout.add(leftLabel);
		if (location == TextLocation.LEFT) {
			addMiddleWidgets(layout);
		}
		layout.add(firstPage);
		layout.add(prevPage);
		if (location == TextLocation.CENTER) {
			addMiddleWidgets(layout);
		}
		layout.add(nextPage);
		if (showFastForwardButton) {
			layout.add(fastForward);
		}
		if (showLastPageButton) {
			layout.add(lastPage);
		}
		if (location == TextLocation.RIGHT) {
			addMiddleWidgets(layout);
		}

		// Add style names to the cells.
		firstPage.getElement().getParentElement().addClassName(style.button());
		prevPage.getElement().getParentElement().addClassName(style.button());
		middleLeftLabel.getElement().getParentElement().addClassName(style.pageDetails());
		pageNumber.getElement().getStyle().setWidth(3, com.google.gwt.dom.client.Style.Unit.EM);
		middleRightLabel.getElement().getParentElement().addClassName(style.pageDetails());
		leftLabel.getElement().getParentElement().addClassName(style.pageDetails());
		nextPage.getElement().getParentElement().addClassName(style.button());
		if (showFastForwardButton) {
			fastForward.getElement().getParentElement().addClassName(style.button());
		}
		if (showLastPageButton) {
			lastPage.getElement().getParentElement().addClassName(style.button());
		}

		pageNumber.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (pageNumber.getValue() != null) {
					Range range = getDisplay().getVisibleRange();
					int totalNumberOfRecords = getDisplay().getRowCount();
					int nextStartRecord = (pageNumber.getValue() - 1) * range.getLength();
					int numberOfRecordsToDisplay = range.getLength();
					if ((totalNumberOfRecords - nextStartRecord) < numberOfRecordsToDisplay) {
						lastPage();
					} else if (getDisplay() != null) {
						setPageStart(nextStartRecord);
					}
				}
			}
		});

		// Disable the buttons by default.
		setDisplay(null);
	}

	private void addMiddleWidgets(HorizontalPanel layout) {
		layout.add(middleLeftLabel);
		layout.add(pageNumber);
		layout.add(middleRightLabel);
	}

	/**
	 * Construct a {@link FlexiblePager} with the specified resources and default image button names.
	 *
	 * @param location the location of the text relative to the buttons
	 * @param resources the {@link Resources} to use
	 * @param showFastForwardButton if true, show a fast-forward button that
	 *          advances by a larger increment than a single page
	 * @param fastForwardRows the number of rows to jump when fast forwarding
	 * @param showLastPageButton if true, show a button to go the the last page
	 */
	public FlexiblePager(TextLocation location, Resources resources,
						 boolean showFastForwardButton, final int fastForwardRows,
						 boolean showLastPageButton) {
		this(location, resources, showFastForwardButton, fastForwardRows, showLastPageButton,
				GWT.<ImageButtonsConstants>create(ImageButtonsConstants.class));
	}

	@Override
	public void firstPage() {
		super.firstPage();
	}

	@Override
	public int getPage() {
		return super.getPage();
	}

	@Override
	public int getPageCount() {
		return super.getPageCount();
	}

	@Override
	public boolean hasNextPage() {
		return super.hasNextPage();
	}

	@Override
	public boolean hasNextPages(int pages) {
		return super.hasNextPages(pages);
	}

	@Override
	public boolean hasPage(int index) {
		return super.hasPage(index);
	}

	@Override
	public boolean hasPreviousPage() {
		return super.hasPreviousPage();
	}

	@Override
	public boolean hasPreviousPages(int pages) {
		return super.hasPreviousPages(pages);
	}

	@Override
	public void lastPage() {
		super.lastPage();
	}

	@Override
	public void lastPageStart() {
		super.lastPageStart();
	}

	@Override
	public void nextPage() {
		super.nextPage();
	}

	@Override
	public void previousPage() {
		super.previousPage();
	}

	@Override
	public void setDisplay(HasRows display) {
		// Enable or disable all buttons.
		boolean disableButtons = (display == null);
		setFastForwardDisabled(disableButtons);
		setNextPageButtonsDisabled(disableButtons);
		setPrevPageButtonsDisabled(disableButtons);
		super.setDisplay(display);
	}

	@Override
	public void setPage(int index) {
		super.setPage(index);
	}

	@Override
	public void setPageSize(int pageSize) {
		super.setPageSize(pageSize);
	}

	/**
	 * Let the page know that the table is loading. Call this method to clear all
	 * data from the table and hide the current range when new data is being
	 * loaded into the table.
	 */
	public void startLoading() {
		getDisplay().setRowCount(0, true);
		middleLeftLabel.setHTML("");
		pageNumber.setValue(1);
		middleRightLabel.setHTML("");
		leftLabel.setHTML("");
	}

	@Override
	protected void onRangeOrRowCountChanged() {
		HasRows display = getDisplay();
		Range range = display.getVisibleRange();
		
		int pageCount = Math.max(1, (display.getRowCount() / range.getLength()) + ((display.getRowCount() % range.getLength() != 0) ? 1 : 0));		
		int startIntex = Math.min(display.getRowCount(), range.getStart() + 1);
		int endIndex = Math.min(display.getRowCount(), range.getStart() + range.getLength());
		int page = range.getStart() / range.getLength() + 1;

		boolean exact = display.isRowCountExact();

		leftLabel.setHTML(templates.leftLabel(startIntex + "-" + endIndex, display.getRowCount()));
		middleLeftLabel.setText("Страница ");
		pageNumber.setValue(page);
		middleRightLabel.setText((exact ? " из " : " более ") + pageCount);

		// Update the prev and first buttons.
		setPrevPageButtonsDisabled(!hasPreviousPage());

		// Update the next and last buttons.
		if (display.isRowCountExact()) {
			setNextPageButtonsDisabled(!hasNextPage());
			setFastForwardDisabled(!hasNextPages(getFastForwardPages()));
		}
	}

	/**
	 * Check if the next button is disabled. Visible for testing.
	 */
	boolean isNextButtonDisabled() {
		return nextPage.isDisabled();
	}

	/**
	 * Check if the previous button is disabled. Visible for testing.
	 */
	boolean isPreviousButtonDisabled() {
		return prevPage.isDisabled();
	}

	/**
	 * Get the number of pages to fast forward based on the current page size.
	 *
	 * @return the number of pages to fast forward
	 */
	private int getFastForwardPages() {
		int pageSize = getPageSize();
		return pageSize > 0 ? fastForwardRows / pageSize : 0;
	}

	/**
	 * Enable or disable the fast forward button.
	 *
	 * @param disabled true to disable, false to enable
	 */
	private void setFastForwardDisabled(boolean disabled) {
		if (fastForward == null) {
			return;
		}
		if (disabled) {
			fastForward.setResource(resources.flexiblePagerFastForwardDisabled());
			fastForward.getElement().getParentElement().addClassName(
					style.disabledButton());
		} else {
			fastForward.setResource(resources.flexiblePagerFastForward());
			fastForward.getElement().getParentElement().removeClassName(
					style.disabledButton());
		}
	}

	/**
	 * Enable or disable the next page buttons.
	 *
	 * @param disabled true to disable, false to enable
	 */
	private void setNextPageButtonsDisabled(boolean disabled) {
		nextPage.setDisabled(disabled);
		if (lastPage != null) {
			lastPage.setDisabled(disabled);
		}
	}

	/**
	 * Enable or disable the previous page buttons.
	 *
	 * @param disabled true to disable, false to enable
	 */
	private void setPrevPageButtonsDisabled(boolean disabled) {
		firstPage.setDisabled(disabled);
		prevPage.setDisabled(disabled);
	}
}
