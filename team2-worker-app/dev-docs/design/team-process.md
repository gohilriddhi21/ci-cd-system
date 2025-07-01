
# Team 2 CI/CD System Documentation

## Repository Structure

### Repository Organization
We maintain a **mono-repository** structure for our CI/CD system implementation. This design decision allows us to:

- Keep all related services (CLI, server, worker) in a single place.
- Simplify dependency and version management.
- Enable atomic commits across components.
- Streamline code review and team collaboration.

### Submodule Breakdown

Each subdirectory in the monorepo represents a key service of the system:

```
root/
├── .github/workflows/         # GitHub Actions for CI/CD automation
├── .pipelines/                # Reusable pipeline definitions (YAML)
├── dependencies/              # Shared dependency resources
├── dev-docs/                  # Internal developer documentation
├── team2-cli-app/             # CLI to interact with pipelines and services
├── team2-server-app/          # Main application server exposing REST/SSE APIs
├── team2-worker-app/          # Worker for executing jobs from the queue
├── docker-compose.yml         # Orchestrates local multi-service environment
└── README.md                  # Project overview and usage instructions
```

---

## Development Guidelines

### Code Style & Format
- Java 17 standard applied across all submodules
- Style enforced via `checkstyle.xml` in each service
- All contributors use pre-configured IntelliJ settings

### Testing Strategy
- **JUnit 5** for all unit and integration tests
- **JaCoCo** for code coverage reporting
- Coverage threshold: **70% minimum** per module
- Tests must be structured as:
  - Unit tests for isolated logic
  - Integration tests for service-level functionality
  - End-to-end for user-level CLI or API validation

### Commit & Branching Strategy

| Type       | Branch Format          | Description                            |
|------------|------------------------|----------------------------------------|
| Main       | `main`                 | Always production ready                |
| Feature    | `feature/<desc>`       | New enhancements                       |
| Bugfix     | `fix/<desc>`           | Issue patches                          |
| Release    | `release/vX.Y.Z`       | Production deployments                 |

- Use clear, concise commit messages
- Each commit should encapsulate a single logical change

---

## CI/CD Pipeline (GitHub Actions)

### Continuous Integration
- Triggered on PR and push to `main`
- Performs:
  - Build via Maven
  - Test execution with JUnit
  - Code coverage validation via JaCoCo
  - Static analysis via Checkstyle & SpotBugs
  - Docker image build for CLI/Server/Worker

### Continuous Deployment
- Deploys tagged releases
- Pushes Docker images to GitHub Container Registry
- Applies `docker-compose` updates in environments (dev/prod)

### Quality Gates
- All of the following **must pass**:
  - Build and test
  - Static analysis (no critical errors)
  - >70% test coverage
  - No high CVEs in `dependency-check`

---

## Tooling Summary

- **Maven**: Project builds and dependency resolution
- **Spring Boot**: Server and Worker backends
- **Apache Commons CLI**: CLI parsing in the terminal tool
- **Docker & Compose**: Containerization and orchestration
- **MongoDB**: Document store for pipeline/job metadata
- **RabbitMQ**: Queue system between server and worker
- **Checkstyle / SpotBugs / PMD**: Static code analysis
- **JaCoCo**: Code coverage analysis
- **JUnit 5**: Unit & integration testing
- **MinIO SDK**: Object storage client for job artifacts

---

## Documentation Standards

### In-Code Documentation
- Every public class/method should have JavaDoc
- Complex logic must have inline comments

### Repo Documentation
- Each submodule has its own `README.md` with:
  - Setup instructions
  - Usage guidelines
  - Test execution
  - Deployment commands

---

## Team Practices

### Meetings
- Weekly Sprint Planning
- Biweekly Retrospectives
- Ad-hoc tech syncs as needed

### Communication Channels
- **GitHub Issues**: Task and bug tracking
- **Pull Requests**: Code reviews and discussion
- **Team Chat**: Coordination and updates

---
