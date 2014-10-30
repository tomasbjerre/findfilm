'use strict';

findFilmApp.directive('errSrc', function() {
   return {
     link: function(scope, element, attrs) {
       element.bind('error', function() {
         if (attrs.src != attrs.errSrc) {
           attrs.$set('src', attrs.errSrc);
         }
       });
     }
   }
 });

findFilmApp.directive('films', function() {
   return {
    restrict: 'E',
    scope: {
     films: '='
    },
    templateUrl: 'partials/films.html'
   }
 });
