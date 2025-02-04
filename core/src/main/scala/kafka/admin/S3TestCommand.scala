/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.admin

import com.automq.stream.utils.S3Utils
import joptsimple.OptionSpec
import kafka.utils.{CommandDefaultOptions, CommandLineUtils, Exit, Logging}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}

import java.util

object S3TestCommand extends Logging {
  def main(args: Array[String]): Unit = {
    try {
      val opts = new S3CommandOptions(args)

      CommandLineUtils.printHelpAndExitIfNeeded(opts, "This tool helps to test your access to s3")

      opts.checkArgs()

      processCommand(opts)
    } catch {
      case t: Throwable =>
        logger.debug(s"Error while executing s3 test command with args '${args.mkString(" ")}'", t)
        System.err.println(s"Error while executing s3 test command with args '${args.mkString(" ")}'")
        t.printStackTrace(System.err)
        Exit.exit(1)
    }
  }

  private def processCommand(opts: S3CommandOptions): Unit = {
    val s3Endpoint = opts.options.valueOf(opts.s3EndpointOpt)
    val s3Bucket = opts.options.valueOf(opts.s3BucketOpt)
    val s3Region = opts.options.valueOf(opts.s3RegionOpt)
    val s3AccessKey = opts.options.valueOf(opts.s3AccessKeyOpt)
    val s3SecretKey = opts.options.valueOf(opts.s3SecretKeyOpt)
    val forcePathStyle = opts.has(opts.forcePathStyleOpt)

    val context = S3Utils.S3Context.builder()
      .setEndpoint(s3Endpoint)
      .setCredentialsProviders(util.List.of(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey))))
      .setBucketName(s3Bucket)
      .setRegion(s3Region)
      .setForcePathStyle(forcePathStyle)
      .build()

    S3Utils.checkS3Access(context)
  }


  class S3CommandOptions(args: Array[String]) extends CommandDefaultOptions(args) {
    val s3EndpointOpt = parser.accepts("endpoint", "The S3 endpoint to connect to. Note that bucket name SHOULD NOT be included in the endpoint.")
      .withRequiredArg
      .describedAs("s3 endpoint to connect to")
      .ofType(classOf[String])
    val s3BucketOpt = parser.accepts("bucket", "The S3 bucket to connect to.")
      .withRequiredArg
      .describedAs("s3 bucket")
      .ofType(classOf[String])
    val s3RegionOpt = parser.accepts("region", "The S3 region to connect to.")
      .withRequiredArg
      .describedAs("s3 region")
      .ofType(classOf[String])
    val s3AccessKeyOpt = parser.accepts("ak", "The S3 access key to use.")
      .withRequiredArg
      .describedAs("s3 access key")
      .ofType(classOf[String])
    val s3SecretKeyOpt = parser.accepts("sk", "The S3 secret key to use.")
      .withRequiredArg()
      .describedAs("s3 secret key")
      .ofType(classOf[String])
    val forcePathStyleOpt = parser.accepts("force-path-style", "Force path style access. Set it if you are using minio. " +
      "As a result, the bucket name is always left in the request URI and never moved to the host as a sub-domain.")


    options = parser.parse(args: _*)

    def has(builder: OptionSpec[_]): Boolean = options.has(builder)

    def checkArgs(): Unit = {
      CommandLineUtils.printHelpAndExitIfNeeded(this, "This tool helps to test S3 access.")

      // check required args
      if (!has(s3EndpointOpt))
        throw new IllegalArgumentException("--endpoint must be specified")
      if (!has(s3BucketOpt))
        throw new IllegalArgumentException("--bucket must be specified")
      if (!has(s3RegionOpt))
        throw new IllegalArgumentException("--region must be specified")
      if (!has(s3AccessKeyOpt))
        throw new IllegalArgumentException("--ak must be specified")
      if (!has(s3SecretKeyOpt))
        throw new IllegalArgumentException("--sk must be specified")

    }
  }
}
