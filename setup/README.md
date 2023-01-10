All setup files have been configurated to use a docker container called openam, you must chanche it if it has anhother name

If you want to upload the module to your own openam instance, you should change the "tortilla" reference in all the docker files or just comment the line
and create your own password file containg your amadmin password in /tmp called pwd.txt
