window.movies = window.movies || (function($) {
	
	function fetchAndShowMovies(userId) {
		var getImage = function(path) {
			if (!path || path == 'N/A') {
				return "images/movie_no_poster.png";
			}
			return path;
		};
		
		$.get("data/posts/movies/" + userId).done(function (data) {
			var movies = data.movies,
				$container = $("#placeholder");
			
			$container.empty();
			
			$.each(movies, function (index, movie) {
				var $linkContainer = $("<a ></a>");
				if (movie.value.imdbId) {
					$linkContainer = $("<a href=\"http://imdb.com/title/" + movie.value.imdbId + "\" target=\"_blank\"></a>");
				}
				var $picContainer = $("<div></div>").addClass("movie-item-container").attr("id", movie._id),
					$nameContaner = $("<p></p>").addClass("name-container").append(movie.value.name + " (" + movie.value.rating + ") [" + movie.value.friendId.length + "]"),
					$img = $("<img class=\"movie-poster\"></img>").attr({
						src: getImage(movie.value.poster)
					});
				
				$container.append($linkContainer);
				$linkContainer.append($picContainer);
				$picContainer.append($img);
				$picContainer.append($nameContaner);
			});
			
		}).fail(function (err) {
			console.log("could not fetch movies info");
			console.dir(err);
		});
	}

	return {
		fetchAndShowMovies : fetchAndShowMovies
	};
}(jQuery));