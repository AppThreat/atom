copy ..\..\target\atom.zip plugins\
Expand-Archive -Path ..\..\target\atom.zip -DestinationPath plugins\ -Force
Remove-Item -Force plugins\atom.zip

cd .\packages\atom-parsetools
.\build.ps1
cd ..\..\

npm ci
