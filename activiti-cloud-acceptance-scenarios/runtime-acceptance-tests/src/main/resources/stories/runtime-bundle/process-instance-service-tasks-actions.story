Meta:

Narrative:
As a user
I want to perform operations on process instance having service tasks

Scenario: audit service tasks integration context events for process instance 
Given the user is authenticated as testadmin
When the user starts a process with service tasks called CONNECTOR_PROCESS_INSTANCE
Then integration context events are emitted for the process
And the process with service tasks completed
