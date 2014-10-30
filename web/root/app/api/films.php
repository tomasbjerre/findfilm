<?php
include("common.inc.php");

if ($_SERVER['REQUEST_METHOD'] == "GET") {
 if (array_key_exists('id',$_GET)) {
  $id = $_GET['id'];
  $films = remove_internals(get_films(array('id' => $id)));
  if (is_array($films) && count($films) == 1) {
   responde($films[0]);
  } else {
   error("Unable to find film \"$id\"",404);
  }
 } else {
  $meminstance = new Memcache();
  $meminstance->pconnect('localhost', 11211);
  $filtered = $meminstance->get("allFilms");
  if (!$filtered || !isset($_GET['secretkey'])) {
   $filtered = array();
   $films = get_films();
   foreach ($films as $k => $v) {
    $v = remove_internals($v);
    array_push($filtered,$v);
   }
   $meminstance->set("allFilms", $filtered, 0, 60);
  }
  responde($filtered);
 }
}

check_api_key();

if ($_SERVER['REQUEST_METHOD'] == "DELETE") { 
 $id = $_GET['id'];
 $id or error("No id supplied",400);
 responde(remove_internals(remove_film($id)));
}

$data = json_decode(file_get_contents('php://input'), true);
if ($_SERVER['REQUEST_METHOD'] == "POST") {
 responde(remove_internals(create_film(get_safe_film($data))));
}

if ($_SERVER['REQUEST_METHOD'] == "PUT") {
 $films = get_films(array('id' => $data['id']));
 if (!is_array($films) || count($films) != 1) {
  error("Couldnt find film to update.",404);
 }
 $data['_id'] = $films[0]['_id'];
 $data['lastSeen'] = date('Y-m-d');
 save_film($data);
 responde(remove_internals($data));
}
?>
