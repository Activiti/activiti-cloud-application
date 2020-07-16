# activiti-cloud-full-chart

<br>All chart archives are stored in https://github.com/Activiti/activiti-cloud-helm-charts 
<br>Full chart located at https://github.com/Activiti/activiti-cloud-full-chart 
<br>Common chart is a base chart for all charts now located at https://github.com/Activiti/activiti-cloud-common-chart 
<br>Charts for components located at component folders like: runtime https://github.com/Activiti/example-runtime-bundle/tree/master/charts/runtime-bundle and example cloud connector https://github.com/Activiti/example-cloud-connector/tree/master/charts/activiti-cloud-connector


## Getting started located at https://activiti.gitbook.io/activiti-7-developers-guide/getting-started/getting-started-activiti-cloud

## Preview Environments 

There is a stage in Jenkinsfile pipeline triggered on feature-* branch pattern. It installs Helm chart from feature branch commit into preview namespace for development and testing.

To create preview environment use the following commands  i.e.

```bash
git checkout <tag or branch>
git checkout -b feature-awesome
git push -u origin feature-awesome

```
or use provided `make preview` command, i.e.

```bash
make preview FROM=<master or tag> FEATURE=awesome

```

After pushing branch to remote, check your branch deployment status on Github: https://github.com/activiti/activiti-cloud-full-chart/branches'

If you make any changes and push the commit to remote, it will trigger preview stage again and upgrade the environment automatically.

To delete preview environment, simply delete your feature-* branch from remote. Once Jenkins runs the clean up, it will trigger another Jenkins pipeline to delete deployed release and namespace in the K8s cluster.

## Skipping CI

You want to skip running release pipeline stages, simply add `[ci skip]` to commit message.

# activiti-cloud
Activiti Cloud Parent and BOM (Bill of Materials)
## CI/CD

Running on Travis, requires the following environment variable to be set:

| Name | Description |
|------|-------------|
| MAVEN_USERNAME | Internal Maven repository username |
| MAVEN_PASSWORD | Internal Maven repository password |
| GITHUB_TOKEN | Github token for git service account |
| GITHUB_USER | Github user name for git service account |
| K8S_API_TOKEN | Kubernetes API token |
| K8S_API_URL | Kubernetes API url |


