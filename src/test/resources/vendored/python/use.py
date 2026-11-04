from third_party.lib import Base


def take(b: Base) -> int:
    x = Base()
    return x.go() + b.go()
