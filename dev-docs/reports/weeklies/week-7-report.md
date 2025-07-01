# Sprint 7 Report

## Week 7

## Completed Tasks

List of tasks completed during this sprint:

| Task                                            | Weight | Assignee   | Link                                                     |
|-------------------------------------------------|--------|------------|----------------------------------------------------------|
| Fix SLF4J logging issue                         | S      | Yashvi     | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/81) |
| Create HowToDocument                            | M      | Riddhi     | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/83) |
| Create `--help` option that displays all usages | S      | Yashvi     | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/81) |
| Create `--vv` option for advanced logging       | S      | Georgina   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/78) |
| Fix `Null pointer` issue in PipelineRun         | S      | Yashvi     | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/92) |
| Write unite tests for `--check` command         | M      | Georgina   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/50) |

## Carry Over Tasks

| Task                                                  | Weight | Assignee | Link |
|-------------------------------------------------------|--------|----------|------|
| Write Unit Tests for `--dry-run` and `--run` commands | M      | -        | -    |

## New tasks

| Task                                                                         | Weight | Assignee | Link                                                      |
|------------------------------------------------------------------------------|--------|----------|-----------------------------------------------------------|
| Change the logic from MongoDB cloud to MongoDB local                         | M      | Riddhi   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/107) |
| Create MongoDB ER Diagram                                                    | S      | Riddhi   | [PR](https://github.com/CS6510-SEA-SP25/t2-cicd/pull/107) |
| Update the reports format; Default: JSON, for table `--format = table`       | S      | Riddhi   | [PR]()                                                    |
| Include the `--runNumber` for MongoDB filtering in stages and job reports    | S      | Georgina | [PR]()                                                    |
| Implement the `status` command                                               | M      | Yashvi   | [PR]()                                                    |
| Implement C1 & C2: Create hidden directory and more correct pipelines        | S      | Georgina | [PR]()                                                    |
| Implement C3: Support global section in the configuration file               | S      | Georgina | [PR]()                                                    |
| Implement C4: Stages have a default but the configuration file can override  | M      | Georgina | [PR]()                                                    | 

---

### What Worked This Week?

- We had already implemented the asynchronization run logic, hence we didn't have to do much there. 
- This helped us focus on other small issues with our application like `--runNumber` and `mongoDB local`

### What Did Not Work This Week?

- We added the unit test to carry over tasks; that will need to be picked up by a new person next sprint.

### Design Updates

- Added MongoDB pre-req in `HowToDocument.md`
- Create an ER diagram for representing MongoDB collection.