<?php
include("common.inc.php");

check_api_key();

$data = json_decode(file_get_contents('php://input'), true);
if ($_SERVER['REQUEST_METHOD'] == "POST") {
 $films = get_films($data);
 responde(remove_internals($films));
}

error($_SERVER['REQUEST_METHOD']+" is not implemented yet");
?>
