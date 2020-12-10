# GreenVulcano VCL Adapter for S3 (Alpha)

This is the implementation of a GreenVulcano VCL Adapter for the Amazon S3 Bucket service. It's meant to run as an Apache Karaf bundle.

<span style="color:red">
The adapter was developed in a rush and has a lot of room for improvement. Take everything you read with a grain of salt.
</span>

## Getting started

### Installation

First, you need to have installed Java Development Kit (JDK) 11 or above.

Then, you need to have installed Apache Karaf 4.2.x. Please refer to the following links for further reference: [Apache Karaf](http://karaf.apache.org/manual/latest/)

Next, you need to install the GreenVulcano engine on the Apache Karaf container. Please refer to [this link](https://greenvulcano.github.io/gv-documentation/pages/installation/Installation/#installation) for further reference.

In order to install the bundle in Apache Karaf to use it for a GreenVulcano application project, you need to install its dependencies (16 in total). Normally, they should auto-install themselves, but in case of failure you need to install them manually. Open the Apache Karaf terminal by running the Karaf executable and type the following commands:

```shell
karaf@root()> bundle:install mvn:com.amazonaws/aws-java-sdk-osgi/1.11.911
karaf@root()> bundle:install mvn:software.amazon.ion/ion-java/1.5.1
karaf@root()> bundle:install mvn:org.apache.httpcomponents/httpclient-osgi/4.5.13
karaf@root()> bundle:install mvn:org.apache.httpcomponents/httpcore-osgi/4.4.14
karaf@root()> bundle:install mvn:com.fasterxml.jackson.dataformat/jackson-dataformat-cbor/2.12.0
karaf@root()> bundle:install mvn:com.fasterxml.jackson.core/jackson-core/2.12.0
karaf@root()> bundle:install mvn:com.fasterxml.jackson.core/jackson-databind/2.12.0
karaf@root()> bundle:install mvn:com.fasterxml.jackson.core/jackson-annotations/2.12.0
karaf@root()> bundle:install mvn:io.netty/netty-testsuite-osgi/4.1.54.Final
karaf@root()> bundle:install mvn:io.netty/netty-buffer/4.1.54.Final
karaf@root()> bundle:install mvn:io.netty/netty-resolver/4.1.54.Final
karaf@root()> bundle:install mvn:io.netty/netty-handler/4.1.54.Final
karaf@root()> bundle:install mvn:io.netty/netty-codec/4.1.54.Final
karaf@root()> bundle:install mvn:io.netty/netty-transport/4.1.54.Final
karaf@root()> bundle:install mvn:io.netty/netty-common/4.1.54.Final
karaf@root()> bundle:install mvn:io.netty/netty-codec-http/4.1.54.Final
karaf@root()> bundle:install -s -l 96 install mvn:it.greenvulcano.gvesb.adapter/gvvcl-s3/4.1.0-SNAPSHOT
```

### Using the VCL adapter in your GreenVulcano project

In order to use the features of the S3 adapter in your GreenVulcano project, you need to define a proper System-Channel-Operation set of nodes. You can do that by manually editing the GVCore.xml file, or by using DeveloperStudio. In that case, you will have to download the ``s3-call.dtd`` file on this repository and merge it with the ``GVCore.dtd`` file in your current project folder.

### Declaring the System-Channel-Operation for S3 Bucket

Currently, only 4 actions are implemented:
 * List Objects (list)
 * Put Object (put)
 * Get Object (get)
 * Deleted Object (delete)

Here's an example of a list action:

```xml
<System id-system="MySystem" system-activation="on">
	<Channel id-channel="s3">
	    <s3-call class="it.greenvulcano.gvesb.virtual.s3.S3Call"
	               name="s3Example" type="call"
	               akid="<akid>"
	               skid="<skid>"
	               region="eu-central-1"
	               bucket="<bucket-name>"
	               action="list">
	    </s3-call>
	</Channel>
</System>
```
