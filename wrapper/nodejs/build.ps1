copy ..\..\target\atom.zip plugins\
Expand-Archive -Path ..\..\target\atom.zip -DestinationPath plugins\ -Force
Remove-Item -Force plugins\atom.zip

.\packages\atom-parsetools\build.ps1

npm ci
