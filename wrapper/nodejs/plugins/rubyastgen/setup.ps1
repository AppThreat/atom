$env:GEM_HOME="."
bundle install --binstubs=../bin --no-cache --standalone=frontend
Remove-Item -Recurse -Force bundle\ruby\3.4.0\bundler\gems\ruby_ast_gen-05140cf67b0c\.git*
Remove-Item -Recurse -Force bundle\ruby\3.4.0\cache
