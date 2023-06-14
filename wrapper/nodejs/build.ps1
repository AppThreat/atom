copy ..\..\target\atom.zip plugins\
Expand-Archive -Path ..\..\target\atom.zip -DestinationPath plugins\ -Force
Remove-Item plugins\atom.zip
npm install
