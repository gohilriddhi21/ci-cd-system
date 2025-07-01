# Sprint 6 Report

## Week 6

## Completed Tasks

List of tasks completed during this sprint:

| Task                                                            | Weight | Assignee | Link                                                      |
|-----------------------------------------------------------------|--------|----------|-----------------------------------------------------------|
| Add options for report command, stage summary and job summary   | L      | Riddhi   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/65)  |
| Dockerization: Executes pipeline jobs inside Docker containers  | S      | Yashvi   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/63)  |

## Carry Over Tasks

| Task                                                  | Weight | Assignee | Link                                                     |
|-------------------------------------------------------|--------|----------|----------------------------------------------------------|
| Create test cases for `-c` and `-f` commands          | M      | Georgina | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/28) |
| Write Unit Tests for `--dry-run` and `--run` commands | M      | Georgina | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/51) |
| Create Docker Container for MongoDB Setup             | M      | Riddhi   | -                                                        |
| Fix SLF4J logging issue                               | S      | Yashvi   | -                                                        |


## New tasks

| Task                                             | Weight | Assignee | Link |
|--------------------------------------------------|--------|----------|------|
| Create HowToDocument                             | M      | Riddhi   | -    |
| Create `--help` option that displays all usages  | S      | Yashvi   | -    |
| Create `--vv` option for advanced logging        | S      | Georgina | -    |

---

### What Worked This Week?

- **Implementation of DockerContainerExecutor** which allows us to execute pipeline jobs inside Docker containers.
- **Job and Stage Summary:** We added options for the report command: stage summary and job summary.
- **Reports Improvements:** We updated the output of the report command to be more readable.

### What Did Not Work This Week?

- We added the unit test to carry over tasks; that will need to be picked up by a new person next sprint.

### Design Updates

- Added the `Asciitable` Java library to display stage and job reports in a presentable format.