import glob

files = glob.glob('app/src/main/java/com/example/messmanagementsystem/ui/admin/*.kt')

for file in files:
    with open(file, 'r') as f:
        content = f.read()

    # Fix the missing comma after title = { Text("...") }
    content = content.replace(
        '}\n                navigationIcon = {',
        '},\n                navigationIcon = {'
    )
    
    # Fix the trailing },) which should just be })
    content = content.replace(
        ' },)',
        ' })'
    )

    with open(file, 'w') as f:
        f.write(content)

