import click
from celery import Celery

class Command(click.Command):
    pass

class Worker(Celery):
    pass

import argparse

class Formatter(argparse.HelpFormatter):
    def add(self, usage):
        self.add_usage(usage, None, None)
