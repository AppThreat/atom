import requests
import urllib3

def guarded():
    try:
        return requests.get("http://x")
    except requests.ConnectionError:
        return None
    except (urllib3.exceptions.HTTPError, ValueError):
        return None

def grouped():
    try:
        return requests.head("http://x")
    except* requests.RequestException:
        return None
