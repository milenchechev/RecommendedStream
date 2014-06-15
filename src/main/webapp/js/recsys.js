function deleteRequest(requestId) {
    FB.api(requestId, 'delete', function(response) {
        console.log(response);
        text = document.getElementById(requestId.replace("/","_")).innerHTML;
        userId = document.getElementById("userId").innerHTML;
        console.log("delete before "+text);
        text = text.replace("<img height='28px' src='https://graph.facebook.com/"+userId+"/picture'>","");
        text = text.replace("<img src='https://graph.facebook.com/"+userId+"/picture' height='28px'>","");
        text = text.replace("<img height=\"28px\" src=\"https://graph.facebook.com/"+userId+"/picture\">","");
        text = text.replace("<img src=\"https://graph.facebook.com/"+userId+"/picture\" height=\"28px\">","");
        text = text.replace(document.getElementById("userName").innerHTML+" ","");
        console.log("delete after "+text);
        document.getElementById(requestId.replace("/","_")).innerHTML = text;
        actionToObject(requestId.replace("/likes",""),"unlike");
    });
    likesNumber = parseInt(document.getElementById(requestId.replace("/","_")+"_number").innerHTML)
    if(likesNumber > 1){
        likesNumber--;
        document.getElementById(requestId.replace("/","_")+"_number").innerHTML= likesNumber+ " likes"
    }else{
        document.getElementById(requestId.replace("/","_")+"_number").innerHTML="";
    }
}
function postRequest(requestId) {
    FB.api(requestId, 'post', function(response) {
        if(response.error){
            console.log(response.error.message);
            alert("You need to give publish permission to the application to use the functionalities to add likes and comments");
            FB.login(function(response) {
                FB.api(requestId, 'post', function(response) {
                    if(response.error){
                        console.log(response.error.message);
                        console.log('second error-give up...');
                    };
                });
                            
            // handle the response
            }, {
                scope: 'publish_stream'
            });
        }
        text = document.getElementById(requestId.replace("/","_")).innerHTML;
        userId = document.getElementById("userId").innerHTML;
        likesNumber = parseInt(document.getElementById(requestId.replace("/","_")+"_number").innerHTML);
        if(likesNumber > 0){
            likesNumber++;
            document.getElementById(requestId.replace("/","_")+"_number").innerHTML= likesNumber+ " likes"
        }else{
            document.getElementById(requestId.replace("/","_")+"_number").innerHTML= "1 like"
        }

        document.getElementById(requestId.replace("/","_")).innerHTML = "<img height='28px' src='https://graph.facebook.com/"+userId+"/picture'>"+document.getElementById("userName").innerHTML+" "+text;
        actionToObject(requestId.replace("/likes",""),"like");
    });
}

function actionToObject(objectID,action) {
    accessToken = document.getElementById("accessToken") ? document.getElementById("accessToken").innerHTML : "";
    json = "{\"access_token\":\""+accessToken+"\",\"post_id\" : \""+objectID+"\",\"collection\" :\"docs\"}";
    $.ajax({
        url:"data/posts/"+action+"?access_token="+accessToken+"&post_id="+objectID+"&collection=docs",
        type:"GET" ,
        success:function(data){
            console.log("success update");
        },
        error:function(xhr){
            console.log("ERROR update");
        }
    });
}


function addComment(requestId,text) {
    FB.api(requestId, 'post',{
        message : text
    }, function(response) {
        console.log(response);
    });
    actionToObject(requestId.replace("/comments",""),"comment");
    commentDiv =document.getElementById(requestId.replace("/","_"));
    var content = "<br/>";
    content += "<img height='28px' style='position:static;top:0px;' src='https://graph.facebook.com/"+document.getElementById("userId").innerHTML+"/picture'>"
    content += " ";
    content += text;
    
    var currentTime = new Date().getTime();
    content += "<font size=1 color='#7287B5'>"+timeConverter(currentTime)+"</font>";
    commentDiv.innerHTML = commentDiv.innerHTML+content;
}
function timedRefresh(timeoutPeriod) {
    setTimeout("location.reload(true);",timeoutPeriod);
}
function popup(mylink)
{
    if (!window.focus)return true;
    var href;
    if (typeof(mylink) == 'string')
        href=mylink;
    else
        href=mylink.href;
    window.open(href, "share", 'width=400,height=200,scrollbars=yes');
    return false;
} 

function isOnScreen(element)
{
    var curPos = element.offset();
    var curTop = curPos.top;
    var screenHeight = $(window).height();
    return (curTop > screenHeight) ? false : true;
}

