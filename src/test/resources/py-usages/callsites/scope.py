import requests

def shadow_param(requests):
    return requests.get("http://x")

def shadow_local():
    requests = object()
    return requests

def reassigned():
    global json
    import json
    json = {}
    return json

def captures():
    import datetime

    def nested():
        return datetime.date(1, 1, 1)

    lam = lambda: datetime.timedelta(0)
    comp = [datetime.timezone.utc for _ in range(1)]
    return nested, lam, comp
