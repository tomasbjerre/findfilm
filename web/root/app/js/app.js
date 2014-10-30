'use strict';

var findFilmApp = angular.module('findFilmApp', ['ui.bootstrap', 'infinite-scroll'])
 .config(['$locationProvider',function($locationProvider) {
//  $locationProvider.html5Mode(true).hashPrefix('!');
}]);
