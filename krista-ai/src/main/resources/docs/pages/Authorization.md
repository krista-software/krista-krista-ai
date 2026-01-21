# Authorization Requirements

## Overview

The Krista AI Extension requires workspace admin privileges for all operations. This page explains the authorization requirements and how to verify your access level.

---

## Access Requirements

| Requirement | Description |
|-------------|-------------|
| **Role** | Workspace Administrator |
| **Scope** | All catalog requests and entity operations |
| **Verification** | Automatic check on each request |

---

## Why Workspace Admin Access?

The Krista AI Extension manages critical AI model operations including:

- **Model Version Management** - Changing live versions affects production predictions
- **Dataset Upload** - Training data directly impacts model accuracy
- **Retraining Jobs** - Resource-intensive operations that affect system performance

These operations require elevated privileges to prevent unauthorized modifications to AI models.

---

## Error Messages

### Authorization Errors

If you don't have the required access, you'll see the following error:

| Error Message | Cause | Resolution |
|---------------|-------|------------|
| "Access denied. You must be a workspace admin to access AI models. Please contact your administrator if you need access." | User lacks workspace admin role | Contact your workspace administrator to request access |

### Model Access Errors

| Error Message | Cause | Resolution |
|---------------|-------|------------|
| "The specified model does not exist. Please verify the Model ID using entity selection or check that the model has not been deleted." | Invalid model ID or model was deleted | Use entity selection to get a valid Model ID |
| "Unable to fetch model information. Please try again later." | Temporary service unavailability | Wait a few moments and retry the operation |

### Read-Only Entity Errors

The Model entity is read-only. Attempting to modify models directly will result in these errors:

| Error Message | Cause | Resolution |
|---------------|-------|------------|
| "Models are read-only and cannot be created through this extension. Models are managed by the Krista AI service." | Attempted to create a model via the store | Models are created automatically by the Krista AI service |
| "Models are read-only and cannot be updated through this extension. Use the retraining workflow to create new model versions." | Attempted to update a model via the store | Use the retraining workflow to create new versions |
| "Models are read-only and cannot be deleted through this extension. Model lifecycle is managed by the Krista AI service." | Attempted to delete a model via the store | Model deletion is managed by the Krista AI service |

---

## Verifying Your Access

To verify you have workspace admin access:

1. Navigate to your workspace settings
2. Check your assigned roles
3. Confirm "Workspace Administrator" is listed

---

## Limitations

- **System Triggered Conversations**: Catalog requests cannot be used with System Triggered Conversations
- **Cross-Workspace Access**: Access is limited to models within your workspace

---

## See Also

- [Upload Data for Retraining](pages/UploadDataForRetraining.md)
- [Get Model Versions](pages/GetModelVersions.md)
- [Change Live Version](pages/ChangeLiveVersion.md)
- [Retrain Model](pages/RetrainModel.md)
- [Get Retraining Status](pages/GetRetrainingStatus.md)

