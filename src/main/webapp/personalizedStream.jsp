
<%@page import="java.util.List"%>
<%@page import="java.util.LinkedList"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.chechev.phd.recommendedstream.datatypes.ExtendedPost"%>
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
        <center><h3>## The Application is in process of Development. Alfa version is expected at 1 Nov 2012!!!</h3></center>

        <%

            String access_token = (String) request.getSession().getAttribute("access_token");
            long startall=System.currentTimeMillis();
            System.out.println("#access token get from session=" + access_token);



        %>



        <center>
            <h1>Recommended Stream </h1>
            <br/>
            <form action="/search">
                <input type="text" name="Query" size="70"> <input type="submit" value="Search Post">
            </form>
            <br/>

            <%
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

                    HttpGet getRequest = new HttpGet("http://localhost:8080/RecommendedStream/data/posts/get_peronalized?access_token=" + access_token);
                    getRequest.addHeader("accept", "application/json");
                    resp = httpClient.execute(getRequest);

                    if (resp.getStatusLine().getStatusCode() != 200) {
                        throw new RuntimeException("Failed : HTTP error code : "
                                + resp.getStatusLine().getStatusCode());
                    }

                    br = new BufferedReader(
                            new InputStreamReader((resp.getEntity().getContent()), "utf-8"));

                    out.println("<table width=50%  style='word-wrap: break-word;'>");
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
                            out.println("<tr><td>");
                            ExtendedPost xpost = mapper.readValue("{" + post + "}", ExtendedPost.class);
                            out.println("<table width=100% frame='border'bgcolor='#FCFCFF'><tr><td width=50 valign='top'>");
                            out.println("<img style='position:static;top:0px;' src=http://graph.facebook.com/" + xpost.getFrom() + "/picture>");

                            out.println("</td><td><table><tr><td>");
                            out.println(xpost.getName());
                            out.println("</td></tr><tr><td><table>");
                            if (xpost.getLink() != null && xpost.getPicture() != null) {
                                out.println("<td width=10>");
                                out.println("<a target='_blank' href=\"" + xpost.getLink() + "\">");
                                out.println("<img style='max-width:600px' src=" + xpost.getPicture() + ">");
                                out.println("</a>");
                                out.println("</td>");
                            } else if (xpost.getPicture() != null) {
                                out.println("<td width=10>");
                                out.println("<img style='max-width:600px' src=" + xpost.getPicture() + ">");
                                out.println("</td>");
                            }


                            //style='max-height: 500px; max-width: 500px'
                            out.println("<td>");
                            if (xpost.getDescription() != null) {
                                out.println(xpost.getDescription().replaceAll("\\\\n\\\\r", "<br/>").replaceAll("\\\\n", "<br/>"));
                                out.println("<br/>");
                                out.println(xpost.getUpdatedTime());
                                out.println("<br/>");
                            }
                            //out.println(xpost.getLink());
                            //out.println("<br/>");
                            out.println(xpost.getType());
                            out.println("</table></td></tr></table></td></tr></table></td></tr>");
                        }

                    }
                    out.println("</table>");
                    httpClient.getConnectionManager().shutdown();

                } catch (ClientProtocolException e) {

                    e.printStackTrace();

                } catch (IOException e) {

                    e.printStackTrace();
                }

                System.out.println("spended time=" + (System.currentTimeMillis() - startall));
            %>
    </body>
</html>
