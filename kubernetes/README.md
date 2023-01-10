- Download mimikube
- Add the user to docker group
- Start mimikube
- Download kubectl

For using your local image with minikube use:
- docker login
- eval $(minikube -p minikube docker-env)
- docker build .
  
Start the docker container with the image daniizquie921/openamtypingmodule:daniizquiesite
- copy the directory /usr/openam to your own computer (docker cp openam:/usr/openam .)

- apply the files in this github repo with: kubectl apply -f .
copy the directory you copied before to the pod created

- kubectl cp ./openam openam-0:/usr/

If you want to enter the pod via localhost use
- kubectl port-forward svc/openam 80 443

Edit the /etc/hosts file and include the minikube ip followed by openam.daniizquie921.com

In OPENAM admin UI
- sites: add new site, add the address https://openam.daniizquie921.com:443/openam to primary URL, and http://openam.daniizquie921.com:80/openam to secundary
- in configuracion -> global services -> platform -> add in coockie domains openam.daniizquie921.com

NOTE:
- if you want to use an address different than openam.daniizquie921.com you should edit the places where it appears in this file. 
- Also replace it from the openam-ingress.yaml
- You may also experience problems with the ingress. You should enter the OpenAM admin UI via localhost with the kubectl port-forward command
described before and edit the cookie domains and site 
