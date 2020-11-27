CURRENT=$(shell pwd)
NAME := $(or $(APP_NAME),$(shell basename $(CURRENT)))
OS := $(shell uname)

$(eval HELM_ACTIVITI_VERSION = $(or $(HELM_ACTIVITI_VERSION),$(shell cat VERSION |rev|sed 's/\./-/'|rev)))

RELEASE_VERSION := $(or $(shell cat VERSION), $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout))
GROUP_ID := $(shell mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout)
ARTIFACT_ID := $(shell mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)
RELEASE_ARTIFACT := $(GROUP_ID):$(ARTIFACT_ID)
ACTIVITI_CLOUD_FULL_EXAMPLE_DIR := .updatebot-repos/github/activiti/activiti-cloud-full-chart/charts/activiti-cloud-full-example

ACTIVITI_CLOUD_FULL_CHART_VERSIONS := runtime-bundle $(VERSION) \
									  activiti-cloud-connector $(VERSION) \
    								  activiti-cloud-query $(VERSION)  \
    								  activiti-cloud-modeling $(VERSION)
    
charts := "activiti-cloud-query/charts/activiti-cloud-query" \
	      "example-runtime-bundle/charts/runtime-bundle" \
	      "example-cloud-connector/charts/activiti-cloud-connector" \
	      "activiti-cloud-modeling/charts/activiti-cloud-modeling"

updatebot/push:
	@echo doing updatebot push $(RELEASE_VERSION)
	updatebot push --ref $(RELEASE_VERSION)

updatebot/push-version:
	@echo Resolving push versions for artifacts........
	$(eval ACTIVITI_CLOUD_VERSION=$(shell mvn help:evaluate -Dexpression=activiti-cloud-mono-aggregator.version -q -DforceStdout))
	@echo Doing updatebot push-version.....
	@echo updatebot push-version --dry --kind maven \
		org.activiti.cloud.modeling:activiti-cloud-modeling-dependencies $(RELEASE_VERSION) \
		org.activiti.cloud.audit:activiti-cloud-audit-dependencies $(RELEASE_VERSION) \
		org.activiti.cloud.api:activiti-cloud-api-dependencies $(RELEASE_VERSION) \
		org.activiti.cloud.build:activiti-cloud-parent $(RELEASE_VERSION) \
		org.activiti.cloud.build:activiti-cloud-dependencies-parent $(RELEASE_VERSION)\
		org.activiti.cloud.connector:activiti-cloud-connectors-dependencies $(RELEASE_VERSION) \
		org.activiti.cloud.messages:activiti-cloud-messages-dependencies $(RELEASE_VERSION) \
		org.activiti.cloud.modeling:activiti-cloud-modeling-dependencies $(RELEASE_VERSION) \
		org.activiti.cloud.notifications.graphql:activiti-cloud-notifications-graphql-dependencies $(RELEASE_VERSION) \
		org.activiti.cloud.query:activiti-cloud-query-dependencies $(RELEASE_VERSION) \
		org.activiti.cloud.rb:activiti-cloud-runtime-bundle-dependencies $(RELEASE_VERSION) \
		org.activiti.cloud.common:activiti-cloud-service-common-dependencies $(RELEASE_VERSION)

updatebot/update:
	@echo doing updatebot update $(RELEASE_VERSION)
	updatebot update

updatebot/update-loop:
	@echo doing updatebot update-loop $(RELEASE_VERSION)
	updatebot update-loop --poll-time-ms 60000

install: release
	helm version
	cd  $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && \
            	helm upgrade ${PREVIEW_NAMESPACE} . \
            		--install \
            		--set global.gateway.domain=${GLOBAL_GATEWAY_DOMAIN} \
            		--namespace ${PREVIEW_NAMESPACE} \
            		--create-namespace \
            		--wait

delete:
	helm delete ${PREVIEW_NAMESPACE} --namespace  ${PREVIEW_NAMESPACE} || echo "try to remove helm chart"
	kubectl delete ns ${PREVIEW_NAMESPACE} || echo "try to remove namespace ${PREVIEW_NAMESPACE}"

release: 
	echo "RELEASE_VERSION: $(RELEASE_VERSION)"
	updatebot --dry push-version --kind helm activiti-cloud-dependencies $(RELEASE_VERSION)
	cd $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && helm dep up
	updatebot --dry push-version --kind helm $(ACTIVITI_CLOUD_FULL_CHART_VERSIONS)

	sed -i -e "s/version:.*/version: $(VERSION)/" $(ACTIVITI_CLOUD_FULL_CHART_VERSIONS)/Chart.yaml

	@for chart in $(charts) ; do \
		cd $$chart ; \
		make version; \
		make build; \
		make release; \
		rm $(CURRENT)/$(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR)/charts/$$(basename `pwd`)*.tgz; \
		cp $$(basename `pwd`)*.tgz $(CURRENT)/$(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR)/charts/; \
		cd - ; \
	done
	
	cat $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR)/Chart.yaml -la
	cat $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR)/requirements.yaml -la
	ls $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR)/charts -la
	
	cd  $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && \
		rm -rf requirements.lock && \
		rm -rf *.tgz && \
		helm lint && \
		helm package .
	

publish:
	@for chart in $(charts) ; do \
		cd $$chart ; \
		make version; \
		make build; \
		make release; \
		make github; \
		cd - ; \
	done
	
update-common-helm-chart-version:
	@for chart in $(charts) ; do \
		cd $$chart ; \
		make common-helm-chart-version; \
		cd -; \
	done

docker/%:
	set -e
	mvn verify -B -pl $@ -am
	@echo "Building docker image for $@..."
	docker build -f $@/Dockerfile -q -t docker.io/activiti/$@:$(cat VERSION) $@
	docker push docker.io/activiti/$@:$(cat VERSION)
