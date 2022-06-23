Meta:

Narrative:
As a user
I want to search user and groups

Scenario: retrieve roles for user logged as modeler
Given the user is authenticated as modeler
When the user retrieves his roles
Then roles list contains global role ACTIVITI_MODELER

Scenario: retrieve roles for user logged as test user
Given the user is authenticated as testuser
When the user retrieves his roles
Then roles list contains global role ACTIVITI_USER
And roles list for simpleapp contains role ACTIVITI_USER

Scenario: search groups by name
Given the user is authenticated as testuser
When the user searches  for groups containing sa
Then group search contains sales
And group search contains processanalytics
And group search contains processadmin
And group search does not contain hr
And group search does not contain testgroup

Scenario: search users by name
Given the user is authenticated as testuser
When the user searches for users containing user
Then user search contains hruser
And user search contains testuser
And user search does not contain testActivitiAdmin
