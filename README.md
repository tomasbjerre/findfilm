FindFilm
========

Collects films from different sources and makes them available in a unified, filterable, collection.

The webpage is deployed at http://findfilm.se/ .

# Implementation overview
There is a tiny REST API implemented with PHP. It has two resources, search and films.

There is a parser job, that is run periodically to collect films from different sources. It, as well as the sources, are implemented with Java 8. It uses the API to store its findings.

There is a web application developed with AngularJS and Bootstrap. It uses the API to expose the film database.

# How to build

## Backend
To generate Eclipse projects, do:
```
 mvn eclipse:eclipse
```
To create a runnable parser job, do:
```
mvn package
```

## Frontend
You need to download backend packages, like Grunt and Bower, and then use them to build the application. Do it like this:
```
npm install
bower install
grunt build #Optional
```

There is a faked API response called films.php.fake that you can use together with 'grunt serve' to do front end development quick and easy.

# How to deploy

## Please dont hack me!
Both backend and front end as well as the API will expect a file at:
```
/etc/pleasedonthackme.properties
```
It will need to contain:
```
netflix_email=netflix user email
netflix_password=netflix user password
api_film_base= Base of API, like: http://findfilm.local/api
api_film_secretkey_hash=... Should be: SHA1(secrtetkey+seed)
api_film_secretkey_seed=...Can be anything, but you need to use it to calculate hash
api_film_secretkey=... Any secret key
```
On the server side, you should not store the api_film_sectetkey. Only store it on the client side. And on the client side, dont store hash and seed values. Or store them all everywhere if you are lazy =)

## Parser job
Take a look at
```
parser-job/cronjob.sh
```

## Front end
There are many alternatives...

### Apache2
```
<VirtualHost *:80>
  ServerName findfilm.local
  DocumentRoot /home/bjerre/workspace/findfilm/web/root/app
  AddDefaultCharset UTF-8
  <Directory /home/bjerre/workspace/findfilm/web/root/app>
   Options +FollowSymLinks +Indexes +ExecCGI
   Require all granted
   DirectoryIndex index.html
   <IfModule mod_php5.c>
    AddType application/x-httpd-php .php
   </IfModule>
  </Directory>
 </VirtualHost>
```
