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
import com.google.gwt.storage.client.Storage;
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
 * TODO aivanov 28.01.14 Класс слишком большой, нужно вынести внутренние интерфейсы в отдельыне классы, и вообще лучше определить через ui.xml
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
	public interface Resources extends ClientBundle {

		/**
		 * The image used to skip ahead multiple pages.
		 */
		@ImageOptions(flipRtl = true, preventInlining = true)
		ImageResource flexiblePagerFastForward();

		/**
		 * The disabled "fast forward" image.
		 */
		@ImageOptions(flipRtl = true, preventInlining = true)
		ImageResource flexiblePagerFastForwardDisabled();

		/**
		 * The image used to go to the first page.
		 */
		@ImageOptions(flipRtl = true, preventInlining = true)
		ImageResource flexiblePagerFirstPage();

		/**
		 * The disabled first page image.
		 */
		@ImageOptions(flipRtl = true, preventInlining = true)
		ImageResource flexiblePagerFirstPageDisabled();

		/**
		 * The image used to go to the last page.
		 */
		@ImageOptions(flipRtl = true, preventInlining = true)
		ImageResource flexiblePagerLastPage();

		/**
		 * The disabled last page image.
		 */
		@ImageOptions(flipRtl = true, preventInlining = true)
		ImageResource flexiblePagerLastPageDisabled();

		/**
		 * The image used to go to the next page.
		 */
		@ImageOptions(flipRtl = true, preventInlining = true)
		ImageResource flexiblePagerNextPage();

		/**
		 * The disabled next page image.
		 */
		@ImageOptions(flipRtl = true, preventInlining = true)
		ImageResource flexiblePagerNextPageDisabled();

		/**
		 * The image used to go to the previous page.
		 */
		@ImageOptions(flipRtl = true, preventInlining = true)
		ImageResource flexiblePagerPreviousPage();

		/**
		 * The disabled previous page image.
		 */
		@ImageOptions(flipRtl = true, preventInlining = true)
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
	public interface Style extends CssResource {

		/**
		 * Applied to buttons.
		 */
		String button();

		/**
		 * Applied to the details text.
		 */
		String pageDetails();

        String pageNumber();

        String rowsCountOnPage();

        String panel();

        String buttonLastPage();
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

		public ImageButton(ImageResource resEnabled, ImageResource resDiabled,
						   String label) {
			super(resEnabled);
			this.resEnabled = resEnabled;
			this.resDisabled = resDiabled;
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
            setResource(disabled ? resDisabled : resEnabled);
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
    private final HTML rightLabel = new HTML();
    private final IntegerBox rowsCountOnPage = new IntegerBox();
    private String type;
    /* количество строк на страницу по умолчанию */
    private Integer defaultPageSize = DEFAULT_PAGE_SIZE;
    private HorizontalPanel layout;

	private final ImageButton lastPage;
	private final ImageButton nextPage;
	private final ImageButton prevPage;

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int DEFAULT_SPACING = 3;

	/**
	 * The {@link Resources} used by this widget.
	 */
	private final Resources resources;

	private static final LocalHtmlTemplates templates = GWT.create(LocalHtmlTemplates.class);

	/**
	 * The {@link Style} used by this widget.
	 */
    @SuppressWarnings("all")
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
				false, "");
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
				showLastPageButton, "");
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
						 final int fastForwardRows, boolean showLastPageButton, String type) {
		this(location, getDefaultResources(), showFastForwardButton, fastForwardRows,
				showLastPageButton, type);
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
     * @param type тип пагинатора (место где оно используется - список форм, справочник...)
     * @param defaultPageSize количство строк на страницу по умолчанию
	 */
	public FlexiblePager(TextLocation location, Resources resources,
						 boolean showFastForwardButton, final int fastForwardRows,
						 boolean showLastPageButton, ImageButtonsConstants imageButtonConstants,
                         String type, Integer defaultPageSize) {
		super();
		
		this.resources = resources;
		this.fastForwardRows = fastForwardRows;
		this.style = resources.flexiblePagerStyle();
		this.style.ensureInjected();
		this.setRangeLimited(false);
        this.type = type;
        if (defaultPageSize != null) {
            this.defaultPageSize = defaultPageSize;
        }

		// Create the buttons.
		firstPage = new ImageButton(resources.flexiblePagerFirstPage(),
				resources.flexiblePagerFirstPageDisabled(), imageButtonConstants.firstPage());
		firstPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				firstPage();
			}
		});
		nextPage = new ImageButton(resources.flexiblePagerNextPage(),
				resources.flexiblePagerNextPageDisabled(), imageButtonConstants.nextPage());
		nextPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				nextPage();
			}
		});
		prevPage = new ImageButton(resources.flexiblePagerPreviousPage(),
				resources.flexiblePagerPreviousPageDisabled(), imageButtonConstants.prevPage());
		prevPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				previousPage();
			}
		});
		if (showLastPageButton) {
			lastPage = new ImageButton(resources.flexiblePagerLastPage(),
					resources.flexiblePagerLastPageDisabled(), imageButtonConstants.lastPage());
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
					resources.flexiblePagerFastForwardDisabled(), imageButtonConstants.fastForward());
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
		layout = new HorizontalPanel();
        layout.setSpacing(3);
		layout.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        layout.getElement().setClassName(style.panel());
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
		firstPage.getElement().addClassName(style.button());
		prevPage.getElement().addClassName(style.button());
		pageNumber.getElement().getStyle().setWidth(3, com.google.gwt.dom.client.Style.Unit.EM);
		middleRightLabel.getElement().addClassName(style.pageDetails());
		nextPage.getElement().addClassName(style.button());

		if (showFastForwardButton) {
			fastForward.getElement().addClassName(style.button());
		}
		if (showLastPageButton) {
			lastPage.getElement().addClassName(style.button());
		}

		pageNumber.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode()==KeyCodes.KEY_ENTER){
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
			}
		});

        rightLabel.setText("Строк на странице:");
        layout.add(rightLabel);
        layout.add(rowsCountOnPage);

        rowsCountOnPage.getElement().getStyle().setWidth(3, com.google.gwt.dom.client.Style.Unit.EM);
        rowsCountOnPage.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getUnicodeCharCode() == 13) {
                    updateRowsCountOnPage();
                } else {
                    // запретить символы кроме цифр, цифры в таблице кодировки 48..57
                    if (event.getUnicodeCharCode() < 48 || 57 < event.getUnicodeCharCode()) {
                        event.preventDefault();
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
						 boolean showLastPageButton, String type) {
		this(location, resources, showFastForwardButton, fastForwardRows, showLastPageButton,
				GWT.<ImageButtonsConstants>create(ImageButtonsConstants.class), type, null);
	}

	@Override
    @SuppressWarnings("all")
	public void firstPage() {
		super.firstPage();
	}

    /**
     * Постраничный режим (одна строка). Используется при просмотре декларации.
     */
    public void setOnlyPages(boolean onlyPages) {
        leftLabel.setVisible(!onlyPages);
        rightLabel.setVisible(!onlyPages);
        rowsCountOnPage.setVisible(!onlyPages);
    }

	@Override
    @SuppressWarnings("all")
    public int getPage() {
        return super.getPage();
    }

	@Override
	public final void setDisplay(HasRows display) {
		// Enable or disable all buttons.
		boolean disableButtons = (display == null);
		setFastForwardDisabled(disableButtons);
		setNextPageButtonsDisabled(disableButtons);
		setPrevPageButtonsDisabled(disableButtons);
		super.setDisplay(display);
	}

    @Override
    @SuppressWarnings("all")
	public void setPage(int index) {
		super.setPage(index);
	}

    @Override
    public int getPageSize() {
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            String value = storage.getItem("tax-rowsCountOnPage_" + type);
            if (value != null && !"".equals(value)) {
                return Integer.valueOf(value);
            } else {
                setPageSize(defaultPageSize);
            }
        }
        return defaultPageSize;
    }

    @Override
	public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            pageSize = getPageSize();
        }
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            storage.setItem("tax-rowsCountOnPage_" + type, String.valueOf(pageSize));
        }
		super.setPageSize(pageSize);
	}

    public Integer getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(Integer defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    /**
     * Показывать только надпись "Показано: 1-xx из xx" или показывать весть пейджинг
     * @param visible true - всё, false - только надпись
     */
    public void isCanEditPage(boolean visible) {
        layout.setSpacing(visible ? DEFAULT_SPACING : 0);
        isVisibleLayoutChild(firstPage, visible);
        isVisibleLayoutChild(prevPage, visible);
        isVisibleLayoutChild(middleLeftLabel, visible);
        isVisibleLayoutChild(pageNumber, visible);
        isVisibleLayoutChild(middleRightLabel, visible);
        isVisibleLayoutChild(nextPage, visible);
        isVisibleLayoutChild(lastPage, visible);
        isVisibleLayoutChild(rightLabel, visible);
        isVisibleLayoutChild(rowsCountOnPage, visible);
    }

    private void isVisibleLayoutChild(Widget childInLayout, boolean visible){
        getStyleParentWidget(childInLayout).setProperty("display", visible ? "table-cell" : "none");
    }

    private com.google.gwt.dom.client.Style getStyleParentWidget(Widget childInLayout){
        return childInLayout.getElement().getParentElement().getStyle();
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
        rowsCountOnPage.setValue(getPageSize());
	}

    /**
     * Установка отображаемого номера страницы
     * @param number
     */
    public void setPageNumber(int number) {
        HasRows display = getDisplay();

        if (display == null) {
            return;
        }

        pageNumber.setValue(number);

        // Update the prev and first buttons.
        setPrevPageButtonsDisabled(!hasPreviousPage());

        // Update the next and last buttons.
        if (display.isRowCountExact()) {
            setNextPageButtonsDisabled(!hasNextPage());
            setFastForwardDisabled(!hasNextPages(getFastForwardPages()));
        }
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
		middleLeftLabel.setText("Страница: ");
        pageNumber.setValue(page);
		middleRightLabel.setText((exact ? " из " : " более ") + pageCount);
        setPageSize(range.getLength());
        rowsCountOnPage.setValue(range.getLength());

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
		} else {
			fastForward.setResource(resources.flexiblePagerFastForward());
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

    /**  Обновить количество строк на странице в локальном хранилище браузера. */
    private void updateRowsCountOnPage() {
        if (rowsCountOnPage.getValue() != null) {
            setPageSize(rowsCountOnPage.getValue());
            getDisplay().setVisibleRange(new Range(0, getPageSize()));
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
