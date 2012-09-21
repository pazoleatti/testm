<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects/>
<% Exception exception = (Exception) request.getAttribute("exception"); %>
<h1>Произошла ошибка: <%= exception.getMessage()%></h1>
<portlet:renderURL var="backUrl" portletMode="view" windowState="normal"></portlet:renderURL>
<p>Для возврата к исходной странице перейдите по <a href="${backUrl}">ссылке</a></p>
<p>
<h2>Стек ошибки:</h2>
<% 
	renderResponse.flushBuffer();
	exception.printStackTrace(renderResponse.getWriter());
%>
