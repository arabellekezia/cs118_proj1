<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Error</title>
    <h1>Error: <%=request.getAttribute("error_code")%></h1>
</head>
<body>
  <div>
    <%=request.getAttribute("error_msg")%>
  </div>
</body>
</html>
