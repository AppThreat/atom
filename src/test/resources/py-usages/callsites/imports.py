import os
import os.path
import numpy as np
from flask import Flask, request
from django.db.models import Q

print(os.getcwd())
print(os.path.join("a", "b"))
print(np.array([1]))
app = Flask(__name__)
print(request.method if False else "")
print(Q())
