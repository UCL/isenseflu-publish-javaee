![i-sense flu](https://res.cloudinary.com/uclfmedia/image/upload/v1563449524/isenseflu/logo_300.svg)

# i-sense flu publish

Module of the i-sense flu application used in the scheduled publication of model scores on social media (Twitter)

[![Build Status](https://travis-ci.org/UCL/isenseflu-publish-javaee.svg?branch=master)](https://travis-ci.org/UCL/isenseflu-publish-javaee)
![GitHub release](https://img.shields.io/github/release/UCL/isenseflu-publish-javaee.svg)


## Background

i-sense flu is a multi-module application that uses Google search data to estimate influenza-like illness (flu) rates in England. The i-sense flu publish module is responsible for publishing a tweet containing the latest score, along with a chart showing the scores for the last 30 days, and the rate of change in the scores, relative to the previous 7-day period.


## Features

- Scheduled publishing of tweets based on a set template
- Creation of a chart containing the scores for the last 30 days
- Scores are received via STOMP messages
- Allows the reception of one score per day


## Technologies

- Java EE8
- STOMP messaging
- REST


## Installation

### Requirements

[Eclipse Glassfish 5][eclipse-glassfish-5] full profile (version 5.1.0) running OpenJDK 8

If using [Glassfish version 5.0][javaee-glassfish-5], make sure that the JDK/JRE version is 1.8.0 u151

```
openjdk version "1.8.0_151"
OpenJDK Runtime Environment (build 1.8.0_151-b12)
OpenJDK 64-Bit Server VM (build 25.151-b12, mixed mode)
```

Ref: [eclipse-ee4j/glassfish#22436](https://github.com/eclipse-ee4j/glassfish/issues/22436)

> There is some incompatible changes in the JDK after u152 that is impacting 
Grizzly. Hence, Grizzly had to be fixed and integrated into GlassFish 5.0.1.

### Configuration

Before deploying the application for the first time, the following steps are required to integrate the application with the i-sense flu API and Twitter API.

#### Aliases for credentials

Passwords and API keys/tokens need to be configured in Glassfish via aliases instead of clear text. The following aliases need to be created:

Consumer key and access token credentials from [Twitter app dashboard][twitter-app-dashboard]:

- `twitter-apikey-alias`
- `twitter-apisecret-alias`
- `twitter-token-alias`
- `twitter-tokensecret-alias`

And the admin user credential in the message queue (OpenMQ) to be used by the STOMP bridge service:

- `imq-adminpass-alias`

Configure each one with the Glassfish CLI admin tool `asadmin`. For example:

```
asadmin> create-password-alias twitter-apikey-alias
```

#### Message Queue (OpenMQ)

The i-sense flu API component is scheduled to send the latest score via a STOMP message. Glassfish provides an installation of OpenMQ that supports STOMP messaging via a JMS bridge. This can be enabled and configured with `asadmin`

```
asadmin> set configs.config.config-name.jms-service.type=LOCAL
asadmin> set configs.config.config-name.jms-service.property.imq\\.jmsra\\.managed=true
asadmin> set configs.config.config-name.jms-service.property.imq\\.bridge\\.enabled=true
asadmin> set configs.config.config-name.jms-service.property.imq\\.bridge\\.admin\\.user=[username]
asadmin> set configs.config.config-name.jms-service.property.imq\\.bridge\\.admin\\.password=[password]
asadmin> set configs.config.config-name.jms-service.property.imq\\.bridge\\.activelist=stomp
```

Restart the Glassfish domain to activate the configuration

```
asadmin> restart-domain
```

#### System properties/environment variables

The following system properties are required to be configured in the application server:

- `API_SCORES_URI`: URI of the `/scores` REST endpoint of the i-sense flu API component
- `TWITTER_SCHEDULED_FOR`: Time when the Tweet with the latest score report should be published. In `hh:mm` format.

 

- `TWITTER_KEY`: Consumer API key for Twitter set as `twitter-apikey-alias` password alias
- `TWITTER_SECRET`: Consumer API secret key for Twitter set as `twitter-apisecret-alias` password alias
- `TWITTER_TOKEN`: Access token for Twitter set as `twitter-token-alias` password alias
- `TWITTER_TOKEN_SECRET`: Access token secret for Twitter set as `twitter-tokensecret-alias` password alias

In Glassfish these system properties can be configured as follows:

```
asadmin> create-system-properties API_SCORES_URI=http\\://localhost\\:8080/api/flu/scores \
TWITTER_SCHEDULED_FOR=16\\:00
asadmin> create-system-properties 'TWITTER_KEY=${ALIAS\=twitter-apikey-alias}'
```

Alternatively, these values can be configured via environment variables in the shell. This way of configuration can be used when deploying to environments like Openshift or Heroku.

### Deployment

Download the latest release (or build) and deploy the EAR application using the `asadmin` utility from Glassfish:

```
asadmin> deploy --name isenseflu-publish [...path/to/...]isenseflu-ear-[version].ear
```

Or via Administration Console (usually `http://hostname:4848`)


## Building

Build the application from source using the `mvnw` Maven wrapper script provided, it requires `JAVA_HOME` set to JDK8 (tested with Open JDK 8). Fetch the sources from the master branch only.

```
./mvnw package
```

The EAR application will be found under `isenseflu-publish-ear/target/`

The system test runs within a local installation of Glassfish 5.1 and uses Mock Server to mock the external APIs. The tests follow the process from receiving the STOMP message up to publishing the Tweet in the required format. The complete system test should not take more than 3 minutes to complete. Run with:

```
./mvnw verify
```

## Reporting bugs

Please use the GitHub issue tracker for any bugs or feature suggestions:

[https://github.com/UCL/isenseflu-publish-javaee/issues](https://github.com/UCL/isenseflu-publish-javaee/issues)


## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

Please keep the use of 3rd party runtime libraries to a minimum. The application is expected to run on a standard Java EE 8 server ([Eclipse Glassfish 5 full profle][eclipse-glassfish-5]). 


## Authors

- David Guzman (Github: [@david-guzman](https://github.com/david-guzman))


## Acknowledgements

i-sense flu publish is supported by the EPSRC IRC project [i-sense](https://www.i-sense.org.uk/) (Early-Warning Sensing Systems for Infectious Diseases).


## Copyright

isenseflu-publish-javaee is licensed under the GNU General Public License, v3. A copy of this license is included in the file [LICENSE.md](LICENSE.md).


&copy; 2019 UCL ([https://www.ucl.ac.uk](https://www.ucl.ac.uk)).


[eclipse-glassfish-5]: https://projects.eclipse.org/projects/ee4j.glassfish/downloads
[javaee-glassfish-5]: https://javaee.github.io/glassfish/
[twitter-app-dashboard]: https://developer.twitter.com/en/apps
