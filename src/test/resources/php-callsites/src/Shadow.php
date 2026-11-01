<?php
declare(strict_types=1);

namespace App\Other;

#[Widget]
class User
{
    public function make(Widget $w): Widget
    {
        $n = new Widget();
        return Widget::make();
    }
}

class Widget
{
    public static function make(): Widget
    {
        return new Widget();
    }
}
