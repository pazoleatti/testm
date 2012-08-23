<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<script type="text/javascript">
	dojo.require('dijit.form.Button');
	dojo.require('dojo.parser');
</script>
<h1>Здесь будет выводиться список данных по налоговой форме</h1>
<portlet:actionURL var="createNewURL" portletMode="edit">
	<portlet:param name="action" value="new"/>
</portlet:actionURL>
<button dojoType="dijit.form.Button" onClick="window.location.href = '${createNewURL}'">Добавить</button>