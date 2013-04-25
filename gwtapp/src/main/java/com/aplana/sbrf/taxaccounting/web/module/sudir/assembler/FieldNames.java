package com.aplana.sbrf.taxaccounting.web.module.sudir.assembler;

import java.util.HashMap;
import java.util.Map;

public enum FieldNames {
	
	LOGIN(0) {
		@Override
		public String nameField() {
			return "LOGIN";
		}
	},
	NAME(1) {
		@Override
		public String nameField() {
			return "NAME";
		}
	},
	DEPARTAMENT_ID(2) {
		@Override
		public String nameField() {
			return "DEPARTAMENT_ID";
		}
	},
	IS_ACTIVE(3) {
		@Override
		public String nameField() {
			return "IS_ACTIVE";
		}
	},
	EMAIL(4) {
		@Override
		public String nameField() {
			return "EMAIL";
		}
	},
	ROLE_CODE(5) {
		@Override
		public String nameField() {
			return "ROLE_CODE";
		}
	};
	
	private int id;
	FieldNames(int id){this.id = id;}
	public abstract String nameField();
	private int getId(){
		return id;
	}
	
	public static Map<String, Integer> getFieldNamesMap(){
		Map<String, Integer> fieldNames = new HashMap<String, Integer>();
		for (int i = 0; i < FieldNames.values().length; i++)
			fieldNames.put(FieldNames.values()[i].nameField(), FieldNames.values()[i].getId());
		return fieldNames;
	}

}
