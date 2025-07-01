# Sprint 5 Report

## Week 5

## Completed Tasks

List of tasks completed during this sprint:

| Task                                                          | Weight | Assignee | Link                                                          |
|---------------------------------------------------------------|--------|----------|---------------------------------------------------------------|
| Run command needs to use provided docker image to run scripts | M      | Yashvi   | [Issue](https://github.com/CS6510-SEA-SP25/t2-cicd/issues/55) |
| Make the output of the reports command prettier               | S      | Georgina | [Issue](https://github.com/CS6510-SEA-SP25/t2-cicd/issues/52) |

## Carry Over Tasks

| Task                                         | Weight | Assignee         | Link                                                     |
|----------------------------------------------|--------|------------------|----------------------------------------------------------|
| Create test cases for `-c` and `-f` commands | M      | Abhilash         | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/28)
| Write Unit Tests for --dry-run and run commands | M      | Abhilash         | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/51)
| Add options for report command, stage summary and job summary | L      | Abhilash, Riddhi | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/65)

## New tasks

| Task                            | Weight | Assignee | Link                                                          |
|---------------------------------|--------|----------|---------------------------------------------------------------|
| Fix SLF4J logging issue | S      | Riddhi   | [Issue](https://github.com/CS6510-SEA-SP25/t2-cicd/issues/53) |
---

### What Worked This Week?

- **Implementation of DockerContainerExecutor** which allows us to execute pipeline jobs inside Docker containers.
- **Job and Stage Summary:** We added options for the report command: stage summary and job summary.
- **Reports Improvements:** We updated the output of the report command to be more readable.

### What Did Not Work This Week?

- We have some carry over test tasks that will need to be picked up by a new person next sprint.


### Design Updates

- Added Docker Java library which enhances our ability to interact with Docker containers programmatically, providing greater flexibility and control
  over our containerization and deployment processes.  