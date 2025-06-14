import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

df = pd.read_csv('app/build/results/jmh/convolution_results.csv')
df.rename(columns={'Param: imageName': 'img', 'Param: mode': 'mode', 'Score': 'score'}, inplace=True)

df['img'] = df['img'].map({
    'bird.png': '1280x853',
    'kha.bmp': '3000x2000'
})

modes = df['mode'].unique()
imgs = df['img'].unique()
w = 0.8 / len(imgs)
x = np.arange(len(modes))

for i, img in enumerate(imgs):
    s = [df[(df['mode'] == m) & (df['img'] == img)]['score'].values[0] for m in modes]
    plt.bar(x + i * w, s, width=w, label=img)

plt.xticks(x + w * (len(imgs) - 1) / 2, modes, rotation=45)
plt.ylabel('Time, milliseconds')
plt.title('Convolution Benchmark')
plt.legend(title='Image size')
plt.grid(axis='y', linestyle='--', alpha=0.5)
plt.tight_layout()
plt.savefig('convolution.png')
