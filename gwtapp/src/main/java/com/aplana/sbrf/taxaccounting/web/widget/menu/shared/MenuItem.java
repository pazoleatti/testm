package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import java.io.Serializable;

public class MenuItem implements Serializable{
	private static final long serialVersionUID = -2957561387604546546L;
		
	private String name;
	private String link;
	
	public MenuItem(){};
	
	public MenuItem(String name, String link) {
		super();
		this.name = name;
		this.link = link;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}

}
