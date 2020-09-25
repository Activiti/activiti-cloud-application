Meta:

Narrative:
As a user
I want to perform operations on process instance having service tasks

Scenario: audit service tasks integration context events for process instance 
Given the user is authenticated as testadmin
When the user starts a process with service tasks called CONNECTOR_PROCESS_INSTANCE
Then integration context events are emitted for the process
And the process with service tasks completed

Scenario: get service tasks for process instance 
Given the user is authenticated as testadmin
When the user starts a process with service tasks called CONNECTOR_PROCESS_INSTANCE
Then the user can get list of of all service tasks for process instance
And the process with service tasks completed

Scenario: get service task by id 
Given the user is authenticated as testadmin
When the user starts a process with service tasks called CONNECTOR_PROCESS_INSTANCE
Then the user can get service task by id
And the process with service tasks completed

Scenario: get service task integration context by service task id
Given the user is authenticated as testadmin
When the user starts a process with service tasks called CONNECTOR_PROCESS_INSTANCE
Then the user can get service task integration context by service task id
And the process with service tasks completed

Scenario: get all service tasks by status 
Given the user is authenticated as testadmin
When the user starts a process with service tasks called CONNECTOR_PROCESS_INSTANCE
Then the user can get list of of all service tasks with status of COMPLETED
And the process with service tasks completed
