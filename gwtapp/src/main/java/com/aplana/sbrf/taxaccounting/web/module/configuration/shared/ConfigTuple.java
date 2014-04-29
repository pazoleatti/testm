package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import java.io.Serializable;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;

public class ConfigTuple implements Comparable<ConfigTuple>, Serializable{
	private static final long serialVersionUID = -4574362802024386665L;

	private ConfigurationParam param;
	
	private String value;

	public ConfigurationParam getParam() {
		return param;
	}

	public void setParam(ConfigurationParam param) {
		this.param = param;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

    @Override
    public int compareTo(ConfigTuple o) {
        if (param != null && o != null && o.getParam() != null) {
            return param.getCaption().compareTo(o.getParam().getCaption());
        }
        return 0;
    }
}
