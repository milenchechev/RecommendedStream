function postToHtml(post)
{
    resultHtml = "\
<table id="+post.id+" width='80%' class='postContainer' height='100%' bgcolor='#F5F7FA' style='border-top:solid;border-color:#7287B5'>\
	<tbody>\
		<tr>\
			<td>\
				<table width='100%'>\
					<tbody>\
						<tr>\
							<td width='50' valign='top'>\
								<img src='https://graph.facebook.com/"+post.from.id+"/picture' style='position:static;top:0px;'>\
								<br>\
								"+post.type+"\
								<br>\
								"+ (post.type=='status' ? "" : "<img src='"+post.icon+"' style='position:static;top:0px;'>")+"\
								<br>\
							</td>\
							<td>\
								<table width='100%'>\
									<tbody>\
										<tr>\
											<td>\
<table width='100%'><td>\
                                                                                                "+(post.story!=null?getStoryString(post.story,post.storyTags):"<a href='http://facebook.com/"+post.from.id+"' target='_blank'>"+post.from.name+"</a>")+" "+(post.to!=null && post.to[0]!=null ? "to <a href='http://facebook.com/"+post.to[0].id+"' target='_blank'>"+post.to[0].name+"</a>":"")+"\
												<br>\
                                            </td><td valign='top' align='right'>\
<button title='unmark post' style='display:none' class='unmarkPost' id='" + post.id + "'>Unmark</a>\
<button title='interesting' class='interesting mark' id='star-button"+post.id+"' onclick=\"actionToObject('"+post.id+"',\'markInteresting\')\" style=\"position:static;top:0px;right:0px;width: 16px;height: 16px;background: none repeat scroll 0% 0% #7287B5;background-image: url('images/star.png');cursor:pointer\"></button>\
<button title='not interesting' class='notInteresting mark' id='close-button"+post.id+"' onclick=\"actionToObject('"+post.id+"',\'markNotInteresting\')\" style=\"position:static;top:0px;width: 16px;right:0px;height: 16px;background: none repeat scroll 0% 0% #7287B5;background-image: url('images/close.png');cursor:pointer\"></button>\
</td></table>\
											</td>\
        								</tr>\
										<tr>\
											<td>\
												<table width='100%'>\
													<tbody>\
<tr>"+ (post.picture!=null ? "<td width='500px' bgcolor='black'><center><a href='"+post.link+"' target='_blank'><img src='"+preprocessPicture(post.picture)+"' style='min-width:180px;max-width:500px;'/></a></center></td>" :"")+"\
															<td valign='bottom'>\
																"+(post.message!=null?post.message.replace(/\n/gi,"<br/>")+"<br/>":"")+"\
                                                                                                                                "+(post.name!=null?post.name.replace(/\n/gi,"<br/>")+"<br/>":"")+"\
                                                                                                                                "+(post.caption!=null?post.caption.replace(/\n/gi,"<br/>")+"<br/>":"")+"\
                                                                                                                                "+(post.description!=null?post.description.replace(/\\\n/gi,"<br/>")+"<br/>":"")+"\
																<table width='100%' style='border-top:solid;border-width:1px;border-color:#7287B5'>\
																	<tbody>\
																		<tr>\
																			<td >\
																				<div id='"+post.id+"_comments_number'></div>\
                                                                                <div id='"+post.id+"_comments'></div>\
																				<div id='"+post.id+"_likes_number'></div>\
                                                                                <div id='"+post.id+"_likes'></div>\
        																		<button id='like-button"+post.id+"' onclick=\"postRequest('"+post.id+"/likes');$('#like-button"+post.id+"').hide();$('#unlike-button"+post.id+"').show();\" style='background:#7287B5;cursor:pointer'>like</button>\
																				<button id='unlike-button"+post.id+"' onclick=\"deleteRequest('"+post.id+"/likes');$('#like-button"+post.id+"').show();$('#unlike-button"+post.id+"').hide();\" style='background:#7287B5;cursor:pointer;display:none'>unlike</button>\
																				<button id='share-button"+post.id+"' onclick=\"actionToObject('"+post.id+"',\'share\');popup(&quot;http://www.facebook.com/sharer.php?s=100&p[images][0]="+post.picture + "&p[url]="+encodeURIComponent(post.link) + "%22&appid=<%=GlobalConstants.APP_URL%>&quot;)\" style='background:#7287B5;cursor:pointer;'>share</button>\
																				<input id='comments-box"+post.id+"' type='text' onclick=\"if(document.getElementById('comments-box"+post.id+"').value=='add comment'){document.getElementById('comments-box"+post.id+"').value=''}\"onkeypress=\"if(event.keyCode==13){addComment('"+post.id+"/comments',document.getElementById('comments-box"+post.id+"').value);document.getElementById('comments-box"+post.id+"').value=''}\" value='add comment'>\
																			</td>\
																		</tr>\
																	</tbody>\
																</table>\
															</td>\
														</tr>\
													</tbody>\
												</table>\
											</td>\
										</tr>\
									</tbody>\
								</table>\
							</td>\
						</tr>\
					</tbody>\
				</table>\
				<table width='100%'>\
					<tbody>\
						<tr>\
							<td><div id='"+post.id+"_time'> <div>\
							</td>\
							<td align='right'> </td>\
						</tr>\
					</tbody>\
				</table>\
			</td>\
		</tr>\
	</tbody>\
</table>"
    return resultHtml;
}

