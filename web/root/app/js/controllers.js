'use strict';

findFilmApp.controller('FindFilmFilter', ['$scope', 'findFilmBackend', '$filter', '$location', function ($scope, findFilmBackend, $filter, $location) {

  function updatePagination() {
   var filtered = $filter('startWith')($scope.allFilms, $scope.startWith.model);
   filtered = $filter('filmFilter')(filtered,$scope.query);
   $scope.totalItems = filtered.length;
   if ($scope.orderBy == 'title') {
    filtered = $filter('orderBy')(filtered, $scope.orderBy, false);
   }
   if ($scope.orderBy == 'added') {
    filtered = $filter('orderBy')(filtered, function(film) {
     var added = "0";
     angular.forEach(film.sources, function(source) {
      if ($scope.allSources[source.identifier]) {
       if (added < source['added']) {
        added = source['added'];
       }
      }
     });
     return added;
    }, true);
   }
 
   function hasEnabledSource(film) {
    for (var i = 0; i < film.sources.length; i++) {
     var source = film.sources[i];
     if ($scope.allSources[source.identifier]) {
      return true;
     }
    }
    return false;
   }
   var toInclude = [];
   for (var i = 0; i < filtered.length; i++) {
    var film = filtered[i];
    if (hasEnabledSource(film)) {
     toInclude.push(film);
    }
   }
   filtered = toInclude;

   $scope.films =  $filter('limitTo')(filtered,$scope.entryLimit);
  }

  $scope.allFilms = [];
  $scope.loading = true;
  $scope.startWithList = ["Alla",'#','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','Å','Ä','Ö'];
  $scope.orderBy = $location.search().orderBy || 'title';
  $scope.startWith = {model: $location.search().startWith || 'Alla' };
  $scope.query = $location.search().query || '';
  $scope.allSources = {};
  angular.forEach($location.search().sources ? [].concat($location.search().sources) : [], function(source) {
   $scope.allSources[source] = true;
  });

  function setupWatches() {
   $scope.$watch("startWith.model", function(value, old) {
    if (value == 'Alla') {
     $location.search('startWith', null);
    } else {
     $location.search('startWith', value);
    }
    $scope.entryLimit = 30;
    updatePagination();
   }); 

   $scope.$watch("query", function(value) {
    if (value == '') {
     $location.search('query', null);
    } else {
     $location.search('query', value);
    }
    $scope.entryLimit = 30;
    updatePagination();
   });

   $scope.$watch("orderBy", function(value) {
    if (value == 'title') {
     $location.search('orderBy', null);
    } else {
     $location.search('orderBy', value);
    }
    $scope.entryLimit = 30;
    updatePagination();
   });

   $scope.$watchCollection("allSources", function(value, old) {
    var sources = [];
    angular.forEach(value, function(value, key) {
     if (value) {
      sources.push(key);
     }
    });
    if (Object.keys($scope.allSources).length == sources.length) {
     $location.search('sources', null);
    } else {
     $location.search('sources', sources);
    }
    $scope.entryLimit = 30;
    updatePagination();
   });
  }

  $scope.infiniteScroll = function() {
   $scope.entryLimit += 10;
   updatePagination();
  }

  findFilmBackend.getFilms().success(function(data) {
   $scope.allFilms = data;
   if ($scope.allSources == undefined) {
    $scope.allSources = {};
   }

   var allTrue = Object.keys($scope.allSources).length == 0;
   angular.forEach($scope.allFilms,function(film) {
    angular.forEach(film.sources,function(source) {
     $scope.allSources[source.identifier] = allTrue || $scope.allSources[source.identifier] == true;
    });
   });

   $scope.films = [];
   
   $scope.entryLimit = 20;
   $scope.totalItems = $scope.allFilms.length;
   $scope.loading = false;
   updatePagination();
   setupWatches();
  }).error(function(){
   $scope.loading = false;
   alert('error :(');
  });
}]);
