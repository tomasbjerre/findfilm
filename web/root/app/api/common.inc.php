<?php
date_default_timezone_set('UTC');

function responde($data,$httpCode=200) {
 header('HTTP/1.1 '.$httpCode);
 header('Content-type: application/json');
 print json_encode($data);
 exit;
}

function get_settings() {
 $config_files[] = "/etc/pleasedonthackme.properties";
 $settings = NULL;
 foreach ($config_files as $path) {
  $settings = parse_ini_file($path);
 }
 $settings or error("Unable to read config. Looked in:\n".var_export($config_files,true),500);
 return $settings;
}

function error($message,$httpCode=400) {
 header('HTTP/1.1 '.$httpCode);
 header('Content-type: application/json');
 print "{\"message\": ".json_encode($message)."}";
 exit;
}

function check_api_key() {
 $settings = get_settings();
 $correctKey = $settings['api_film_secretkey_hash'];
 $correctKeySeed = $settings['api_film_secretkey_seed'];
 $secretkey = $_GET['secretkey'];
 $hashedInputKey = sha1($secretkey.$correctKeySeed);
 if ($hashedInputKey != $correctKey) { 
  error("API key, '".$secretkey."', not correct! ",500);
 }
}

function get_findfilm_film_collection() {
 $client = new MongoClient() or error("Unable to create MongoClient!",500);
 return $client->findfilm->films;
}

function create_film($film) {
 $film['id'] = sha1($film['title']);
 $existing = get_films(array('id' => $film['id']));
 count($existing) > 0 && error("Film exists with id '".$film['id']."'",500);
// foreach ($film['sources'] as $k => $s) {
//  $film['sources'][$k]['added'] = date('Y-m-d');
// }
 get_findfilm_film_collection()->insert($film);
 return $film;
}

function remove_film($filmId) {
 $films = get_films(array('id' => $filmId));
 count($films) != 1 and error("Unable to find film '".$filmId."'",404);
 get_findfilm_film_collection()->remove(array('id' => $filmId)) or error("Unable to delete \"".$filmId."\"",500);
 return $films[0];
}

function save_film($film) {
 get_findfilm_film_collection()->save($film);
}

function get_films($query=array()) {
 if (isset($query['sources'])) {
  $sources = array();
  foreach ($query['sources'] as $id => $source) {
   foreach ($source as $k => $v) {
    $query['sources.'.$k] = $v;
   }
  }
  unset($query['sources']);
 }
 $arr = array();
 foreach(get_findfilm_film_collection()->find($query) as $key => $value) {
   array_push($arr, $value);
  }
 return $arr;
}

function remove_internals($object) {
 if (!is_array($object)) {
  return $object;
 } else {
  unset($object['_id']);
  foreach ($object as $k => $v) {
   $object[$k] = remove_internals($v);
  }
 }
 return $object;
}

function trimmed($value) {
 $value = trim($value);
 if (empty($value)) {
  return NULL;
 }
 return $value;
}

function mandatory($data, $key) {
 $value = trimmed($data[$key]);
 if (!isset($value)) {
  error("Missing $key.\nGot:".var_export($data,true));
 }
 return $value;
}

function optional($data, $key) {
 return trimmed($data[$key]);
}

function get_safe_film($data) {
 $safeData['title'] = mandatory($data, 'title');
 is_array($data['sources']) and count($data['sources']) > 0 or error("No sources specified");
 $safeData['sources'] = array();
 foreach ($data['sources'] as $k => $source) {
  $s = array();
  $s['thumbnail'] = optional($source, 'thumbnail');
  $s['url'] = mandatory($source, 'url');
  $s['identifier'] = mandatory($source, 'identifier');
  $s['filmSourceId'] = mandatory($source, 'filmSourceId');
  $s['added'] = optional($source, 'added');
  $s['lastSeen'] = optional($source, 'lastSeen');//date('Y-m-d');
  array_push($safeData['sources'],$s);
 }
 return $safeData;
}
?>
