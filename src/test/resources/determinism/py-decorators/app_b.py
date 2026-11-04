from flask import Blueprint

app = Blueprint("b", __name__)


@app.route("/b")
def b():
    return "b"
