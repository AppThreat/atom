<?php
declare(strict_types=1);

namespace App;

use Acme\Widgets\Widget;
use Acme\Widgets\Gadget;
use Acme\Widgets\Failure;
use Acme\Widgets\Flag;
use Acme\Widgets\Mark;
use Acme\Widgets\Shares;
use function Acme\Util\helper;

#[Mark]
class Shapes extends Widget implements \Countable
{
    use Shares;

    #[Mark]
    private ?Widget $prop = null;
    public const Flag|int F = 1;

    #[Mark]
    public function __construct(#[Mark] private Gadget $gadget, ?Widget $w = null)
    {
    }

    public function count(): int
    {
        return 0;
    }

    public function calls(Widget $w, Gadget|Failure $u, string $s): ?Widget
    {
        helper();
        strlen($s);
        collect();
        local();
        $v = Widget::make();
        $n = new Widget();
        $l = new Local();
        $x = Flag::LIMIT;
        $y = Flag::class;
        $z = Flag::$count;
        $w instanceof Failure;
        try {
            $w->render();
        } catch (Failure $e) {
            $d = new \PDO('sqlite::memory:');
        }
        $o = new Gadget();
        $o->run();
        (new Gadget())->go();
        $this->gadget->go();
        $this->prop->render();
        $f = function () use ($w) { return $w->render(); };
        $re = new Gadget();
        $re = new Gadget();
        $re->run();
        $u->run();
        $r = Widget::make()
            ->render();
        return $w;
    }

    public function piped(): void
    {
        $r = 'x' |> trim(...) |> (new Widget(...));
        $s = Widget::make() |> strval(...);
    }
}

function local(): void
{
}

#[Mark]
enum Suit: string
{
    #[Mark]
    case Hearts = 'h';
}

class Hooked
{
    public string $name {
        get => Widget::make()->render();
    }
}

#[Mark(Flag::LIMIT)]
class Defaults
{
    public function __construct(
        public int $code = Flag::LIMIT,
        public string $name = Flag::class,
        public ?Gadget $gadget = new Gadget(),
    ) {
    }
}

class Fluent
{
    public function chain(Gadget $g): void
    {
        $l = new \Acme\Util\Legacy();
        $g
            ->run();
    }
}
