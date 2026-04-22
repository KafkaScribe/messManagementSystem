import os
import glob

directory = 'app/src/main/java/com/example/messmanagementsystem/ui/admin'
files = glob.glob(f'{directory}/*.kt')

for file in files:
    with open(file, 'r') as f:
        content = f.read()

    # if already has ArrowBack, skip
    if 'ArrowBack' in content:
        continue

    # Add imports
    imports_to_add = [
        "import androidx.compose.material.icons.Icons",
        "import androidx.compose.material.icons.filled.ArrowBack",
        "import androidx.compose.material3.Icon",
        "import androidx.compose.material3.IconButton"
    ]
    
    lines = content.split('\n')
    import_index = 0
    for i, line in enumerate(lines):
        if line.startswith('import '):
            import_index = i

    for imp in imports_to_add:
        if imp not in content:
            lines.insert(import_index + 1, imp)
            
    content = '\n'.join(lines)
    
    # Replace TopAppBar
    # We look for "TopAppBar(" and insert navigationIcon right after the title or inside it.
    import re
    # Match TopAppBar(title = { Text("Something") }
    pattern = re.compile(r'(TopAppBar\s*\(\s*title\s*=\s*\{\s*Text\("[^"]+"\)\s*\},?)')
    
    replacement = r'\1\n                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },'
    
    new_content = pattern.sub(replacement, content)
    
    if new_content != content:
        with open(file, 'w') as f:
            f.write(new_content)
        print(f"Updated {file}")
    else:
        print(f"No match in {file}")

