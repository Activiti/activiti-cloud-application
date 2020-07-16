#!/bin/bash

export HELM_ACTIVITI_VERSION=7.1.0-M4
export APP_ACTIVITI_VERSION=7.1.0.M4
export GITHUB_CHARTS_REPO="https://github.com/Activiti/activiti-cloud-helm-charts.git"

make printrelease
sed -i -e "s/appVersion: .*/appVersion: $APP_ACTIVITI_VERSION/" Chart.yaml
sed -i -e "s/#tag: .*/tag: $APP_ACTIVITI_VERSION/" values.yaml
make tag
make release
make github
