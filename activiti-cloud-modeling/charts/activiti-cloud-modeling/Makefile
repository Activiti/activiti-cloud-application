CURRENT=$(shell pwd)
NAME := activiti-cloud-modeling
ORG := $(or $(ORG),activiti)
APP_NAME := $(or $(APP_NAME),$(NAME))
OS := $(shell uname)
RELEASE_ARTIFACT := $(or $(RELEASE_ARTIFACT),$(APP_NAME))

GITHUB_CHARTS_REPO := $(or $(GITHUB_CHARTS_REPO),$(shell git config --get remote.origin.url))
GITHUB_CHARTS_DIR := ../../../activiti-cloud-helm-charts
GITHUB_CHARTS_BRANCH := $(or $(GITHUB_CHARTS_BRANCH),gh-pages)

build: clean
	helm init --client-only
	helm repo add charts_activiti https://activiti.github.io/activiti-cloud-charts/
	helm repo add activiti-cloud-helm-charts https://activiti.github.io/activiti-cloud-helm-charts/
	helm dependency build
	helm lint

install:
	helm install . --name ${NAME}

upgrade: clean build
	helm upgrade ${NAME} .

delete:
	helm delete --purge ${NAME}

clean:
	rm -rf requirements.lock
	rm -rf charts
	rm -rf ${NAME}*.tgz

release:
	helm package .

github:
	echo "$(GITHUB_CHARTS_DIR) - GITHUB_CHARTS_DIR"
	pwd
	ls
	if test -d $(GITHUB_CHARTS_DIR); \
		then echo exists; \
	else git clone -b "$(GITHUB_CHARTS_BRANCH)" https://${GITHUB_TOKEN}@github.com/Activiti/activiti-cloud-helm-charts.git $(GITHUB_CHARTS_DIR); \
  fi
	cp "$(NAME)-$(VERSION).tgz" $(GITHUB_CHARTS_DIR)
		cd $(GITHUB_CHARTS_DIR) && \
		git pull && \
	  helm repo index . && \
	  git add  $(NAME)-$(VERSION).tgz index.yaml && \
	  git status && \
	  git commit -m "fix:(version) release $(NAME)-$(VERSION).tgz"

version:
	echo "VERSION $(VERSION)"
	sed -i -e "s/version:.*/version: $(VERSION)/" Chart.yaml
	sed -i -e "s/apiVersion:.*/apiVersion: $(VERSION)/" Chart.yaml
	export VERSION=$(VERSION) && perl -0777 -pi -e '$$version = $$ENV{'VERSION'};s/repository: activiti\/activiti-cloud-modeling\n\s*tag: ([0-9]|[a-zA-Z]|\.)*/repository: activiti\/activiti-cloud-modeling\n    tag: $$version/' values.yaml

common-helm-chart-version:
	sed -i -e "s/version:.*/version: $(VERSION)/" requirements.yaml
