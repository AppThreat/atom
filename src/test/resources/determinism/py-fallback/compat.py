try:
    from urllib.parse import quote
except ImportError:
    from urllib import quote

print(quote("a b"))
