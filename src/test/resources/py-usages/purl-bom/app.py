import yaml
import requests

def load(text):
    return yaml.safe_load(text)

def fetch(url):
    return requests.get(url)