function updateNewsFeed(data){
    
    if(document.getElementById("mainTableX").innerHTML==""){
        //initial load
        text = "";
        for(var i=0; i< data.length;i++){
            if(!document.getElementById(data[i].id)){
                text +=postToHtml(data[i]);
            }
        }
        document.getElementById("mainTableX").innerHTML = text+document.getElementById("mainTableX").innerHTML;
    }else{
        for(var i=0; i< data.length && i< 2;i++){ // suggest to load new posts only if they are at the top of the list
            if(!document.getElementById(data[i].id)){
                document.getElementById("newPostsDiv").innerHTML="Load new posts"
            }
        }
    }
    //
    //
    //update likes
    updateLikes(data);
                            
    //update time
    for(var i=0; i< data.length;i++){
        if(document.getElementById(data[i].id+"_time")){
                                    
            var d = new Date();
            var n = d.getTime(); 
            timeDiv =document.getElementById(data[i].id+"_time");
            time = n - data[i].updatedTime;
            createdtime = n - data[i].createdTime;
            text = "";
            if(data[i].updatedTime ==data[i].createdTime){
                text += "created: "
                if (time / 3600000 > 1) {
                    text += Math.round(time / 3600000) + " hours ago";
                } else if (time / 60000 > 1) {
                    text += Math.round(time / 60000) + " minutes ago";
                }
            } else{
                /* if (createdtime / 3600000 > 1) {
                    text = Math.round(createdtime / 3600000) + " hours ago";
                } else if (createdtime / 60000 > 1) {
                    text = Math.round(createdtime / 60000) + " minutes ago";
                }*/
                text += "updated:"
                if (time / 3600000 > 1) {
                    text += Math.round(time / 3600000) + " hours ago";
                } else if (time / 60000 > 1) {
                    text += Math.round(time / 60000) + " minutes ago";
                }
            }
                                    
            timeDiv.innerHTML = text;
        }
    }
                            
    //comments
    for(var i=0; i< data.length;i++){
        if(document.getElementById(data[i].id+"_comments")){
            commentDiv =document.getElementById(data[i].id+"_comments");
            if(data[i].comments==null){
                continue;
            }
            commentCount = data[i].comments.data.length;
            text = "";
            if(commentCount == 1 ){
                text += commentCount +" comment";
            }else if(commentCount == 100){
                text += "100+ comments";
            }else if(commentCount > 1){
                text += commentCount +" comments";
            }
            for(var j=0 ; j < 10 && j < data[i].comments.data.length;j++){
                text += "<br/>";
                text += "<img height='28px' style='position:static;top:0px;' src='https://graph.facebook.com/"+data[i].comments.data[j].from.id+"/picture' alt='"+data[i].comments.data[j].from.name+"' title='"+data[i].comments.data[j].from.name+"'>"
                text += data[i].comments.data[j].message + " ";
                text += "<font size=1 color='#7287B5'>"+timeConverter(data[i].comments.data[j].createdTime)+"</font>";
            }
                                        
            
            commentDiv.innerHTML = text;
        }
    }
}
function getStoryString(story, tags){
    var text = "";
    var start_index = 0;
    var indexes = [];
    for(x in tags){
        indexes.push(x);
    }
    indexes.sort();
    
    for(var i = 0; i < indexes.length; i++){
        var x = indexes[i];
        text += story.substring(start_index,parseInt (tags[x][0].offset)) +"<a href='http://facebook.com/"+tags[x][0].id+"' target='_blank'>"+ tags[x][0].name+ "</a>";
        start_index = parseInt (tags[x][0].length)+ parseInt (tags[x][0].offset);
    }
    text +=story.substring(start_index);
    return text;
}

