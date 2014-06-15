
<%@page
   import="com.chechev.phd.recommendedstream.datatypes.ExtendedPost"%>
<%@page import="com.chechev.phd.datacollectors.UserDataCollector"%>
<%@page import="com.chechev.phd.datacollectors.CollectorManager"%>
<%@page import="com.chechev.phd.datacollectors.NewsFeedCollector"%>
<%@page import="com.chechev.phd.utils.GlobalConstants"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.util.Properties"%>
<%@page import="com.restfb.FacebookClient"%>
<%@page import="com.restfb.types.Comment"%>
<%@page import="com.chechev.phd.recommendedstream.services.XPostWS"%>
<%@page import="com.restfb.json.JsonArray"%>
<%@page import="com.restfb.DefaultFacebookClient"%>
<%@page import="com.restfb.JsonMapper"%>
<%@page import="com.restfb.DefaultJsonMapper"%>
<%@page import="com.restfb.types.Post"%>
<%@page import="java.nio.charset.Charset"%>
<%@page import="com.chechev.phd.search.DataSearcher"%>
<%@page import="java.util.List"%>
<%@page import="java.util.LinkedList"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="org.apache.http.entity.StringEntity"%>
<%@page import="org.apache.http.client.methods.HttpPost"%>
<%@page import="java.io.IOException"%>
<%@page import="org.apache.http.client.ClientProtocolException"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="org.apache.http.HttpResponse"%>
<%@page import="org.apache.http.client.methods.HttpGet"%>
<%@page import="org.apache.http.impl.client.DefaultHttpClient"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="refresh" content="1200" />
<title>Recommended Stream</title>
<script src="js/jquery-1.8.2.js"></script>
<script src="js/recsys.js"></script>



</head>
<%
   //Properties prop = new Properties();
        //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //prop.load(classLoader.getResourceAsStream("/configuration.properties"));
        String MY_APP_ID = GlobalConstants.APP_ID;
        String MY_APP_SECRET = GlobalConstants.APP_SECRET;
        String MY_APP_URL = GlobalConstants.APP_URL;
        System.out.println(MY_APP_ID);
        System.out.println(MY_APP_SECRET);
        System.out.println(MY_APP_URL);
