require "json"
require "faraday"

module Shop
  class Cart; end

  class Checkout < Faraday::Middleware
    include Rack::Utils
    include Trackable
    extend Forwardable
    prepend Observable
    has_many :items

    def run(id, url)
      Faraday::Utils.escape(id)
      Faraday::Connection.new(url)
      Cart.new
      Widget.find(id)
      Gadget.build(id)
      Receipt.new
      JSON.parse("{}")
      ::JSON.generate({})
      Rack::Request.new(url)
      Rails.root
      Net::HTTP::Get.new(url)
      raise ArgumentError, "bad id"
    end

    def tolerant
      yield
    rescue Net::ReadTimeout, ::Timeout::Error => e
      e
    rescue StandardError
      nil
    end

    def lexical
      Cart.find(1)
      Cache.fetch(2)
    end
  end
end

class Array
  def second
    self[1]
  end
end

class Reports
  def build(width)
    Array.new(width)
    Limit::MAX
  end
end

def typed(path, host)
  log = Logger.new($stdout)
  log.info("start")
  log.level = 1

  again = Logger.new($stderr)
  again = Logger.new($stdout)
  again.warn("twice")

  http = Net::HTTP.new(host)
  http.get(path)

  conn = Faraday::Connection.new(host)
  conn.get(path)
end

def parameter(log)
  log = Logger.new($stdout)
  log.error("param")
end

module I18n
  class Reporter
    def run
      Backend.reset
      Missing.reset
    end
  end
end
