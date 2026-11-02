module Paperclip
  class Transcoder < Paperclip::Processor
  end
end

class Admin::Account < ActiveModel::Serializer
end

class AddIndex < ActiveRecord::Migration[7.2]
end

class Failure < StandardError
end

class Slots
  def current
    ::Thread.current[:slot]
    Paperclip.options
    Paperclip::Transcoder.new
  end
end

class Kernelish
  def run(value)
    Array(value)
    raise "boom" if value.nil?
    (..10).cover?(value)
  end
end
