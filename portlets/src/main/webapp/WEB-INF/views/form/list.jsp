<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<h1>Список налоговых форм</h1>
<c:forEach items="${forms}" var="form">
	<portlet:actionURL portletMode="edit" name="edit" var="editUrl">
		<portlet:param name="formId" value="${form.id}"/>
	</portlet:actionURL>
	<a href="${editUrl}"><c:out value="${form.id}"/> - <c:out value="${form.type.name}"/></a><br/>
</c:forEach>