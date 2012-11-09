require 'rubygems'
require "bundler/setup"
require 'buildr-dependency-extensions'

# Version number for this release
VERSION_NUMBER = "1.3.2.#{ENV['BUILD_NUMBER'] || 'SNAPSHOT'}"
# Group identifier for your projects
COPYRIGHT = ""

# Specify Maven 2.0 remote repositories here, like this:
repositories.remote << "http://www.ibiblio.org/maven2"
repositories.release_to = 'sftp://artifacts:repository@192.168.0.96/home/artifacts/repository'

define "jaws" do
  extend PomGenerator

  ENV['JAVA_OPTS'] ||= '-Xmx1g'
  project.version = VERSION_NUMBER
  project.group = "fcr"
  manifest["Implementation-Vendor"] = COPYRIGHT
  
  download(artifact("junit4:junit4:jar:4.8.2")=> "https://github.com/downloads/KentBeck/junit/junit-4.8.2.jar")
  
  compile.with 'commons-io:commons-io:jar:2.0.1'
  
  resources
  
  test.using :java_args => [ '-Xmx1g' ]
  test.compile.with 'junit4:junit4:jar:4.8.2'
  test.resources
  
  package(:jar)
  
end
