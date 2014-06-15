
<%@page import="com.chechev.phd.recommendedstream.datatypes.ExtendedPost"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.bson.io.OutputBuffer"%>
<%@page import="java.nio.charset.Charset"%>
<%@page import="com.chechev.phd.search.DataSearcher"%>
<%@page import="com.restfb.types.Post"%>
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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Recommended Stream</title>
    </head>
    <body >
        <center><h3>!!! The Application is in process of Development. Alfa version is expected at 1 Nov 2012!!!</h3></center>
        <%
            long startall = System.currentTimeMillis();
            String access_token = "";
            if (request.getSession().getAttribute("access_token") == null && request.getParameter("code") == null) {

        %>
        <script>
            var oauth_url = 'https://www.facebook.com/dialog/oauth/';
            oauth_url += '?client_id=120873951361847';
            oauth_url += '&redirect_uri=' + encodeURIComponent('https://apps.facebook.com/recommended_stream/');
            oauth_url += '&scope=email,read_stream,offline_access,friends_photos,friends_status,friends_likes'

            window.top.location = oauth_url;
        </script>
        <%        } else {
            if (request.getSession().getAttribute("access_token") == null) {
                String MY_APP_ID = "120873951361847";
                String MY_APP_SECRET = "ffd3bdadb9fda7930b51b97e93fded47";
                DefaultHttpClient httpClient1 = new DefaultHttpClient();
                HttpGet getRequest1 = new HttpGet("https://graph.facebook.com/oauth/access_token?client_id=" + MY_APP_ID + "&redirect_uri=" + "https://apps.facebook.com/recommended_stream/"
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

            } else {
                access_token = (String) request.getSession().getAttribute("access_token");
                System.out.println("access token get from session=" + access_token);
            }
            System.out.println(request.getCharacterEncoding());

            String query = request.getParameter("query");

            System.out.println(query);
        %>



        <center>
            <h1>Recommended Stream </h1>
            <br/>
            <form accept-charset="UTF-8" name="formname" action="" method="GET">
                <input method="GET" type="text" name="query" size="70"> <input type="submit" value="Search Post">
            </form>
            <br/>

            <%
                    List<ExtendedPost> resultList = new LinkedList<ExtendedPost>();
                    if (query != null) {
                        if (query.trim().equals("")) {
                            query = "*";
                        }
                        resultList = new DataSearcher().getPosts(query, null, 0);
                    } else {
                        try {
                            DefaultHttpClient httpClient = new DefaultHttpClient();
                            HttpPost postRequest = new HttpPost("http://localhost:8080/RecommendedStream/data/posts/post");
                            StringEntity input = new StringEntity(access_token);
                            input.setContentType("application/json");
                            postRequest.setEntity(input);
                            HttpResponse resp = httpClient.execute(postRequest);

                            if (resp.getStatusLine().getStatusCode() != 201) {
                                throw new RuntimeException("Failed : HTTP error code : "
                                        + resp.getStatusLine().getStatusCode());
                            }
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader((resp.getEntity().getContent())));

                            String output;
                            while ((output = br.readLine()) != null) {
                                //just consume the response
                            }

                            HttpGet getRequest = new HttpGet("http://localhost:8080/RecommendedStream/data/posts/get?access_token=" + access_token);
                            getRequest.addHeader("accept", "application/json");
                            resp = httpClient.execute(getRequest);

                            if (resp.getStatusLine().getStatusCode() != 200) {
                                throw new RuntimeException("Failed : HTTP error code : "
                                        + resp.getStatusLine().getStatusCode());
                            }

                            br = new BufferedReader(
                                    new InputStreamReader((resp.getEntity().getContent()), "utf-8"));

                            while ((output = br.readLine()) != null) {
                                ObjectMapper mapper = new ObjectMapper();
                                output = output.substring(1, output.length() - 1);
                                if (output.length() > 2) {
                                    output = output.substring(1, output.length() - 1);
                                }
                                String[] posts = output.split("\\}\\,\\{");
                                int count = 0;
                                for (String post : posts) {
                                    count++;

                                    resultList.add(mapper.readValue("{" + post + "}", ExtendedPost.class));

                                }

                            }

                            httpClient.getConnectionManager().shutdown();

                        } catch (ClientProtocolException e) {

                            e.printStackTrace();

                        } catch (IOException e) {

                            e.printStackTrace();
                        }


                    }

                    List<ExtendedPost> resultList2 = new LinkedList<ExtendedPost>();
                    try {

                        DefaultHttpClient httpClient = new DefaultHttpClient();

                        HttpGet getRequest = new HttpGet("http://localhost:8080/RecommendedStream/data/posts/get_peronalized?access_token=" + access_token);
                        getRequest.addHeader("accept", "application/json");
                        HttpResponse resp = httpClient.execute(getRequest);

                        if (resp.getStatusLine().getStatusCode() != 200) {
                            throw new RuntimeException("Failed : HTTP error code : "
                                    + resp.getStatusLine().getStatusCode());
                        }

                        BufferedReader br = new BufferedReader(
                                new InputStreamReader((resp.getEntity().getContent()), "utf-8"));
                        String output;
                        while ((output = br.readLine()) != null) {
                            ObjectMapper mapper = new ObjectMapper();
                            output = output.substring(1, output.length() - 1);
                            if (output.length() > 2) {
                                output = output.substring(1, output.length() - 1);
                            }
                            String[] posts = output.split("\\}\\,\\{");
                            int count = 0;
                            for (String post : posts) {
                                count++;

                                resultList2.add(mapper.readValue("{" + post + "}", ExtendedPost.class));

                            }

                        }

                        httpClient.getConnectionManager().shutdown();

                    } catch (ClientProtocolException e) {

                        e.printStackTrace();

                    } catch (IOException e) {

                        e.printStackTrace();
                    }



                    out.println("<table width=70%  style='word-wrap: break-word;'>");

                    Iterator<ExtendedPost> iterator2 = resultList2.iterator();
                    if(resultList.size() != resultList2.size()){
                        System.err.println("Different result list sizes");
                    }
                    for (ExtendedPost xpost : resultList) {
                        out.println("<tr><td>");
                        out.println("<table width=100% frame='border'bgcolor='#FCFCFF'><tr><td width=50 valign='top'>");
                        out.println("<img style='position:static;top:0px;' src=http://graph.facebook.com/" + xpost.getFrom() + "/picture>");

                        out.println("</td><td><table><tr><td>");
                        out.println(xpost.getName());
                        out.println("</td></tr><tr><td><table><tr>");
                        if (xpost.getLink() != null && xpost.getPicture() != null) {
                            out.println("<td width=10>");
                            out.println("<a target='_blank' href=\"" + xpost.getLink() + "\">");
                            out.println("<img  src=" + xpost.getPicture() + ">");
                            out.println("</a>");
                            out.println("</td>");
                        } else if (xpost.getPicture() != null) {
                            out.println("<td width=10>");
                            out.println("<img  src=" + xpost.getPicture() + ">");
                            out.println("</td>");
                        }



                        out.println("<td>");
                        if (xpost.getDescription() != null) {
                            out.println(xpost.getDescription().replaceAll("\\\\n\\\\r", "<br/>").replaceAll("\\\\n", "<br/>"));
                            out.println("<br/>");
                            out.println(xpost.getUpdatedTime());
                            out.println("<br/>");
                        }
                        out.println(xpost.getType());
                        out.println("</td>");
                        out.println("</tr></table></td></tr></table></td></tr></table></td><td>");


                        ExtendedPost xpost2 = iterator2.next();
                        out.println("<table width=100% frame='border'bgcolor='#FCFCFF'><tr><td width=50 valign='top'>");
                        out.println("<img style='position:static;top:0px;' src=http://graph.facebook.com/" + xpost2.getFrom() + "/picture>");

                        out.println("</td><td><table><tr><td>");
                        out.println(xpost2.getName());
                        out.println("</td></tr><tr><td><table><tr>");
                        if (xpost2.getLink() != null && xpost2.getPicture() != null) {
                            out.println("<td width=10>");
                            out.println("<a target='_blank' href=\"" + xpost2.getLink() + "\">");
                            out.println("<img  src=" + xpost2.getPicture() + ">");
                            out.println("</a>");
                            out.println("</td>");
                        } else if (xpost2.getPicture() != null) {
                            out.println("<td width=10>");
                            out.println("<img  src=" + xpost2.getPicture() + ">");
                            out.println("</td>");
                        }
                        out.println("<td>");
                        if (xpost.getDescription() != null) {
                            out.println(xpost2.getDescription().replaceAll("\\\\n\\\\r", "<br/>").replaceAll("\\\\n", "<br/>"));
                            out.println("<br/>");
                            out.println(xpost2.getUpdatedTime());
                            out.println("<br/>");
                        }
                        out.println(xpost2.getType());
                        out.println("</td>");
                        out.println("</tr></table></td></tr></table></td></tr></table>");


                        out.println("</td></tr>");
                    }
                    out.println("</table>");

                }


                System.out.println("spended time=" + (System.currentTimeMillis() - startall));
            %>
    </body>
</html>
