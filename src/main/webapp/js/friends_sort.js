window.friends = window.friends || (function($) {
	
	function fetchAndShowFriends(userId) {
		$.get("data/posts/friends/" + userId).done(function (data) {
			var friends = data.friends,
				$container = $("#placeholder");
			
			$container.empty();
			
			$.each(friends, function (index, friend){
				var friendId = friend.id,
					friendGraphUrl = "http://graph.facebook.com/" + friendId,
					$picContainer = $("<div></div>").addClass("sortable-item-placeholder").attr("id", friendId),
					$nameContaner = $("<p></p>").addClass("name-container").append(friend.name);
				
				$container.append($picContainer);
				$picContainer.append($nameContaner);
			});
			
			$.each(friends, function (index, friend){
				var friendId = friend.id,
					$img = $("<img></img>").attr({
					src: "http://graph.facebook.com/" + friendId + "/picture",
					id: friendId
				});
				$("#" + friendId).prepend($img);
			});
			
			$container.sortable({
				update: function (event, ui) {
					var friends = $(this).sortable('toArray'),
						changedFriend = ui.item.attr("id");
					
					$.ajax({
						"type": "GET",
						"url": "data/posts/friends/update/" + userId + "?friends=" + JSON.stringify(friends) + "&changedFriend=" + changedFriend
					});
				},
				"placeholder": "sortable-item-placeholder"
			});
		}).fail(function (err) {
			console.log("could not fetch friends info");
			console.dir(err);
		});
	}

	return {
		fetchAndShowFriends : fetchAndShowFriends
	};
}(jQuery));