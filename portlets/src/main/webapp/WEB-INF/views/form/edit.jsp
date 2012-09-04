<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<style type="text/css"><%-- TODO: переделать импорт CSS --%>
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/Grid.css";
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/tundraGrid.css";
</style>
<portlet:defineObjects />
<c:set var="namespace"><portlet:namespace /></c:set>
<script type="text/javascript">
	dojo.require('dojo.parser');
	dojo.require('dojo.data.ItemFileWriteStore');
	dojo.require('dijit.form.Button');
	dojo.require('dijit.layout.BorderContainer');
	dojo.require('dijit.layout.TabContainer');
	dojo.require('dijit.layout.ContentPane');
	dojo.require('dojox.grid.DataGrid');
	dojo.require('dojox.grid.cells.dijit');

	var ${namespace}_columns = ${columnsJson};
	var ${namespace}_columnsGrid;

	dojo.addOnLoad(function(){
		dojo.parser.parse();
		var store = new dojo.data.ItemFileWriteStore({data: ${namespace}_columns});
		${namespace}_columnsGrid = new dojox.grid.DataGrid({
			store: store,
			structure: [[
				{field: 'alias', width: '25em', name: 'Алиас'},
				{field: 'name', width: '25em', name: 'Наименование'}
			]],
			autoHeight: true,
			canSort: function(index) { return false; }
		});
		${namespace}_columnsGrid.placeAt('${namespace}_columnsGridDiv');
		${namespace}_columnsGrid.startup();
	});
</script>

<div id="${namespace}_columnsGridDiv"></div>
<%-- 
<div dojoType="dijit.layout.TabContainer" style="width: 1000px; height: 750px;">
	<div dojoType="dijit.layout.ContentPane" title="Столбцы">
		Список столбцов
		
	</div>
	<div dojoType="dijit.layout.ContentPane" title="Строки">
		Описание строк формы
		<table id="${namespace}_rowsGrid"></table>
	</div>
	<div dojoType="dijit.layout.ContentPane" title="Скрипты">
		Описание скриптов
		<table id="${namespace}_scriptsGrid"></table>
	</div>
</div>
--%>
<portlet:renderURL portletMode="view" var="backUrl" />
<a href="${backUrl}">Назад</a>