import firebase_admin
from firebase_admin import credentials, db

from dash import Dash, html, dcc, callback, Output, Input
import plotly.express as px
import pandas as pd


cred_obj = credentials.Certificate("artflow-17f2c-firebase-adminsdk-kl9hx-87309dbcef.json")
databaseURL = "https://artflow-17f2c-default-rtdb.firebaseio.com/" 
db = firebase_admin.initialize_app(cred_obj, {'databaseURL': databaseURL})


# Função para buscar dados da Realtime Database
def fetch_data():
    ref = db.reference("drawings")
    docs = ref.get()
    data = []
    for doc_id, doc_data in docs.items():
        paths = doc_data.get('paths', {})
        for path_id, path in paths.items():
            row = {'timestamp': doc_id, 'rating': doc_data.get('rating')}
            row.update(path)
            data.append(row)
    df = pd.DataFrame(data)
    return df


app = Dash(__name__)

app.layout = html.Div([
    html.H1(children='Firebase Data Visualization', style={'textAlign': 'center'}),
    dcc.Graph(id='graph-rating'),
    dcc.Graph(id='graph-stroke-width'),
])

@app.callback(Output('graph-rating', 'figure'), [])
def update_rating_graph():
    df = fetch_data()
    fig = px.histogram(df, x='rating', title='Rating Distribution')
    return fig

@app.callback(Output('graph-stroke-width', 'figure'), [])
def update_stroke_width_graph():
    df = fetch_data()
    fig = px.histogram(df, x='strokeWidth', title='Stroke Width Distribution')
    return fig

# Executar o aplicativo
if __name__ == '__main__':
    app.run_server(debug=True)