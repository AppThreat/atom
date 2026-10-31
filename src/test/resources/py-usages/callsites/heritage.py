import click
from celery import Celery

class Command(click.Command):
    pass

class Worker(Celery):
    pass
