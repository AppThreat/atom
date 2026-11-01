<?php
namespace Acme\Widgets;

class Widget
{
    public static function make(): static { return new static(); }
    public function render(): string { return 'widget'; }
}
