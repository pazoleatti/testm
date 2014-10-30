package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;


public class DownloadUtils {

    // Т.к в ie8 Window.open() внутри AsyncCallback не работает http://jira.aplana.com/browse/SBRFACCTAX-9279 ,
    // то мы для скачивания файла будем использовать iframe
    public static void openInIframe(String urlToFile) {
        final Frame f = new Frame();
        f.setUrl(urlToFile);
        f.setSize("0px", "0px");
        RootPanel.get().add(f);
        // Configure a timer to remove the element from the DOM
        new Timer() {
            public void run() {
                f.removeFromParent();
            }
        }.schedule(10000);
    }
}
