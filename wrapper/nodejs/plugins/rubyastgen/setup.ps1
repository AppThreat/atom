$env:GEM_HOME="."
bundle install --binstubs=../bin --no-cache --standalone=frontend
Remove-Item -Recurse -Force bundle\ruby\*\bundler\gems\ruby_ast_gen-*\.git*
Remove-Item -Recurse -Force bundle\ruby\*\cache
