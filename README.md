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
build script. From a terminal window in the root folder of the project, run the
command:

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

(Note: The build tools are in the process of being migrated to Gradle and then
ant will be removed from this project.)
