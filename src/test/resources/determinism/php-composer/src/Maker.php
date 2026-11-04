<?php
namespace App;

use Acme\Widgets\Gadget;

class Maker
{
    public function make(): Gadget
    {
        return new Gadget();
    }
}
