package com.aplana.sbrf.taxaccounting.web.module.testpage2.client;

public class TestPage2Item {
    private String title;
    private int id;

    public TestPage2Item(String title, int id) {
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

        TestPage2Item testItem = (TestPage2Item) o;

        if (id != testItem.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
