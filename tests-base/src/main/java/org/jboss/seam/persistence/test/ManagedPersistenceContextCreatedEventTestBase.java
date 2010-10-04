/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.persistence.test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import junit.framework.Assert;

import org.jboss.seam.persistence.transaction.DefaultTransaction;
import org.jboss.seam.persistence.transaction.SeamTransaction;
import org.jboss.seam.transactions.test.util.HelloService;
import org.jboss.seam.transactions.test.util.Hotel;
import org.jboss.seam.transactions.test.util.ManagedPersistenceContextObserver;
import org.jboss.seam.transactions.test.util.ManagedPersistenceContextProvider;
import org.junit.Test;

public class ManagedPersistenceContextCreatedEventTestBase
{

   public static Class<?>[] getTestClasses()
   {
      return new Class[] { ManagedPersistenceContextCreatedEventTestBase.class, ManagedPersistenceContextObserver.class, Hotel.class, ManagedPersistenceContextProvider.class, HelloService.class };
   }

   @Inject
   @DefaultTransaction
   SeamTransaction transaction;

   @Inject
   EntityManager em;

   @Inject
   ManagedPersistenceContextObserver observer;

   @Test
   public void testSMPCCreationObserved() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException
   {
      em.isOpen(); // need to make a call on the EM to force creation

      Assert.assertTrue(observer.isObserverRun());
      Assert.assertEquals(FlushModeType.COMMIT, em.getFlushMode());
   }

}