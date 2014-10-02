<%--
    Document   : newjsp
    Created on : Dec 29, 2012, 12:40:38 PM
    Author     : Milen
--%>

<%@page import="com.chechev.phd.utils.GlobalConstants"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="js/jquery-1.8.2.js"></script>
        <script src="js/jquery.url.js"></script>
        <script src="js/recsys.js"></script>
        <script src="js/ui_helper.js"></script>
        <script src="js/underscore.min.js"></script>
        <script src="js/scroll.js"></script>
        <title>Recommended Stream</title>

    </head>
    <body>
        <div id="fb-root"></div>
        <script>
            window.fbAsyncInit = function() {
                // init the FB JS SDK
                FB.init({
                    appId      : '<%= GlobalConstants.APP_ID%>', // App ID from the App Dashboard
                    channelUrl : '//chechev.com/RecommendedStream/channel.html',
                    status     : true, // check the login status upon init?
                    cookie     : true, // set sessions cookies to allow your server to access the session?
                    xfbml      : true  // parse XFBML tags on this page?
                });

                // Additional initialization code such as adding Event Listeners goes here

                FB.login(function(response) {
                    if(response.status=='connected'){
                        console.log(response.authResponse.accessToken);
                        
                        updateFunction(response.authResponse.accessToken,true);
                        checkIsNewUser(response.authResponse.accessToken);
                        setInterval(function(){updateFunction(response.authResponse.accessToken,false);},60*1000);
                        FB.api('/me', function(response) {
                            document.getElementById("userId").innerHTML = response.id;
                            document.getElementById("userName").innerHTML = response.name;
                        });
                        /*$.ajax({url:"data/posts/pesho", data:response.authResponse.accessToken,contentType : "application/json",dataType : "application/json", type:"POST" ,
                        statusCode: {
                            201: function() {
                              console.log("+++");

                            }
                          }});*/

                        // $.post("data/posts/pesho","{ 'func': 'getNameAndTime' }",
                        // function(data){
                        //   console.log(data); // John
                        // }, "json");
                    }else{
                        console.log(response);
                    }
                    // handle the response
                }, {
                    scope: 'email,read_stream,offline_access,friends_photos,friends_status,friends_likes'
                });

            };

            // Load the SDK's source Asynchronously
            (function(d, debug){
                var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
                if (d.getElementById(id)) {return;}
                js = d.createElement('script'); js.id = id; js.async = true;
                js.src = "//connect.facebook.net/en_US/all" + (debug ? "/debug" : "") + ".js";
                ref.parentNode.insertBefore(js, ref);
            }(document, /*debug*/ false));

            var timer = setInterval(function(){
                document.getElementById("loadingDiv").innerHTML +=".";
            },1000);
            var query = jQuery.url.param("query");
            if(query==null){
                query='';
            }
            function checkIsNewUser(accessToken){
            	if(window.location.search.indexOf("firstvisit=no") >= 0) {
            		return;
            	}
            	
                $.ajax({url:"data/posts/isnew?access_token="+accessToken, type:"GET" ,
                    success:function(data){
                        if(data==true){
                        	window.location = "intro.jsp";
                        }

                    },
                    error:function(xhr){
                        console.log(xhr);
                    }});
            }
            function updateFunction(accessToken,isInitialLoad){
                FB.getLoginStatus(function(response) {
                    if (response.status === 'connected') {
                        //var uid = response.authResponse.userID;
                        accessToken = response.authResponse.accessToken;
                        document.getElementById("accessToken").innerHTML =accessToken;
                        $.ajax({url:"data/posts/get?access_token="+accessToken+"&since="+<%= System.currentTimeMillis() - 24 * 60 * 60 * 1000%>+"&query="+query +"&initial_load="+isInitialLoad, type:"GET" ,
                            success:function(data){
                                updateNewsFeed(data);
                                console.log("updated news feed");
                                if(timer!=null){
                                    window.clearInterval(timer);
                                    document.getElementById("loadingDiv").innerHTML="";
                                }
                            },
                            error:function(xhr){
                                console.log(xhr);
                                if(timer!=null){
                                    window.clearInterval(timer);
                                    document.getElementById("loadingDiv").innerHTML="";
                                }
                            }});

                    } else {
                        FB.login(function(responce){},{scope: 'email,read_stream,offline_access,friends_photos,friends_status,friends_likes'});
                    }
                });

            }
            

        </script>
        <table style="background-image:url('images/logo.png'); height: 52px; width: 100%">
            <tr>
                <td width="60px">
                    <a href="/RecommendedStream/">Home</a>
                </td>
                <td width="60px">
                    <a href="/RecommendedStream/movies.jsp">Movies</a>
                </td>

                <td>
            <center>
                <font size="5">News Feed</font>
            </center>
        </td>
        <td width="60px">
            <a href="/RecommendedStream/config.jsp">Config</a>
        </td>
        <td width="60px">
            <a href="/RecommendedStream/about.jsp">About</a>
        </td>
    </tr>
</table>
<center>
    <br/>
    <form name="searchForm" accept-charset="UTF-8"  action="" method="GET">
        <input  method="GET" type="text" name="query" size="70">
        <script>if(query!= null) document.forms['searchForm'].query.value=query; </script>
        <input type="submit" value="Search">
    </form>

    <br/>
    <div style="display: none;" id="userId"></div>
    <div style="display: none;" id="userName"></div>
    <div style="display: none;" id="accessToken"></div>
    <div id="loadingDiv"> Loading</div>
    <a id="newPostsDiv" href="/RecommendedStream/"></a>
    <div id="mainTableX"></div>
</center>
</body>
</html>
