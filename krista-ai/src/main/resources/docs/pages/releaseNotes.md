### Release Notes - Krista AI Extension

## Version 1.0.2

- **Developer Name** : Vrushali Gaikwad
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2025.11.5

## Enhancement Highlights

#### Key Highlights of Version 1.0.2

* **Documentation Enhancement** - Comprehensive documentation improvements with detailed examples and diagrams
  - Added detailed README with problem statement and use cases
  - Fixed image paths in documentation (updated to use `./pages/_media/` format)
  - Added visual workflow diagrams for all catalog requests
  - Enhanced inline code documentation and Javadoc

* **Build Gradle Improvements** - Enhanced build configuration and testing infrastructure
  - Added JaCoCo code coverage reporting (94% coverage achieved)
  - Configured test coverage verification with 80% minimum threshold
  - Added code lines statistics plugin for metrics tracking
  - Improved test logging with detailed output
  - Added Java 21 compatibility with Mockito 5.11.0
  - Enhanced release properties generation task

* **Logger Changes** - Comprehensive logging implementation across all components
  - Added SLF4J logging to all catalog area classes (DatasetsArea, ModelsArea, TrainingArea)
  - Implemented structured logging in ModelStore with debug and error levels
  - Added log4j2.xml configuration for consistent log formatting
  - Enhanced error logging with contextual information (model IDs, job IDs, operation details)
  - Added INFO level logging for successful operations
  - Added DEBUG level logging for search and authorization operations

* **Test Coverage** - Comprehensive unit test suite added
  - 45 unit tests with 94% code coverage
  - Tests for all catalog request methods (success and error scenarios)
  - ModelStore tests covering get, search, contains, and authorization
  - Entity tests for Model factory methods
  - Proper mocking with Mockito for all dependencies

## Resolved Bugs

* Fixed image path references in documentation (changed from `./_media/` to `./pages/_media/`)
* Fixed Gradle build configuration for area file paths
* Fixed enum mocking in tests for AiModelStatus

## Known Issues

NA

---

## Version 1.0.1

- **Developer Name** : Vaibhav Choudhary
- **Krista Service APIs Java** : 1.0.97

## Enhancement Highlights

#### Key Highlights of Version 1.0.0

* **Upload Dataset** - Stage new data for retraining of a specific model
* **Get Model Versions** - Get all the versions of a specific model with performance metrics
* **Change Live Version** - Take any available version of a model live
* **Retrain Model** - Start a retraining job for a specific model
* **Get Retraining Status** - Get the status of a retraining job

## Note:
* Users must have workspace admin privileges to use catalog requests and entities
* These requests or entities cannot be used with the System Triggered Conversations
* At present, only the retraining and versioning flow is allowed to be controlled through this extension

## Resolved Bugs
NA

## Known Issues
NA