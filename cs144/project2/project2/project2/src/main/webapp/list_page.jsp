<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Post List</title>
</head>
<body>
    <form action="post" method="POST">
        <div>
            <button type="submit" name="action" value="open">New Post</button>
            <input type="hidden" name="username" value="<%=request.getParameter("username")%>">
            <input type="hidden" name="postid" value="0">
        </div>
    </form>

    <div><h1>Post List</h1></div>

    <%@ page import="java.util.ArrayList"%>

    <table>
      <tr>
        <th>Title</th>
        <th>Date Created</th>
        <th> Date Modified</th>
      </tr>

      <%ArrayList<String> users = (ArrayList)request.getAttribute("users");%>
      <%ArrayList<Integer> ids = (ArrayList)request.getAttribute("ids");%>
      <%ArrayList<String> titles = (ArrayList)request.getAttribute("titles");%>
      <%ArrayList<String> modifs = (ArrayList)request.getAttribute("modified_times");%>
      <%ArrayList<String> creates = (ArrayList)request.getAttribute("created_times");%>
      <%int ct = Integer.parseInt(request.getAttribute("post_ct").toString());%>
      <%for (int i = 0; i < ct; i++){%>
      <tr>
        <form action="post" method="POST">
          <input type="hidden" name="username" value="<%out.print(users.get(i));%>">
          <input type="hidden" name="postid" value="<%out.print(ids.get(i));%>">
          <td><%out.print(titles.get(i));%></td>
          <td><%out.print(modifs.get(i));%></td>
          <td><%out.print(creates.get(i));%></td>

          <td>
            <button type="submit" name="action" value="open">Open</button>
            <button type="submit" name="action" value="delete">Delete</button>
          </td>
        </form>
      </tr>
      <%}%>

    </table>
</body>
</html>
