package com.aplana.sbrf.taxaccounting.model;

public class DictionaryRegion {
    private Integer code;
    private String name;
    private String okato;
    private String okatoDefinition;

    public String getOkatoDefinition() {
        return okatoDefinition;
    }

    public void setOkatoDefinition(String okatoDefinition) {
        this.okatoDefinition = okatoDefinition;
    }

    public String getOkato() {
        return okato;
    }

    public void setOkato(String okato) {
        this.okato = okato;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

	@Override
    public boolean equals(Object obj) {
        if (obj instanceof DictionaryRegion) {
            DictionaryRegion val = (DictionaryRegion) obj;
            if (val.getCode().equals(getCode())
                    && val.getName().equals(getName())
                    && val.getOkato().equals(getOkato())
                    && val.getOkatoDefinition().equals(getOkatoDefinition())) {
                return true;
            }
        }
        return false;
    }

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
