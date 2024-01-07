# Cave Converter

Copyright (C) 2009-2024 Paul Fretwell - aka 'Footleg'

You can find the lastest code on GitHub: https://github.com/Footleg/caveconverter/

Cave Converter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Cave Converter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Cave Converter.  If not, see <http://www.gnu.org/licenses/>.


## Building from Source
Cave Converter can be built from the Java source code using the provided gradle
build script. I have not checked the gradle wrapper jar into the source 
repository so in order to build, you will need to download gradle (v8.0 or 
later) and add it to your path. Then from a terminal window in the root folder
of the project, run the command: 
``
gradle wrapper
``

Now you can build using the gradle script:
``
gradlew build
``

LEGACY ANT BUILD NOTES:
To enable the ant script to work on Linux (Debian) with Open-JDK it was
necessary to define the env.JAVA_HOME property in the build.properties file.
This was not necessary when building on a Windows machine where the system 
environment variable JAVA_HOME was already pointing to a JDK.
To build on Debian I added this line to build.properties:
env.JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

To build and run all tests, just type:
 ant

To just build the release distribution:
 ant zip

This should generate the jar file in a build folder, and a complete 
distributable zip file in a dist folder.

To run full unit and regression tests, and generate code coverage reports:
 ant coverage.regressions

To generate the Javadoc:
 ant javadoc

To do a full clean build and generate everything (this is the default target):
 ant all

Other primary ant targets can be listed by running:
 ant -p

Note that some internal targets will not work with the source distribution 
as they are related to code in development which is not yet functional :-)

This source package has been put together with the full code needed to build 
the command line cave converter tool using OpenJDK8

(Note: The build tools are in the process of being migrated to Gradle and then
ant will be removed from this project.)
