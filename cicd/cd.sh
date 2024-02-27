kubectl delete -f ./kube/deploy.yaml
kubectl delete -f ./kube/istioVirtualService.yaml

kubectl apply -f ./kube/deploy.yaml
kubectl apply -f ./kube/istioVirtualService.yaml
