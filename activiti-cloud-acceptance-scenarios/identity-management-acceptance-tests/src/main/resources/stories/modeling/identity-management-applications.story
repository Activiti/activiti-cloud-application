Meta:

Narrative:
As a user
I want to search user and groups

Scenario: retrieve roles for user logged as modeler
Given the user is authenticated as modeler
When the user retrieves his roles
Then roles list contains global role ACTIVITI_MODELER
