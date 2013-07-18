/*
 * Copyright 2010 Proofpoint, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.node.testing;

import com.google.common.base.Optional;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import io.airlift.node.NodeConfig;
import io.airlift.node.NodeInfo;
import org.weakref.jmx.guice.MBeanModule;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public class TestingNodeModule
        implements Module
{
    // avoid having an accidental dependency on the environment name
    private static final AtomicLong nextId = new AtomicLong(ThreadLocalRandom.current().nextInt(1000000));

    private final String environment;

    public TestingNodeModule()
    {
        this(Optional.<String>absent());
    }

    public TestingNodeModule(Optional<String> environment)
    {
        this(environment.or("test" + nextId.getAndIncrement()));
    }

    public TestingNodeModule(String environment)
    {
        checkArgument(!isNullOrEmpty(environment), "environment is null or empty");
        this.environment = environment;
    }

    @Override
    public void configure(Binder binder)
    {
        binder.bind(NodeInfo.class).in(Scopes.SINGLETON);
        binder.bind(NodeConfig.class).toInstance(new NodeConfig()
                .setEnvironment(environment)
                .setNodeInternalIp(getV4Localhost())
                .setNodeBindIp(getV4Localhost()));

        MBeanModule.newExporter(binder).export(NodeInfo.class).withGeneratedName();
    }

    @SuppressWarnings("ImplicitNumericConversion")
    private static InetAddress getV4Localhost()
    {
        try {
            return InetAddress.getByAddress("localhost", new byte[] {127, 0, 0, 1});
        }
        catch (UnknownHostException e) {
            throw new AssertionError("Could not create localhost address");
        }
    }
}