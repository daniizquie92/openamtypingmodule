#!/bin/bash
echo "your docker image name must be openam, if not, change all openam ocurrences in this file"
docker cp autocomplete-disabler.js openam:/usr/local/tomcat/webapps/openam/XUI/
docker cp typing-visualizer.js openam:/usr/local/tomcat/webapps/openam/XUI/
docker cp typingdna.js openam:/usr/local/tomcat/webapps/openam/XUI/
docker cp typing_text.css openam:/usr/local/tomcat/webapps/openam/XUI/
docker cp TypingAuth2.html openam:/usr/local/tomcat/webapps/openam/XUI/templates/openam/authn/
