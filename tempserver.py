import os
from flask import Flask
from flask import request

app = Flask(__name__)

@app.route("/", methods=['POST'])
def index():
    postData = request.form
    query = postData['query']
    print query
    return "nah"

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    app.run(host='0.0.0.0', port=port)
