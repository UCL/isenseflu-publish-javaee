# isenseflu-publish-javaee

[![Build Status](https://travis-ci.org/UCL/isenseflu-publish-javaee.svg?branch=master)](https://travis-ci.org/UCL/isenseflu-publish-javaee)
![GitHub release](https://img.shields.io/github/release/UCL/isenseflu-publish-javaee.svg)

i-sense flu component for publishing scores on social media

## Installation

### Requirements

[Eclipse Glassfish 5](https://projects.eclipse.org/projects/ee4j.glassfish/downloads) full profile (version 5.1.0) running OpenJDK 8

If using [Glassfish version 5.0](https://javaee.github.io/glassfish/), make sure that the JDK/JRE version is 1.8.0 u151

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

#### Message Queue (OpenMQ)

The i-sense flu API component is scheduled to send the latest score via a STOMP message. Glassfish provides an installation of OpenMQ that supports STOMP messaging via a JMS bridge. This can be enabled and configured with `asadmin`

```
asadmin> set configs.config.config-name.jms-service.type=LOCAL
asadmin> set configs.config.config-name.jms-service.property.imq\\.jmsra\\.managed=true
asadmin> set configs.config.config-name.jms-service.property.imq\\.bridge\\.enabled=true
asadmin> set configs.config.config-name.jms-service.property.imq\\.bridge\\.admin\\.user=[username]
asadmin> set configs.config.config-name.jms-service.property.imq\\.bridge\\.admin\\.password=[password-alias]
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

In Glassfish these system properties can be configured as follows:

```
asadmin> create-system-properties API_SCORES_URI=http\\://localhost\\:8080/api/flu/scores \
TWITTER_SCHEDULED_FOR=16\\:00
```

Alternatively, these values can be configured via environment variables in the shell. This way of configuration can be used when deploying to environments like Openshift or Heroku.

### Deployment

Download the latest release (or build) and deploy the EAR application using the `asadmin` utility from Glassfish:

```
asadmin> deploy --name isenseflu-publish [...path/to/...]isenseflu-ear-[version].ear
```

Or via Administration Console (usually `http://hostname:4848`)


## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

Please keep the use of 3rd party runtime libraries to a minimum. The application is expected to run on a standard Java EE 8 server (Glassfish 5 full profile). 