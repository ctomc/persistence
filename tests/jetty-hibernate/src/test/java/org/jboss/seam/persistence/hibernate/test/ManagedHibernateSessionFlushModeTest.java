/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.seam.persistence.hibernate.test;

import java.util.HashMap;

import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.persistence.test.jetty.util.JettyTestUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.context.bound.Bound;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.MutableBoundRequest;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ManagedHibernateSessionFlushModeTest extends ManagedHibernateSessionFlushModeTestBase {
    @Inject
    @Bound
    private BoundConversationContext context;

    @Deployment
    public static Archive<?> createTestArchive() {
        WebArchive war = JettyTestUtils.createHibernateTestArchive();
        war.addWebResource("WEB-INF/hibernate-beans.xml", "beans.xml");
        war.addWebResource("META-INF/hibernate-std.cfg.xml", "classes/hibernate.cfg.xml");
        war.addClasses(getTestClasses());
        return war;
    }

    @Override
    public void testChangedTouchedSessionFlushMode() {
        context.associate(new MutableBoundRequest(new HashMap<String, Object>(), new HashMap<String, Object>()));
        context.activate();
        super.testChangedTouchedSessionFlushMode();
        context.deactivate();
    }
}
