/** IntegrationTest.java
 *
 * Copyright 2017 President and Fellows of Harvard College
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
package org.kurator.validation;

/**
 * Marker for integration tests.
 * 
 * Annotate integration test classes with Category(IntegrationTest.class).
 * 
 * Include or exclude in plugin configurations in pom with: 
 * 
 * <excludedGroups>org.kurator.validation.IntegrationTest</excludedGroups>
 * 
 * <configuration>
 *     <groups>edu.harvard.mcz.imagecapture.tests.IntegrationTest</groups>
 * </configuration>
 * 
 * @author mole
 *
 */
public interface IntegrationTest { }
