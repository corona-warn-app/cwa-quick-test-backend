<h1 align="center">
    Corona-Warn-App Quick Test Backend
</h1>

<p align="center">
    <a href="https://github.com/corona-warn-app/cwa-quick-test-backend/commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/corona-warn-app/cwa-quick-test-backend?style=flat"></a>
    <a href="https://github.com/corona-warn-app/cwa-quick-test-backend/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-quick-test-backend?style=flat"></a>
    <a href="https://github.com/corona-warn-app/cwa-quick-test-backend/blob/master/LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

<p align="center">
  <a href="#development">Development</a> •
  <a href="#documentation">Documentation</a> •
  <a href="#support-and-feedback">Support</a> •
  <a href="#how-to-contribute">Contribute</a> •
  <a href="#contributors">Contributors</a> •
  <a href="#repositories">Repositories</a> •
  <a href="#licensing">Licensing</a>
</p>

The goal of this project is to develop the official Corona-Warn-App for Germany based on the exposure notification API
from [Apple](https://www.apple.com/covid19/contacttracing/)
and [Google](https://www.google.com/covid19/exposurenotifications/). The apps (for both iOS and Android) use Bluetooth
technology to exchange anonymous encrypted data with other mobile phones (on which the app is also installed) in the
vicinity of an app user's phone. The data is stored locally on each user's device, preventing authorities or other
parties from accessing or controlling the data. This repository contains the **log upload** for the Corona-Warn-App.

## Status

![ci](https://github.com/corona-warn-app/cwa-quick-test-backend/workflows/ci/badge.svg)
[![quality gate](https://sonarcloud.io/api/project_badges/measure?project=corona-warn-app_cwa-quick-test-backend&metric=alert_status)](https://sonarcloud.io/dashboard?id=corona-warn-app_cwa-quick-test-backend)
[![coverage](https://sonarcloud.io/api/project_badges/measure?project=corona-warn-app_cwa-quick-test-backend&metric=coverage)](https://sonarcloud.io/dashboard?id=corona-warn-app_cwa-quick-test-backend)
[![bugs](https://sonarcloud.io/api/project_badges/measure?project=corona-warn-app_cwa-quick-test-backend&metric=bugs)](https://sonarcloud.io/dashboard?id=corona-warn-app_cwa-quick-test-backend)

## About this component

tbd...

## Development

tbd...

### Prerequisites

[Open JDK 11](https://openjdk.java.net)  
[Maven](https://maven.apache.org)
*(optional)*: [Docker](https://www.docker.com)

### Build

Whether you cloned or downloaded the 'zipped' sources you will either find the sources in the chosen checkout-directory
or get a zip file with the source code, which you can expand to a folder of your choice.

In either case open a terminal pointing to the directory you put the sources in. The local build process is described
afterwards depending on the way you choose.

#### Maven based build

For actively take part on the development this is the way you should choose.   
Please check, whether following prerequisites are fulfilled

- [Open JDK 11](https://openjdk.java.net) or a similar JDK 11 compatible VM
- [Maven](https://maven.apache.org)

is installed on your machine.  
You can then open a terminal pointing to the root directory of the log upload and do the following:

    mvn package
    java -jar target/cwa-quick-test-backend-0.0.1-SNAPSHOT.jar  

The backend will start up and run locally on your machine available on port 8081. Please keep in mind, that you need
another component [cwa-verification-iam] the get this running in a sensable manner.

#### Docker based build

We recommend that you first check the prerequisites to ensure that

- [Docker](https://www.docker.com)

is installed on your machine.

On the command line do the following:

```bash
docker build -f|--file <path to dockerfile>  -t <imagename>  <path-to-quick-test-backend-root>
docker run -p 127.0.0.1:8081:8081/tcp -it <imagename>
```

or simply

```bash
docker build --pull --rm -f "Dockerfile" -t cwa-quick-test-backend "."
docker run -p 127.0.0.1:8081:8081/tcp -it cwa-quick-test-backend
```

if you are in the root of the checked out repository.  
The docker image will then run on your local machine on port 8081 assuming you configured docker for shared network
mode.

## Code of Conduct

This project has adopted the [Contributor Covenant](https://www.contributor-covenant.org/) in version 2.0 as our code of
conduct. Please see the details in our [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md). All contributors must abide by the code
of conduct.

## Working Language

We are building this application for Germany. We want to be as open and transparent as possible, also to interested
parties in the global developer community who do not speak German. Later on this application might also serve as a
template for other projects outside of Germany. For these reasons, we decided to apply _English_ as the primary project
language.

Consequently, all content will be made available primarily in English. We also ask all interested people to use English
as language to create issues, in their code (comments, documentation etc.) and when you send requests to us. The
application itself, documentation and all end-user facing content will - of course - be made available in German (and
probably other languages as well). We also try to make some developer documentation available in German, but please
understand that focussing on the _Lingua Franca_ of the global developer community makes the development of this
application as efficient as possible.

## Documentation

The full documentation for the Corona-Warn-App can be found in
the [cwa-documentation](https://github.com/corona-warn-app/cwa-documentation) repository. The documentation repository
contains technical documents, architecture information, and white papers related to this implementation.

## Support and Feedback

The following channels are available for discussions, feedback, and support requests:

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **General
Discussion**   | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="General Discussion"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/question.svg?style=flat-square"></a> </a>   |
| **Concept
Feedback**    | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="Open Concept Feedback"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/architecture.svg?style=flat-square"></a>  |
| **Quick Test Backend
Issue**    | <a href="https://github.com/corona-warn-app/cwa-quick-test-backend/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-quick-test-backend?style=flat"></a>  |
| **Other
Requests**    | <a href="mailto:cwa-opensource@telekom.de" title="Email CWA Team"><img src="https://img.shields.io/badge/email-CWA%20team-green?logo=mail.ru&style=flat-square&logoColor=white"></a>   |

## How to Contribute

Contribution and feedback is encouraged and always welcome. For more information about how to contribute, the project
structure, as well as additional contribution information, see our [Contribution Guidelines](./CONTRIBUTING.md). By
participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Contributors

The German government has asked SAP AG and Deutsche Telekom AG to develop the Corona-Warn-App for Germany as open source
software. Deutsche Telekom is providing the network and mobile technology and will operate and run the backend for the
app in a safe, scalable and stable manner. SAP is responsible for the app development, its framework and the underlying
platform. Therefore, development teams of SAP and Deutsche Telekom are contributing to this project. At the same time
our commitment to open source means that we are enabling -in fact encouraging- all interested parties to contribute and
become part of its developer community.

## Repositories

| Repository          | Description                                                           |
| ------------------- | --------------------------------------------------------------------- |
| [cwa-documentation] | Project overview, general documentation, and white papers.            |
| [cwa-app-ios]       | Native iOS app using the Apple/Google exposure notification API.      |
| [cwa-app-android]   | Native Android app using the Apple/Google exposure notification API.  |
| [cwa-wishlist]      | Community feature requests.                                           |
| [cwa-website]       | The official website for the Corona-Warn-App                          |
| [cwa-server]        | Backend implementation for the Apple/Google exposure notification API.|
| [cwa-verification-server] | Backend implementation of the verification process.             |
| [cwa-verification-portal] | The portal to interact with the verification server             |
| [cwa-verification-iam]    | The identity and access management to interact with the verification server |
| [cwa-testresult-server]   | Receives the test results from connected laboratories           |

[cwa-documentation]: https://github.com/corona-warn-app/cwa-documentation

[cwa-app-ios]: https://github.com/corona-warn-app/cwa-app-ios

[cwa-app-android]: https://github.com/corona-warn-app/cwa-app-android

[cwa-wishlist]: https://github.com/corona-warn-app/cwa-wishlist

[cwa-website]: https://github.com/corona-warn-app/cwa-website

[cwa-server]: https://github.com/corona-warn-app/cwa-server

[cwa-verification-server]: https://github.com/corona-warn-app/cwa-verification-server

[cwa-verification-portal]: https://github.com/corona-warn-app/cwa-verification-portal

[cwa-verification-iam]: https://github.com/corona-warn-app/cwa-verification-iam

[cwa-testresult-server]: https://github.com/corona-warn-app/cwa-testresult-server

## Licensing

Copyright (c) 2021 Deutsche Telekom AG.

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with
the License.

You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "
AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the [LICENSE](./LICENSE) for
the specific language governing permissions and limitations under the License.
