# ast
**Abstract Syntax Tree** for kotlin classes

This program allows you to build *trees of classes* by their inheritance structure inside a project. 
It calculates metrics for them and puts them out in *JSON* file.

### Usage
Just run *main* function in *main.kt* and:
1. Specify the path to *kotlin* project (in the system input stream)
2. Specify the path where metrics *JSON* file will be created (in the system input stream)

### Results
1. **AST** of the project's classes will be printed in the system output stream
2. Calculated *metrics* will be printed in the system output stream
3. Calculated *metrics* will be also written in the specified-path file *metrics.json*

### Metrics
This program calculates the following metrics for *AST* of classes:
* Max classes inheritance depth
* Mean classes inheritance depth
* Mean classes' properties number
* Mean classes' overridden methods number
* ABC metrics
  * Assignments
  * Branches
  * Conditions
