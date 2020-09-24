Meta:

Narrative:
As a user
I want to perform operations on process instance having service tasks

Scenario: get a list of all service tasks for process instance 
Given the user is authenticated as testadmin
When the user starts a process with service tasks called CONNECTOR_PROCESS_INSTANCE
Then integration context events are emitted for the process
And the user can query list of of all service task for process instance
And the process with service tasks completed
