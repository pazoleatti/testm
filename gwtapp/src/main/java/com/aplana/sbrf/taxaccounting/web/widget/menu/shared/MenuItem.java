package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MenuItem implements Serializable{
	private static final long serialVersionUID = -2957561387604546546L;
		
	private String name;
	private String link;
	private String meta;
	private List<MenuItem> subMenu = new ArrayList<MenuItem>();

	public MenuItem(){};

	public MenuItem(String name) {
		super();
		this.name = name;
		link = "";
	}

	public MenuItem(String name, String link) {
		super();
		this.name = name;
		this.link = link;
	}

	public MenuItem(String name, String link, String meta) {
		super();
		this.name = name;
		this.link = link;
		this.meta = meta;
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

	public List<MenuItem> getSubMenu() {
		return subMenu;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}
}
