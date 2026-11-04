import os
from third_party.lib import run, Base
from src.not_third_party import helper


class Child(Base):
    pass


def main():
    run(1)
    helper()
    os.getcwd()
