import csv
import networkx as nx
import matplotlib.pyplot as plt

edges_in_path = []
path_nodes = set()

try:
    with open('output_path.csv', mode='r') as infile:
        reader = csv.DictReader(infile)
        for row in reader:
            u = int(row['From'])
            v = int(row['City'])
            coupon = row['CouponUsed'] == 'True'
            edges_in_path.append((u, v, coupon))
            path_nodes.add(u)
            path_nodes.add(v)
except FileNotFoundError:
    print("output_path.csv not found! Run your Java program first.")
    exit()

G = nx.DiGraph()
for u, v, coupon in edges_in_path:
    G.add_edge(u, v)

if not G.nodes:
    print("No path found to visualize.")
    exit()

node_colors = []
start_node = min(path_nodes) if path_nodes else 1
end_node = max(path_nodes) if path_nodes else 4

for node in G.nodes():
    if node == start_node:
        node_colors.append('#2ecc71')  # Green for Start
    elif node == end_node:
        node_colors.append('#e74c3c')  # Red for Destination
    else:
        node_colors.append('#f39c12')  # Orange for Intermediate Stops

edge_colors = []
edge_labels = {}

for u, v in G.edges():
    is_on_path = False
    is_coupon = False
    for pu, pv, coupon in edges_in_path:
        if u == pu and v == pv:
            is_on_path = True
            is_coupon = coupon
            break
            
    if is_on_path:
        if is_coupon:
            edge_colors.append('#f1c40f')  # Gold for Coupon Applied
            edge_labels[(u, v)] = "Coupon Used"
        else:
            edge_colors.append('#3498db')  # Blue for Path
            edge_labels[(u, v)] = "Path"
    else:
        edge_colors.append('#bdc3c7')

plt.figure(figsize=(8, 6))
pos = nx.spring_layout(G, seed=42)

nx.draw_networkx_nodes(G, pos, node_color=node_colors, node_size=800)
nx.draw_networkx_labels(G, pos, font_weight='bold', font_color='white')
nx.draw_networkx_edges(G, pos, edge_color=edge_colors, width=3, arrowsize=20)
nx.draw_networkx_edge_labels(G, pos, edge_labels=edge_labels, font_size=10)

plt.title("Budget Traveller - Path Visualization\n(Green: Start | Red: Destination | Orange: Stops | Gold: Coupon Flight)", fontsize=10)
plt.axis('off')

plt.show()