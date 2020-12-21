CURRENT=$(shell pwd)
NAME := $(or $(APP_NAME),$(shell basename $(CURRENT)))
OS := $(shell uname)
ACTIVITI_CLOUD_VERSION := $(shell grep -oPm1 "(?<=<activiti-cloud.version>)[^<]+" "activiti-cloud-dependencies/pom.xml")
RELEASE_VERSION := $(or $(shell cat VERSION), $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout))
ACTIVITI_CLOUD_FULL_EXAMPLE_DIR := activiti-cloud-full-chart/charts/activiti-cloud-full-chart

updatebot/push-version:
	updatebot push-version --kind maven \
		org.activiti.cloud:activiti-cloud-dependencies ${RELEASE_VERSION} \
		org.activiti.cloud:activiti-cloud-modeling-dependencies ${ACTIVITI_CLOUD_VERSION} \
		org.activiti.cloud:activiti-cloud-audit-dependencies ${ACTIVITI_CLOUD_VERSION} \
		org.activiti.cloud:activiti-cloud-api-dependencies ${ACTIVITI_CLOUD_VERSION} \
		org.activiti.cloud:activiti-cloud-parent ${ACTIVITI_CLOUD_VERSION} \
		org.activiti.cloud:activiti-cloud-connectors-dependencies ${ACTIVITI_CLOUD_VERSION} \
		org.activiti.cloud:activiti-cloud-messages-dependencies ${ACTIVITI_CLOUD_VERSION} \
		org.activiti.cloud:activiti-cloud-modeling-dependencies ${ACTIVITI_CLOUD_VERSION} \
		org.activiti.cloud:activiti-cloud-notifications-graphql-dependencies ${ACTIVITI_CLOUD_VERSION} \
		org.activiti.cloud:activiti-cloud-query-dependencies ${ACTIVITI_CLOUD_VERSION} \
		org.activiti.cloud:activiti-cloud-runtime-bundle-dependencies ${ACTIVITI_CLOUD_VERSION} \
		org.activiti.cloud:activiti-cloud-service-common-dependencies ${ACTIVITI_CLOUD_VERSION} \
		--merge false;

updatebot/update:
	@echo doing updatebot update $(RELEASE_VERSION)
	updatebot update

updatebot/update-loop:
	@echo doing updatebot update-loop $(RELEASE_VERSION)
	updatebot update-loop --poll-time-ms 60000

install: release
	helm version
	cd $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && \
            	helm upgrade ${PREVIEW_NAMESPACE} . \
            		--install \
            		--set global.gateway.domain=${GLOBAL_GATEWAY_DOMAIN} \
            		--namespace ${PREVIEW_NAMESPACE} \
            		--create-namespace \
            		--wait

delete:
	helm delete ${PREVIEW_NAMESPACE} --namespace ${PREVIEW_NAMESPACE} || echo "try to remove helm chart"
	kubectl delete ns ${PREVIEW_NAMESPACE} || echo "try to remove namespace ${PREVIEW_NAMESPACE}"

release:
	echo "RELEASE_VERSION: $(RELEASE_VERSION)"
	git clone -b fix-modeling https://${GITHUB_TOKEN}@github.com/Activiti/activiti-cloud-full-chart.git
	cd $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && \
    helm dep up && \
	  yq write --inplace Chart.yaml 'version' $(VERSION) && \
    env BACKEND_VERSION=$(VERSION) FRONTEND_VERSION=master make update-docker-images

	cd $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && \
    cat Chart.yaml && \
	  cat requirements.yaml && \
	  ls charts -la

docker/%:
	$(eval MODULE=$(word 2, $(subst /, ,$@)))

	mvn verify -B -pl $(MODULE) -am
	@echo "Building docker image for $(MODULE):$(RELEASE_VERSION)..."
	docker build -f $(MODULE)/Dockerfile -q -t docker.io/activiti/$(MODULE):$(RELEASE_VERSION) $(MODULE)
	docker push docker.io/activiti/$(MODULE):$(RELEASE_VERSION)

version:
	mvn versions:set -DprocessAllModules=true -DgenerateBackupPoms=false  -DnewVersion=$(RELEASE_VERSION)

deploy:
	mvn clean deploy -DskipTests

tag:
	git add -u
	git commit -m "Release $(RELEASE_VERSION)" --allow-empty
	git tag -fa v$(RELEASE_VERSION) -m "Release version $(RELEASE_VERSION)" || travis_terminate 1;
	git push -f -q https://${GITHUB_TOKEN}@github.com/${TRAVIS_REPO_SLUG}.git v$(RELEASE_VERSION) || travis_terminate 1;

test/%:
	$(eval MODULE=$(word 2, $(subst /, ,$@)))

	cd activiti-cloud-acceptance-scenarios && \
		mvn -pl '$(MODULE)' -Droot.log.level=off verify

promote: version deploy tag updatebot/push-version
