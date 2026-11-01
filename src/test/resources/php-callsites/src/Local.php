<?php
declare(strict_types=1);

namespace App;

class Local
{
    public function collect(array $xs): array
    {
        $out = [];
        foreach ($xs as $x) {
            $out[] = $x;
        }
        return $out;
    }

    public function misc(array $xs): string
    {
        global $g;
        $c = clone $this;
        return "value {$xs[0]}";
    }
}
