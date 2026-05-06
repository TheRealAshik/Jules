# Jules API Reference

KMP SDK for the [Jules REST API](https://jules.google/docs/api/reference/overview/).

## Base URL

```
https://jules.googleapis.com/v1alpha
```

## Authentication

All requests require an API key in the `x-goog-api-key` header.

Get your key at [jules.google.com/settings](https://jules.google.com/settings).

```bash
export JULES_API_KEY="your-api-key-here"
curl -H "x-goog-api-key: $JULES_API_KEY" https://jules.googleapis.com/v1alpha/sessions
```

---

## Endpoints

### Sessions

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/v1alpha/sessions` | Create a session |
| `GET` | `/v1alpha/sessions` | List sessions |
| `GET` | `/v1alpha/sessions/{sessionId}` | Get a session |
| `DELETE` | `/v1alpha/sessions/{sessionId}` | Delete a session |
| `POST` | `/v1alpha/sessions/{sessionId}:sendMessage` | Send a message |
| `POST` | `/v1alpha/sessions/{sessionId}:approvePlan` | Approve a plan |

#### Create a Session — `POST /v1alpha/sessions`

Request body:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `prompt` | string | ✅ | Task description for Jules |
| `title` | string | | Optional title; auto-generated if omitted |
| `sourceContext` | SourceContext | | Repo + branch context; omit for repoless sessions |
| `requirePlanApproval` | boolean | | If true, plans need explicit approval before execution |
| `automationMode` | AutomationMode | | Use `AUTO_CREATE_PR` to auto-create PRs |

```json
{
  "prompt": "Add unit tests for the authentication module",
  "title": "Add auth tests",
  "sourceContext": {
    "source": "sources/github-myorg-myrepo",
    "githubRepoContext": { "startingBranch": "main" }
  },
  "requirePlanApproval": true
}
```

Returns: `Session`

#### List Sessions — `GET /v1alpha/sessions`

| Query param | Type | Default | Description |
|-------------|------|---------|-------------|
| `pageSize` | integer | 30 | Results per page (1–100) |
| `pageToken` | string | | Token from previous response |

Returns: `ListSessionsResponse`

#### Get a Session — `GET /v1alpha/sessions/{sessionId}`

Returns: `Session` (includes `outputs` when completed)

#### Delete a Session — `DELETE /v1alpha/sessions/{sessionId}`

Returns: empty

#### Send a Message — `POST /v1alpha/sessions/{sessionId}:sendMessage`

Request body:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `prompt` | string | ✅ | Message to send to Jules |

Returns: `SendMessageResponse` (empty)

#### Approve a Plan — `POST /v1alpha/sessions/{sessionId}:approvePlan`

Empty request body. Only needed when `requirePlanApproval` was `true`.

Returns: `ApprovePlanResponse` (empty)

#### Session States

| State | Description |
|-------|-------------|
| `STATE_UNSPECIFIED` | Unspecified |
| `QUEUED` | Waiting to be processed |
| `PLANNING` | Creating a plan |
| `AWAITING_PLAN_APPROVAL` | Plan ready, waiting for approval |
| `AWAITING_USER_FEEDBACK` | Needs user input |
| `IN_PROGRESS` | Actively working |
| `PAUSED` | Paused |
| `COMPLETED` | Completed successfully |
| `FAILED` | Failed |

---

### Activities

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/v1alpha/sessions/{sessionId}/activities` | List activities |
| `GET` | `/v1alpha/sessions/{sessionId}/activities/{activityId}` | Get an activity |

#### List Activities — `GET /v1alpha/sessions/{sessionId}/activities`

| Query param | Type | Default | Description |
|-------------|------|---------|-------------|
| `pageSize` | integer | 50 | Results per page (1–100) |
| `pageToken` | string | | Token from previous response |
| `createTime` | string | | Filter activities after this timestamp |

Returns: `ListActivitiesResponse`

#### Get an Activity — `GET /v1alpha/sessions/{sessionId}/activities/{activityId}`

Returns: `Activity`

---

### Sources

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/v1alpha/sources` | List sources |
| `GET` | `/v1alpha/sources/{sourceId}` | Get a source |

Sources are read-only via API; connect repos through the Jules web UI.

#### List Sources — `GET /v1alpha/sources`

| Query param | Type | Default | Description |
|-------------|------|---------|-------------|
| `pageSize` | integer | 30 | Results per page (1–100) |
| `pageToken` | string | | Token from previous response |
| `filter` | string | | AIP-160 filter, e.g. `name=sources/foo OR name=sources/bar` |

Returns: `ListSourcesResponse`

#### Get a Source — `GET /v1alpha/sources/{sourceId}`

Returns: `Source`

---

## Types

### Session

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Full resource name: `sessions/{session}` |
| `id` | string | Session ID |
| `prompt` | string | Task description |
| `title` | string | Session title |
| `state` | SessionState | Current state |
| `url` | string | URL to view in Jules web app |
| `sourceContext` | SourceContext | Repo + branch context |
| `automationMode` | AutomationMode | Automation setting |
| `outputs` | SessionOutput[] | Results (e.g. PRs); populated on completion |
| `createTime` | string (datetime) | Creation timestamp |
| `updateTime` | string (datetime) | Last update timestamp |

### AutomationMode

| Value | Description |
|-------|-------------|
| `AUTOMATION_MODE_UNSPECIFIED` | No automation (default) |
| `AUTO_CREATE_PR` | Auto-create a PR when code changes are ready |

### Activity

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Full resource name: `sessions/{session}/activities/{activity}` |
| `id` | string | Activity ID |
| `originator` | string | `user`, `agent`, or `system` |
| `description` | string | Human-readable description |
| `createTime` | string (datetime) | Creation timestamp |
| `artifacts` | Artifact[] | Outputs produced |
| `planGenerated` | PlanGenerated | *(one of)* Plan was generated |
| `planApproved` | PlanApproved | *(one of)* Plan was approved |
| `userMessaged` | UserMessaged | *(one of)* User posted a message |
| `agentMessaged` | AgentMessaged | *(one of)* Jules posted a message |
| `progressUpdated` | ProgressUpdated | *(one of)* Progress update |
| `sessionCompleted` | SessionCompleted | *(one of)* Session completed |
| `sessionFailed` | SessionFailed | *(one of)* Session failed |

Exactly one event field is populated per activity.

### Source

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Full resource name: `sources/{source}` |
| `id` | string | Source ID |
| `githubRepo` | GitHubRepo | GitHub repository details |

### Plan

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Plan ID |
| `steps` | PlanStep[] | Ordered steps |
| `createTime` | string (datetime) | Creation timestamp |

### PlanStep

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Step ID |
| `index` | integer | 0-based index |
| `title` | string | Step title |
| `description` | string | Step details |

### Artifact

Exactly one field is populated:

| Field | Type | Description |
|-------|------|-------------|
| `changeSet` | ChangeSet | Code changes |
| `bashOutput` | BashOutput | Command output |
| `media` | Media | Image/video output |

### ChangeSet

| Field | Type | Description |
|-------|------|-------------|
| `source` | string | Source resource name |
| `gitPatch` | GitPatch | Patch in Git format |

### GitPatch

| Field | Type | Description |
|-------|------|-------------|
| `baseCommitId` | string | Commit the patch applies to |
| `unidiffPatch` | string | Unified diff |
| `suggestedCommitMessage` | string | Suggested commit message |

### BashOutput

| Field | Type | Description |
|-------|------|-------------|
| `command` | string | Command that was run |
| `output` | string | Combined stdout + stderr |
| `exitCode` | integer | Exit code |

### Media

| Field | Type | Description |
|-------|------|-------------|
| `mimeType` | string | e.g. `image/png` |
| `data` | string (base64) | Encoded media data |

### GitHubRepo

| Field | Type | Description |
|-------|------|-------------|
| `owner` | string | Org or user name |
| `repo` | string | Repository name |
| `isPrivate` | boolean | Whether repo is private |
| `defaultBranch` | GitHubBranch | Default branch |
| `branches` | GitHubBranch[] | All active branches |

### GitHubBranch

| Field | Type | Description |
|-------|------|-------------|
| `displayName` | string | Branch name |

### GitHubRepoContext

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `startingBranch` | string | ✅ | Branch to start the session from |

### SourceContext

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `source` | string | ✅ | Source resource name: `sources/{source}` |
| `githubRepoContext` | GitHubRepoContext | | GitHub-specific context |

### SessionOutput

| Field | Type | Description |
|-------|------|-------------|
| `pullRequest` | PullRequest | PR created by the session |

### PullRequest

| Field | Type | Description |
|-------|------|-------------|
| `url` | string | PR URL |
| `title` | string | PR title |
| `description` | string | PR description |

---

## Activity Event Types

### PlanGenerated
```json
{ "planGenerated": { "plan": { "id": "plan1", "steps": [...], "createTime": "..." } } }
```

### PlanApproved
```json
{ "planApproved": { "planId": "plan1" } }
```

### UserMessaged
```json
{ "userMessaged": { "userMessage": "Please also add integration tests" } }
```

### AgentMessaged
```json
{ "agentMessaged": { "agentMessage": "I've completed the unit tests." } }
```

### ProgressUpdated
```json
{ "progressUpdated": { "title": "Writing tests", "description": "Creating test cases..." } }
```

### SessionCompleted
```json
{ "sessionCompleted": {} }
```

### SessionFailed
```json
{ "sessionFailed": { "reason": "Unable to install dependencies" } }
```

---

## Common Patterns

### Pagination

```bash
# First page
curl -H "x-goog-api-key: $JULES_API_KEY" \
  "https://jules.googleapis.com/v1alpha/sessions?pageSize=10"

# Next page (use nextPageToken from previous response)
curl -H "x-goog-api-key: $JULES_API_KEY" \
  "https://jules.googleapis.com/v1alpha/sessions?pageSize=10&pageToken=TOKEN"
```

### Resource Names

Resources use the format `{collection}/{id}`:
- Sessions: `sessions/1234567`
- Activities: `sessions/1234567/activities/act1`
- Sources: `sources/github-myorg-myrepo`

### Error Handling

Standard HTTP status codes. Common errors:

| Status | Meaning |
|--------|---------|
| `400` | Bad request / invalid parameters |
| `401` | API key not valid |
| `403` | Permission denied |
| `404` | Resource not found |
| `429` | Quota exceeded / rate limited |

---

## Workflow Example

```bash
# 1. Find your connected repo
curl -H "x-goog-api-key: $JULES_API_KEY" \
  "https://jules.googleapis.com/v1alpha/sources"

# 2. Create a session
curl -X POST \
  -H "x-goog-api-key: $JULES_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Add unit tests for the auth module",
    "sourceContext": {
      "source": "sources/github-myorg-myrepo",
      "githubRepoContext": { "startingBranch": "main" }
    },
    "requirePlanApproval": true,
    "automationMode": "AUTO_CREATE_PR"
  }' \
  https://jules.googleapis.com/v1alpha/sessions

# 3. Poll for activities (plan will appear here)
curl -H "x-goog-api-key: $JULES_API_KEY" \
  "https://jules.googleapis.com/v1alpha/sessions/SESSION_ID/activities"

# 4. Approve the plan
curl -X POST \
  -H "x-goog-api-key: $JULES_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{}' \
  https://jules.googleapis.com/v1alpha/sessions/SESSION_ID:approvePlan

# 5. Poll session until state = COMPLETED
curl -H "x-goog-api-key: $JULES_API_KEY" \
  https://jules.googleapis.com/v1alpha/sessions/SESSION_ID
```
