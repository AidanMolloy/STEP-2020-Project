// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertTrue;

import com.google.sps.servlets.ExamsUserOwnsServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import javax.servlet.http.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ExamsUserOwnsServletTest extends ExamsUserOwnsServlet {
  private final LocalServiceTestHelper helper = 
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
      .setEnvIsLoggedIn(true).setEnvEmail("test@example.com").setEnvAuthDomain("example.com");
    
  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testdoGetFunction() throws IOException {
    /*Tests the doGet function to see if all tests a user owns get
    * retrieved correctly.
    */
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    final Long date = (new Date()).getTime(); 
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);

    List<Long> list = new ArrayList<>();
    /*Create two fake TestEntities */
    Entity testEntity = new Entity("Exam");
    testEntity.setProperty("name", "Trial");
    testEntity.setProperty("duration", "30");
    testEntity.setProperty("ownerID", "test@example.com");
    testEntity.setProperty("date", date);
    testEntity.setProperty("questionsList", list);

    Entity anotherEntity= new Entity("Exam");
    anotherEntity.setProperty("name", "AnotherExam");
    anotherEntity.setProperty("duration", "45");
    anotherEntity.setProperty("ownerID", "test@example.com");
    anotherEntity.setProperty("date", date);
    anotherEntity.setProperty("questionsList", list);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(testEntity);
    datastore.put(anotherEntity);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    
    ExamsUserOwnsServlet servlet = new ExamsUserOwnsServlet();
    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("\"name\":\"Trial\",\"examID\":1,"
        + "\"duration\":30.0,\"ownerID\":\"test@example.com\""));
    Assert.assertTrue(result.contains("\"name\":\"AnotherExam\",\"examID\":2,"
        +"\"duration\":45.0,\"ownerID\":\"test@example.com\""));
  }
}