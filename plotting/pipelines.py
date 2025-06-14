import json
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

with open('app/build/results/jmh/pipeline_results.json') as f:
    data = json.load(f)

records = []
for b in data:
    if b['benchmark'] in [
        'bench.pipeline.BenchPipeline.parallelPipeline',
        'bench.pipeline.BenchPipeline.seqPipeline'
    ]:
        records.append({
            'mode': b['params']['mode'],
            'benchmark': b['benchmark'],
            'score': b['primaryMetric']['score']
        })

df = pd.DataFrame(records)

modes_order = df['mode'].drop_duplicates().tolist()

p = df.pivot(index='mode', columns='benchmark', values='score')
p = p.reindex(modes_order)

x = np.arange(len(p))
w = 0.35

plt.bar(x - w / 2, p.iloc[:, 0], w, label='parallel pipeline')
plt.bar(x + w / 2, p.iloc[:, 1], w, label='sequential pipeline')
plt.xticks(x, p.index, rotation=45, ha='right')
plt.ylabel('Time, seconds')
plt.title('Pipeline Benchmark')
plt.legend()
plt.grid(axis='y', linestyle='--', alpha=0.5)
plt.tight_layout()
plt.savefig('pipelines.png')
