#!/bin/bash
echo "your docker image name must be openam, if not, change all openam ocurrences in this file"
mv ../typing-authentication-module2/target/typing-authentication-module-14.6.7-SNAPSHOT.jar ../typing-authentication-module.jar
docker cp ../typing-authentication-module2/target/typing-authentication-module.jar openam:/usr/local/tomcat/webapps/openam/WEB-INF/lib
docker cp ../typing-authentication-module2/src openam:/root
docker exec openam touch /tmp/pwd.txt
docker exec openam echo "tortilla" > /tmp/pwd.txt
docker exec -w /usr/openam/ssoadmintools/openam/bin/ openam ./ssoadm delete-svc --servicename iPlanetAMAuthTypingAuthService --adminid amadmin --password-file /tmp/pwd.txt
docker exec -w /usr/openam/ssoadmintools/openam/bin/ openam ./ssoadm create-svc  --adminid amadmin  --password-file /tmp/pwd.txt  --xmlfile /root/src/main/resources/amAuthTypingAuth.xml
docker exec -w /usr/openam/ssoadmintools/openam/bin/ openam ./ssoadm register-auth-module  --adminid amadmin  --password-file /tmp/pwd.txt  --authmodule org.typing.forgerock.auth.TypingAuth
docker restart openam
echo "module registered"
