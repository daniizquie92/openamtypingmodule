All setup files have been configurated to use a docker container called openam, you must change it if it has another name

If you want to upload the module to your own openam instance, you should change the "tortilla" reference in all the docker files or just comment the line
and create your own password file containg your amadmin password in /tmp called pwd.txt

If your openam instance does not run with docker, you can use the commands in the docker_uploads files included here, changing de docker command part.
