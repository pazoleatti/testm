package com.aplana.sbrf.taxaccounting.web.module.testpage.client;

/**
 * Created with IntelliJ IDEA.
 * User: vpetrov
 * Date: 12.12.13
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class TestItem {
    private String title;
    private int id;

    public TestItem(String title, int id) {
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestItem testItem = (TestItem) o;

        if (id != testItem.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
