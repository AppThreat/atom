from flask import Flask, request
from sqlalchemy.orm import Session

app = Flask(__name__)

@app.route("/x")
def view(s: Session) -> str:
    return request.method

def caller(req):
    return req.headers

def make_session(engine):
    return Session(engine)

import typing

def sessions(engine) -> typing.Iterator[Session]:
    yield Session(engine)
