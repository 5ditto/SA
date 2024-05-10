import firebase_admin
from firebase_admin import credentials, db

from dash import Dash, html, dcc, callback, Output, Input,dash
import plotly.express as px
import plotly.graph_objects as go
import pandas as pd
import csv



cred_obj = credentials.Certificate("artflow-17f2c-firebase-adminsdk-kl9hx-87309dbcef.json")
databaseURL = "https://artflow-17f2c-default-rtdb.firebaseio.com/"
firebase_admin.initialize_app(cred_obj, {'databaseURL': databaseURL})


# Função para buscar dados da Realtime Database
def get_data():
    ref_drawings = db.reference('drawings')
    data_drawings = ref_drawings.get()
    csv_data_draw = []
    csv_data_path = []

    for drawing_id, drawing_data in data_drawings.items():
        n_paths = 0
        rating = drawing_data.get('rating', 0)
        n_paths = sum(1 for key in drawing_data.keys() if key.startswith('path'))
        image = drawing_data.get('Draw File','')
        csv_data_draw.append([drawing_id, n_paths, rating,image])
        for key, value in drawing_data.items():
            if key.startswith('path'):
                path_color = value.get('paint', {}).get('color', '')
                path_points = value.get('path', '').split(';')
                num_datapoints = len(path_points)
                stroke_width = value.get('paint', {}).get('strokeWidth', '')
                csv_data_path.append([drawing_id, key, path_color, num_datapoints, stroke_width])


    with open('draw_data.csv', 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(["DrawingID", "NoPaths","Rating","Image"])
        writer.writerows(csv_data_draw)
    
    with open('paths_data.csv', 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(["DrawingID", "PathID","Color","NoDataPoints","StrokeWidth"])
        writer.writerows(csv_data_path)

get_data()

df_draw = pd.read_csv("draw_data.csv")
df_paths = pd.read_csv("paths_data.csv")

df = pd.merge(df_draw, df_paths, on='DrawingID')

total_drawings = df_draw.shape[0]

app = Dash(__name__)

app.layout = html.Div([
    html.H1(children='Firebase Data Visualization', style={'textAlign': 'center', 'margin-bottom': '50px'}),
    html.H2(children='General Data', style={'textAlign': 'center', 'margin-bottom': '50px'}),
    
    html.Div([
        html.Div([
            html.Div([
                html.H2("Total Drawings Made", style={'color': '#007bff', 'margin-bottom': '10px'}),
                html.H1(f"{total_drawings}", style={'color': '#007bff', 'font-size': '48px', 'margin': '0'})
            ], style={'textAlign': 'center', 'border': '2px solid #007bff', 'border-radius': '10px', 'padding': '20px', 'margin-bottom': '20px'}),
            
            dcc.Graph(id='graph-rating'),
        ], style={'width': '30%', 'display': 'inline-block', 'vertical-align': 'top'}),
        
        html.Div([
            dcc.Graph(id='graph-path-distribution', style={'margin-bottom': '20px'})
        ], style={'width': '68%', 'display': 'inline-block', 'vertical-align': 'top', 'margin-left': '2%'})
    ]),

    html.Div([
        html.H2(children='Data of Individual Draws', style={'textAlign': 'center', 'margin-bottom': '50px'}),
        html.Div([
            dcc.Dropdown(
                id='dropdown-drawings',
                options=[{'label': f'Drawing {i}', 'value': i} for i in df_draw['DrawingID']],
                value=df_paths['DrawingID'].iloc[0],
                style={'width': '80%', 'margin': 'auto'}
            ),
        ]),

        html.Div([
            html.Div(id='color-counts',style={'textAlign': 'center', 'border': '2px solid #007bff', 'border-radius': '10px', 'padding': '20px', 'margin-bottom': '20px','margin-top': '20px'}),
        ], style={'width': '30%', 'display': 'inline-block', 'vertical-align': 'top', 'align-items': 'center'}),   
        

    ]),
    html.Div([
        html.Div([
            dcc.Graph(id='strokeWidth_distribution'),
        ], style={'width': '48%', 'display': 'inline-block'}),

        html.Div([
            dcc.Graph(id='update_datapoints_graph'),
        ], style={'width': '48%', 'display': 'inline-block', 'margin-left': '2%'})
    ]),
        
    html.Div([    
        html.H3(children='Draw:', style={'textAlign': 'center', 'margin-bottom': '50px'}),
            html.Div([
            html.Img(id='image-display', style={'width': '50%', 'border': '2px solid black', 'margin': 'auto'})
        ], style={'text-align': 'center', 'margin-top': '20px'}),
    ]),

    
    html.Div(id='hidden-div', style={'display': 'none'})  # Adicionando uma entrada falsa
])



# Geral

@app.callback(
    Output('graph-rating', 'figure'),
    [Input('hidden-div', 'children')]
)
def update_rating_graph(_):
    # Rating Distribution
    fig_rating = go.Figure(go.Pie(labels=[f'Rating {i}' for i in range(1, 6)],
                                   values=df['Rating'].value_counts().sort_index(),
                                   hole=0.5,
                                   marker=dict(colors=['#007bff', '#6c757d', '#28a745', '#ffc107', '#dc3545']),
                                   hoverinfo="label+value+percent",
                                   textinfo="label+value"))
    fig_rating.update_layout(title='Rating Distribution',
                             margin=dict(l=20, r=20, t=50, b=20),
                             legend=dict(orientation="h", x=0.5, y=-0.2))
    
    return fig_rating

# Atualizar o gráfico de distribuição de paths
@app.callback(
    Output('graph-path-distribution', 'figure'),
    [Input('hidden-div', 'children')]
)
def update_path_distribution_graph(_):
    path_counts = df_draw['NoPaths'].value_counts().sort_index()
    fig_path_distribution = go.Figure(go.Scatter(x=path_counts.index,
                                                  y=path_counts.values,
                                                  mode='lines+markers',
                                                  marker_color='#007bff'))
    fig_path_distribution.update_layout(title='Path Distribution',
                                        xaxis_title='Number of Paths',
                                        yaxis_title='Number of Drawings',
                                        margin=dict(l=20, r=20, t=50, b=20),
                                        plot_bgcolor='#f8f9fa',
                                        paper_bgcolor='#f8f9fa')
    
    return fig_path_distribution


## Individual   

@app.callback(
    Output('update_datapoints_graph', 'figure'),
    [Input('dropdown-drawings', 'value')]
)
def update_datapoints_graph(selected_drawing):
    selected_drawing_data = df_paths[df_paths['DrawingID'] == selected_drawing]
    fig_datapoints = go.Figure(go.Bar(x=selected_drawing_data.index + 1,
                                      y=selected_drawing_data['NoDataPoints'],
                                      marker_color='#007bff'))
    fig_datapoints.update_layout(title=f'Drawing {selected_drawing} Data',
                                 xaxis_title='Path',
                                 yaxis_title='Number of Datapoints',
                                 margin=dict(l=20, r=20, t=50, b=20),
                                 plot_bgcolor='#f8f9fa',
                                 paper_bgcolor='#f8f9fa')
    return fig_datapoints

@app.callback(
    Output('color-counts', 'children'),
    [Input('dropdown-drawings', 'value')]
)
def update_color_counts(selected_drawing):
    selected_drawing_data = df[df['DrawingID'] == selected_drawing].copy()  
    unique_colors = selected_drawing_data['Color'].nunique()
    return html.Div([
        html.H4('Número de cores:', style={'margin-bottom': '5px'}),
        html.H1(f'{unique_colors}', style={'color': '#007bff', 'margin': '0'})
    ])

@app.callback(
    Output('strokeWidth_distribution', 'figure'),
    [Input('dropdown-drawings', 'value')]
)
def update_strokeWisdth_distribution(selected_drawing):
    selected_drawing_data = df_paths[df_paths['DrawingID'] == selected_drawing].copy()
    width_bins = [0, 10, 20, 30, 40, float('inf')]
    width_labels = ['<= 10', '11-20', '21-30', '31-40', '> 40']
    selected_drawing_data['WidthGroup'] = pd.cut(selected_drawing_data['StrokeWidth'], bins=width_bins, labels=width_labels)
    width_counts = selected_drawing_data['WidthGroup'].value_counts().sort_index()
    fig_width_distribution = go.Figure(go.Bar(x=width_counts.index,
                                              y=width_counts.values,
                                              marker_color='#007bff'))
    fig_width_distribution.update_layout(title='Stroke Width Distribution',
                                             xaxis_title='Thickness',
                                             yaxis_title='Number of Drawings',
                                             margin=dict(l=20, r=20, t=50, b=20),
                                             plot_bgcolor='#f8f9fa',
                                             paper_bgcolor='#f8f9fa')
    
    return fig_width_distribution

@app.callback(
    Output('image-display', 'src'),
    [Input('dropdown-drawings', 'value')]
)
def update_image_src(selected_drawing):
    selected_drawing_data = df_draw[df_draw['DrawingID'] == selected_drawing].copy()
    base64_string = selected_drawing_data['Image'].iloc[0]
    return f'data:image/png;base64,{base64_string}'


if __name__ == '__main__':
    app.run_server(debug=True)