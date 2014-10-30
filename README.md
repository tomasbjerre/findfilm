FindFilm
========

Collects films from different sources and makes them available in a unified, filterable, collection.

The webpage is deployed at http://findfilm.se/ .

= Implementation overview =
There is a tiny REST API implemented with PHP. It has two resources, search and films.

There is a parser job, that is run periodically to collect films from different sources. It, as well as the sources, are implemented with Java 8. It uses the API to store its findings.

There is a web application developed with AngularJS and Bootstrap. It uses the API to expose the film database.

= How to build =

== Backend ==
To generate Eclipse projects, do:
 mvn eclipse:eclipse
To create a runnable parser job, do:
 mvn package

== Frontend ==
You need to download backend packages, like Grunt and Bower, and then use them to build the application. Do it like this:
 npm install
 bower install
 grunt build #Optional

There is a faked API response called films.php.fake that you can use together with 'grunt serve' to do front end development quick and easy.

= How to deploy =

== Parser job ==

== Front end ==

=== Apache2 ===
