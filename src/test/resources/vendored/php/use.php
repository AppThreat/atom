<?php
require_once 'third_party/lib/Helper.php';

class Child extends \Vend\Helper {}

function take(\Vend\Tool $t) {
    return $t->go();
}

$h = new \Vend\Tool();
