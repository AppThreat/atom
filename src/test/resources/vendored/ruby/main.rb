require_relative 'third_party/lib/helper'
require_relative 'src/not_third_party/util'

Vend::Helper.run(1)
NotVend::Util.help
puts "x"
