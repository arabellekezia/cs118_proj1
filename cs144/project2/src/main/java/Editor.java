import java.io.IOException;
import java.sql.* ;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Servlet implementation class for Servlet: ConfigurationTest
 *
 */
public class Editor extends HttpServlet {
    /**
     * The Servlet constructor
     *
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public Editor() {}

    public void init() throws ServletException
    {
        /*  write any servlet initialization code here or remove this function */
    }

    public void destroy()
    {
        /*  write any servlet cleanup code here or remove this function */
    }

    /**
     * Handles HTTP GET requests
     *
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
     *      HttpServletResponse response)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
      Connection con = null;
      try{
        Class.forName("org.mariadb.jdbc.Driver");
      } catch (ClassNotFoundException ex){
        System.out.println(ex);
      }

      try{
        con = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CS144", "cs144", "");
      } catch (SQLException ex){
        System.out.println("SQLException caught");
      }

      // get all the params we need
      String action = request.getParameter("action");
      String username = request.getParameter("username");
      String postid = request.getParameter("postid");
      String title = request.getParameter("title");
      String body = request.getParameter("body");

      // convert postid to int
      int id = 0;
      if (postid != null){
        id = Integer.parseInt(postid);
      }

      // check for errors
      int err = getError("GET", action, username);
      if (err != 200){
        response.sendError(400, "Invalid GET request for action=" + action);
        return;
      }

      switch (action){
        case "open":
          if (id == 0){
            // if no title and body passed, set to empty string
            if (title == null) { title = ""; }
            if (body == null) { body = ""; }
          }
          else if (title == null || body == null){
            // if eithe the title or body is null, try to get existing vals
            PostInfo post = getPostInfo(username, id);
            if (post != null){
              if (title == null){
                title = post.title;
              }
              if (body == null){
                body =  post.body;
              }
            }
            else if (post == null){
              response.sendError(404, "Cannot find username and postid during action=" + action);
              return;
            }
          }

          // set the attributes based on the params
          request.setAttribute("title", title);
          request.setAttribute("body", body);
          request.getRequestDispatcher("/edit.jsp").forward(request, response);
          break;

        case "preview":
          if (title == null || body == null){
            response.sendError(404, "Require title and body for action=" + action);
            return;
          }

          // use commonmark Java library to create HTML format
          Parser parser = Parser.builder().build();
          HtmlRenderer renderer = HtmlRenderer.builder().build();
          String title_html = renderer.render(parser.parse(title));
          String body_html = renderer.render(parser.parse(body));

          // set the attributes
          request.setAttribute("username", username);
          request.setAttribute("postid", id);
          request.setAttribute("title", title);
          request.setAttribute("body", body);
          request.setAttribute("title_html", title_html);
          request.setAttribute("body_html", body_html);
          request.getRequestDispatcher("/preview_page.jsp").forward(request, response);
          break;

        case "list":
          List<PostInfo> posts = getInfos(username);

          // if no posts found given username, then username not in DB
          if (posts == null){
            response.sendError(404, "Cannot find username and postid during action=" + action);
            return;
          }

          // set each values that we need to present in separate lists
          List<String> l_user = new ArrayList<String>();
          List<Integer> l_id = new ArrayList<Integer>();
          List<String> l_title = new ArrayList<String>();
          List<String> l_created = new ArrayList<String>();
          List<String> l_modified = new ArrayList<String>();

          for (PostInfo p : posts){
            l_user.add(p.username);
            l_id.add(p.postid);
            l_title.add(p.title);
            l_created.add(p.created.toString());
            l_modified.add(p.modified.toString());
          }

          // set the attributes to be presented in list_page.jsp
          request.setAttribute("users", l_user);
          request.setAttribute("ids", l_id);
          request.setAttribute("titles", l_title);
          request.setAttribute("modified_times", l_modified);
          request.setAttribute("created_times", l_created);

          // set count to loop in jsp
          request.setAttribute("post_ct", posts.size());

          // send request to list_page.jsp
          request.getRequestDispatcher("/list_page.jsp").forward(request, response);
          break;

        default:
          response.sendError(400, "Invalid request: action not recognized");
          return;
      }

      try{
        con.close();
      } catch (Exception e){ /* ignored */ }
    }


    /**
     * Handles HTTP POST requests
     *
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
     *      HttpServletResponse response)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
      Connection con = null;
      try{
        Class.forName("org.mariadb.jdbc.Driver");
      } catch (ClassNotFoundException ex){
        System.out.println(ex);
      }

      try{
        con = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CS144", "cs144", "");
      } catch (SQLException ex){
        System.out.println("SQLException caught");
      }

      // get all the params we need
      String action = request.getParameter("action");
      String username = request.getParameter("username");
      String postid = request.getParameter("postid");
      String title = request.getParameter("title");
      String body = request.getParameter("body");

      // convert postid to int
      int id = 0;
      if (postid != null){
        id = Integer.parseInt(postid);
      }

      // check for errors
      int err = getError("POST", action, username);
      if (err != 200){
        response.sendError(400, "POST: " + err + " " + action + " " + username);
        return;
      }

      switch (action){
        case "open":
          if (id == 0){
            // if no title and body passed, set to empty string
            if (title == null) { title = ""; }
            if (body == null) { body = ""; }
          }
          else if (title == null || body == null){
            // if eithe the title or body is null, try to get existing vals
            PostInfo post = getPostInfo(username, id);
            if (post != null){
              if (title == null){
                title = post.title;
              }
              if (body == null){
                body =  post.body;
              }
            }
            else if (post == null){
              response.sendError(404, "Cannot find username and postid during action=" + action);
              return;
            }
          }

          // set the attributes based on the params
          request.setAttribute("title", title);
          request.setAttribute("body", body);
          request.getRequestDispatcher("/edit.jsp").forward(request, response);
          break;

        case "delete":
          // if no rows affected, (username, postid) not found
          if (deletePost(username, id) == 0){
            response.sendError(404, "Cannot find username and postid during action=" + action);
            return;
          }
          request.getRequestDispatcher("/refresh.jsp").forward(request, response);
          break;

        case "save":
          if (id <= 0){
            id = 0;
          }
          if (title == null || body == null){
            response.sendError(404, "Require title and body for action=" + action);
            return;
          }
          else{
            // if savePost returns 0, no rows affected and (username, postid) not found
            if (savePost(username, id, title, body) == 0){
              response.sendError(404, "Cannot find username and postid during action=" + action);
              return;
            }
          }

        case "list":
          List<PostInfo> posts = getInfos(username);

          if (posts == null){
            response.sendError(404, "Cannot find username and postid during action=" + action);
            return;
          }

          // set each values that we need to present in separate lists
          List<String> l_user = new ArrayList<String>();
          List<Integer> l_id = new ArrayList<Integer>();
          List<String> l_title = new ArrayList<String>();
          List<String> l_created = new ArrayList<String>();
          List<String> l_modified = new ArrayList<String>();

          for (PostInfo p : posts){
            l_user.add(p.username);
            l_id.add(p.postid);
            l_title.add(p.title);
            l_created.add(p.created.toString());
            l_modified.add(p.modified.toString());
          }

          // set the attributes to be presented in list_page.jsp
          request.setAttribute("users", l_user);
          request.setAttribute("ids", l_id);
          request.setAttribute("titles", l_title);
          request.setAttribute("modified_times", l_modified);
          request.setAttribute("created_times", l_created);

          // set count to loop in jsp
          request.setAttribute("post_ct", posts.size());

          // send request to list_page.jsp
          request.getRequestDispatcher("/list_page.jsp").forward(request, response);
          break;

        case "preview":
          if (title == null || body == null){
            response.sendError(404, "Require title and body for action=" + action);
            return;
          }

          // use commonmark Java library to create HTML format
          Parser parser = Parser.builder().build();
          HtmlRenderer renderer = HtmlRenderer.builder().build();
          String title_html = renderer.render(parser.parse(title));
          String body_html = renderer.render(parser.parse(body));

          // set the attributes
          request.setAttribute("username", username);
          request.setAttribute("postid", id);
          request.setAttribute("title_html", title_html);
          request.setAttribute("body_html", body_html);
          request.getRequestDispatcher("/preview_page.jsp").forward(request, response);
          break;

        default:
          response.sendError(400, "Invalid request: action not recognized");
          return;

      }
      try{
        con.close();
      } catch (Exception e){ /* ignored */ }

    }


    private int savePost(String username, int postid, String title, String body){

      Connection c = null;
      PreparedStatement s1 = null;
      PreparedStatement s2 = null;
      ResultSet rs1 = null;
      int res = 0;

      try {
            c = DriverManager.getConnection("jdbc:mysql://localhost:3306/CS144", "cs144", "");

            // if postid is 0 -> add post to DB
            if (postid <= 0){
              // find max postid
              s1 = c.prepareStatement("SELECT MAX(postid) max FROM Posts WHERE username=?");
              s1.setString(1, username);
              rs1 = s1.executeQuery();

              int new_postid = 1;
              if (rs1.next()){
                new_postid = rs1.getInt("max") + 1;
              }

              // get current time
              Timestamp now = new Timestamp(System.currentTimeMillis());

              // insert values into DB with new postid
              s2 = c.prepareStatement("INSERT INTO Posts VALUES (?,?,?,?,?,?)");
              s2.setString(1, username);
              s2.setInt(2, new_postid);
              s2.setString(3, title);
              s2.setString(4, body);
              s2.setTimestamp(5, now);
              s2.setTimestamp(6, now);

              res = s2.executeUpdate();
            }

            else{
              // update the existing file based on username and postid
              s1 = c.prepareStatement("UPDATE Posts SET title=?, body=?, modified=? WHERE postid=? AND username=?");

              // get time
              Timestamp now = new Timestamp(System.currentTimeMillis());

              // set parameters to be updated
              s1.setString(1, title);
              s1.setString(2, body);
              s1.setTimestamp(3, now);
              s1.setInt(4, postid);
              s1.setString(5, username);

              res = s1.executeUpdate();
            }
        } catch (SQLException ex){
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ) {
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
        } finally {
            try { rs1.close(); } catch (Exception e) { /* ignored */ }
            try { s1.close(); } catch (Exception e) { /* ignored */ }
            try { s2.close(); } catch (Exception e) { /* ignored */ }
            try { c.close(); } catch (Exception e) { /* ignored */ }
            return res;
        }

    }


    private PostInfo getPostInfo(String username, int postid){
      if (username == null){
        return null;
      }

      Connection c = null;
      PreparedStatement s = null;
      ResultSet rs = null;

      PostInfo p = null;
      try {
            /* create an instance of a Connection object */
            c = DriverManager.getConnection("jdbc:mysql://localhost:3306/CS144", "cs144", "");

            s = c.prepareStatement("SELECT * FROM Posts WHERE username=? AND postid=?");
            s.setString(1, username);
            s.setInt(2, postid);
            rs = s.executeQuery();

            if (rs.next()){
              // set each values in each row to the posts list
              String user = rs.getString("username");
              int id = rs.getInt("postid");
              String title = rs.getString("title");
              String body = rs.getString("body");
              Timestamp mod = rs.getTimestamp("modified");
              Timestamp created = rs.getTimestamp("created");

              // create new instance of PostInfo to return
              p = new PostInfo(user, id, title, body, mod, created);
            }
            else{
              p = null;
            }

        } catch (SQLException ex){
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ) {
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
        } finally {
            try { rs.close(); } catch (Exception e) { /* ignored */ }
            try { s.close(); } catch (Exception e) { /* ignored */ }
            try { c.close(); } catch (Exception e) { /* ignored */ }
            return p;
        }
    }

    private int deletePost(String username, int postid){
      if (username == "" || username.length() == 0){
        return -1;
      }

      Connection c = null;
      PreparedStatement s = null;
      ResultSet rs = null;
      int ret = 0;

      try {
        /* create an instance of a Connection object */
        c = DriverManager.getConnection("jdbc:mysql://localhost:3306/CS144", "cs144", "");

        s = c.prepareStatement("DELETE FROM Posts WHERE username=? AND postid=?");
        s.setString(1, username);
        s.setInt(2, postid);

        ret = s.executeUpdate();

        } catch (SQLException ex){
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ) {
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
        } finally {
            try { rs.close(); } catch (Exception e) { /* ignored */ }
            try { s.close(); } catch (Exception e) { /* ignored */ }
            try { c.close(); } catch (Exception e) { /* ignored */ }
            return ret;
        }

    }

    private List<PostInfo> getInfos(String username){
      if (username == "" || username == null){
        return null;
      }

      Connection c = null;
      PreparedStatement s = null;
      ResultSet rs = null;

      List<PostInfo> posts = new ArrayList<PostInfo>();
      try {
            /* create an instance of a Connection object */
            c = DriverManager.getConnection("jdbc:mysql://localhost:3306/CS144", "cs144", "");

            s = c.prepareStatement("SELECT * FROM Posts WHERE username=?");
            s.setString(1, username);
            rs = s.executeQuery();

            // if the username does not exist, return null
            if (rs.next()){
              rs.previous();
              // set each values in each row to the posts list
              while(rs.next()){
                String user = rs.getString("username");
                int id = rs.getInt("postid");
                String title = rs.getString("title");
                String body = rs.getString("body");
                Timestamp mod = rs.getTimestamp("modified");
                Timestamp created = rs.getTimestamp("created");

                // add each post by the username to the list of posts
                PostInfo temp = new PostInfo(user, id, title, body, mod, created);
                posts.add(temp);
              }
            }
            else{
              posts = null;
            }

        } catch (SQLException ex){
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ) {
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
        } finally {
            try { rs.close(); } catch (Exception e) { /* ignored */ }
            try { s.close(); } catch (Exception e) { /* ignored */ }
            try { c.close(); } catch (Exception e) { /* ignored */ }
            return posts;
        }
    }

    private int getError(String method, String action, String username){
      if (action.equals("save") || action.equals("delete")){
        if (method.equals("GET") || username == null || username == ""){
          return 400;
        }
        else{
          return 200;
        }
      }
      else if (action == null || username == null || username == ""){
        return 400;
      }
      else if (action.equals("open") || action.equals("preview") || action.equals("list")){
        return 200;
      }
      else{
        return 400;
      }
    }
}

