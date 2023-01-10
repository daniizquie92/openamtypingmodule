#!/bin/bash
echo "your docker image name must be openam, if not, change all openam ocurrences in this file"
docker exec openam cp /usr/openam/config/config/xml/amUser.xml /usr/openam/config/config/xml/amUser.xml.orig
docker cp ./amUser.xml openam:/usr/openam/config/config/xml/amUser.xml
docker exec -w /usr/openam/ssoadmintools/openam/bin/ openam ./ssoadm delete-svc --servicename iPlanetAMUserService --adminid amadmin --password-file /tmp/pwd.txt
docker exec -w /usr/openam/ssoadmintools/openam/bin/ openam ./ssoadm create-svc  --adminid amadmin  --password-file /tmp/pwd.txt  --xmlfile /usr/openam/config/config/xml/amUser.xml

docker cp ./typingEnrollments-attr.ldif openam:/usr/openam/config/opends/bin/
docker cp ./typingPhrase-attr.ldif openam:/usr/openam/config/opends/bin/
docker exec -w /usr/openam/config/opends/bin/ openam ./ldapmodify --port 50389 --hostname openam.example.com --bindDN "cn=Directory Manager" --bindPassword tortilla --filename typingEnrollments-attr.ldif
docker exec -w /usr/openam/config/opends/bin/ openam ./ldapmodify --port 50389 --hostname openam.example.com --bindDN "cn=Directory Manager" --bindPassword tortilla --filename typingPhrase-attr.ldif

echo "THE NEXT STEPS MUST BE DONE MANNUALY"
echo ""
echo "In OpenAM console, browse to Realms > Realm Name > Data Stores > Data Store Name"
echo "Add the object class, here typingPhraseObjectclass and typingEnrollmentsObjectclass, to the LDAP User Object Class list"
echo "Add the attribute type, here typingPhrase and typingEnrollments, to the LDAP User Attributes list."
