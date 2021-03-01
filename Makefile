RELEASE_VERSION := $(or $(shell cat VERSION), $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout))
ifeq ($(FRONT_RELEASE_VERSION),)
FRONT_RELEASE_VERSION := master
endif
ACTIVITI_CLOUD_FULL_CHART_CHECKOUT_DIR := .git/activiti-cloud-full-chart
ACTIVITI_CLOUD_FULL_EXAMPLE_DIR := $(ACTIVITI_CLOUD_FULL_CHART_CHECKOUT_DIR)/charts/activiti-cloud-full-example
ACTIVITI_CLOUD_FULL_CHART_BRANCH := dependency-activiti-cloud-application-$(RELEASE_VERSION)

updatebot/push-version:
	updatebot push-version --kind maven \
		org.activiti.cloud:activiti-cloud-dependencies ${RELEASE_VERSION} \
		--merge false

install: release
	echo helm $(helm version --short)
	cd $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && \
		helm dep up && \
		helm upgrade ${PREVIEW_NAMESPACE} . \
			--install \
			--set global.gateway.http=false \
			--set global.gateway.domain=${GLOBAL_GATEWAY_DOMAIN} \
			--namespace ${PREVIEW_NAMESPACE} \
			--create-namespace \
			--wait

delete:
	helm uninstall ${PREVIEW_NAMESPACE} --namespace ${PREVIEW_NAMESPACE} || echo "try to remove helm chart"
	kubectl delete ns ${PREVIEW_NAMESPACE} || echo "try to remove namespace ${PREVIEW_NAMESPACE}"

clone-chart:
	rm -rf $(ACTIVITI_CLOUD_FULL_CHART_CHECKOUT_DIR) && \
		git clone https://${GITHUB_TOKEN}@github.com/Activiti/activiti-cloud-full-chart.git $(ACTIVITI_CLOUD_FULL_CHART_CHECKOUT_DIR) --depth 1

create-pr: update-chart
	cd $(ACTIVITI_CLOUD_FULL_CHART_CHECKOUT_DIR) && \
		(git push -q origin :$(ACTIVITI_CLOUD_FULL_CHART_BRANCH) || true) && \
	  git checkout -q -b $(ACTIVITI_CLOUD_FULL_CHART_BRANCH) && \
		helm-docs && \
		git diff && \
		git commit -am "Update 'activiti-cloud-application' dependency to $(RELEASE_VERSION)" && \
		git push -qu origin $(ACTIVITI_CLOUD_FULL_CHART_BRANCH) && \
		gh pr create --fill --head $(ACTIVITI_CLOUD_FULL_CHART_BRANCH) --label updatebot ${GH_PR_CREATE_OPTS}

update-chart: clone-chart
	cd $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && \
		yq write --inplace Chart.yaml 'version' $(RELEASE_VERSION) && \
		env BACKEND_VERSION=$(RELEASE_VERSION) FRONTEND_VERSION=$(FRONT_RELEASE_VERSION) make update-docker-images

release: update-chart
	echo "RELEASE_VERSION: $(RELEASE_VERSION)"
	cd $(ACTIVITI_CLOUD_FULL_EXAMPLE_DIR) && \
    helm dep up && \
    helm lint && \
    cat Chart.yaml && \
	  cat values.yaml && \
	  ls charts -la

mvn/%:
	$(eval MODULE=$(word 1, $(subst mvn/, ,$@)))

	mvn -s settings.xml verify -B -pl $(MODULE) -am

docker/%:
	$(eval MODULE=$(word 1, $(subst docker/, ,$@)))

	make mvn/$(MODULE)
	@echo "Building docker image for $(MODULE):$(RELEASE_VERSION)..."
	docker build -f $(MODULE)/Dockerfile -q -t docker.io/activiti/$(MODULE):$(RELEASE_VERSION) $(MODULE)
	docker push docker.io/activiti/$(MODULE):$(RELEASE_VERSION)

docker-delete/%:
	$(eval MODULE=$(word 2, $(subst /, ,$@)))

	@echo "Delete image from Docker Hub for $(MODULE):$(RELEASE_VERSION)..."
	curl --silent --show-error --fail -X DELETE -u "${DOCKERHUB_USERNAME}:${DOCKERHUB_PASSWORD}" \
		https://hub.docker.com/v2/repositories/activiti/$(MODULE)/tags/$(RELEASE_VERSION)

docker-delete-all: docker-delete/example-runtime-bundle docker-delete/activiti-cloud-query \
	docker-delete/example-cloud-connector docker-delete/activiti-cloud-modeling

version:
	mvn versions:set -DprocessAllModules=true -DgenerateBackupPoms=false -DnewVersion=$(RELEASE_VERSION)

deploy:
	mvn deploy -DskipTests

tag:
	git add -u
	git commit -m "Release $(RELEASE_VERSION)" --allow-empty
	git tag -fa v$(RELEASE_VERSION) -m "Release version $(RELEASE_VERSION)"
	git push -f -q origin v$(RELEASE_VERSION)

test/%:
	$(eval MODULE=$(word 2, $(subst /, ,$@)))

	cd activiti-cloud-acceptance-scenarios && \
		mvn -pl '$(MODULE)' -Droot.log.level=off verify

promote: version deploy tag updatebot/push-version create-pr
