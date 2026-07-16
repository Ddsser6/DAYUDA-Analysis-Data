from flask import Flask, request, jsonify
import pandas as pd
import io

app = Flask(__name__)

@app.route('/status', methods=['GET'])
def status():
  return jsonify({"status": "Server Python Aktif dan Siap!"}), 200

@app.route('/process-dataset', methods=['POST'])
def process_dataset():
  if 'file' not in request.files:
    return jsonify({"error": "Tidak Ada File Yang Di Kirim"}), 400

  file = request.files['file']
  filename = file.filename

  try:
    if filename.endswith('.csv'):
      df = pd.read_csv(io.StringIO(file.stream.read().decode("utf-8")))
    elif filename.endswith('.xlsx') or filenanme.endswith('.xls'):
      df = pd.read_exel(io.ByteIO(file.stream.read()))
    else:
      return jsonify({"error": "Format file tidak didukung! Harus .csv atau .xlsx"}), 400

    df = df.fillna("-")

    headers = list(df.columns)
    rows = df.values.tolist()

    result = {
        "columns": headers
          "data": [[str(cell) for cell in row] for row in rows]
    }

    return jsonify(result), 200

  except Exception as e:
      return jsonify({"error": str(e)}), 500

if __name__ == '__name__':
  print("=== SERVER BACKEND DAYUDA MODE STANDAR AKTIF ===")
  app.run(host='127.0.0.1', port=5000, debug=True)
