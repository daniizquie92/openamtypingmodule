- Download minikube
- Add the user to docker group
- Start mimikube
- minikube addons enable ingress
- Download kubectl
  
Make sure the root user has access to the same minikube cluster as the user

Start the docker container with the image daniizquie921/openamtypingmodule:daniizquiesite
- copy the directory /usr/openam to your own computer (docker cp openam:/usr/openam .)


For using your local image with minikube use:
- docker login
- eval $(minikube -p minikube docker-env)
- docker build .

- apply the files in this github repo with: kubectl apply -f .
copy the directory you copied before to the pod created

- kubectl cp ./openam openam-0:/usr/


Edit the /etc/hosts file and include the minikube ip followed by openam.daniizquie921.com
Edit the /etc/hosts file and include openam.example.com to the localhost reference
Enter the pod via localhost use
- kubectl port-forward svc/openam 80 443
- access: https://openam.example.com:443/openam (credentials are: user:amadmin password:tortilla)
  
In OPENAM admin UI
  - sites: add new site, add the address https://openam.daniizquie921.com:443/openam to primary URL, and http://openam.daniizquie921.com:80/openam to secundary
  - in configuracion -> global services -> platform -> add in coockie domains openam.daniizquie921.com

Now you should be able to access via ingress, you can stop the port-forwarding

NOTE:
- if you want to use an address different than openam.daniizquie921.com you should edit the places where it appears in this file. 
- Also replace it from the openam-ingress.yaml
- You may also experience problems with the ingress. You should enter the OpenAM admin UI via localhost with the kubectl port-forward command
described before and edit the cookie domains and site 
