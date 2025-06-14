import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

df = pd.read_csv('app/build/results/jmh/pipeline_results.csv')
df = df[df['Benchmark'].isin([
    'bench.pipeline.BenchPipeline.parallelPipeline',
    'bench.pipeline.BenchPipeline.seqPipeline'
])][['Param: mode', 'Benchmark', 'Score']]

modes_order = df['Param: mode'].drop_duplicates().tolist()

p = df.pivot(index='Param: mode', columns='Benchmark', values='Score')

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
