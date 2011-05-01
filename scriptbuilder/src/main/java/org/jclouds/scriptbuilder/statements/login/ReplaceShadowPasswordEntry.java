/**
 *
 * Copyright (C) 2011 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package org.jclouds.scriptbuilder.statements.login;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.encryption.internal.JCECrypto;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.domain.StatementList;
import org.jclouds.scriptbuilder.domain.Statements;
import org.jclouds.scriptbuilder.util.Sha512Crypt;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Adrian Cole
 */
public class ReplaceShadowPasswordEntry implements Statement {

   private final String login;
   private final String password;

   public ReplaceShadowPasswordEntry(String login, String password) {
      this.login = checkNotNull(login, "login");
      this.password = checkNotNull(password, "password");
   }

   @Override
   public Iterable<String> functionDependencies(OsFamily family) {
      return ImmutableList.of();
   }

   @Override
   public String render(OsFamily family) {
      checkNotNull(family, "family");
      if (family == OsFamily.WINDOWS)
         throw new UnsupportedOperationException("windows not yet implemented");
      try {
         String shadowPasswordEntry = Sha512Crypt.makeShadowLine(password, null, new JCECrypto());
         String shadowFile = "/etc/shadow";
         Statement replaceEntryInTempFile = Statements
               .exec(String
                     .format(
                           "awk -v user=^%1$s: -v password='%2$s' 'BEGIN { FS=OFS=\":\" } $0 ~ user { $2 = password } 1' %3$s >%3$s.%1$s",
                           login, shadowPasswordEntry, shadowFile));
         Statement replaceShadowFile = Statements.exec(String.format("test -f %2$s.%1$s && mv %2$s.%1$s %2$s", login,
               shadowFile));
         return new StatementList(ImmutableList.of(replaceEntryInTempFile, replaceShadowFile)).render(family);
      } catch (Exception e) {
         Throwables.propagate(e);
         return null;
      }
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((login == null) ? 0 : login.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ReplaceShadowPasswordEntry other = (ReplaceShadowPasswordEntry) obj;
      if (login == null) {
         if (other.login != null)
            return false;
      } else if (!login.equals(other.login))
         return false;
      return true;
   }
}