function preprocessPicture(pictureUrl){
    if (pictureUrl.indexOf("http://external.ak.fbcdn.net/safe_image.php")==0) {
        pictureUrl = pictureUrl.substring(pictureUrl.indexOf("&url=")+5);
        if(pictureUrl.indexOf("&")!=-1){
            pictureUrl = pictureUrl.substring(0,pictureUrl.indexOf("&"));
        }
        pictureUrl=   decodeURIComponent(pictureUrl);
    } else {
        pictureUrl = pictureUrl.replace("_s.jpg", "_n.jpg");
    }
    return pictureUrl;
}

function timeConverter(timestamp){
    var a = new Date(timestamp/1000);
    var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    var year = a.getFullYear();
    var month = months[a.getMonth()];
    var date = a.getDate();
    var hour = a.getHours();
    var min = a.getMinutes();
    var sec = a.getSeconds();
    var time = date+'.'+month+'.'+year+' '+hour+':'+min+':'+sec ;
    return time;
}


function postToFeed(url,photo) {

    // calling the API ...
    var obj = {
        method: 'feed',
        redirect_uri: encodeURIComponent('<%=GlobalConstants.APP_URL%>'),
        link: url,
        picture: photo,
        name: 'Share dialog'
    // caption: 'Reference Documentation',
    // description: 'Using Dialogs to interact with people.'
    };

    function callback(response) {
        alert( "Post ID: " + response['post_id']);
    }

    FB.ui(obj, callback);
}
function updateLikes(data){
    userId = document.getElementById("userId").innerHTML;
    $.get("data/posts/friends_ids/" + userId).done(function (friendsdata) {
        var friends = friendsdata.friends;
        for(var i=0; i< data.length;i++){
            if(document.getElementById(data[i].id+"_likes")){
                likes = document.getElementById(data[i].id+"_likes");
                if(data[i].likes!=null){
                    text = "";
                    if(data[i].likes.data.length>=100){
                        document.getElementById(data[i].id+"_likes_number").innerHTML="100+ likes";
                    }else{
                        document.getElementById(data[i].id+"_likes_number").innerHTML=data[i].likes.data.length + " likes";
                    }


                    //process likes
                    for(var j = 0 ; j < data[i].likes.data.length ; j++){
                        if(data[i].likes.data[j].id==userId){
                            $("#unlike-button"+data[i].id).show();
                            $("#like-button"+data[i].id).hide();
                            text += "<img height='28px' src='https://graph.facebook.com/"+data[i].likes.data[j].id+"/picture'>" + data[i].likes.data[j].name+" ";
                        }else if($.inArray(data[i].likes.data[j].id, friends)>=0){
                            text += "<img height='28px' src='https://graph.facebook.com/"+data[i].likes.data[j].id+"/picture'>" +data[i].likes.data[j].name+" ";
                        }
                    }
                    likes.innerHTML = text;
                }
            }
        }


    }).fail(function (err) {
        console.log("could not fetch friends info");
        console.dir(err);
    });
}