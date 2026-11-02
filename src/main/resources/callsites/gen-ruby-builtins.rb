# Generates ruby-builtins.txt, the constants Ruby's core and standard library define.
#
#   ruby --disable-gems -W0 gen-ruby-builtins.rb | sort -u > ruby-builtins.txt
#
# (then prepend the header of the committed file). Core: every constant reachable from Object.
# Standard library: every feature under the interpreter's library and extension directories is
# required in a subprocess of its own, and the constants it adds (to a depth of four names) are
# credited to it. Each line is `<kind> <Constant::Path> [new]`: kind c (class), m (module), k (any
# other constant); `new` marks a class whose `new` is the plain Class#new.
require "json"
require "rbconfig"
require "open3"
require "timeout"

DEPTH = 4
SKIP_FEATURES = %w[debug irb mkmf rubygems bundler un rdoc readline bundled_gems rake enc].freeze

SNAPSHOT = <<~'RUBY'
  # Breadth first, so that each module is named by its shortest path (`Object::Foo` never is one)
  def snapshot
    out = {}
    seen = { Object.object_id => true }
    level = [[Object, ""]]
    1.upto(#{DEPTH}) do
      nxt = []
      level.each do |mod, prefix|
        mod.constants(false).sort.each do |n|
          v = (mod.const_get(n, false) rescue next)
          fq = prefix.empty? ? n.to_s : "#{prefix}::#{n}"
          next if out.key?(fq)
          out[fq] = if v.is_a?(Class)
            [:c, !(v <= Module) && v != Struct && v != Data && (v.method(:new).owner == Class rescue false)]
          elsif v.is_a?(Module) then [:m, false]
          else [:k, false] end
          next if !v.is_a?(Module) || seen[v.object_id]
          seen[v.object_id] = true
          nxt << [v, fq]
        end
      end
      level = nxt
    end
    out
  end
RUBY

def run(code, feature = nil)
  env = { "RUBYOPT" => nil, "BUNDLE_GEMFILE" => nil }
  args = [RbConfig.ruby, "--disable-gems", "-W0", "-e", code]
  args << feature if feature
  Timeout.timeout(20) { Open3.capture3(env, *args, stdin_data: "") }
rescue StandardError
  nil
end

helpers = SNAPSHOT.gsub("\#{DEPTH}", DEPTH.to_s)
core_out, = run("require 'rbconfig'\n" + helpers + "puts snapshot.map { |k, v| [k, v[0], v[1]].join(' ') }")
core = {}
core_out.each_line { |l| k, kind, nw = l.split; core[k] = [kind.to_sym, nw == "true"] }

features = []
lib = RbConfig::CONFIG["rubylibdir"]
Dir.glob("**/*.rb", base: lib).each { |f| features << f.sub(/\.rb\z/, "") }
arch = RbConfig::CONFIG["archdir"]
Dir.glob("**/*.{so,bundle}", base: arch).each { |f| features << f.sub(/\.(so|bundle)\z/, "") unless f.include?(".dSYM") } if arch
features = (ENV["ONLY"] ? ENV["ONLY"].split(",") : features).uniq.reject { |f| SKIP_FEATURES.include?(f.split("/").first) || f.start_with?("rubygems") || f.include?("/version") }

found = core.dup
queue = Queue.new
features.sort.each { |f| queue << f }
lock = Mutex.new
code = "require 'rbconfig'\n" + helpers + <<~'RUBY'
  before = snapshot
  require ARGV[0]
  after = snapshot
  puts after.reject { |k, _| before.key?(k) }.map { |k, v| [k, v[0], v[1]].join(' ') }
RUBY
Array.new(8) do
  Thread.new do
    loop do
      f = (queue.pop(true) rescue break)
      o, _e, st = run(code, f)
      unless o && st&.success?
        $stderr.puts "skipped feature #{f}: #{(_e || '').lines.first&.strip}"
        next
      end
      lock.synchronize { o.each_line { |l| k, kind, nw = l.split; found[k] ||= [kind.to_sym, nw == "true"] if k } }
    end
  end
end.each(&:join)

found.each do |fq, (kind, plain)|
  next if fq.split("::").any? { |s| s == "Object" || s.start_with?("_") || !s.match?(/\A[A-Z][A-Za-z0-9_]*\z/) }
  puts [kind, fq, (plain ? "new" : nil)].compact.join(" ")
end