%>
<body>
   <div id="fb-root"></div>
   <script>
            window.fbAsyncInit = function() {
                // init the FB JS SDK
                FB.init({
                    appId      : '<%=MY_APP_ID%>', // App ID from the App Dashboard
                    status     : true, // check the login status upon init?
                    cookie     : true, // set sessions cookies to allow your server to access the session?
                    xfbml      : true  // parse XFBML tags on this page?
                });

                // Additional initialization code such as adding Event Listeners goes here

            };

            // Load the SDK's source Asynchronously
            (function(d, debug){
                var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
                if (d.getElementById(id)) {return;}
                js = d.createElement('script'); js.id = id; js.async = true;
                js.src = "//connect.facebook.net/en_US/all" + (debug ? "/debug" : "") + ".js";
                ref.parentNode.insertBefore(js, ref);
            }(document, /*debug*/ false));
            //FB.Canvas.setAutoGrow(7);


        </script>

   <div style="display: none;" id="user_name"></div>

   <%
      System.out.println("start loading index page");
               long startall = System.currentTimeMillis();
               String access_token = (String) request.getSession().getAttribute("access_token");
               if (access_token == null && request.getParameter("code") == null) {
   %>
   <script>
            var oauth_url = 'https://www.facebook.com/dialog/oauth/';
            oauth_url += '?client_id= <%=MY_APP_ID%>';
            oauth_url += '&redirect_uri=' + encodeURIComponent('<%=MY_APP_URL%>');
            oauth_url += '&scope=email,read_stream,offline_access,friends_photos,friends_status,friends_likes'

            window.top.location = oauth_url;
        </script>
   <%
      } else if (access_token == null) {


               DefaultHttpClient httpClient1 = new DefaultHttpClient();
               HttpGet getRequest1 = new HttpGet("https://graph.facebook.com/oauth/access_token?client_id=" + MY_APP_ID + "&redirect_uri=" + MY_APP_URL
                       + "&client_secret=" + MY_APP_SECRET + "&code=" + request.getParameter("code"));
               //getRequest1.addHeader("accept", "application/json");
               HttpResponse resp1 = httpClient1.execute(getRequest1);

               if (resp1.getStatusLine().getStatusCode() != 200) {
                   throw new RuntimeException("Failed : HTTP error code : "
                           + resp1.getStatusLine().getStatusCode());
               }
               BufferedReader br1 = new BufferedReader(
                       new InputStreamReader((resp1.getEntity().getContent())));

               String line = br1.readLine();
               System.out.println(line);
               access_token = line.split("&expires=")[0].substring(13);
               request.getSession().setAttribute("access_token", access_token);
               httpClient1.getConnectionManager().shutdown();
               if (!NewsFeedCollector.userMap.containsKey(CollectorManager.getUserId(access_token)) && request.getParameter("firstvisit") == null) { // TODO: the new user check to be changed!
                   CollectorManager.addUserToCollect(access_token);
   %>

   <script>
            var oauth_url ="<%=MY_APP_URL%>intro.jsp"
            window.top.location = oauth_url;
        </script>
   <%
      } else {
               CollectorManager.addUserToCollect(access_token);
   %>

   <script>
            var oauth_url ="<%=MY_APP_URL%>"
            window.top.location = oauth_url;
        </script>
   <%
      }
           } else {
               System.out.println("access token get from session=" + access_token);


               String query = request.getParameter("query");
               if (query == null) {
                   query = "";
               }
   %>



   <table
      style="background-image: url('images/logo.png'); height: 52px; width: 100%">
      <tr>
         <td width="60px"><a href="/RecommendedStream/">Home</a></td>
         <td>
            <center>
               <font size="5">Recommended Stream </font>
            </center>
         </td>
         <td width="60px"><a href="/RecommendedStream/config.jsp">Config</a>
         </td>
         <td width="60px"><a href="/RecommendedStream/about.jsp">About</a>
         </td>
      </tr>
   </table>

   <center>

      <br />
      <form accept-charset="UTF-8" name="formname" action="" method="GET">
         <input method="GET" type="text" name="query" value="<%=query%>"
            size="70"> <input type="submit" value="Search">
      </form>
      <br />

      <%
         //boolean publish_permission =
                      List<ExtendedPost> resultList = new LinkedList<ExtendedPost>();
                      if (!query.equals("")) {
                          resultList = new DataSearcher().getPosts(query, CollectorManager.getUserId(access_token),  200);
                      } else {
      //                    resultList = (new ExtendedPost()).(access_token, 0, 100);
                      }
                      if (query.equals("") && resultList.size() == 0) {
                          request.getSession().removeAttribute("access_token");
      %>
      <script>
                alert("The application cannot retrieve any posts from your news feed.\nPlease, ensure that you have provide read_stream permissions to it");
                var oauth_url = 'https://www.facebook.com/dialog/oauth/';
                oauth_url += '?client_id= <%=MY_APP_ID%>';
                oauth_url += '&redirect_uri=' + encodeURIComponent('<%=MY_APP_URL%>
         ');
         oauth_url += '&scope=email,read_stream,offline_access,friends_photos,friends_status,friends_likes'

         window.top.location = oauth_url;
      </script>

      <%
         }
            out.println("<table id='mainTableX' width=80%  style='word-wrap: break-word;'>");
            for (Post xpost : resultList) {
               out.println("<tr><td>");
               out.println("<table height=100% width=100% bgcolor='#F5F7FA' style=\"border-top:solid;border-color:#7287B5\"><tr><td><table width=100% ><tr><td width=50 valign='top'>");
               out.println("<img style='position:static;top:0px;' src=http://graph.facebook.com/"
                     + xpost.getFrom().getId() + "/picture>");
               out.println("<br/>");
               out.println(xpost.getType());
               out.println("<br/>");
               if (xpost.getIcon() != null) {
                  out.println("<img style='position:static;top:0px;' src="
                        + xpost.getIcon() + ">");
                  out.println("<br/>");
               }
               long time = System.currentTimeMillis()
                     - (xpost.getUpdatedTime().getTime());
               //if(time/3600000l>0){
               //    out.println(time/3600000 + "h ago");
               //}else if(time/60000l > 1){
               //    out.println(time/60000 + "m ago");
               // }
               out.println("</td><td><table width=100% ><tr><td>");
               out.println("<a target='_blank' href='http://facebook.com/"
                     + xpost.getFrom().getId() + "'>"
                     + xpost.getFrom().getName() + "</a>");
               if (xpost.getTo() != null
                     && xpost.getTo().size() > 0
                     && !xpost
                           .getTo()
                           .get(0)
                           .getId()
                           .equals(CollectorManager
                                 .getUserId(access_token))) {
                  out.println("&nbsp;to&nbsp;<a target='_blank' href='http://facebook.com/"
                        + xpost.getTo().get(0).getId()
                        + "'>"
                        + xpost.getTo().get(0).getName() + "</a>");
               }
               out.println("<br/>");
               String pictureUrl = xpost.getPicture();
               if (xpost.getPicture() != null) {
                  if (xpost.getPicture().startsWith(
                        "http://external.ak.fbcdn.net/safe_image.php")) {
                     pictureUrl = URLDecoder
                           .decode(xpost
                                 .getPicture()
                                 .replaceAll(
                                       "http://external.ak.fbcdn.net/safe_image.php?.*&url=",
                                       "").replaceAll("&.*", ""),
                                 "UTF-8");
                  } else {
                     pictureUrl = xpost.getPicture().replace("_s.jpg",
                           "_n.jpg");
                  }
               }
               out.println("</td></tr><tr><td><table width=100% ><tr>");
               if (xpost.getLink() != null && xpost.getPicture() != null) {
                  out.println("<td width=500px bgcolor=black><center>");
                  out.println("<a target='_blank' href=\""
                        + xpost.getLink() + "\">");
                  if (xpost.getPicture().startsWith(
                        "http://external.ak.fbcdn.net/safe_image.php")) {
                     pictureUrl = URLDecoder
                           .decode(xpost
                                 .getPicture()
                                 .replaceAll(
                                       "http://external.ak.fbcdn.net/safe_image.php?.*&url=",
                                       "").replaceAll("&.*", ""),
                                 "UTF-8");
                  } else {
                     pictureUrl = xpost.getPicture().replace("_s.jpg",
                           "_n.jpg");

                  }
                  out.println("<img   style='min-width:180px;max-width:500px;' src="
                        + pictureUrl + ">");
                  out.println("</a>");
                  out.println("</center></td>");
               } else if (xpost.getPicture() != null) {
                  out.println("<td width=10>");
                  out.println("<img  src=" + xpost.getPicture() + ">");
                  out.println("</td>");
               }

               out.println("<td valign='bottom'>");
               if (xpost.getMessage() != null) {
                  if (xpost.getMessage().length() < 1000) {
                     out.println(xpost.getMessage()
                           .replaceAll("\\\\n\\\\r", "<br/>")
                           .replaceAll("\\\\n", "<br/>"));
                  } else {
                     out.println(xpost.getMessage()
                           .replaceAll("\\\\n\\\\r", "<br/>")
                           .replaceAll("\\\\n", "<br/>")
                           .substring(0, 1000)
                           + " ...");
                  }
                  out.println("<br/>");
                  out.println("<br/>");
               }
               if (xpost.getName() != null) {

                  if (xpost.getName().length() < 1000) {
                     out.println(xpost.getName()
                           .replaceAll("\\\\n\\\\r", "<br/>")
                           .replaceAll("\\\\n", "<br/>"));
                  } else {
                     out.println(xpost.getName()
                           .replaceAll("\\\\n\\\\r", "<br/>")
                           .replaceAll("\\\\n", "<br/>")
                           .substring(0, 1000)
                           + " ...");
                  }
                  out.println("<br/>");
               }
               if (xpost.getCaption() != null) {
                  if (xpost.getCaption().length() < 1000) {
                     out.println(xpost.getCaption()
                           .replaceAll("\\\\n\\\\r", "<br/>")
                           .replaceAll("\\\\n", "<br/>"));
                  } else {
                     out.println(xpost.getCaption()
                           .replaceAll("\\\\n\\\\r", "<br/>")
                           .replaceAll("\\\\n", "<br/>")
                           .substring(0, 1000)
                           + " ...");
                  }
                  out.println("<br/>");
               }
               if (xpost.getDescription() != null) {
                  if (xpost.getDescription().length() < 1000) {
                     out.println(xpost.getDescription()
                           .replaceAll("\\\\n\\\\r", "<br/>")
                           .replaceAll("\\\\n", "<br/>"));
                  } else {
                     out.println(xpost.getDescription()
                           .replaceAll("\\\\n\\\\r", "<br/>")
                           .replaceAll("\\\\n", "<br/>")
                           .substring(0, 1000)
                           + " ...");
                  }
                  out.println("<br/>");
               }

               if (xpost.getMessage() == null
                     && xpost.getCaption() == null
                     && xpost.getDescription() == null
                     && xpost.getName() == null) {
                  out.println("<a target='_blank' href=\"http://facebook.com/"
                        + xpost.getId().replace("_", "/posts/")
                        + "\">link</a>");
               }
               out.println("<br/>");

               //comments likes
               out.println("<table width=100% style=\"border-top:solid;border-width:1px;border-color:#7287B5\"><tr><td>");

               if (xpost.getComments().getData().size() > 0) {

                  out.println(xpost.getComments().getCount()
                        + " Comments:<br/>"); //xpost.getComments().getCount()
                  out.println("<font size='2' color='darkblue'>");
                  int counter = 0;
                  for (Comment comment : xpost.getComments().getData()) {
                     out.println("<img height=14px style='position:static;top:0px;' src=http://graph.facebook.com/"
                           + comment.getFrom().getId() + "/picture>");
                     out.println(comment.getFrom().getName() + " : "
                           + comment.getMessage());
                     out.println("<br/>");
                     counter++;
                  }
                  if (counter < xpost.getComments().getCount()) {
                     out.println("<a target='_blank' href=\"http://facebook.com/"
                           + xpost.getId().replace("_", "/posts/")
                           + "\">see all comments<a><br>");
                  }
                  out.println("</font>");

               }
               if (xpost.getLikesCount() != null
                     && xpost.getLikesCount() > 0) {
                  out.println("<div id=" + xpost.getId() + "_likes>"
                        + xpost.getLikes().getCount() + " likes </div>");
                  //out.println("<br/>");
               }
               String likeButtonText = "";
               String unlikeButtonText = "";
               if (xpost.getObjectId() != null
                     && UserDataCollector.userMap.get(
                           CollectorManager.getUserId(access_token))
                           .isLiked(xpost.getObjectId())) {
                  likeButtonText = ";display:none";
               } else {
                  unlikeButtonText = ";display:none";
               }

               out.println("<button style='background:#7287B5;cursor:pointer"
                     + likeButtonText
                     + "' id='like-button"
                     + xpost.getId()
                     + "' onclick=\"postRequest('"
                     + xpost.getId()
                     + "/likes');$('#like-button"
                     + xpost.getId()
                     + "').hide();$('#unlike-button"
                     + xpost.getId() + "').show();\">like</button>");
               out.println("<button style='background:#7287B5;cursor:pointer"
                     + unlikeButtonText
                     + "' id='unlike-button"
                     + xpost.getId()
                     + "'  onclick=\"deleteRequest('"
                     + xpost.getId()
                     + "/likes');$('#like-button"
                     + xpost.getId()
                     + "').show();$('#unlike-button"
                     + xpost.getId() + "').hide();\">unlike</button>");

               //if (xpost.getType().equals("photo")) {
               //    out.println("<button style='background:#7287B5;cursor:pointer;' id='share-button" + xpost.getId() + "'  onclick=\"popup(&quot;https://www.facebook.com/sharer.php?s=2&p%5B0%5D=" + xpost.getId().replace("_", "&p%5B1%5D=") + "&appid=" + MY_APP_ID + "&quot;)\">share</button>");
               //
               //} else
               if (xpost.getType().equals("status")) {
                  out.println("<button style='background:#7287B5;cursor:pointer;' id='share-button"
                        + xpost.getId()
                        + "'  onclick=\"popup(&quot;http://www.facebook.com/sharer.php?u="
                        + URLEncoder.encode(
                              "http://facebook.com/"
                                    + xpost.getId().replace("_",
                                          "/posts/"), "utf-8")
                        + "&quot;);\">share</button>");
               } else { //out.println("<a role=\"button\" title=\"Send this to friends or post it on your timeline.\" data-ft=\"{&quot;tn&quot;:&quot;J&quot;,&quot;type&quot;:25}\" rel=\"dialog\" href=\"https://www.facebook.com/ajax/sharer/?s=2&appid=2305272732&p%5B0%5D="+xpost.getId().split("_")[0] +"&p%5B1%5D="+xpost.getId().split("_")[1]+"\">share</a>");
                  //
                  String params = "";
                  if (xpost.getPicture() != null) {
                     params += "&p[images][0]="
                           + URLEncoder.encode(pictureUrl, "utf-8");
                  }
                  if (xpost.getLink() != null) {
                     params += "&p[url]="
                           + URLEncoder.encode(xpost.getLink(),
                                 "utf-8");
                  } else {
                     params += "&p[url]="
                           + URLEncoder.encode(
                                 "http://facebook.com/"
                                       + xpost.getId().replace(
                                             "_", "/posts/"),
                                 "utf-8");
                  }
                  if (xpost.getName() != null) {
                     params += "&p[title]=%22" + xpost.getName();
                  } else {
                     params += "&p[title]=%22"
                           + xpost.getFrom().getName() + "'s%20"
                           + xpost.getType();
                  }
                  if (xpost.getDescription() != null) {
                     params += "%22&p[summary]=%22"
                           + xpost.getDescription();
                  }
                  out.println("<button style='background:#7287B5;cursor:pointer;' id='share-button"
                        + xpost.getId()
                        + "'  onclick=\"popup(&quot;http://www.facebook.com/sharer.php?s=100"
                        + params
                        + "%22&appid="
                        + MY_APP_ID
                        + "&quot;)\">share</button>");
               }

               out.println("<input value='add comment' type='text' id='"
                     + xpost.getId()
                     + "'onkeypress=\"if(event.keyCode==13){addComment('"
                     + xpost.getId()
                     + "/comments',document.getElementById('"
                     + xpost.getId() + "').value);}\">");

               out.println("</td></tr></table>");
               out.println("</td>");
               //out.println("<td valign='top' align='right'> <table frame='border' bgcolor='#F5F5F5'><tr><td>");
               //if (xpost.getLikesCount() != null && xpost.getLikesCount() > 0) {
               //    out.println(xpost.getLikes().getCount() + " likes");
               //    out.println("<br/>");
               //}
               //out.println("</td></tr></table></td>");
               out.println("</tr></table></td></tr></table></td></tr></table><table width=100% ><tr><td>");
               if (time / 3600000l > 0) {
                  out.println(time / 3600000 + " hours ago");
               } else if (time / 60000l > 1) {
                  out.println(time / 60000 + " minutes ago");
               }
               out.println("</td><td align='right'>");

               out.println("</td></tr></table></td></tr></table></td></tr>");
            }

            out.println("</table>");

         }

         System.out.println("spended time="
               + (System.currentTimeMillis() - startall));
      %>

</body>
</html>
