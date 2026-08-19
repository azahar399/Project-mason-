import os
import re

bengali_pattern = re.compile(r'[\u0980-\u09FF]')

for root, dirs, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                lines = f.readlines()
                for i, line in enumerate(lines):
                    if bengali_pattern.search(line):
                        print(f"{filepath}:{i+1}: {line.strip()}")
