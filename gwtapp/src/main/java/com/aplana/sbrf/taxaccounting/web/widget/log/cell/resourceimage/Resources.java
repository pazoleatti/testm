package com.aplana.sbrf.taxaccounting.web.widget.log.cell.resourceimage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

public interface Resources extends ClientBundle {
	
	public static final Resources INSTANCE =  GWT.create(Resources.class);
	
	@Source("cross.png")
	@ImageOptions(height=15,width=15)
	public ImageResource error();

}
