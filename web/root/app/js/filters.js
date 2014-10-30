'use strict';

findFilmApp.filter('filmFilter',[ function () {
    return function(films, searchText) {
        if (!searchText || searchText.trim() == "") {
         return films;
        }
        var filtered = [];
        var regex = new RegExp(".*" + searchText + ".*", "ig");
        angular.forEach(films, function(film) {
            if(regex.test(film.title)){
                filtered.push(film);
            }
        });
        return filtered;
    }
}]);

findFilmApp.filter('startWith', function() {
    return function(films,prefix) {
      if (prefix == "Alla") {
       return films;
      }
     function startsWith(string, prefix) {
      if (!string || string.length == 0) {
       return false;
      }
      if (prefix == '#' && string[0].match(/[^A-Z]/gi)
       || string[0] == prefix) {
       return true;
      }
     }
     var included = [];
     angular.forEach(films, function(film) {
      if (startsWith(film.title,prefix)) {
       included.push(film);
      }
     });
     return included;
    };
  });

