package com.aplana.sbrf.taxaccounting.web.module.sudir.ws.assembler;

import java.util.HashMap;
import java.util.Map;

public enum FieldNames {
	
	LOGIN() {
		@Override
		public String nameField() {
			return "LOGIN";
		}
	},
	NAME() {
		@Override
		public String nameField() {
			return "NAME";
		}
	},
	DEPARTAMENT_ID() {
		@Override
		public String nameField() {
			return "DEPARTAMENT_ID";
		}
	},
	IS_ACTIVE() {
		@Override
		public String nameField() {
			return "IS_ACTIVE";
		}
	},
	EMAIL() {
		@Override
		public String nameField() {
			return "EMAIL";
		}
	},
	ROLE_CODE() {
		@Override
		public String nameField() {
			return "ROLE_CODE";
		}
	};

	public abstract String nameField();

	public static Map<String, FieldNames> getFieldNamesMap(){
		Map<String, FieldNames> fieldNames = new HashMap<String, FieldNames>(FieldNames.values().length);
		for (int i = 0; i < FieldNames.values().length; i++)
			fieldNames.put(FieldNames.values()[i].nameField(), FieldNames.values()[i]);
		return fieldNames;
	}

    public static boolean containsName(String attrName){
        for (int i = 0; i < FieldNames.values().length; i++)
            if (FieldNames.values()[i].nameField().equals(attrName))
                return true;
        return false;
    }

    public static FieldNames getByName(String attrName){
        for (int i = 0; i < FieldNames.values().length; i++)
            if (FieldNames.values()[i].nameField().equals(attrName))
                return FieldNames.values()[i];
        return null;
    }
}
