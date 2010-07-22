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
package org.jboss.seam.persistence;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnit;

import org.jboss.weld.extensions.annotated.AnnotatedTypeBuilder;
import org.jboss.weld.extensions.bean.BeanBuilder;
import org.jboss.weld.extensions.literal.DefaultLiteral;

/**
 * Extension the wraps producer methods/fields that produce an entity manager to
 * turn them into Seam Managed Persistence Contexts.
 * 
 * At present this happens automatically, in future we will need some way to
 * configure it
 * 
 * @author stuart
 * 
 */
public class ManagedPersistenceContextExtension implements Extension
{

   Set<Bean<?>> beans = new HashSet<Bean<?>>();

   /**
    * loops through the fields on an AnnotatedType looking for a @PersistnceUnit
    * producer field that is annotated {@link SeamManaged}. Then found a
    * corresponding smpc bean is created and registered. Any scope declaration
    * on the producer are removed as this is not supported by the spec
    * 
    */
   public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event, BeanManager manager)
   {
      AnnotatedTypeBuilder<T> modifiedType = null;
      for (AnnotatedField<? super T> f : event.getAnnotatedType().getFields())
      {
         // look for a seam managed persistence unit declaration
         if (f.isAnnotationPresent(SeamManaged.class) && f.isAnnotationPresent(PersistenceUnit.class) && f.isAnnotationPresent(Produces.class))
         {
            if (modifiedType == null)
            {
               modifiedType = AnnotatedTypeBuilder.newInstance(event.getAnnotatedType()).mergeAnnotations(event.getAnnotatedType(), true);
            }
            Set<Annotation> qualifiers = new HashSet<Annotation>();
            Class<? extends Annotation> scope = Dependent.class;
            // get the qualifier and scope for the new bean
            for (Annotation a : f.getAnnotations())
            {
               if (manager.isQualifier(a.annotationType()))
               {
                  qualifiers.add(a);
               }
               else if (manager.isScope(a.annotationType()))
               {
                  scope = a.annotationType();
               }
            }
            if (qualifiers.isEmpty())
            {
               qualifiers.add(new DefaultLiteral());
            }
            // we need to remove the scope, they are not nessesarily supported
            // on producer fields
            if (scope != Dependent.class)
            {
               modifiedType.removeFromField(f.getJavaMember(), scope);
            }
            // create the new bean to be registerd later
            AnnotatedTypeBuilder<EntityManager> typeBuilder = AnnotatedTypeBuilder.newInstance(EntityManager.class);
            BeanBuilder<EntityManager> builder = new BeanBuilder<EntityManager>(typeBuilder.create(), manager);
            builder.defineBeanFromAnnotatedType();
            builder.setQualifiers(qualifiers);
            builder.setScope(scope);
            builder.setBeanLifecycle(new ManagedPersistenceContextBeanLifecycle(qualifiers, manager));
            beans.add(builder.create());
         }

      }
      if (modifiedType != null)
      {
         event.setAnnotatedType(modifiedType.create());
      }
   }

   public void afterBeanDiscovery(@Observes AfterBeanDiscovery event)
   {
      for (Bean<?> i : beans)
      {
         event.addBean(i);
      }
   }

}
