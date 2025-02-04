/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.tools.automq;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.apache.kafka.common.utils.Exit;

public class AutoMQKafkaAdminTool {
    public static final String GENERATE_S3_URL_CMD = "generate-s3-url";
    public static final String GENERATE_START_COMMAND_CMD = "generate-start-command";
    public static final String GENERATE_CONFIG_PROPERTIES_CMD = "generate-config-properties";

    public static void main(String[] args) {
        // suppress slf4j inner warning log
        System.err.close();
        ArgumentParser parser = ArgumentParsers
            .newArgumentParser("automq-admin-tool")
            .defaultHelp(true)
            .description("This AutoMQ admin tool contains several tools to help user init and manage AutoMQ cluster easily.");
        if (args.length == 0) {
            System.out.println("Please pass valid arguments. Check usage first.");
            parser.printHelp();
            Exit.exit(0);
        }

        Subparsers subparsers = parser.addSubparsers().title("commands");

        Subparser generateS3UrlCmdParser = subparsers.addParser(GENERATE_S3_URL_CMD)
            .help("generate s3url for AutoMQ")
            .description(String.format("This cmd is used to generate s3url for AutoMQ that is used to connect to s3 or other cloud object storage service. Execute '%s -h' to check its usage.", GENERATE_S3_URL_CMD));
        GenerateS3UrlCmd.addArguments(generateS3UrlCmdParser);

        Subparser generateStartCommandCmdParser = subparsers.addParser(GENERATE_START_COMMAND_CMD)
            .help("generate config file and local start command")
            .description(String.format("This cmd is used to generate config file and local start command. Execute '%s -h' to check its usage.", GENERATE_START_COMMAND_CMD));
        GenerateStartCmdCmd.addArguments(generateStartCommandCmdParser);

        Subparser generateConfigPropertiesCmdParser = subparsers.addParser(GENERATE_CONFIG_PROPERTIES_CMD)
            .help("generate multi config properties")
            .description(String.format("This cmd is used to generate multi config properties depend on your arguments. Execute '%s -h' to check its usage.", GENERATE_CONFIG_PROPERTIES_CMD));
        GenerateConfigFileCmd.addArguments(generateConfigPropertiesCmdParser);

        switch (args[0]) {
            case GENERATE_S3_URL_CMD:
                processGenerateS3UrlCmd(args, generateS3UrlCmdParser);
                break;
            case GENERATE_START_COMMAND_CMD:
                processGenerateStartCmd(args, generateStartCommandCmdParser);
                break;
            case GENERATE_CONFIG_PROPERTIES_CMD:
                processGenConfigPropertiesCmd(args, generateConfigPropertiesCmdParser);
                break;
            default:
                System.out.println(String.format("Not supported command %s. Check usage first.", args[0]));
                parser.printHelp();
                Exit.exit(0);
        }

        Exit.exit(0);

    }

    private static Namespace parseArguments(ArgumentParser parser, String[] args) {
        try {
            return parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            if (args.length == 1) {
                parser.printHelp();
                Exit.exit(0);
            } else {
                parser.handleError(e);
                Exit.exit(1);
            }
            return null;
        }
    }

    private static void runCommandWithParameter(ArgumentParser parser, Namespace res, Command command) {
        if (res == null) {
            parser.handleError(new ArgumentParserException("Namespace is null", parser));
            Exit.exit(1);
        } else {
            try {
                command.run();
            } catch (Exception e) {
                System.out.printf("FAILED: Caught exception %s%n%n", e.getMessage());
                e.printStackTrace();
                Exit.exit(1);
            }
        }
    }

    @FunctionalInterface
    public interface Command {
        void run() throws Exception;
    }

    private static void processGenerateS3UrlCmd(String[] args, ArgumentParser parser) {
        Namespace res = parseArguments(parser, args);
        runCommandWithParameter(parser, res, () -> new GenerateS3UrlCmd(new GenerateS3UrlCmd.Parameter(res)).run());
    }

    private static void processGenerateStartCmd(String[] args, ArgumentParser parser) {
        Namespace res = parseArguments(parser, args);
        runCommandWithParameter(parser, res, () -> new GenerateStartCmdCmd(new GenerateStartCmdCmd.Parameter(res)).run());
    }

    private static void processGenConfigPropertiesCmd(String[] args, ArgumentParser parser) {
        Namespace res = parseArguments(parser, args);
        runCommandWithParameter(parser, res, () -> new GenerateConfigFileCmd(new GenerateConfigFileCmd.Parameter(res)).run());
    }

}
