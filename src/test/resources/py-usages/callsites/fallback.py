try:
    import yaml
except ImportError:
    yaml = None

def use():
    return yaml.safe_load("{}")

from typing import TYPE_CHECKING
if TYPE_CHECKING:
    from fastapi import Response

def typed(r: "Response") -> "Response":
    return r
