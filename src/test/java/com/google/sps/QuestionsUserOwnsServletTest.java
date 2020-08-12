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

import com.google.sps.servlets.QuestionsUserOwnsServlet;
import java.io.IOException;
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
import java.util.Date;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class QuestionsUserOwnsServletTest extends QuestionsUserOwnsServlet {
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
    /*Tests the doGet function to see if the questions that the
    * user owns get retrieved correctly */
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    Long date = (new Date()).getTime(); 

    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);
   
    /*Set up two fake question entities */
    Entity questionEntity = new Entity("Question");
    questionEntity.setProperty("question", "What day is it?");
    questionEntity.setProperty("marks", "5");
    questionEntity.setProperty("date", date);
    questionEntity.setProperty("ownerID", "test@example.com");

    Entity anotherQuestionEntity = new Entity("Question");
    anotherQuestionEntity.setProperty("question", "What year is it?");
    anotherQuestionEntity.setProperty("marks", "10");
    anotherQuestionEntity.setProperty("date", date);
    anotherQuestionEntity.setProperty("ownerID", "test@example.com");
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(questionEntity);
    datastore.put(anotherQuestionEntity);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    
    QuestionsUserOwnsServlet servlet= new QuestionsUserOwnsServlet();
    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("What day is it? (5)"));
    Assert.assertTrue(result.contains("What year is it? (10)"));
  }
}