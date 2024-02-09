# Project SophiaTech Eats | 2023-2024
<hr>

## Table of Contents
1. [Team B](#-team-b)
2. [Project Description](#-project-description)
3. [Project Management](#-project-management)
   1. [GitHub Project Board](#github-project-board)
   2. [Our naming convention for our branch](#our-naming-convention-for-our-branch)
   3. [Our naming convention for our issue](#our-naming-convention-for-our-issue)
   4. [Our naming convention for our commit](#our-naming-convention-for-our-commit)
4. [Project Structure](#-project-structure)
5. [How to run the project](#-how-to-run-the-project)


### 👨‍👩‍👧‍👦 Team B

| Name           | Github                                            | Role             |
|----------------|---------------------------------------------------|------------------|
| DELILLE Axel   | [Tsukoyachi](https://github.com/Tsukoyachi)       | System Architect |
| BOULTON Nina   | [ninaboulton2](https://github.com/ninaboulton2)   | Product Owner    |
| STENGEL Damien | [DamienStengel](https://github.com/DamienStengel) | DevOps           |
| CHARLEUX Karim | [KarimCharleux](https://github.com/KarimCharleux) | Quality Analyst  |
| ANDRES Lucie   | [lucieandres](https://github.com/lucieandres)     | Quality Analyst  |

<hr>

### 📋 Project Description
The project aims to develop an online food ordering system for a campus environment. It
addresses the need for convenient food delivery services for students and campus community
members. The system enables users to place orders from various restaurants by selecting
delivery locations on campus. 

<hr>

### 📚 Project Management

#### GitHub Project Board
The project is managed using the Agile methodology. The team uses the Scrum framework.
We use a [GitHub project board](https://github.com/orgs/PNS-Conception/projects/27) to manage the project. The board is divided into 5 columns :
- **🆕 New** is the backlog of the project. It contains all the user stories that have not been assigned to a sprint yet.
- **📋 To Do** is the sprint backlog. It contains all the user stories that have been assigned to the current sprint.
- **🚧 In Progress** contains all the user stories that are currently being worked on.
- **👀 Review** contains all the user stories that have been completed and are waiting for review.
- **✅ Done** contains all the user stories that have been completed and reviewed.

#### Our naming convention for our branch
- A branch main (protected)
- A branch develop 
- Branch XX-Feature-YY for the feature YY of the milestone XX
- For a feature we're free to create a branch for a task or juste commit directly inside the feature
- For refractor the template of branch name is XX-Refractor-YY-Feature-ZZ for the milestone XX and the feature ZZ. YY is the refractor number

#### Our naming convention for our issue
- Feature-XX where XX is the number of feature
- Task-YY where YY is the number of task
- Refractor XX - Task YY where XX is the number of refractor and YY is the number of task

#### Our naming convention for our commit
- Release-XX for the tag of the commit of a PR for a release
- No tag for PR of a slice inside develop
- ```
  type(scope) : description (example : "Task(test) : xxx" or "Feat(Bot) : xxx")
  ````
With differents type of commit :
- **build** : change who affect the build system or external dependency (npm, make…)
- **ci** : change who affect configuration script (Travis, Ansible, BrowserStack…)
- **feat** : add new functionnalities
- **fix** : fix a bug
- **perf** : improve perfomance
- **refactor** : refractor code
- **style** : change style of code
- **docs** : add or update docs
- **test** : add or update tests
- **setup** : same as **ci**


<hr>

### 🗂️ Project Structure
The project is divided into 3 main directories :
- **src** contains the source code of the project.
  - **src/main** contains the main code of the project.
  - **src/tests** contains the unit tests and cucumber tests of the project.

<hr>

### 🕹️ How to run the project
To run the project, you need to have [Maven](https://maven.apache.org/) installed on your computer and you need to have a JDK 17 installed on your computer.
Then, you can run the following command in the root directory of the project :
```
mvn clean install
```
This command will compile the project and run the unit tests and the cucumber tests.
After that, you can run the following command to run the project :
```
mvn exec:java
```


