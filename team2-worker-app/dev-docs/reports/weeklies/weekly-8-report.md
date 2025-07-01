# Sprint 8 Report

## Week 8

## Completed Tasks

List of tasks completed during this sprint:

| Task                                                                        | Weight | Assignee | Link                                                     |
|-----------------------------------------------------------------------------|--------|----------|----------------------------------------------------------|
| Change the logic from MongoDB cloud to MongoDB local                        | M      | Riddhi   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/107) |
| Create MongoDB ER Diagram                                                   | S      | Riddhi   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/107) |
| Update the reports format; Default: JSON, for table `--format = table`      | S      | Riddhi   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/112) |
| Include the `--runNumber` for MongoDB filtering in stages and job reports   | S      | Riddhi   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/111) |
| Implement the `status` command                                              | M      | Yashvi   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/116) |   
| Set Up RabbitMQ                                                             | M      | Yashvi   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/116) |
| C3 support global section in the configuration file             | S      | Georgina | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/110)             |     
| C3.2 Enforce unique pipeline names              | S      | Georgina | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/114)                             |     
| C4: Stages have a default but the configuration file can override | M      | Georgina | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/115) |
## Carry Over Tasks

| Task                                                  | Weight | Assignee | Link |
|-------------------------------------------------------|--------|----------|------|
| Write Unit Tests for `--dry-run` and `--run` commands | M      | -        | -    |
| C2 Create more correct pipeline for .pipelines folder | S      | Georgina | -    |

## New tasks

| Task                                                              | Weight | Assignee | Link                                                      |
|-------------------------------------------------------------------|--------|----------|-----------------------------------------------------------|
| Store artifacts in DB using MinIO                                 | M      | Riddhi   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/126) |
| Restructuring of entire codebase into different components        | M      | Yashvi   | [PR]() |
| Update documentation for Asynchronous Run set up                  | S      | Yashvi   | [PR]() |
| Update documentation for MinIO set up                             | S      | Riddhi   | [PR]() |
| Add `repo` command - Pull and run pipelines from public git repos | M      | Georgina | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/129)                                                    |


---

### What Worked This Week?

- Enhanced reporting functionality with format options (JSON default with table alternative using `--format = table`), giving users more flexibility
- Successfully implemented the `status` command, providing users with real-time visibility into pipeline execution state
- Set up RabbitMQ infrastructure for asynchronous message processing, improving system scalability and responsiveness
- Added support for global section in configuration files, enhancing the flexibility of pipeline definitions
- Successfully integrated MinIO object storage for pipeline artifacts, providing a scalable and efficient solution for storing and retrieving build outputs

### What Did Not Work This Week?

- We added the unit test to carry over tasks; that will need to be picked up by a new person next sprint. 

### Design Updates

- Updated documentation to reflect the new asynchronous execution model using RabbitMQ
- Redesigned the configuration file structure to support both global settings and pipeline-specific overrides
- Established a consistent format for artifact storage in the database with appropriate metadata
