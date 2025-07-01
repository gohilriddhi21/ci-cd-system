## 🗃️ Data Store Design 
### MongoDB

We have used NoSQL DB beacuse we wanted a support for Dynamic Schema. 

### 📑 Collection: `pipelineRuns`

Stores metadata and execution details for each pipeline run, including stages and jobs.

---

### 📐 Schema Design

Each document in the `pipelineRuns` collection follows this structure:

```json
```json
{
  "pipelineName": "string",        // Name of the pipeline
  "runNumber": int,                // Run sequence number
  "repo": "string",                // Repository URL, local if not provided
  "fileName": "string",            // Name of the pipeline configuration file
  "branch": "string",              // Branch name where the pipeline ran
  "commit": "string",              // Commit hash for the pipeline execution
  "startTime": long,               // Pipeline start time (epoch)
  "completionTime": long,          // Pipeline completion time (epoch)
  "pipelineStatus": "string",      // Final pipeline status (e.g., SUCCESS, FAILED, CANCELLED)
  "isLocal": boolean,              // Whether the run was triggered locally
  "stages": [                      // Array of stages
    {
      "stageName": "string",
      "stageStatus": "string",     // Status of the stage (e.g., SUCCESS, FAILED, CANCELLED)
      "startTime": long,
      "completionTime": long,
      "jobs": [                    // Array of jobs in the stage
        {
          "jobName": "string",
          "jobStatus": "string",   // Status of the job
          "allowFailure": boolean,
          "startTime": long,
          "completionTime": long
        }
      ]
    }
  ]
}
```
