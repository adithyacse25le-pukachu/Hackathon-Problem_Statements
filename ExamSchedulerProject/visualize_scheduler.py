import pandas as pd
import networkx as nx
import matplotlib.pyplot as plt

try:
    edges_df = pd.read_csv('graph.csv')
    colors_df = pd.read_csv('node_colors.csv')
    benchmark_df = pd.read_csv('benchmark.csv') # Reads the new scaling data
except FileNotFoundError:
    print("CSV files not found. Run your Java program first.")
    exit()

fig = plt.figure(figsize=(18, 6))
fig.canvas.manager.set_window_title('Exam Timetable Scheduling')

palette = ['#e74c3c', '#3498db', '#2ecc71', '#f1c40f', '#9b59b6', '#e67e22']
subjects = ["AI", "SEPM", "TOC", "RMIPR", "EVS", "CN"]
hall_names = ["LH501", "LH502", "LH530", "LH531", "LH532", "LH533"]

# --- 1. Conflict Graph Subplot ---
ax1 = fig.add_subplot(131)
G = nx.Graph()
for _, row in edges_df.iterrows():
    G.add_edge(int(row['u']), int(row['v']))
for _, row in colors_df.iterrows():
    G.add_node(int(row['node']), slot=int(row['slot']))

node_colors = [palette[G.nodes[n].get('slot', 0) % len(palette)] for n in G.nodes()]
pos = nx.spring_layout(G, seed=42)
labels = {n: (subjects[n] if n < len(subjects) else f"C{n}") for n in G.nodes()}

nx.draw(G, pos, ax=ax1, labels=labels, node_color=node_colors, node_size=1600, 
        font_color='white', font_weight='bold', font_size=9, edge_color='gray')
ax1.set_title("Conflict Graph", fontweight='bold', fontsize=12)

# --- 2. Real Timetable Grid Subplot ---
ax2 = fig.add_subplot(132)
ax2.axis('off')
ax2.set_title("Official Exam Timetable", fontweight='bold', fontsize=12)

grid_data = []
with open('timetable.csv', 'r') as f:
    for line in f:
        row = line.strip().split(',')
        slot_name = row[0].replace("Slot", "Time Slot") + "\n(2:00 PM - 5:00 PM)"
        formatted_row = [slot_name]
        for x in row[1:]:
            if x:
                idx = int(x)
                formatted_row.append(subjects[idx] if idx < len(subjects) else f"Course {x}")
        grid_data.append(formatted_row)

max_cols = max((len(row) for row in grid_data), default=0)
for row in grid_data:
    row.extend([""] * (max_cols - len(row))) 

headers = ["Date & Time"]
for i in range(1, max_cols):
    headers.append(hall_names[i-1] if (i-1) < len(hall_names) else f"LH5{30+i}")
    
table_data = [headers] + grid_data
table = ax2.table(cellText=table_data, loc='center', cellLoc='center')
table.scale(1, 3.5) 

for (i, j), cell in table.get_celld().items():
    if i == 0: 
        cell.set_facecolor('#2c3e50') 
        cell.get_text().set_color('white')
        cell.get_text().set_weight('bold')
    elif j > 0 and cell.get_text().get_text() != "": 
        cell.set_facecolor(palette[(i-1) % len(palette)])
        cell.get_text().set_color('white')
        cell.get_text().set_weight('bold')
    else:
        cell.get_text().set_weight('bold')

# --- 3. STRICT COMPLIANCE: Performance As N Increases ---
ax3 = fig.add_subplot(133)
ax3.set_title("Performance vs N (Courses)", fontweight='bold', fontsize=12)
ax3.set_xlabel("Number of Courses (N)", fontweight='bold')
ax3.set_ylabel("Running Time (ms)", fontweight='bold')

# Plot Running Time
line1 = ax3.plot(benchmark_df['N'], benchmark_df['GreedyTimeMs'], label='Greedy Time (ms)', color='#3498db', marker='o', linewidth=2)
line2 = ax3.plot(benchmark_df['N'], benchmark_df['ExactTimeMs'], label='Exact Time (ms)', color='#e74c3c', marker='s', linewidth=2)

# Create second Y-axis for Slots Used
ax4 = ax3.twinx()
ax4.set_ylabel("Total Slots Used", fontweight='bold')
line3 = ax4.plot(benchmark_df['N'], benchmark_df['GreedySlots'], label='Greedy Slots', color='#3498db', linestyle='--', marker='x')
line4 = ax4.plot(benchmark_df['N'], benchmark_df['ExactSlots'], label='Exact Slots', color='#e74c3c', linestyle='--', marker='^')

# Combine legends from both axes
lines = line1 + line2 + line3 + line4
labels_legend = [l.get_label() for l in lines]
ax3.legend(lines, labels_legend, loc='upper left', fontsize=9)
ax3.grid(True, linestyle=':', alpha=0.6)

plt.tight_layout()
plt.show()