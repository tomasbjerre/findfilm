'use strict';

findFilmApp.factory('findFilmBackend', ['$http', function($http) {
 return {
  getFilms: function() {
   return $http.get('api/films.php', {cache: true});  
  }
 }
}]);
